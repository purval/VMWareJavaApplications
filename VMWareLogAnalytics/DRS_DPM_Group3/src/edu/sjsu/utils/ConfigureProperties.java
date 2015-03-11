package edu.sjsu.utils;


public class ConfigureProperties {
	
	String adminvCenterUrl;
	String adminUserName;
	String adminPwd;
	String vCenterUrl;
	String userName;
	String pwd;

	public ConfigureProperties() {
		super();
	}

	public ConfigureProperties(String adminvCenterUrl, String adminUserName,
			String adminPwd, String vCenterUrl, String userName, String pwd) {
		super();
		this.adminvCenterUrl = adminvCenterUrl;
		this.adminUserName = adminUserName;
		this.adminPwd = adminPwd;
		this.vCenterUrl = vCenterUrl;
		this.userName = userName;
		this.pwd = pwd;
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

	@Override
	public String toString() {
		return "ConfigureProperties [adminvCenterUrl=" + adminvCenterUrl
				+ ", adminUserName=" + adminUserName + ", adminPwd=" + adminPwd
				+ ", vCenterUrl=" + vCenterUrl + ", userName=" + userName
				+ ", pwd=" + pwd + "]";
	}
}
