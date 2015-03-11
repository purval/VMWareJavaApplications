package edu.sjsu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmTriggeringAction;
import com.vmware.vim25.AuthorizationPrivilege;
import com.vmware.vim25.DatastoreInfo;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.GuestNicInfo;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.VirtualHardware;
import com.vmware.vim25.VirtualMachineCapability;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineQuickStats;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualMachineSummary;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.AuthorizationManager;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class DisasterRecovery {
	
	final static Logger logger = Logger.getLogger(DisasterRecovery.class);
	private static List<HostSystem> guestHostSystems = new ArrayList<HostSystem>();
	private static HostSystem[] hostList;
	protected static VirtualMachine[] vmList;
	private static InventoryNavigator iNav;
	private static InventoryNavigator adminINav;
	private static AlarmManager alarm;
	private static ArrayList<ResourcePool> resourceP;
	private static ClusterComputeResource[] clusterList;
	private static List<VirtualMachine> targetedVm = new ArrayList<VirtualMachine>();
	private static List<String> targetedVMnames = new ArrayList<String>();
	private static ConfigureProperties properties;
	public static void main(String[] args) throws IOException {
		
		// read properties file to load configuration
		try{
			Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = DisasterRecovery.class.getClassLoader().getResourceAsStream(propFileName);
			prop.load(inputStream);
			if (inputStream == null) {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
	 
			// load a properties file
			prop.load(inputStream);
			
			// get the property value and print it out	
			logger.info("setting up confuration for the project");
			
			logger.info("Targeted vms to be monitored");
			for(String vmName :  prop.getProperty("virtualMachines").split(",")){
				targetedVMnames.add(vmName);
				logger.info("vm -- "+vmName);
			}
			
			properties = new ConfigureProperties(targetedVMnames, prop.getProperty("adminvCenterUrl"),
					prop.getProperty("adminUserName"),prop.getProperty("adminPwd"), prop.getProperty("hostName"),
					prop.getProperty("vCenterUrl"), prop.getProperty("userName"), prop.getProperty("pwd"),
					Long.parseLong(prop.getProperty("pingOccuranceInSec")), Long.parseLong(prop.getProperty("snapShotOccurenceInSec")));
			
			logger.info("---------------------------- ");
		} catch (IOException ex) {
			ex.printStackTrace();
		} 

		URL adminUrl = new URL(properties.adminvCenterUrl);
		ServiceInstance siAdmin = new ServiceInstance(adminUrl, properties.adminUserName, properties.adminPwd, true);
		Folder rootFolderAdmin = siAdmin.getRootFolder();
		adminINav = new InventoryNavigator(rootFolderAdmin);
		String hostName = properties.hostName;
		VirtualMachine hostVM = (VirtualMachine) new InventoryNavigator(rootFolderAdmin)
		.searchManagedEntity("VirtualMachine", hostName);
		
		new HostSnapshot(hostVM);
		
		URL url = new URL(properties.vCenterUrl);
		ServiceInstance si = new ServiceInstance(url, properties.userName, properties.pwd, true);
		Folder rootFolder = si.getRootFolder();
		iNav = new InventoryNavigator(rootFolder);
		alarm = si.getAlarmManager();		
		
		logger.info("authorizations addded!!!");
		AuthorizationManager authorizationManager = si.getAuthorizationManager();
		AuthorizationPrivilege[] authorizationPrivileges = authorizationManager.getPrivilegeList();
		String[] privIds = new String[authorizationPrivileges.length];		
		for(int i=0; i<authorizationPrivileges.length; i++)
		{	
			privIds[i] = authorizationPrivileges[i].getPrivId();
		}	
		authorizationManager.addAuthorizationRole("vAPI", privIds);
		
		logger.info("Inventory list----");
		
		getHostList(iNav);
		getTargetedVM(hostList);
		//getResourcePool(iNav);
		
		for(VirtualMachine vm : targetedVm){
			logger.info("Targeted VM's"+ vm.getName() +" statistics-- ");
			showStats(vm, si);
			//new VMPing(vm, guestHostSystems, hostList, hostVM, rootFolderAdmin, rootFolder);
			//new VMSnapshotThreads(vm);
		}
	}
	
	public static void getHostList(InventoryNavigator iNav) throws InvalidProperty, RuntimeFault, RemoteException{
		ManagedEntity[] managedEntities = iNav.searchManagedEntities("HostSystem");
		hostList = new HostSystem[managedEntities.length];
		
		for(int iterator=0; iterator<managedEntities.length; iterator++) {
			hostList[iterator]=(HostSystem) managedEntities[iterator];
		}
		
		logger.info("Host list");
		for (int i=0; i<hostList.length; i++)
		{
			logger.info((i+1) + ". " + hostList[i].getName());
		}
		logger.info("-----------------------------");
	}
	
	public static void getTargetedVM(HostSystem[] hostSystemList) throws InvalidProperty, RuntimeFault, RemoteException{
		for(HostSystem hostSystem : hostSystemList){
			VirtualMachine[] vmList = hostSystem.getVms();
			for(VirtualMachine vm : vmList){
				if(targetedVMnames.contains(vm.getName())){
					targetedVm.add(vm);
					if(!guestHostSystems.contains(hostSystem)){
						guestHostSystems.add(hostSystem);
					}
					//setAlarm(vm, alarm);
				}
			}
		}
	}
	
	public static void setAlarm(VirtualMachine vm, AlarmManager alarmManager) throws InvalidName, RuntimeFault, RemoteException{
		/*Alarm[] as = alarmManager.getAlarm(vm);
		for(Alarm alarm : as){
			AlarmInfo info = alarm.getAlarmInfo();
			logger.debug(vm.getName()+"   "+info.description);
		}*/
		String alarmName = vm.getName() + "- AlarmPowerOff";
		
		StateAlarmExpression sae = new StateAlarmExpression();
		sae.setType("VirtualMachine");
		sae.setStatePath("runtime.powerState");
	    sae.setOperator(StateAlarmOperator.isEqual);
	    sae.setRed("poweredOff");
		
	    AlarmTriggeringAction alarmAction = new AlarmTriggeringAction();
		alarmAction.setYellow2red(true);
		
		AlarmSetting as = new AlarmSetting();
		as.setReportingFrequency(0); 
		as.setToleranceRange(0);
		
		AlarmSpec spec = new AlarmSpec();
		spec.setName(alarmName);
		spec.setDescription("Monitor VM state if VM power's off");
		spec.setExpression(sae);
		//spec.setAction(alarmAction);
		spec.setSetting(as);
		spec.setEnabled(true);
		
		try {
			alarmManager.createAlarm(vm,spec);	
		}
		catch(DuplicateName e){
			logger.error(vm.getName() + " alarm is already created.");
		}
		
		logger.info("Alarm is set for:" + vm.getName());
		logger.info("-----------------------------");
	}
	
	public static void showStats(VirtualMachine vm, ServiceInstance si) throws InvalidProperty, RuntimeFault, RemoteException{
		VirtualMachineConfigInfo vmc = vm.getConfig();
		VirtualMachineRuntimeInfo vmri = vm.getRuntime();
		VirtualMachineCapability vmcap = vm.getCapability();
		ResourcePool rp = vm.getResourcePool();
		ComputeResource rp2 = rp.getOwner();
		HostSystem[] rp3 = rp2.getHosts();
		
		logger.info("-----------------------------");
		logger.info("VM" + ":" + vm.getName());
		logger.info("OS:" + vmc.getGuestFullName());
		logger.info("Status:" + vmri.getPowerState());
		logger.info("Host Name:" + rp3[0].getName());
		
		logger.info("Multiple snapshot supported (True/False): " + vmcap.isMultipleSnapshotsSupported());
		logger.info("Provision supported (True/False): " + si.getCapability().isProvisioningSupported());
		/*logger.info("Service Content: " + si.getServiceContent());*/
		
		VirtualMachineSummary vms = vm.getSummary();
		VirtualMachineQuickStats vmqs = vms.getQuickStats();
		VirtualHardware vmh = vmc.getHardware();
		
		logger.info("Number of CPUs:" + vmh.getNumCPU());
		logger.info("CPU Speed:" + vmri.getMaxCpuUsage() + "MHz");
		logger.info("CPU Usage:" + vmqs.getOverallCpuUsage() + "MHz");
			
		logger.info("Total RAM:" + vmh.getMemoryMB()+ "MB");
		logger.info("RAM Usage:" + vmqs.getHostMemoryUsage()+ "MB");
		
		GuestInfo guestInfo = vm.getGuest();
		logger.debug(guestInfo.appHeartbeatStatus);
		logger.debug(guestInfo.guestFullName);
		logger.debug(guestInfo.guestState);
		logger.debug(guestInfo.hostName);
		GuestNicInfo[] nic = guestInfo.getNet();
		
		logger.info("IP:"+ vm.getGuest().ipAddress);
		if(nic!=null) 
		{
			if(nic.length>0 && nic[0]!=null)
			logger.info("Network:" + nic[0].getNetwork());
		}
		else{
			logger.debug("nic null");
		}
		
		Datastore[] vmn = vm.getDatastores();
		for(int i=0; i<vmn.length; i++) 
		{	
		logger.info("Datastore:" + vmn[i].getName());
		
		DatastoreInfo a = vmn[i].getInfo();
		logger.info("Location:" + a.getUrl());
		
		DatastoreSummary b = vmn[i].getSummary();
		logger.info("Total Size:" + (b.getCapacity()/(1024*1024*1024))+ " GB");
		logger.info("Free space:" + (a.getFreeSpace()/(1024*1024*1024)) + " GB");
		}
		logger.info("-----------------------------");
	}
	/*public static void getResourcePool(InventoryNavigator iNav){
		
	}*/
}
