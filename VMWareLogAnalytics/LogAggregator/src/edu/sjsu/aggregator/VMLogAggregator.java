package edu.sjsu.aggregator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class VMLogAggregator implements Runnable{
final static Logger logger = Logger.getLogger(VMLogAggregator.class);
	
	private volatile boolean threadStopper = false;
	Thread aggregatorThread;
	private static DB db;
	private DBProperties properties;
	private ConfigProperties configProperties;
	
	public VMLogAggregator(DBProperties properties, ConfigProperties configProperties){
		this.properties = properties;
		this.configProperties = configProperties;
		
		aggregatorThread = new Thread(this);
		aggregatorThread.start();
	}
	
	@Override
	public void run(){
		while(!threadStopper){
			try {
				aggregatorThread.sleep(4*60*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				ServiceInstance si = new ServiceInstance(new URL(configProperties.getvCenterURL()), configProperties.getUsername(),
						configProperties.getPassword(), true);
				Folder rootFolder = si.getRootFolder();
				InventoryNavigator iNav = new InventoryNavigator(rootFolder);
				List<VirtualMachine> vmList = getVMList(iNav);
				java.util.Date dt = new java.util.Date();
			    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
			                "yyyy-MM-dd HH:mm:ss");
			    String currentTime = sdf.format(dt);
			    String datetime = Timestamp.valueOf(currentTime).toString();
				for(VirtualMachine vm : vmList){
					if(pingIP(vm.getGuest().getIpAddress())){
						getAverage(vm.getGuest().getIpAddress(), datetime);
					}
				}
				si.getServerConnection().logout();
			} catch (Exception e){
				e.printStackTrace();
			}	
		}
	}
	
	public static List<VirtualMachine> getVMList(InventoryNavigator iNav) throws InvalidProperty, RuntimeFault, RemoteException{
		ManagedEntity[] managedEntities = iNav.searchManagedEntities("VirtualMachine");
		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		
		for(int iterator=0; iterator<managedEntities.length; iterator++) {
			vmList.add((VirtualMachine) managedEntities[iterator]);
		}
		return vmList;
	}
	
	public boolean pingIP(String ip){
		boolean result= false;
		String cmd = "ping "+ ip;
		String consoleResult="";
		try{
			if(ip!=null){
				Runtime r=Runtime.getRuntime();
				Process p= r.exec(cmd);
				int counter = 0;
				BufferedReader input= new BufferedReader(new InputStreamReader(p.getInputStream()));
				while(input.readLine()!=null){
					logger.info(input.readLine());
					if(counter>2){
						break;
					}
					consoleResult+=input.readLine();
					counter++;
				}
				input.close();

				if(consoleResult.contains("Request timed out")){
					logger.info("Packets Dropped/ could not connect");
					result=false;
				}
				else{
					logger.info("connection made successfully");
					result=true;
				}

			} 
			else{
				logger.info("IP invalid/not found!");
				result = false; 
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public DB getConnection(String ip) throws UnknownHostException{
		db = null;
		MongoClient client = new MongoClient(ip, properties.getPort());
		db = client.getDB("logcollection"); 
	  return db;
	}
	
	public Connection getSqlConnection(String ip) throws ClassNotFoundException, SQLException{
			// JDBC driver name and database URL
		  
		   final String JDBC_DRIVER = properties.getJdbcDriver();  
		   //final String DB_URL = properties.getSqlURL().replace("localhost", ip);
		   final String DB_URL = properties.getSqlURL();
		   //  Database credentials
		   final String USER = properties.getUsername();
		   final String PASS = properties.getPassword();
		   Class.forName(JDBC_DRIVER);

		   Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		   return conn;
	}
	
	public void getAverage(String ip, String datetime) throws UnknownHostException, ClassNotFoundException, SQLException, ParseException{
		DB db = getConnection(ip);
		DBCollection stats = db.getCollection("vmstats");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Calendar cal = Calendar.getInstance();
		logger.info("Current Date Time : " + dateFormat.format(cal.getTime()));
		
		BasicDBObject query = new BasicDBObject();
		query.put("logdate", new BasicDBObject("$lte", dateFormat.format(cal.getTime())));
		cal.add(Calendar.MINUTE, -5);
		query.put("logdate", new BasicDBObject("$gte",dateFormat.format(cal.getTime())));
		logger.info("Back Date Time : " + dateFormat.format(cal.getTime()));
		
		DBCursor cursor = stats.find(query).sort(new BasicDBObject("logdate", -1));
		
	 	DBCollection collection = db.getCollection("bkplog");
		while(cursor.hasNext()) {
			DBObject dbObjectTemp = cursor.next();
			collection.insert(dbObjectTemp);
			collection.save(dbObjectTemp);
		}
		
		DBCollection aggregateStats = db.getCollection("bkplog");
		String grp = vmAverageQuery();
		DBObject group = (DBObject) JSON.parse(grp);
		AggregationOutput output = aggregateStats.aggregate(group);  
		
		logger.info("Agggregate output ::"+ output);
		
		ArrayList<DBObject> list = (ArrayList<DBObject>) output.results();
		Pattern pattern = Pattern.compile("(T03+)");
		for (DBObject dbObject : list) {
			Matcher matcher = pattern.matcher(dbObject.get("_id").toString());
			if (matcher.find()){
				insertAggregatedData(dbObject, ip, datetime);
			} 
		}
		aggregateStats.drop();
	}

	private String vmAverageQuery() {
		String grpVm = "{$group:{_id:'$vmname',"
				+ "avgcpu_usage:{$avg:'$cpu_usage'},"
				+ "avgcpu_used:{$avg:'$cpu_used'},"
				+ "avgcpu_usagemhz:{$avg:'$cpu_usagemhz'},"
				+ "avgcpu_system:{$avg:'$cpu_system'},"
				+ "avgmem_shared:{$avg:'$mem_shared'},"
				+ "avgmem_granted:{$avg:'$mem_granted'},"
				+ "avgmem_usage:{$avg:'$mem_usage'},"
				+ "avgmem_consumed:{$avg:'$mem_consumed'},"
				+ "avgnet_usage:{$avg:'$net_usage'},"
				+ "avgnet_received:{$avg:'$net_received'},"
				+ "avgnet_transmitted:{$avg:'$net_transmitted'},"
				+ "avgsys_heartbeat:{$avg:'$sys_heartbeat'},"
				+ "avgsys_osUptime:{$avg:'$sys_osUptime'},"
				+ "avgsys_uptime:{$avg:'$sys_uptime'},"
				+ "avgvirtualDisk_write:{$avg:'$virtualDisk_write'},"
				+ "avgvirtualDisk_read:{$avg:'$virtualDisk_read'}}}";
		return grpVm;
	}
	
	public void insertAggregatedData(DBObject dbObject, String ip, String date) throws ClassNotFoundException, SQLException{
		String sql = null;
		sql = insertVMQuery(dbObject, date);
		
		//logger.info(sql);
		Connection con = getSqlConnection(ip);
		Statement stmt = con.createStatement();
		
		int rowsUpdated = stmt.executeUpdate(sql);
		if(rowsUpdated>0){
			//.info("rows inserted");
		}
		con.close();
	}

	private String insertVMQuery(DBObject dbObject, String date) {
		String name = (String) dbObject.get("_id");
		Double cpu_usage = (Double) dbObject.get("avgcpu_usage");
		Double cpu_used = (Double) dbObject.get("avgcpu_used");
		Double cpu_usagemhz = (Double) dbObject.get("avgcpu_usagemhz");
		Double cpu_system = (Double) dbObject.get("avgcpu_system");
		Double mem_shared = (Double) dbObject.get("avgmem_shared");
		Double mem_granted = (Double) dbObject.get("avgmem_granted");
		Double mem_usage = (Double) dbObject.get("avgmem_usage");
		Double mem_consumed = (Double) dbObject.get("avgmem_consumed");
		Double net_usage = (Double) dbObject.get("avgnet_usage");
		Double net_received = (Double) dbObject.get("avgnet_received");
		Double net_transmitted = (Double) dbObject.get("avgnet_transmitted");
		Double sys_heartbeat = (Double) dbObject.get("avgsys_heartbeat");
		Double sys_osUptime = (Double) dbObject.get("avgsys_osUptime");
		Double sys_uptime = (Double) dbObject.get("avgsys_uptime");
		Double virtualDisk_write = (Double) dbObject.get("avgvirtualDisk_write");
		Double virtualDisk_read = (Double) dbObject.get("avgvirtualDisk_read");
		
		String sql = "INSERT INTO vmstatistics (`vmname`,`cpu_usage`,`cpu_used`,`cpu_usagemhz`,`cpu_system`,`mem_shared`,`mem_granted`,`mem_usage`,`mem_consumed`,"
				     +"`net_usage`,`net_received`,`net_transmitted`,`sys_heartbeat`,`sys_osUptime`,`sys_uptime`,`virtualDisk_write`,`virtualDisk_read`,`inserteddatetime`) values ('"
					 + name +"',"
					 + cpu_usage +","
					 + cpu_used +","
					 + cpu_usagemhz +","
					 + cpu_system +","
					 + mem_shared +","
					 + mem_granted +","
					 + mem_usage+","
					 + mem_consumed +","
					 + net_usage +","
					 + net_received +","
					 + net_transmitted +","
					 + sys_heartbeat +","
					 + sys_osUptime +","
					 + sys_uptime +","
					 + virtualDisk_write +","
					 + virtualDisk_read +",'"
					 + date+"'"
					 +");";
		return sql;
	}
}
