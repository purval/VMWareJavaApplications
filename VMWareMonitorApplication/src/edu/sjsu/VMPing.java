package edu.sjsu;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;

import com.vmware.vim25.FileFault;
import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VMPing implements Runnable{
	private static Logger logger = Logger.getLogger(VMPing.class);

	private volatile boolean threadStopper = false;
	
	VirtualMachine vm;
	VirtualMachine hostVM;
	Thread vmPingThread;
	List<HostSystem> guestHostSystems;
	HostSystem[] hostList;
	Folder rootFolderAdmin;
	Folder rootFolder;
	
	public VMPing(VirtualMachine vm, List<HostSystem> guestHostSystems, HostSystem[] hostList, VirtualMachine hostVM, Folder rootFolderAdmin ,Folder rootFolder){
		logger.info("start pinging vm "+vm.getName());
		this.vm = vm;
		this.hostVM = hostVM;
		this.guestHostSystems = guestHostSystems;
		this.hostList= hostList;
		this.rootFolderAdmin = rootFolderAdmin;
		this.rootFolder = rootFolder;
		vmPingThread = new Thread(this);
		vmPingThread.start();
		logger.info(vmPingThread.getName());
	}
	
	@Override
	public void run(){
		while(!threadStopper){
			try {
				vmPingThread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			/*try{
				String IP = vm.getGuest().getIpAddress();
				if(IP!=null){
					InetAddress inet = InetAddress.getByName(IP);
					if(inet.isReachable(4000)){
						logger.info(vm.getName()+" is replying " + IP);
					}else{
						logger.info(vm.getName()+" is not replying " + IP);
						if(vm.getTriggeredAlarmState() != null){
							if(vm.getTriggeredAlarmState()[0].getOverallStatus().equals(ManagedEntityStatus.red)){
								logger.info(vm.getName()+" is powered off by user");
							}
						}else{
							logger.info(vm.getName()+" is ---not--- powered off by user "+ IP);
							for(HostSystem hostSystem : guestHostSystems){
								VirtualMachine[] vmList = hostSystem.getVms();
								for(VirtualMachine vmCheck : vmList){
									if(vmCheck.getName().equalsIgnoreCase(vm.getName())){
										String hostIP = hostSystem.getName();
										int retryCount = 0;
										if(pinghost(hostIP)){
											logger.info("Host is running !!!");
											if(retryCount<2){
												String retryIP = vm.getGuest().getIpAddress();
												logger.info("retrying to connect "+vm.getName() +" count "+retryCount);
												if(pinghost(retryIP)){
													break;
												}
												retryCount++;
												try {
													vmPingThread.sleep(3000);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}
											RetriveVM.retriveVM(vmCheck, hostList);	
											
											try {
												vmPingThread.sleep(1000*120);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											
											new VMPing(vm, guestHostSystems, hostList, hostVM, rootFolderAdmin, rootFolder);
											logger.info("closing current thread !!!");
											threadStopper = true;
										}
										else{
											logger.info("Host not running !!!");
											if(retriveHost(hostVM)){
												logger.info("vhost restarted successfully");
											}else{
												String newHost = searchingHost();
												migratingNewHost(newHost);
											}						
										}
									}
								}
							}	
						}
					}
				}else{
					logger.info(vm.getName()+" not replying " + vmPingThread.getName());
					
					if(vm.getTriggeredAlarmState() != null){
						if(vm.getTriggeredAlarmState()[0].getOverallStatus().equals(ManagedEntityStatus.red)){
							logger.info(vm.getName()+" is powered off by user");
						}
					}else{
						logger.info(vm.getName()+" is !!!not!!! powered off by user");
						for(HostSystem hostSystem : guestHostSystems){
							VirtualMachine[] vmList = hostSystem.getVms();
							for(VirtualMachine vmCheck : vmList){
								if(vmCheck.getName().equalsIgnoreCase(vm.getName())){
									String hostIP = hostSystem.getName();
									int retryCount = 0;
									if(pinghost(hostIP)){
										logger.info("Host is running");
										if(retryCount<2){
											String retryIP = vm.getGuest().getIpAddress();
											logger.info("retrying to connect "+vm.getName() +" count "+retryCount);
											if(pinghost(retryIP)){
												break;
											}
											retryCount++;
											try {
												vmPingThread.sleep(3000);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										RetriveVM.retriveVM(vmCheck, hostList);
										try {
											vmPingThread.sleep(1000*90);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										
										new VMPing(vm, guestHostSystems, hostList, hostVM, rootFolderAdmin, rootFolder);
										logger.info("closing current thread !!!");
										threadStopper = true;
									}
									else{
										logger.info("Host not running");
										if(!pinghost(hostIP) && retryCount<2){
											logger.info("retrying to connect vHost "+vm.getName() +" count "+retryCount);
											if(pinghost(hostIP)){
												break;
											}
											retryCount++;
											try {
												vmPingThread.sleep(3000);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										
										if(retriveHost(hostVM)){
											logger.info("vhost restarted successfully");
										}else{
											String newHost = searchingHost();
											migratingNewHost(newHost);
										}
									}
								}
							}
						}
					}
				}
				
			}*/try{
				String IP = vm.getGuest().getIpAddress();
				if(IP!=null){
					InetAddress inet = InetAddress.getByName(IP);
					if(pinghost(IP)){
						logger.info(vm.getName()+" is replying " + IP);
					}else{
						logger.info(vm.getName()+" is not replying " + IP);
						if(vm.getTriggeredAlarmState() != null){
							if(vm.getTriggeredAlarmState()[0].getOverallStatus().equals(ManagedEntityStatus.red)){
								logger.info(vm.getName()+" is powered off by user");
							}
						}else{
							logger.info(vm.getName()+" is ---not--- powered off by user "+ IP);
							for(HostSystem hostSystem : guestHostSystems){
								VirtualMachine[] vmList = hostSystem.getVms();
								for(VirtualMachine vmCheck : vmList){
									if(vmCheck.getName().equalsIgnoreCase(vm.getName())){
										String hostIP = hostSystem.getName();
										int retryCount = 0;
										if(pinghost(hostIP)){
											logger.info("Host is running !!!");
											if(retryCount<2){
												String retryIP = vm.getGuest().getIpAddress();
												logger.info("retrying to connect "+vm.getName() +" count "+retryCount);
												if(pinghost(retryIP)){
													break;
												}
												retryCount++;
												try {
													vmPingThread.sleep(3000);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}
											RetriveVM.retriveVM(vmCheck, hostList);	
											
											try {
												vmPingThread.sleep(1000*120);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											
											new VMPing(vm, guestHostSystems, hostList, hostVM, rootFolderAdmin, rootFolder);
											logger.info("closing current thread !!!");
											threadStopper = true;
										}
										else{
											logger.info("Host not running !!!");
											if(retriveHost(hostVM)){
												logger.info("vhost restarted successfully");
											}else{
												String newHost = searchingHost();
												migratingNewHost(newHost);
											}						
										}
									}
								}
							}	
						}
					}
				}else{
					logger.info(vm.getName()+" not replying " + vmPingThread.getName());
					
					if(vm.getTriggeredAlarmState() != null){
						if(vm.getTriggeredAlarmState()[0].getOverallStatus().equals(ManagedEntityStatus.red)){
							logger.info(vm.getName()+" is powered off by user");
						}
					}else{
						logger.info(vm.getName()+" is !!!not!!! powered off by user");
						for(HostSystem hostSystem : guestHostSystems){
							VirtualMachine[] vmList = hostSystem.getVms();
							for(VirtualMachine vmCheck : vmList){
								if(vmCheck.getName().equalsIgnoreCase(vm.getName())){
									String hostIP = hostSystem.getName();
									int retryCount = 0;
									if(pinghost(hostIP)){
										logger.info("Host is running");
										if(retryCount<2){
											String retryIP = vm.getGuest().getIpAddress();
											logger.info("retrying to connect "+vm.getName() +" count "+retryCount);
											if(pinghost(retryIP)){
												break;
											}
											retryCount++;
											try {
												vmPingThread.sleep(3000);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										RetriveVM.retriveVM(vmCheck, hostList);
										try {
											vmPingThread.sleep(1000*90);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										
										new VMPing(vm, guestHostSystems, hostList, hostVM, rootFolderAdmin, rootFolder);
										logger.info("closing current thread !!!");
										threadStopper = true;
									}
									else{
										logger.info("Host not running");
										if(!pinghost(hostIP) && retryCount<2){
											logger.info("retrying to connect vHost "+vm.getName() +" count "+retryCount);
											if(pinghost(hostIP)){
												break;
											}
											retryCount++;
											try {
												vmPingThread.sleep(3000);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										
										if(retriveHost(hostVM)){
											logger.info("vhost restarted successfully");
										}else{
											String newHost = searchingHost();
											migratingNewHost(newHost);
										}
									}
								}
							}
						}
					}
				}
				
			}catch(Exception ex){
				ex.printStackTrace();
			}
			if(threadStopper){
				logger.info("final step of thread!!!");
			}
		}
	}
	
	public boolean pinghost(String ip){
		boolean result= false;
		String cmd = "ping "+ ip;
		String consoleResult="";
		try{
			if(ip!=null){
				Runtime r=Runtime.getRuntime();
				Process p= r.exec(cmd);

				BufferedReader input= new BufferedReader(new InputStreamReader(p.getInputStream()));
				while(input.readLine()!=null){
					logger.info(input.readLine());
					consoleResult+=input.readLine();	    				
				}
				input.close();

				if(consoleResult.contains("Request timed out")){
					logger.info("Packets Dropped/ could not connect");
					result=false;
				}
				else{
					logger.info("connection made successfully");
					result=true;
				}

			} 
			else{
				logger.info("IP invalid/not found!");
				result = false; 
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public synchronized boolean retriveHost(VirtualMachine hostVM) throws VmConfigFault, TaskInProgress, FileFault, InvalidState, InsufficientResourcesFault, RuntimeFault, InvalidProperty, RemoteException{
		logger.info("reverting vhost from snapshot "+ hostVM.getName());

		Task task= hostVM.getCurrentSnapShot().revertToSnapshot_Task(null);
		
		while(task.getTaskInfo().getState()==TaskInfoState.running){
		}
		if(task.getTaskInfo().getState()==TaskInfoState.success){
			logger.info("host successfully revived from snapshot "+hostVM.getName());
			VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) hostVM.getRuntime();
			if(vmri.getPowerState() == VirtualMachinePowerState.poweredOff){
				Task powerOnHost = hostVM.powerOnVM_Task(null);
				while(powerOnHost.getTaskInfo().getState()==TaskInfoState.running){
				}
				if(powerOnHost.getTaskInfo().getState()==TaskInfoState.success){
					logger.info("vHost:" + hostVM.getName() + " powered on.");
					
					try {
						vmPingThread.sleep(1000*90);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					new VMPing(vm, guestHostSystems, hostList, hostVM, rootFolderAdmin, rootFolder);
					logger.info("closing current thread !!!");
					threadStopper = true;
				}else{
					logger.info("vHost:" + hostVM.getName() + " could not be powered on.");
					return false;
				}
			 }
			return true;
		}else{
			logger.debug("host could not be revived from snapshot "+hostVM.getName());
			return false;
		}
	}
	
	public String searchingHost() throws InvalidProperty, RuntimeFault, RemoteException
	{	
		String destHost = null;
		for(HostSystem hostSys : hostList){
			logger.info(" pinging to host " + hostSys.getName());
			if(pinghost(hostSys.getName())){
				destHost = hostSys.getName();
				logger.info("new destination host for migration " +hostSys.getName());
				break;
			}
		}
		return destHost;
	}
	
	/*public String addingNewHost()
	{
		String ret = "";
		try {	
			ManagedEntity [] mes =  new InventoryNavigator(rootFolderAdmin).searchManagedEntities("Datacenter");
			Datacenter dc = new Datacenter(rootFolderAdmin.getServerConnection(),  mes[0].getMOR());
			HostConnectSpec hs = new HostConnectSpec();
			String ip= "130.65.133.72";
			hs.hostName= ip;
			hs.userName ="root";
			hs.password = "12!@qwQW";
			hs.managementIp = "130.65.133.70";
			hs.setSslThumbprint("C5:EF:CA:98:96:80:6D:2E:46:CB:B1:D2:BB:87:4A:18:AF:26:83:20");
			//hs.setSslThumbprint("90:BD:8C:C1:4E:F6:E9:A3:1A:DF:4B:FA:16:6B:9A:0D:73:DC:6A:F7");
			ComputeResourceConfigSpec crcs = new ComputeResourceConfigSpec();
			Task t = dc.getHostFolder().addStandaloneHost_Task(hs,crcs, true);
			if(t.getTaskInfo().getState()==TaskInfoState.success){
				ret = ip;
			}
			else{
				ret = "";
			}
		}   
		catch (Exception re){
			System.out.println(re.toString());
			System.out.println("Unable to connect to Vsphere server");
		}
		return ret;
	}*/
	
	public void migratingNewHost(String newHostIp) throws InvalidProperty, RuntimeFault, RemoteException, MalformedURLException
	{
		ServiceInstance siMigrate = new ServiceInstance(
		        new URL("https://130.65.132.103/sdk"), "administrator", "12!@qwQW", true);

	    Folder rootFolderMigrate = siMigrate.getRootFolder();
	    VirtualMachine vmToBeMigrated = (VirtualMachine) new InventoryNavigator(
	    		rootFolderMigrate).searchManagedEntity(
	            "VirtualMachine", vm.getName());
	    HostSystem newHost = (HostSystem) new InventoryNavigator(
	    		rootFolderMigrate).searchManagedEntity(
	            "HostSystem", newHostIp);
	    ComputeResource cr = (ComputeResource) newHost.getParent();
	    VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vmToBeMigrated.getRuntime();
	    if(vmri.getPowerState() == VirtualMachinePowerState.poweredOn){
	    	Task taskPowerOff = vmToBeMigrated.powerOffVM_Task();
	    	while(taskPowerOff.getTaskInfo().getState()==TaskInfoState.running){
			}
			if(taskPowerOff.getTaskInfo().getState()==TaskInfoState.success){
				logger.info("vm:" + vmToBeMigrated.getName() + " powered off.");
			}else{
				logger.info("vm:" + vmToBeMigrated.getName() + " could not be powered off.");
			}
		}
	    String[] checks = new String[] {"cpu", "software"};
	    HostVMotionCompatibility[] vmcs =
	    		siMigrate.queryVMotionCompatibility(vmToBeMigrated, new HostSystem[] 
	         {newHost},checks );
	    
	    String[] comps = vmcs[0].getCompatibility();
	    if(checks.length != comps.length){
	      System.out.println("CPU/software NOT compatible. Exit.");
	      siMigrate.getServerConnection().logout();
	      return;
	    }
	    
	    Task taskColdMigration = vmToBeMigrated.migrateVM_Task(cr.getResourcePool(), newHost,
	        VirtualMachineMovePriority.highPriority, 
	        VirtualMachinePowerState.poweredOff);
	  
	    while(taskColdMigration.getTaskInfo().getState()==TaskInfoState.running){
		}
		if(taskColdMigration.getTaskInfo().getState()==TaskInfoState.success){
			logger.info("vm:" + vmToBeMigrated.getName() + " migrated to new vHost "+newHostIp);
		}else{
			logger.info("vm:" + vmToBeMigrated.getName() + " could not be migrated to new vHost "+newHostIp);
		}
	    siMigrate.getServerConnection().logout();
	}	
}
