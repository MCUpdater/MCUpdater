package org.mcupdater.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Module extends GenericModule {
	private List<ConfigFile> configs = new ArrayList<ConfigFile>();
	private List<GenericModule> submodules = new ArrayList<GenericModule>();
	
	public Module(String name, String id, List<PrioritizedURL> url, String depends, boolean required, boolean inJar, int jarOrder, boolean keepMeta, boolean extract, boolean inRoot, boolean isDefault, boolean coreMod, String md5, List<ConfigFile> configs, String side, String path, HashMap<String, String> meta, boolean isLibrary, boolean litemod, String launchArgs, String jreArgs, List<GenericModule> submodules){
		super(name,id,url,depends,required,inJar,jarOrder,keepMeta,extract,inRoot,isDefault,coreMod,md5,side,path,meta,isLibrary,litemod,launchArgs,jreArgs);	
		if(configs != null)
		{
			this.configs = configs;
		} else {
			this.configs = new ArrayList<ConfigFile>();
		}
		this.submodules.addAll(submodules);
	}

	@Deprecated
	public Module(String name, String id, List<PrioritizedURL> url, String depends, boolean required, boolean inJar, int jarOrder, boolean keepMeta, boolean extract, boolean inRoot, boolean isDefault, boolean coreMod, String md5, List<ConfigFile> configs, String side, String path, HashMap<String, String> meta){
		this(name,id,url,depends,required,inJar,jarOrder,keepMeta,extract,inRoot,isDefault,coreMod,md5,configs,side,path,meta,false,false,null,null,null);
	}

	@Deprecated
	public Module(String name, String id, String url, String depends, boolean required, boolean inJar, boolean extract, boolean inRoot, boolean isDefault, boolean coreMod, String md5, List<ConfigFile> configs)
	{
		this(name, id, makeList(url), depends, required, inJar, 0, true, extract, inRoot, isDefault, coreMod, md5, configs, null, null, null);
	}
	
	private static List<PrioritizedURL> makeList(String url) {
		List<PrioritizedURL> urls = new ArrayList<PrioritizedURL>();
		urls.add(new PrioritizedURL(url, 0));
		return urls;
	}

	public List<ConfigFile> getConfigs()
	{
		return configs;
	}
	
	public void setConfigs(List<ConfigFile> configs)
	{
		this.configs = configs;
	}
	
	public boolean hasConfigs() {
		return (this.configs.size() > 0);
	}
	
	public boolean hasSubmodules() {
		return (this.submodules.size() > 0);
	}
	
	public List<GenericModule> getSubmodules() {
		return this.submodules;
	}

}

