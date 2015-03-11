package edu.sjsu.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class TestCollector implements Runnable{
final static Logger logger = Logger.getLogger(TestCollector.class);
	
	private volatile boolean threadStopper = false;
	Thread vmLogCollector;
	
	public TestCollector() throws RemoteException, MalformedURLException{
		vmLogCollector = new Thread(this);
		vmLogCollector.start();
	}
	
	@Override
	public void run(){
		int counter = 0;
		String fileNameTimestamp = "";
		SimpleDateFormat formatFileName = new SimpleDateFormat("MM-dd-yyyy");
		Date dateForFileName = new Date();
		fileNameTimestamp = formatFileName.format(dateForFileName);
			while(!threadStopper){
				try {
					vmLogCollector.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					for(int vmIterate=1;vmIterate<4;vmIterate++){
						Date date = new Date(System.currentTimeMillis());
						SimpleDateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd'T'HH:mm:ss'Z'");
						StringBuffer outputStr = new StringBuffer();
						outputStr.append(format.format(date));
						outputStr.append(",VM"+vmIterate);
						for(int metricIterate = 0; metricIterate<10; metricIterate++){
							outputStr.append("," + (counter+new Integer(10)));
							counter++;
						}
						try {
							String fileNameNewTimestamp = "";
							SimpleDateFormat formatNewFileName = new SimpleDateFormat("MM-dd-yyyy");
							Date dateForNewFileName = new Date();
							fileNameNewTimestamp = formatNewFileName.format(dateForNewFileName);
							System.out.println("new name "+fileNameNewTimestamp);
							if(!fileNameNewTimestamp.equalsIgnoreCase(fileNameTimestamp)){
								fileNameTimestamp = fileNameNewTimestamp;
							}
							System.out.println("file name "+fileNameTimestamp);
							String fileName = "E:\\2ndSem\\283\\lab\\lab4\\vmlog"+fileNameTimestamp+".log";
							File file = new File(fileName);
							if (!file.exists()) {
								file.createNewFile();
							}
							
							FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
							BufferedWriter bw = new BufferedWriter(fw);
							//System.out.println("Write: "+outputStr.toString());
							bw.append(outputStr.toString());
							bw.append("\n");
							bw.flush();
							bw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}	
					}	
				}catch(Exception e){
					
				}
			}
	}		
}
