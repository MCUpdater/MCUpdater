package org.smbarbour.mcu.util;

public class ServerList {
	private String name;
	private String packUrl;
	private String newsUrl;
	private String version;
	private String address;
	private boolean generateList = true;
	
	public ServerList(String name, String packUrl, String newsUrl, String version, String address, boolean generateList)
	{
		this.name = name;
		this.packUrl = packUrl;
		this.newsUrl = newsUrl;
		this.version = version;
		this.address = address;
		this.generateList = generateList;
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
}
