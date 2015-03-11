package edu.sjsu.aggregator;

public class DBProperties {
	String IP;
	int port;
	String jdbcDriver;
	String sqlURL;
	String username;
	String password;
	
	public DBProperties(String iP, int port, String jdbcDriver, String sqlURL,
			String username, String password) {
		super();
		IP = iP;
		this.port = port;
		this.jdbcDriver = jdbcDriver;
		this.sqlURL = sqlURL;
		this.username = username;
		this.password = password;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	public String getSqlURL() {
		return sqlURL;
	}

	public void setSqlURL(String sqlURL) {
		this.sqlURL = sqlURL;
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

	@Override
	public String toString() {
		return "DBProperties [IP=" + IP + ", port=" + port + ", jdbcDriver="
				+ jdbcDriver + ", sqlURL=" + sqlURL + ", username=" + username
				+ ", password=" + password + "]";
	}
}
