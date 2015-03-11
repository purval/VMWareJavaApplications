package edu.sjsu;

import java.rmi.RemoteException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VirtualMachineSnapshot;

public class HostSnapshot implements Runnable{
	final static Logger logger = Logger.getLogger(HostSnapshot.class);
	
	VirtualMachine hostVM;
	Thread hostSnapShotThread;
	public HostSnapshot(VirtualMachine hostVM){
		logger.info("snapshot procedure started for vhost "+hostVM.getName());
		this.hostVM = hostVM;
		hostSnapShotThread = new Thread(this);
		hostSnapShotThread.start();
	}
	
	@Override
	public void run(){
		while(true){
			/*try {
				hostSnapShotThread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			
			/*try{
				VirtualMachineSnapshot vmSnap= hostVM.getCurrentSnapShot();
				if(vmSnap !=null) {
					Task removeSnapshots = vmSnap.removeSnapshot_Task(true);
					while(removeSnapshots.getTaskInfo().getState()==TaskInfoState.running){
					}
					if(removeSnapshots.getTaskInfo().getState()==TaskInfoState.success){
						logger.info("all snapshots removed for vhost "+hostVM.getName());
					}else{
						logger.debug("snapshots could not be removed for vhost "+hostVM.getName());
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}*/
			
			String snapshotName = hostVM.getName()+"_snapshot_"+new Date().toString();
			String snapshotDiscription = "snapshot_to_be_taken_for_vHost:"+hostVM.getName();
			logger.debug(hostVM.getName()+" start time "+new Date().toString());
			try {
				Task task = hostVM.createSnapshot_Task(snapshotName,snapshotDiscription,false,false);
				while(task.getTaskInfo().getState()==TaskInfoState.running){
				}
				if(task.getTaskInfo().getState()==TaskInfoState.success){
					logger.info("snapshot successfully taken of vHost "+hostVM.getName());
				}else{
					logger.debug("snapshot procedure failed for vHost "+hostVM.getName());
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			try {
				hostSnapShotThread.sleep(1000*120*20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
