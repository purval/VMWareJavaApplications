package edu.sjsu.collector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class VMlogCollector implements Runnable{

	final static Logger logger = Logger.getLogger(VMlogCollector.class);
	
	private volatile boolean threadStopper = false;
	Thread vmLogCollector;
	private static HashMap<Integer, PerfCounterInfo> headerInfo = new HashMap<Integer, PerfCounterInfo>();
	private int maxSamples=3;
	private ConfigProperties properties;
	
	public VMlogCollector(ConfigProperties properties) throws RemoteException, MalformedURLException{
		this.properties = properties;
		ServiceInstance si = new ServiceInstance(new URL(properties.getvCenterURL()), properties.getUsername(), properties.getPassword(), true);

		PerformanceManager performanceManager = si.getPerformanceManager();
		PerfCounterInfo[] infos = performanceManager.getPerfCounter();
		for (PerfCounterInfo info : infos) {
			headerInfo.put(new Integer(info.getKey()), info);
		}
		
		vmLogCollector = new Thread(this);
		vmLogCollector.start();
	}
	
	@Override
	public void run(){
		/*String fileNameTimestamp = "";
		SimpleDateFormat formatFileName = new SimpleDateFormat("MM-dd-yyyy");
		Date dateForFileName = new Date();
		fileNameTimestamp = formatFileName.format(dateForFileName);*/
		while(!threadStopper){
			try {
				vmLogCollector.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
					
				Date date = new Date(System.currentTimeMillis());
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss'Z'");
				StringBuffer outputStr = new StringBuffer();
				outputStr.append( format.format(date));
				outputStr.append(","+properties.getVmName());
				
				HashMap<String, HashMap<String, String>> metricsMap = getPerformanceMetrics(properties.getVmName());
				
				List<String> grpList = MetricsList.getGrpList();
				List<String> metricsList = MetricsList.getMetricList();
			
				for (String grpName : grpList) {
					
					HashMap<String, String> metricProps = metricsMap
							.get(grpName);
					
					for (String param : metricProps.keySet()) {
						if (metricsList.contains(param)) {
							if(metricProps.get(param).equalsIgnoreCase("")){
								outputStr.append(",0");
							}else{
								outputStr.append(param+"," + metricProps.get(param));
							}
						}
					}
				}
				
				try {
					/*String fileNameNewTimestamp = "";
					SimpleDateFormat formatNewFileName = new SimpleDateFormat("MM-dd-yyyy");
					Date dateForNewFileName = new Date();
					fileNameNewTimestamp = formatNewFileName.format(dateForNewFileName);
					
					if(!fileNameNewTimestamp.equalsIgnoreCase(fileNameTimestamp)){
						fileNameTimestamp = fileNameNewTimestamp;
					}*/
					
					//String fileName = properties.getOutputFile()+"vmlog"+fileNameTimestamp+".log";
					String fileName = properties.getOutputFile()+"vmlog.log";
					File file = new File(fileName);
					if (!file.exists()) {
						file.createNewFile();
					}
					
					FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
					BufferedWriter bw = new BufferedWriter(fw);
					System.out.println("Write: "+outputStr.toString());
					bw.append(outputStr.toString());
					bw.append("\n");
					bw.flush();
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
					
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	protected HashMap<String, HashMap<String, String>> getPerformanceMetrics(
			String vmName) throws Exception {

		ServiceInstance serviceInstance = new ServiceInstance(new URL(properties.getvCenterURL()), properties.getUsername(),
				properties.getPassword(), true);
		InventoryNavigator inventoryNavigator = new InventoryNavigator(
				serviceInstance.getRootFolder());
		VirtualMachine virtualMachine = (VirtualMachine) inventoryNavigator
				.searchManagedEntity("VirtualMachine", vmName);
		
		if (virtualMachine == null) {
			throw new Exception("Virtual Machine '" + vmName + "' not found.");
		}

		PerformanceManager performanceManager = serviceInstance
				.getPerformanceManager();
		
		PerfQuerySpec perfQuerySpec = new PerfQuerySpec();
		perfQuerySpec.setEntity(virtualMachine.getMOR());
		perfQuerySpec.setMaxSample(new Integer(maxSamples));
		perfQuerySpec.setFormat("normal");

		PerfProviderSummary pps = performanceManager
				.queryPerfProviderSummary(virtualMachine);
		perfQuerySpec
				.setIntervalId(new Integer(pps.getRefreshRate().intValue()));

		PerfEntityMetricBase[] pValues = performanceManager
				.queryPerf(new PerfQuerySpec[] { perfQuerySpec });

		if (pValues != null) {
			HashMap<String, HashMap<String, String>> metricsMap = generatePerformanceResult(pValues);
			serviceInstance.getServerConnection().logout();
			return metricsMap;
		} else {
			serviceInstance.getServerConnection().logout();
			throw new Exception("No values found!");
		}
	}
	
	private HashMap<String, HashMap<String, String>> generatePerformanceResult(
			PerfEntityMetricBase[] pValues) {
		HashMap<String, HashMap<String, String>> propertyGroups = new HashMap<String, HashMap<String, String>>();
		for (PerfEntityMetricBase p : pValues) {
			PerfEntityMetric pem = (PerfEntityMetric) p;
			PerfMetricSeries[] pms = pem.getValue();
			
			for (PerfMetricSeries pm : pms) {
				int counterId = pm.getId().getCounterId();
				PerfCounterInfo info = headerInfo.get(new Integer(counterId));
				
				String value = "";

				if (pm instanceof PerfMetricIntSeries) {
					PerfMetricIntSeries series = (PerfMetricIntSeries) pm;
					long[] values = series.getValue();
					long result = 0;
					for (long v : values) {
						result += v;
					}
					result = (long) (result / values.length);
					value = String.valueOf(result);
				} else if (pm instanceof PerfMetricSeriesCSV) {
					PerfMetricSeriesCSV seriesCsv = (PerfMetricSeriesCSV) pm;
					value = seriesCsv.getValue() + " in "
							+ info.getUnitInfo().getLabel();
				}
				
				HashMap<String, String> properties;
				if (propertyGroups.containsKey(info.getGroupInfo().getKey())) {
					properties = propertyGroups.get(info.getGroupInfo()
							.getKey());
				} else {
					properties = new HashMap<String, String>();
					propertyGroups
							.put(info.getGroupInfo().getKey(), properties);
				}

				String propName = String.format("%s_%s", info.getGroupInfo()
						.getKey(), info.getNameInfo().getKey());
				properties.put(propName, value);
				//System.out.println(propName+"  "+value);
			}
		}
		return propertyGroups;
	}

}
