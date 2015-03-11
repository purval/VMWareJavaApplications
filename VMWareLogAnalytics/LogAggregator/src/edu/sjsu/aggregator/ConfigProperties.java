package edu.sjsu.aggregator;

public class ConfigProperties {
	String vCenterURL;
	String username;
	String password;
	String vmName;
	String outputFile;
	
	public ConfigProperties(String vCenterURL, String username,
			String password, String vmName, String outputFile) {
		super();
		this.vCenterURL = vCenterURL;
		this.username = username;
		this.password = password;
		this.vmName = vmName;
		this.outputFile = outputFile;
	}

	public String getvCenterURL() {
		return vCenterURL;
	}

	public void setvCenterURL(String vCenterURL) {
		this.vCenterURL = vCenterURL;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVmName() {
		return vmName;
	}

	public void setVmName(String vmName) {
		this.vmName = vmName;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public String toString() {
		return "ConfigProperties [vCenterURL=" + vCenterURL + ", username="
				+ username + ", password=" + password + ", vmName=" + vmName
				+ ", outputFile=" + outputFile + "]";
	}
}
