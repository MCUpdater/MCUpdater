package cah.melonar.mcu.util;

public class ServerList {
	private String name;
	private String packUrl;
	private String newsUrl;
	private String version;
	private String address;
	
	public ServerList(String name, String packUrl, String newsUrl, String version, String address)
	{
		this.name = name;
		this.packUrl = packUrl;
		this.newsUrl = newsUrl;
		this.version = version;
		this.address = address;
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
}
