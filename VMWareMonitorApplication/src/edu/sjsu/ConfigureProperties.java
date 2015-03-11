package edu.sjsu;

import java.util.List;

public class ConfigureProperties {
	
	List<String> vmList;
	String adminvCenterUrl;
	String adminUserName;
	String adminPwd;
	String hostName;
	String vCenterUrl;
	String userName;
	String pwd;
	long pingOccuranceInSec;
	long snapShotOccurenceInSec;
	
	public ConfigureProperties() {
		super();
	}

	public ConfigureProperties(List<String> vmList, String adminvCenterUrl,
			String adminUserName, String adminPwd, String hostName,
			String vCenterUrl, String userName, String pwd,
			long pingOccuranceInSec, long snapShotOccurenceInSec) {
		super();
		this.vmList = vmList;
		this.adminvCenterUrl = adminvCenterUrl;
		this.adminUserName = adminUserName;
		this.adminPwd = adminPwd;
		this.hostName = hostName;
		this.vCenterUrl = vCenterUrl;
		this.userName = userName;
		this.pwd = pwd;
		this.pingOccuranceInSec = pingOccuranceInSec;
		this.snapShotOccurenceInSec = snapShotOccurenceInSec;
	}

	public List<String> getVmList() {
		return vmList;
	}

	public void setVmList(List<String> vmList) {
		this.vmList = vmList;
	}

	public String getAdminvCenterUrl() {
		return adminvCenterUrl;
	}

	public void setAdminvCenterUrl(String adminvCenterUrl) {
		this.adminvCenterUrl = adminvCenterUrl;
	}

	public String getAdminUserName() {
		return adminUserName;
	}

	public void setAdminUserName(String adminUserName) {
		this.adminUserName = adminUserName;
	}

	public String getAdminPwd() {
		return adminPwd;
	}

	public void setAdminPwd(String adminPwd) {
		this.adminPwd = adminPwd;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getvCenterUrl() {
		return vCenterUrl;
	}

	public void setvCenterUrl(String vCenterUrl) {
		this.vCenterUrl = vCenterUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public long getPingOccuranceInSec() {
		return pingOccuranceInSec;
	}

	public void setPingOccuranceInSec(long pingOccuranceInSec) {
		this.pingOccuranceInSec = pingOccuranceInSec;
	}

	public long getSnapShotOccurenceInSec() {
		return snapShotOccurenceInSec;
	}

	public void setSnapShotOccurenceInSec(long snapShotOccurenceInSec) {
		this.snapShotOccurenceInSec = snapShotOccurenceInSec;
	}

	@Override
	public String toString() {
		return "ConfigureProperties [vmList=" + vmList + ", adminvCenterUrl="
				+ adminvCenterUrl + ", adminUserName=" + adminUserName
				+ ", adminPwd=" + adminPwd + ", hostName=" + hostName
				+ ", vCenterUrl=" + vCenterUrl + ", userName=" + userName
				+ ", pwd=" + pwd + ", pingOccuranceInSec=" + pingOccuranceInSec
				+ ", snapShotOccurenceInSec=" + snapShotOccurenceInSec + "]";
	}
	
}
