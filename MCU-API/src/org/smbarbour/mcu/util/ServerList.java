package org.smbarbour.mcu.util;

public class ServerList {
	private String name;
	private String packUrl;
	private String newsUrl;
	private String iconUrl;
	private String version;
	private String address;
	private boolean generateList = true;
	private String revision;
	
	public ServerList(String name, String packUrl, String newsUrl, String iconUrl, String version, String address, boolean generateList, String revision)
	{
		this.name = name;
		this.packUrl = packUrl;
		this.newsUrl = newsUrl;
		this.iconUrl = iconUrl;
		this.version = version;
		this.address = address;
		this.generateList = generateList;
		this.revision = revision;
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
}
