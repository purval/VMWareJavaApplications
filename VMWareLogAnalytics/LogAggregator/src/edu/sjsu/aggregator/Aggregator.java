package edu.sjsu.aggregator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Aggregator {
	final static Logger logger = Logger.getLogger(Aggregator.class);
	
	private static DBProperties properties;
	private static ConfigProperties configProperties;
	public static void main(String[] args) {
		// read properties file to load configuration
		try{
			Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = Aggregator.class.getClassLoader().getResourceAsStream(propFileName);
			prop.load(inputStream);
			if (inputStream == null) {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
	 
			// load a properties file
			prop.load(inputStream);
			
			// get the property value and print it out	
			logger.info("setting up confuration for the project");
			
			properties = new DBProperties(prop.getProperty("MONGODB_IP"),
					Integer.parseInt(prop.getProperty("MONGODB_PORT")),prop.getProperty("JDBC_DRIVER"), prop.getProperty("SQL_URL"),
					prop.getProperty("SQL_USERNAME"), prop.getProperty("SQL_PASSWORD"));
			
			configProperties = new ConfigProperties(prop.getProperty("vCenterUrl"),
					prop.getProperty("userName"),prop.getProperty("pwd"), prop.getProperty("vmname"),
					prop.getProperty("outputvmlogs"));
			
			//new SingleVMLogAggregator(properties);
			new VMLogAggregator(properties, configProperties);
			new VMHourLogCollector(properties, configProperties);
			new VMDaylyLogCollector(properties, configProperties);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
	}
}
