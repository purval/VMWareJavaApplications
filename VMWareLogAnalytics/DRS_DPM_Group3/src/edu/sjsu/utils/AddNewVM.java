package edu.sjsu.utils;

import java.net.URL;

import org.apache.log4j.Logger;

import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class AddNewVM {
	final static Logger logger = Logger.getLogger(AddNewVM.class);
	
	public static void addVM(HostSystem host, ConfigureProperties properties, String vmName){
		try{
			ServiceInstance si = new ServiceInstance(new URL(properties.getvCenterUrl()),properties.getUserName(), 
					properties.getPwd(), true);
			Folder	rootFolder = si.getRootFolder();
			VirtualMachine	vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);
			
			ManagedEntity newHost =new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", host.getName());
			ComputeResource cr= (ComputeResource)newHost.getParent();
			
			if(vm==null){
				logger.info("No VM " + vm + " found");
				si.getServerConnection().logout();
				return;
			}
			
			VirtualMachineRelocateSpec reloc =new VirtualMachineRelocateSpec();
			reloc.setPool(cr.getResourcePool().getMOR());
			reloc.setHost(host.getMOR());
			
			VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
			cloneSpec.setLocation(reloc);
			cloneSpec.setPowerOn(false);
			cloneSpec.setTemplate(false);
			
			Task task = vm.cloneVM_Task((Folder) vm.getParent(), 
					"T03-VM05-Ubuntu", cloneSpec);
			logger.info("Creating new VM. " +
					"Please wait ...");

			while(task.getTaskInfo().getState()==TaskInfoState.running){
    		}
    		
			if(task.getTaskInfo().getState()==TaskInfoState.success){
				logger.info("VM successfully got created.");
			}
			else{
				logger.info("Failure--VM cannot be cloned");
			}
			

		}catch(Exception e){
			e.printStackTrace();
		
		}
	}
}
