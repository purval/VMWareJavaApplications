package edu.sjsu.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class CPUCollector{
	final static Logger logger = Logger.getLogger(CPUCollector.class);
	
	private static final int cpuIndex = 6;	

	public static long getHostsCpu(HostSystem host, ConfigureProperties properties) throws InvalidProperty, RuntimeFault, RemoteException, MalformedURLException{
		long cpuVal = -1;
		ServiceInstance si = new ServiceInstance(new URL(properties.getvCenterUrl()),properties.getUserName(),properties.getPwd(), true);
		HostSystem hostPerf = (HostSystem) new InventoryNavigator(
				si.getRootFolder()).searchManagedEntity(
				"HostSystem", host.getName()); 
		PerformanceManager perfMgr = si.getPerformanceManager();
		
		PerfProviderSummary summary = perfMgr.queryPerfProviderSummary(hostPerf);
		
		int perfInterval = summary.getRefreshRate();
		
		PerfMetricId[] queryAvailablePerfMetric = perfMgr.queryAvailablePerfMetric(hostPerf, null, null,perfInterval);
		
		ArrayList<PerfMetricId> list = new ArrayList<PerfMetricId>();
		for (int iterator = 0; iterator < queryAvailablePerfMetric.length; iterator++) 
		{
			PerfMetricId perfMetricId = queryAvailablePerfMetric[iterator];
			if (cpuIndex == perfMetricId.getCounterId()) {
				list.add(perfMetricId);
			}
		}
		PerfMetricId[] pmis = list.toArray(new PerfMetricId[list
				.size()]);
		PerfQuerySpec qSpec = new PerfQuerySpec();
		qSpec.setEntity(hostPerf.getMOR());
		qSpec.setMetricId(pmis);

		qSpec.intervalId = perfInterval;
		PerfEntityMetricBase[] pembs = perfMgr
				.queryPerf(new PerfQuerySpec[] { qSpec });
		
		for (int i = 0; pembs != null && i < pembs.length; i++) {

			PerfEntityMetricBase val = pembs[i];
			PerfEntityMetric pem = (PerfEntityMetric) val;
			PerfMetricSeries[] vals = pem.getValue();				
			
			for (int j = 0; vals != null && j < vals.length; ++j) {
				PerfMetricIntSeries value = (PerfMetricIntSeries) vals[j];
				long[] cpuValue = value.getValue();
				//logger.info("Host CPU :"+ cpuValue[5]);
				cpuVal =  cpuValue[5];
				//return cpuVal;
			}
		}
		si.getServerConnection().logout();
		return cpuVal;
	}
	
	public static long getVMsCpu(VirtualMachine vm, ConfigureProperties properties) throws InvalidProperty, RuntimeFault, RemoteException, MalformedURLException{
		long cpuVal = -1;
		ServiceInstance si = new ServiceInstance(new URL(properties.getvCenterUrl()),properties.getUserName(),properties.getPwd(), true);
		VirtualMachine vmPerf = (VirtualMachine) new InventoryNavigator(
				si.getRootFolder()).searchManagedEntity(
				"VirtualMachine", vm.getName()); 
		PerformanceManager perfMgr = si.getPerformanceManager();
		
		PerfProviderSummary summary = perfMgr.queryPerfProviderSummary(vmPerf);
		
		int perfInterval = summary.getRefreshRate();
		
		PerfMetricId[] queryAvailablePerfMetric = perfMgr.queryAvailablePerfMetric(vmPerf, null, null,perfInterval);
		
		ArrayList<PerfMetricId> list = new ArrayList<PerfMetricId>();
		for (int iterator = 0; iterator < queryAvailablePerfMetric.length; iterator++) 
		{
			PerfMetricId perfMetricId = queryAvailablePerfMetric[iterator];
			if (cpuIndex == perfMetricId.getCounterId()) {
				list.add(perfMetricId);
			}
		}
		PerfMetricId[] pmis = list.toArray(new PerfMetricId[list
				.size()]);
		PerfQuerySpec qSpec = new PerfQuerySpec();
		qSpec.setEntity(vmPerf.getMOR());
		qSpec.setMetricId(pmis);

		qSpec.intervalId = perfInterval;
		PerfEntityMetricBase[] pembs = perfMgr
				.queryPerf(new PerfQuerySpec[] { qSpec });
		
		for (int i = 0; pembs != null && i < pembs.length; i++) {

			PerfEntityMetricBase val = pembs[i];
			PerfEntityMetric pem = (PerfEntityMetric) val;
			PerfMetricSeries[] vals = pem.getValue();				
			
			for (int j = 0; vals != null && j < vals.length; ++j) {
				PerfMetricIntSeries value = (PerfMetricIntSeries) vals[j];
				long[] cpuValue = value.getValue();
				//logger.info("VM CPU :"+ cpuValue[5]);
				cpuVal =  cpuValue[5];
			}
		}
		si.getServerConnection().logout();
		return cpuVal;
	}
}

