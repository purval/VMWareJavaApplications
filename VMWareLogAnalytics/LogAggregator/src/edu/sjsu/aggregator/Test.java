package edu.sjsu.aggregator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;

public class Test {
	public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException {
		/*
		String[] arr = {"T03-VM01","T03-VM02","T03-VM02","CPU"};
		Pattern pattern = Pattern.compile("(T03+)");
		System.out.println("start");
		for(String a : arr){
			Matcher matcher = pattern.matcher(a);
			if (matcher.find()){
			     System.out.println(a);
			    } 
		}*/
		   final String JDBC_DRIVER = "com.mysql.jdbc.Driver"; 
		   final String DB_URL = "jdbc:mysql://130.65.133.208/vmdb";
		   //  Database credentials
		   final String USER = "root";
		   final String PASS = "";
		   Class.forName(JDBC_DRIVER);

		   Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		   
		   java.util.Date dt = new java.util.Date();
	        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
	                "yyyy-MM-dd HH:mm:ss");

	        String currentTime = sdf.format(dt);
		   
		   String sql = null;
		   sql = insertVMQuery(Timestamp.valueOf(currentTime));
		   System.out.println(sql);
		   Statement stmt = conn.createStatement();
		   int rowsUpdated = stmt.executeUpdate(sql);
		   if(rowsUpdated>0){
				//.info("rows inserted");
		   }else{
			   System.out.println("not inserted");
		   }
		   conn.close();
	}
	
	public static String insertVMQuery(Timestamp datetime) {
		
		String sql =  "INSERT INTO vmstatistics (`vmname`,`cpu_usage`,`cpu_used`,`cpu_usagemhz`,`cpu_system`,`mem_shared`,`mem_granted`,`mem_usage`,`mem_consumed`,"
			     +"`net_usage`,`net_received`,`net_transmitted`,`sys_heartbeat`,`sys_osUptime`,`sys_uptime`,`virtualDisk_write`,`virtualDisk_read`,`inserteddatetime`) values ('"
					 +"sample',"
					 + 1 +","
					 + 2+","
					 + 3 +","
					 + 4 +","
					 + 5 +","
					 + 6 +","
					 + 7 +","
					 + 8 +","
					 + 9 +","
					 + 10 +","
					 + 11 +","
					 + 12 +","
					 + 13 +","
					 + 14 +","
					 + 15 +","
					 + 16 +",'"
					 + datetime.toString()+"'"
					 +");";
		return sql;
	}
}
