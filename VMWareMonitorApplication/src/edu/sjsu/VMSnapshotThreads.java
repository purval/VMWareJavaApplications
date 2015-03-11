package edu.sjsu;

import java.rmi.RemoteException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VirtualMachineSnapshot;

public class VMSnapshotThreads implements Runnable{
	final static Logger logger = Logger.getLogger(VMSnapshotThreads.class);
	
	private VirtualMachine vm;
	private Thread vmStart;
	
	public VMSnapshotThreads(VirtualMachine vm){
		logger.info("snapshot process thread started for VM "+vm.getName());
		this.vm = vm;
		vmStart = new Thread(this);
		vmStart.start();
		logger.info(vmStart.getName());
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void run(){
		while(true){
			/*try {
				vmStart.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			
			/*try{
				VirtualMachineSnapshot vmSnap= vm.getCurrentSnapShot();
				if(vmSnap !=null) {
					Task removeSnapshots = vmSnap.removeSnapshot_Task(true);
					while(removeSnapshots.getTaskInfo().getState()==TaskInfoState.running){
					}
					if(removeSnapshots.getTaskInfo().getState()==TaskInfoState.success){
						logger.info("all snapshots removed for vm "+vm.getName());
					}else{
						logger.debug("snapshots could not be removed for vm "+vm.getName());
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}*/
			
			String snapshotName = vm.getName()+"_snapshot_"+new Date().toString();
			String snapshotDiscription = "snapshot_to_be_taken_for_vm_"+vm.getName();
			logger.debug(vm.getName()+" start time "+new Date().toString());
			try {
				Task task = vm.createSnapshot_Task(snapshotName,snapshotDiscription,false,false);
				while(task.getTaskInfo().getState()==TaskInfoState.running){
				}
				if(task.getTaskInfo().getState()==TaskInfoState.success){
					logger.info("snapshot successfully taken for vm "+vm.getName());
				}else{
					logger.debug("snapshot procedure failed for vm "+vm.getName());
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			try {
				vmStart.sleep(1000*90*10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
