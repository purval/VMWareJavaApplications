package edu.sjsu.collector;

import java.util.ArrayList;
import java.util.List;

public class MetricsList {
	static List<String> merticsList = new ArrayList<String>();
	static List<String> grpList = new ArrayList<String>();

	public MetricsList() {
		for (Metrics metric : Metrics.values()) {
			merticsList.add(metric.toString());
		}	
		for (Grp grp : Grp.values()) {
			grpList.add(grp.toString());
		}
	}
	
	public static List<String> getMetricList(){
		if(merticsList == null){
			new MetricsList();
		}
		return merticsList;
	}
	
	public static List<String> getGrpList(){
		if(grpList == null){
			new MetricsList();
		}
		return grpList;
	}
	
	public enum Grp{
		cpu,
		mem,
		net,
		sys,
		virtualDisk
	}
	
	public enum Metrics {
		cpu_usage,
		cpu_usagemhz,
		cpu_used,
		cpu_system,
		mem_usage,
		mem_shared,
		mem_granted,
		mem_consumed,
		net_usage,
		net_received,
		net_transmitted,
		sys_heartbeat,
		sys_osUptime,
		sys_uptime,
		virtualDisk_read,
		virtualDisk_write
	}
	//cpu_latency
	//virtualDisk_numberReadAveraged
	//virtualDisk_numberWriteAveraged
	//virtualDisk_totalWriteLatency
	//virtualDisk_writeLoadMetric 
	//virtualDisk_readLoadMetric  
	//virtualDisk_totalReadLatency 
	//disk_maxTotalLatency
	//virtualDisk_readOIO
	//virtualDisk_writeOIO
	/* public static int PERF_METRIC_ID_CPU = 6; 
    public static int PERF_METRIC_ID_MEM = 33; 
    public static int PERF_METRIC_ID_DISK = 125; 
    public static int PERF_METRIC_ID_NETWORK = 143; 
    public static int PERF_METRIC_ID_DISK_LATENCY = 133; */
}
