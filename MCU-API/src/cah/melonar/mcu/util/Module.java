package cah.melonar.mcu.util;

import java.util.ArrayList;
import java.util.List;

public class Module {
	private String name;
	private String url;
	private Boolean required;
	private Boolean inJar;
	private List<ConfigFile> configs;
	
	public Module(String name, String url, Boolean required, Boolean inJar, List<ConfigFile> configs)
	{
		this.name=name;
		this.url=url;
		this.required=required;
		this.inJar=inJar;
		if(configs != null)
		{
			this.configs = configs;
		} else {
			this.configs = new ArrayList<ConfigFile>();
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public void setUrl(String url)
	{
		this.url=url;
	}
	
	public Boolean getRequired()
	{
		return required;
	}
	
	public void setRequired(Boolean required)
	{
		this.required=required;
	}
	
	public Boolean getInJar()
	{
		return inJar;
	}
	
	public void setInJar(Boolean inJar)
	{
		this.inJar=inJar;
	}
		
	public List<ConfigFile> getConfigs()
	{
		return configs;
	}
}

