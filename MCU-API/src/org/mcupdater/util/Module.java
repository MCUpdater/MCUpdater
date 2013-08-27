package org.mcupdater.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Module {
	private String name = "";
	private String id = "";
	private List<PrioritizedURL> urls = new ArrayList<PrioritizedURL>();
	private String path = "";
	private String depends = "";
	private boolean required = false;
	private boolean inJar = false;
	private int order = 1;
	private boolean keepMeta = false;
	private boolean extract = false;
	private boolean inRoot = false;
	private boolean isDefault = false;
	private boolean coreMod = false;
	private String md5 = "";
	private ModSide side = ModSide.BOTH;
	private List<ConfigFile> configs = new ArrayList<ConfigFile>();
	private HashMap<String,String> meta = new HashMap<String,String>();
	private boolean isLibrary = false;
	private String launchArgs = "";
	
	public Module(String name, String id, List<PrioritizedURL> url, String depends, boolean required, boolean inJar, int jarOrder, boolean keepMeta, boolean extract, boolean inRoot, boolean isDefault, boolean coreMod, String md5, List<ConfigFile> configs, String side, String path, HashMap<String, String> meta, boolean isLibrary, String launchArgs){
		this(name, id, url, depends, required, inJar, jarOrder, keepMeta, extract, inRoot, isDefault, coreMod, md5, configs, side, path, meta);
		this.setIsLibrary(isLibrary);
		this.setLaunchArgs(launchArgs);
	}
	
	public Module(String name, String id, List<PrioritizedURL> url, String depends, boolean required, boolean inJar, int jarOrder, boolean keepMeta, boolean extract, boolean inRoot, boolean isDefault, boolean coreMod, String md5, List<ConfigFile> configs, String side, String path, HashMap<String, String> meta)
	{
		this.setName(name);
		this.setId(id);
		this.setUrls(url);
		this.setDepends(depends);
		this.setRequired(required);
		this.setInJar(inJar);
		this.setJarOrder(jarOrder+1);
		this.setKeepMeta(keepMeta);
		this.setIsDefault(isDefault);
		this.setExtract(extract);
		this.setInRoot(inRoot);
		this.setCoreMod(coreMod);
		this.setMD5(md5);
		this.setSide(side);
		this.setPath(path);
		if(configs != null)
		{
			this.configs = configs;
		} else {
			this.configs = new ArrayList<ConfigFile>();
		}
		if(meta != null)
		{
			this.setMeta(meta);
		} else {
			this.setMeta(new HashMap<String,String>());
		}
	}

	private void setJarOrder(int jarOrder) {
		this.order = jarOrder;
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

	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public List<URL> getUrls()
	{
		List<URL> result = new ArrayList<URL>();
		for (PrioritizedURL entry : urls) {
			try {
				result.add(new URL(entry.getUrl()));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public void setUrls(List<PrioritizedURL> urls)
	{
		this.urls = urls;
	}
	
	public void addUrl(PrioritizedURL url)
	{
		this.urls.add(url);
	}
	
	public boolean getRequired()
	{
		return required;
	}
	
	public void setRequired(boolean required)
	{
		this.required=required;
	}
	
	public boolean getInJar()
	{
		return inJar;
	}
	
	public void setInJar(boolean inJar)
	{
		this.inJar=inJar;
	}

	public boolean getExtract() {
		return extract;
	}

	public void setExtract(boolean extract) {
		this.extract = extract;
	}

	public boolean getInRoot() {
		return inRoot;
	}

	public void setInRoot(boolean inRoot) {
		this.inRoot = inRoot;
	}
	
	public String getMD5() {
		return (md5 == null ? "" : md5);
	}
	
	public void setMD5(String md5) {
		if( md5 != null )
			this.md5 = md5.toLowerCase(Locale.ENGLISH);
	}

	public boolean getIsDefault() {
		return isDefault;
	}
	
	public void setIsDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public List<ConfigFile> getConfigs()
	{
		return configs;
	}
	
	public void setConfigs(List<ConfigFile> configs)
	{
		this.configs = configs;
	}

	public boolean getCoreMod() {
		return coreMod;
	}

	public void setCoreMod(boolean coreMod) {
		this.coreMod = coreMod;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDepends() {
		return depends;
	}

	public void setDepends(String depends) {
		this.depends = depends;
	}
	
	@Override
	public String toString() {
		return id;
	}

	public ModSide getSide() {
		return side;
	}

	public void setSide(ModSide side) {
		this.side = side;
	}
	public void setSide(String side) {
		if( side == null || side.length() == 0 ) {
			side = "BOTH";
		} else {
			side = side.toUpperCase();
		}
		try {
			setSide( ModSide.valueOf(side) );
		} catch( IllegalArgumentException e ) {
			setSide( ModSide.BOTH );
		}
	}
	
	public boolean isClientSide() {
		return side != ModSide.SERVER;
	}
	public boolean isServerSide() {
		return side != ModSide.CLIENT;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		if (path == null) {
			path = "";
		}
		this.path = path;
	}

	public int getJarOrder() {
		return order;
	}

	public boolean getKeepMeta() {
		return keepMeta;
	}

	public void setKeepMeta(boolean keepMeta) {
		this.keepMeta = keepMeta;
	}

	public HashMap<String,String> getMeta() {
		return meta;
	}

	public void setMeta(HashMap<String,String> meta) {
		this.meta = meta;
	}

	public String getLaunchArgs() {
		return launchArgs;
	}

	public void setLaunchArgs(String launchArgs) {
		this.launchArgs = launchArgs;
	}

	public boolean getIsLibrary() {
		return isLibrary;
	}

	public void setIsLibrary(boolean isLibrary) {
		this.isLibrary = isLibrary;
	}

	public List<PrioritizedURL> getPrioritizedUrls() {
		return this.urls;
	}

}

