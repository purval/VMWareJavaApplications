package edu.sjsu.vmware.executors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
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
import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.AuthorizationManager;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

import edu.sjsu.utils.CPUCollector;
import edu.sjsu.utils.ConfigureProperties;

public class DRS {
final static Logger logger = Logger.getLogger(DRS.class);
	
	private static ConfigureProperties properties;
	private static InventoryNavigator iNav;
	private static HostSystem[] hostList;
	private static Map<String, Double> hostCpuResults = new HashMap<String, Double>();
	private static Map<String, Double> vmCpuResults = new HashMap<String, Double>();
	
	public static void main(String[] args) throws Exception {
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
		List<VirtualMachine> vmList = getVMs(hostSystems);
		//addingNewHost(rootFolder, "130.65.132.154");
		
		for(HostSystem host : hostSystems){
			long cpuResult = CPUCollector.getHostsCpu(host, properties);
			if(cpuResult != -1){
				double cpuPerc= cpuResult;
				double base = 8096.0;
				double perc = (cpuPerc/base)*100;
				//logger.info(host.getName()+"  cpu usage  "+perc+" %");
				hostCpuResults.put(host.getName(), cpuPerc);
				logger.info(host.getName()+"  cpu usage  "+cpuPerc);
			}
		}
		
		for(VirtualMachine vm : vmList){
			long cpuResult = CPUCollector.getVMsCpu(vm, properties);
			if(cpuResult != -1){
				double cpuPerc= cpuResult;
				double base = 4076.0;
				double perc = (cpuPerc/base)*100;
				//logger.info(vm.getName()+"  cpu usage  "+perc+" %");
				vmCpuResults.put(vm.getName(), cpuPerc);
				logger.info(vm.getName()+"   cpu usage  "+cpuPerc);
			}
		}
		
		//DRS algorithm
		/*Map<String, Double> sortedVMCpuResults = compareEntities(vmCpuResults);
		if(hostCpuResults.size()>1){
			Map<String, Double> sortedHostCpuResults = compareEntities(hostCpuResults);
			int index = 0;
			for(String addVmToHost : sortedHostCpuResults.keySet()){
				if(index == 0){
					int vmIndex = 0;
					for(HostSystem host : hostSystems){
						if(host.getName().equalsIgnoreCase(addVmToHost)){
							for(String vmname : sortedVMCpuResults.keySet()){
								if(vmIndex == sortedVMCpuResults.size()-1){
									logger.info("recommend execution for migration ");
									logger.info("migrate vm "+vmname+ " to host "+host.getName());
									 liveMigrateVM(host.getName(), vmname);
								}
								vmIndex++;	
							}
						}
					}
				break;	
				}
				index++;
			}
		}*/
		
		si.getServerConnection().logout();
	}
	
	public static Map<String, Double> compareEntities(Map<String, Double> entityCpuResults){
		List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(entityCpuResults.entrySet());

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
	
	public static List<VirtualMachine> getVMs(HostSystem[] hostSystemList) throws InvalidProperty, RuntimeFault, RemoteException{
		List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
		for(HostSystem hostSystem : hostSystemList){
			VirtualMachine[] vms = hostSystem.getVms();
			for(VirtualMachine vm : vms){
				vmList.add(vm);
			}
		}
		return vmList;
	}
	
	public static HostSystem[] getHostList(InventoryNavigator iNav) throws InvalidProperty, RuntimeFault, RemoteException{
		ManagedEntity[] managedEntities = iNav.searchManagedEntities("HostSystem");
		hostList = new HostSystem[managedEntities.length];
		
		for(int iterator=0; iterator<managedEntities.length; iterator++) {
			hostList[iterator]=(HostSystem) managedEntities[iterator];
		}
		return hostList;
	}
	
	
	public static void addingNewHost(Folder rootFolder, String ip)
	{
		try{	
			ManagedEntity [] mes =  new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter");
			Datacenter dc = new Datacenter(rootFolder.getServerConnection(),  mes[0].getMOR());
			HostConnectSpec hs = new HostConnectSpec();
			
			hs.hostName= ip;
			hs.userName ="root";
			hs.password = "12!@qwQW";
			hs.managementIp = "130.65.132.14";
			hs.setSslThumbprint("09:33:B1:E2:77:80:A4:81:7A:2C:66:3C:AA:4C:ED:38:01:E1:96:E6");
			//hs.setSslThumbprint("BE:80:6B:3E:5D:59:CB:04:63:DD:0D:89:0D:4B:BC:0B:A6:07:1C:25");
			ComputeResourceConfigSpec crcs = new ComputeResourceConfigSpec();
			Task addHost = dc.getHostFolder().addStandaloneHost_Task(hs,crcs, true);
			while(addHost.getTaskInfo().getState()==TaskInfoState.running){
    		}
    		
			if(addHost.getTaskInfo().getState()==TaskInfoState.success){
				logger.info("==============================================================");
				logger.info("vHost is added successfully");
				logger.info("==============================================================");
			}
			else{
				logger.info("There is some error in adding host");
			}
		}   
		catch (Exception re){
		}
	}
	
	public static void liveMigrateVM(String new_host_ip, String vmname)throws Exception{   
		ServiceInstance si= new ServiceInstance(new URL(properties.getvCenterUrl()),
				properties.getUserName(), properties.getPwd(), true);
		Folder rootFolder = si.getRootFolder();
		String ip=new_host_ip;
		try{
			System.out.println("Starting the live migration");
			VirtualMachine vm = (VirtualMachine)new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmname);
			HostSystem newHost =(HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", ip);
			ComputeResource cr =(ComputeResource) newHost.getParent();
			String []checks =new String[]{"cpu","software"};
			HostVMotionCompatibility[] vmcs = si.queryVMotionCompatibility(vm, new HostSystem[]{newHost},checks);
			String[] comps= vmcs[0].getCompatibility();
			if(checks.length !=comps.length){
				System.out.println("CPY/software not compatible");
				return;
			}
			Task liveMigrate =vm.migrateVM_Task(cr.getResourcePool(), newHost, VirtualMachineMovePriority.highPriority, 
					VirtualMachinePowerState.poweredOn);
			while(liveMigrate.getTaskInfo().getState()==TaskInfoState.running){
    		}
    		
			if(liveMigrate.getTaskInfo().getState()==TaskInfoState.success){
				logger.info("==============================================================");
				logger.info("Live migration of virtual machine done successfully");
				logger.info("==============================================================");
			}
			else{
				logger.info("Live migration failed");
				TaskInfo info = liveMigrate.getTaskInfo();
				logger.info(info.getError().getFault());
			}
		}catch (Exception e){
		}
	}
	
	public static void coldMigrateVM(String new_host_ip, String vmName){
		try{
			ServiceInstance si= new ServiceInstance(new URL(properties.getvCenterUrl()),
					properties.getUserName(), properties.getPwd(), true);
		    Folder rootFolder = si.getRootFolder();
				
			VirtualMachine vm = (VirtualMachine)new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);
			HostSystem newHost = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem",new_host_ip);
			ComputeResource cr = (ComputeResource) newHost.getParent();
			Task coldMigrate = vm.migrateVM_Task(cr.getResourcePool(),newHost,	
			VirtualMachineMovePriority.lowPriority,VirtualMachinePowerState.poweredOff);
			while(coldMigrate.getTaskInfo().getState()==TaskInfoState.running){
    		}
    		
			if(coldMigrate.getTaskInfo().getState()==TaskInfoState.success){
				logger.info("==============================================================");
				logger.info("Migration to new host completed.");
				logger.info("==============================================================");
			}else{
				logger.info("cold migration failed");
				TaskInfo info = coldMigrate.getTaskInfo();
				logger.info(info.getError().getFault());
			}  	
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
