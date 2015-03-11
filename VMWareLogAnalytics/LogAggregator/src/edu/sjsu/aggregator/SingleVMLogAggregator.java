package edu.sjsu.aggregator;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class SingleVMLogAggregator implements Runnable{
	final static Logger logger = Logger.getLogger(SingleVMLogAggregator.class);
	
	private volatile boolean threadStopper = false;
	Thread aggregatorThread;
	private static DB db;
	private DBProperties properties;
	
	public SingleVMLogAggregator(DBProperties properties){
		this.properties = properties;
		
		aggregatorThread = new Thread(this);
		aggregatorThread.start();
	}
	
	@Override
	public void run(){
		while(!threadStopper){
			try {
				aggregatorThread.sleep(5*1000);
				try {
					try {
						getAverage();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				getAverage();
			} catch (Exception e){
				e.printStackTrace();
			}	
		}
	}
	
	public DB getConnection() throws UnknownHostException{
		if (db == null) {
			MongoClient client = new MongoClient(properties.getIP(), properties.getPort());
			db = client.getDB("logcollection");
		} 
	  return db;
	}
	
	public Connection getSqlConnection() throws ClassNotFoundException, SQLException{
			// JDBC driver name and database URL
		  
		   final String JDBC_DRIVER = properties.getJdbcDriver();  
		   final String DB_URL = properties.getSqlURL();

		   //  Database credentials
		   final String USER = properties.getUsername();
		   final String PASS = properties.getPassword();
		   Class.forName(JDBC_DRIVER);

		   Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		   return conn;
	}
	
	public void getAverage() throws UnknownHostException, ClassNotFoundException, SQLException, ParseException{
		DB db = getConnection();
		DBCollection stats = db.getCollection("vmstats");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Calendar cal = Calendar.getInstance();
		System.out.println("Current Date Time : " + dateFormat.format(cal.getTime()));
		
		BasicDBObject query = new BasicDBObject();
		query.put("logdate", new BasicDBObject("$lte", dateFormat.format(cal.getTime())));
		cal.add(Calendar.HOUR, -2);
		query.put("logdate", new BasicDBObject("$gte",dateFormat.format(cal.getTime())));
		System.out.println("Back Date Time : " + dateFormat.format(cal.getTime()));
		
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
		
		logger.info("Agggregate outpot ::"+ output);
		
		Date date = new Date();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date myDate = formatter.parse(date.toString());
		java.sql.Date insertdate = new java.sql.Date(myDate.getTime());
		
		ArrayList<DBObject> list = (ArrayList<DBObject>) output.results();
		for (DBObject dbObject : list) {
			insertAggregatedData(dbObject, insertdate);
			//System.out.println(dbObject);
		}
		aggregateStats.drop();
	}

	private String vmAverageQuery() {
		String grpVm = "{$group:{_id:'$vmname',"
				+ "avgcpu_usage:{$avg:'$cpu_usage'},"
				+ "avgcpu_usagemhz:{$avg:'$cpu_usagemhz'},"
				+ "avgcpu_used:{$avg:'$cpu_used'},"
				+ "avgcpu_system:{$avg:'$cpu_system'},"
				+ "avgmem_usage:{$avg:'$mem_usage'},"
				+ "avgmem_shared:{$avg:'$mem_shared'},"
				+ "avgmem_granted:{$avg:'$mem_granted'},"
				+ "avgmem_consumed:{$avg:'$mem_consumed'},"
				+ "avgnet_usage:{$avg:'$net_usage'},"
				+ "avgnet_received:{$avg:'$net_received'},"
				+ "avgnet_transmitted:{$avg:'$net_transmitted'},"
				+ "avgsys_heartbeat:{$avg:'$sys_heartbeat'},"
				+ "avgsys_osUptime:{$avg:'$sys_osUptime'},"
				+ "avgsys_uptime:{$avg:'$sys_uptime'},"
				+ "avgvirtualDisk_read:{$avg:'$virtualDisk_read'},"
				+ "avgvirtualDisk_write:{$avg:'$virtualDisk_write'}}}";
		return grpVm;
	}
	
	public void insertAggregatedData(DBObject dbObject, Date date) throws ClassNotFoundException, SQLException{
		String sql = null;
		sql = insertVMQuery(dbObject, date);
		
		logger.info(sql);
		Connection con = getSqlConnection();
		Statement stmt = con.createStatement();
		
		int rowsUpdated = stmt.executeUpdate(sql);
		if(rowsUpdated>0){
			//.info("rows inserted");
		}
		con.close();
	}

	private String insertVMQuery(DBObject dbObject, Date date) {
		String name = (String) dbObject.get("_id");
		Double cpu_usage = (Double) dbObject.get("avgcpu_usage");
		Double cpu_usagemhz = (Double) dbObject.get("avgcpu_usagemhz");
		Double cpu_used = (Double) dbObject.get("avgcpu_used");
		Double cpu_system = (Double) dbObject.get("avgcpu_system");
		Double mem_usage = (Double) dbObject.get("avgmem_usage");
		Double mem_shared = (Double) dbObject.get("avgmem_shared");
		Double mem_granted = (Double) dbObject.get("avgmem_granted");
		Double mem_consumed = (Double) dbObject.get("avgmem_consumed");
		Double net_usage = (Double) dbObject.get("avgnet_usage");
		Double net_received = (Double) dbObject.get("avgnet_received");
		Double net_transmitted = (Double) dbObject.get("avgnet_transmitted");
		Double sys_heartbeat = (Double) dbObject.get("avgsys_heartbeat");
		Double sys_osUptime = (Double) dbObject.get("avgsys_osUptime");
		Double sys_uptime = (Double) dbObject.get("avgsys_uptime");
		Double virtualDisk_read = (Double) dbObject.get("avgvirtualDisk_read");
		Double virtualDisk_write = (Double) dbObject.get("avgvirtualDisk_write");
		
		String sql = "INSERT INTO vmstatistics (`vmname`,`cpu_usage`,`cpu_usagemhz`,`net_transmitted`,`cpu_used`,`cpu_system`,`mem_usage`,`mem_shared`,`mem_granted`,"
				     +"`mem_consumed`,`net_usage`,`net_received`,`sys_heartbeat`,`sys_osUptime`,`sys_uptime`,`virtualDisk_read`,`virtualDisk_write`.`timestamp`) values ('"
					 + name +"',"
					 + cpu_usage +","
					 + cpu_usagemhz +","
					 + cpu_used +","
					 + cpu_system +","
					 + mem_usage+","
					 + mem_shared +","
					 + mem_granted +","
					 + mem_consumed +","
					 + net_usage +","
					 + net_received +","
					 + net_transmitted +","
					 + sys_heartbeat +","
					 + sys_osUptime +","
					 + sys_uptime +","
					 + virtualDisk_read +","
					 + virtualDisk_write +","
					 + date
					 +");";
		return sql;
	}
}
