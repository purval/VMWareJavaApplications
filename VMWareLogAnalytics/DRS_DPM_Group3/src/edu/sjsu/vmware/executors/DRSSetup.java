package edu.sjsu.vmware.executors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.vmware.vim25.AuthorizationPrivilege;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.AuthorizationManager;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

import edu.sjsu.utils.AddNewVM;
import edu.sjsu.utils.CPUCollector;
import edu.sjsu.utils.ConfigureProperties;

public class DRSSetup {
	final static Logger logger = Logger.getLogger(DRSSetup.class);
	
	private static ConfigureProperties properties;
	private static InventoryNavigator iNav;
	private static HostSystem[] hostList;
	private static Map<String, Double> hostCpuResults = new HashMap<String, Double>();
	
	public static void main(String[] args) throws MalformedURLException, RemoteException {
		// read properties file to load configuration
		try{
			Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = DRSSetup.class.getClassLoader().getResourceAsStream(propFileName);
			prop.load(inputStream);
			if (inputStream == null) {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
	 
			// load a properties file
			prop.load(inputStream);
			
			// get the property value and print it out	
			logger.info("setting up confuration for the project");
			
			properties = new ConfigureProperties(prop.getProperty("adminvCenterUrl"),
					prop.getProperty("adminUserName"),prop.getProperty("adminPwd"),
					prop.getProperty("vCenterUrl"), prop.getProperty("userName"), prop.getProperty("pwd"));
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
		
		URL url = new URL(properties.getvCenterUrl());
		ServiceInstance si = new ServiceInstance(url, properties.getUserName(), properties.getPwd(), true);
		Folder rootFolder = si.getRootFolder();
		iNav = new InventoryNavigator(rootFolder);	
		
		logger.info("authorizations addded!!!");
		AuthorizationManager authorizationManager = si.getAuthorizationManager();
		AuthorizationPrivilege[] authorizationPrivileges = authorizationManager.getPrivilegeList();
		String[] privIds = new String[authorizationPrivileges.length];		
		for(int i=0; i<authorizationPrivileges.length; i++)
		{	
			privIds[i] = authorizationPrivileges[i].getPrivId();
		}	
		authorizationManager.addAuthorizationRole("vAPI", privIds);
		
		HostSystem[] hostSystems = getHostList(iNav);	
		for(HostSystem host : hostSystems){
			long cpuResult = CPUCollector.getHostsCpu(host, properties);
			if(cpuResult != -1){
				double cpuPerc= cpuResult;
				hostCpuResults.put(host.getName(), cpuPerc);
				logger.info(host.getName()+"   cpu usage  "+cpuPerc);
			}
		}
		
		if(hostCpuResults.size()>1){
			Map<String, Double> sortedHostCpuResults = compareHost(hostCpuResults);
			for(String addVmToHost : sortedHostCpuResults.keySet()){
				for(HostSystem host : hostSystems){
					if(host.getName().equalsIgnoreCase(addVmToHost)){
						logger.info("adding new vm to "+host.getName()+"  with cpu Usage "+sortedHostCpuResults.get(addVmToHost));
						AddNewVM.addVM(host, properties, "T03-VM03-Ubuntu");
					}
				}
				break;	
			}
		}
		
		si.getServerConnection().logout();
	}
	
	public static Map<String, Double> compareHost(Map<String, Double> hostCpuResults){
		List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(hostCpuResults.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Double>>()
        {
            public int compare(Entry<String, Double> o1,
                    Entry<String, Double> o2)
            {
               return o1.getValue().compareTo(o2.getValue());
            }
        });

        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Entry<String, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
	}
	
	public static HostSystem[] getHostList(InventoryNavigator iNav) throws InvalidProperty, RuntimeFault, RemoteException{
		ManagedEntity[] managedEntities = iNav.searchManagedEntities("HostSystem");
		hostList = new HostSystem[managedEntities.length];
		
		for(int iterator=0; iterator<managedEntities.length; iterator++) {
			hostList[iterator]=(HostSystem) managedEntities[iterator];
		}
		return hostList;
	}
}
