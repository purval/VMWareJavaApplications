package edu.sjsu;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NotFound;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.SnapshotFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class RetriveVM {
	private static Logger logger = Logger.getLogger(RetriveVM.class);
	public static void retriveVM(VirtualMachine vm, HostSystem[] hostList) throws VmConfigFault, SnapshotFault, TaskInProgress, InvalidState, InsufficientResourcesFault, NotFound, RuntimeFault, RemoteException, MalformedURLException{
		
		logger.info("Recovery called for failed VM:  "+vm.getName());
		VirtualMachineSnapshotInfo x = vm.getSnapshot();
		
		if(x!=null) 
		{
		logger.info("Recovery Started using Snapshot");
		
		Task revertSnapshot = vm.revertToCurrentSnapshot_Task(hostList[0]);
		while(revertSnapshot.getTaskInfo().getState()==TaskInfoState.running){
			
		}
		
			if(revertSnapshot.getTaskInfo().getState()==TaskInfoState.success)
			{
				//VirtualMachine clone = null;
				logger.info("Reverted back to the latest snapshot available");
				VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
			     if(vmri.getPowerState() == VirtualMachinePowerState.poweredOff){
					Task task = vm.powerOnVM_Task(null);
					logger.info("vm:" + vm.getName() + " powered on.");
				 }
			     return;
			}else{
				logger.info("Reverting from snapshot failed");
				logger.info("trying to migrate to new vHost");
				String newVMIP = "130.65.132.155";
				ServiceInstance siMigrate = new ServiceInstance(
				        new URL("https://130.65.132.103/sdk"), "administrator", "12!@qwQW", true);

			    Folder rootFolderMigrate = siMigrate.getRootFolder();
			    VirtualMachine vmToBeMigrated = (VirtualMachine) new InventoryNavigator(
			    		rootFolderMigrate).searchManagedEntity(
			            "VirtualMachine", vm.getName());
			    HostSystem newHost = (HostSystem) new InventoryNavigator(
			    		rootFolderMigrate).searchManagedEntity(
			            "HostSystem", newVMIP);
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
					logger.info("vm:" + vmToBeMigrated.getName() + " migrated to new vHost "+newVMIP);
				}else{
					logger.info("vm:" + vmToBeMigrated.getName() + " could not be migrated to new vHost "+newVMIP);
				}
			    siMigrate.getServerConnection().logout();
			}
		}
	}
}
