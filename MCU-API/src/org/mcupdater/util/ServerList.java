package org.mcupdater.util;

public class ServerList implements Comparable<ServerList>{
	private String name;
	private String packUrl;
	private String newsUrl;
	private String iconUrl;
	private String version;		// minecraft version
	private String mcuVersion;	// minimum version of MCU required to use this pack
	private String address;
	private boolean generateList = true;
	private boolean autoConnect = true;
	private boolean fakeServer = false;
	private String revision;	// serverpack revision
	private String serverId;
	private String mainClass;
	
	public ServerList(String serverId, String name, String packUrl, String newsUrl, String iconUrl, String version, String address, boolean generateList, boolean autoConnect, String revision, boolean fakeServer, String mainClass)
	{
		this.serverId = serverId;
		this.name = name;
		this.packUrl = packUrl;
		this.newsUrl = newsUrl;
		this.iconUrl = iconUrl;
		this.version = version;
		this.address = address;
		this.generateList = generateList;
		this.setAutoConnect(autoConnect);
		this.revision = revision;
		this.setFakeServer(fakeServer);
		this.setMainClass(mainClass);
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getPackUrl()
	{
		return packUrl;
	}
	
	public void setPackUrl(String url)
	{
		this.packUrl = url;
	}

	public String getNewsUrl() {
		return newsUrl;
	}

	public void setNewsUrl(String newsUrl) {
		this.newsUrl = newsUrl;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isGenerateList() {
		return generateList;
	}

	public void setGenerateList(boolean generateList) {
		this.generateList = generateList;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getMCUVersion() {
		return mcuVersion;
	}

	public void setMCUVersion(String mcuVersion) {
		this.mcuVersion = mcuVersion;
	}
	
	public String toString() {
		return this.name;
	}

	public boolean isAutoConnect() {
		if (!this.generateList) {
			return false;
		} else {
			return autoConnect;
		}
	}

	public void setAutoConnect(boolean autoConnect) {
		this.autoConnect = autoConnect;
	}

	public boolean isFakeServer() {
		return fakeServer;
	}

	public void setFakeServer(boolean fakeServer) {
		this.fakeServer = fakeServer;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	@Override
	public int compareTo(ServerList that) {
		return this.getName().compareTo(that.getName());
	}
}
