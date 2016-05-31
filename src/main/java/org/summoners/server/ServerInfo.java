package org.summoners.server;

public class ServerInfo {
	
	public ServerInfo() { }
	
	private String region;
	
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	private String platform;

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	private String server;

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
	
	private String loginQueue;

	public String getLoginQueue() {
		return loginQueue;
	}

	public void setLoginQueue(String loginQueue) {
		this.loginQueue = loginQueue;
	}
	
	private String newsURL;

	public String getNewsURL() {
		return newsURL;
	}

	public void setNewsURL(String newsURL) {
		this.newsURL = newsURL;
	}
	
	private String xmppServer;

	public String getXmppServer() {
		return xmppServer;
	}

	public void setXmppServer(String xmppServer) {
		this.xmppServer = xmppServer;
	}
	
	private boolean useGarena;

	public boolean isUseGarena() {
		return useGarena;
	}

	public void setUseGarena(boolean useGarena) {
		this.useGarena = useGarena;
	}

}
