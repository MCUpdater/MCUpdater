package org.smbarbour.mcu.util;

import java.util.Locale;

public class ConfigFile {
	private String url;
	private String path;
	private String md5;
	private String parentID;
	
	public ConfigFile(String url, String path, String md5)
	{
		setUrl(url);
		setPath(path);
		setMD5(md5);
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public void setUrl(String url)
	{
		this.url = url;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public void setPath(String path)
	{
		this.path = path;
	}
	
	public String getMD5()
	{
		return md5;
	}
	
	public void setMD5(String md5)
	{
		if( md5 != null )
			this.md5 = md5.toLowerCase(Locale.ENGLISH);
	}

	public String getParentID() {
		return parentID;
	}

	public void setParentID(String parentID) {
		this.parentID = parentID;
	}

	@Override
	public String toString() {
		return path;
	}

}
