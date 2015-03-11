package edu.sjsu.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;  
/**
 * @Team12
 * 
 */
public class CPUUsage {
  
    // static final String SERVER_NAME = "130.65.132.14";
    static final String SERVER_NAME = "130.65.132.244";
    static final String USER_NAME = "administrator";
    static final String PASSWORD = "12!@qwQW";

    public static Double getVMCPUUsage(String VMName) {
  
                  
                    try {
                        ServiceInstance si = new ServiceInstance(new URL(
                                "https://130.65.132.244/sdk"), "administrator",
                                "12!@qwQW", true);
  
                        VirtualMachine host = (VirtualMachine) new InventoryNavigator(
                                si.getRootFolder()).searchManagedEntity(
                                "VirtualMachine", VMName);
                        PerformanceManager perfMgr = si.getPerformanceManager();
                        PerfProviderSummary summary = perfMgr
                                .queryPerfProviderSummary(host);
                        int perfInterval = summary.getRefreshRate();
                        PerfMetricId[] queryAvailablePerfMetric = perfMgr
                                .queryAvailablePerfMetric(host, null, null,
                                        perfInterval);
  
                        
                        PerfQuerySpec qSpec = new PerfQuerySpec();
                        qSpec.setEntity(host.getMOR());
  
                       
  
                        qSpec.setMaxSample(1);
                        qSpec.setMetricId(queryAvailablePerfMetric);
  
                        qSpec.intervalId = perfInterval;
                        PerfEntityMetricBase[] pembs = perfMgr
                                .queryPerf(new PerfQuerySpec[] { qSpec });
  
  
                        for (int i = 0; pembs != null && i < pembs.length; i++) {
  
                            PerfEntityMetricBase val = pembs[i];
                            PerfEntityMetric pem = (PerfEntityMetric) val;
                            PerfMetricSeries[] vals = pem.getValue();
                            PerfSampleInfo[] infos = pem.getSampleInfo();
  
                            for (int j = 0; vals != null && j < vals.length; ++j) {
                                PerfMetricIntSeries val1 = (PerfMetricIntSeries) vals[j];
  
  
                                long[] longs = val1.getValue();
  
                                if (val1.getId().getCounterId() == 6)
                                	return new Double(longs[0]);
  
                                
                            }
                        }
                        
                        si.getServerConnection().logout();
                        
                    } catch (InvalidProperty e) {
                        e.printStackTrace();
                    } catch (RuntimeFault e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
					return null;
  
                }
    
    
    
    public static Double getHostCPUUsage(String HostName) {
    	  
        
        try {
            ServiceInstance si = new ServiceInstance(new URL(
                    "https://130.65.132.244/sdk"), "administrator",
                    "12!@qwQW", true);

            HostSystem host = (HostSystem) new InventoryNavigator(
                    si.getRootFolder()).searchManagedEntity(
                    "HostSystem", HostName);
            
            PerformanceManager perfMgr = si.getPerformanceManager();
            PerfProviderSummary summary = perfMgr
                    .queryPerfProviderSummary(host);
            int perfInterval = summary.getRefreshRate();
            PerfMetricId[] queryAvailablePerfMetric = perfMgr
                    .queryAvailablePerfMetric(host, null, null,
                            perfInterval);

            
            PerfQuerySpec qSpec = new PerfQuerySpec();
            qSpec.setEntity(host.getMOR());

           

            qSpec.setMaxSample(1);
            qSpec.setMetricId(queryAvailablePerfMetric);

            qSpec.intervalId = perfInterval;
            PerfEntityMetricBase[] pembs = perfMgr
                    .queryPerf(new PerfQuerySpec[] { qSpec });


            for (int i = 0; pembs != null && i < pembs.length; i++) {

                PerfEntityMetricBase val = pembs[i];
                PerfEntityMetric pem = (PerfEntityMetric) val;
                PerfMetricSeries[] vals = pem.getValue();
                PerfSampleInfo[] infos = pem.getSampleInfo();

                for (int j = 0; vals != null && j < vals.length; ++j) {
                    PerfMetricIntSeries val1 = (PerfMetricIntSeries) vals[j];


                    long[] longs = val1.getValue();

                    if (val1.getId().getCounterId() == 6)
                    	return new Double(longs[0]);

                    
                }
            }
            
            si.getServerConnection().logout();
            
        } catch (InvalidProperty e) {
            e.printStackTrace();
        } catch (RuntimeFault e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
		return null;

    }
  
} 