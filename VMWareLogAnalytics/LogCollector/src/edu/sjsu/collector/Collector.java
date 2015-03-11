package edu.sjsu.collector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Collector {
	final static Logger logger = Logger.getLogger(Collector.class);
	
	private static ConfigProperties properties;
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		// read properties file to load configuration
		try{
			Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = Collector.class.getClassLoader().getResourceAsStream(propFileName);
			prop.load(inputStream);
			if (inputStream == null) {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
	 
			// load a properties file
			prop.load(inputStream);
			
			properties = new ConfigProperties(prop.getProperty("vCenterUrl"),
					prop.getProperty("userName"),prop.getProperty("pwd"), prop.getProperty("vmname"),
					prop.getProperty("outputvmlogs"));
			
			new MetricsList();
			
			new VMlogCollector(properties);
			//new TestCollector();
			//new TestAggregator();
			//TestAggregator agg = new TestAggregator();
			//agg.getAverage();
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
	}
}
