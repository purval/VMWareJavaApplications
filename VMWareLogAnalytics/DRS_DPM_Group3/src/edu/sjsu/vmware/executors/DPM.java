package edu.sjsu.vmware.executors;

import java.net.URL;
import java.rmi.RemoteException;

import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

import edu.sjsu.utils.CPUUsage;

public class DPM {
	static final String SERVER_NAME = "130.65.132.14";
	static final String USER_NAME = "administrator"; //root
	static final String PASSWORD = "12!@qwQW";
	static String url = "https://" + SERVER_NAME + "/sdk";
    static String dcName = "DC_T12"; 
   public DPM() {	}
	public static void main(String args[]){

		//double second = CPUUsage.getHostCPUUsage("130.65.132.232");
		try{
			double lowest = CPUUsage.getHostCPUUsage("130.65.132.151");
			ServiceInstance si = new ServiceInstance(new URL(url),
					USER_NAME, PASSWORD, true);
		
		    Folder rootFolder = si.getRootFolder();
		    int count=0;
		    String hostname="";
		    Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", dcName);
		    ManagedEntity[] hosts =new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		    ManagedEntity target=new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem","130.65.132.245");
		    ResourcePool rp=null;
			for(int i=0; i< hosts.length; i++){
			    if(CPUUsage.getHostCPUUsage(hosts[i].getName())<=lowest){
			    	lowest=CPUUsage.getHostCPUUsage(hosts[i].getName());
			    	hostname=hosts[i].getName();
			    	rp = (ResourcePool) new InventoryNavigator(dc).searchManagedEntities("ResourcePool")[i];
			    }
			}
			if((lowest/100)<30){
				ManagedEntity[] vms=(ManagedEntity[]) new InventoryNavigator(rp).searchManagedEntities("VirtualMachine");
				for(ManagedEntity vm: vms){
					int success=migrateVM(vm,target);
					count=count+success;
				}
				if(count!=vms.length)
					System.out.println("All VM's could not be migrated");
				else{
					ServiceInstance s = new ServiceInstance(new URL("https://130.65.132.13/sdk"),
							"administrator", "12!@qwQW", true);
					Folder root=s.getRootFolder();
					//System.out.println(hostname.substring(6,14));
					VirtualMachine vm=(VirtualMachine) new InventoryNavigator(root).searchManagedEntity("VirtualMachine", "T12-vHostNN-cum2-2GB-NFS2-lab3_base5_"+hostname.substring(6,14)+"_host");
			        	Task task3 = vm.powerOffVM_Task();
						if (task3.waitForTask() == Task.SUCCESS) {
							System.out.println("Host Off! ");
						}
						else
							System.out.println("Host cannot be powered off");
				}
			}
		}
		
		catch(Exception e){
			e.printStackTrace();
			
		}

	}
	private static int migrateVM(ManagedEntity vmc, ManagedEntity target) {
		// TODO Auto-generated method stub
		ComputeResource cr = (ComputeResource) target.getParent();
		VirtualMachine vm=(VirtualMachine)vmc;
		try {
			Task task = vm.migrateVM_Task(cr.getResourcePool(), (HostSystem) target,
			        VirtualMachineMovePriority.highPriority, 
			        VirtualMachinePowerState.poweredOn);
			Task task1 = vm.migrateVM_Task(cr.getResourcePool(), (HostSystem) target,
			        VirtualMachineMovePriority.highPriority, 
			        VirtualMachinePowerState.poweredOff);
			System.out.println("Migration of VMs in the VHost has started");
			if(task.waitForTask()==Task.SUCCESS || task1.waitForTask()==Task.SUCCESS)
			{
				System.out.println("VM Migrated sucessfully !");
				return 1;
			}
			
			else{
				System.out.println("Oops VM Migration failed!");
			      TaskInfo info = task.getTaskInfo();
			      System.out.println(info.getError().getFault());
			}
			
		} catch ( RemoteException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
		
	}

}
