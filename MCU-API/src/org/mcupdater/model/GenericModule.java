package org.mcupdater.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class GenericModule {
	protected String name = "";
	protected String id = "";
	protected List<PrioritizedURL> urls = new ArrayList<PrioritizedURL>();
	protected String path = "";
	protected String depends = "";
	protected boolean required = false;
	protected boolean inJar = false;
	protected int order = 1;
	protected boolean keepMeta = false;
	protected boolean extract = false;
	protected boolean inRoot = false;
	protected boolean isDefault = false;
	protected boolean coreMod = false;
	protected boolean litemod = false;
	protected String md5 = "";
	protected ModSide side = ModSide.BOTH;
	protected HashMap<String,String> meta = new HashMap<String,String>();
	protected boolean isLibrary = false;
	protected String launchArgs = "";
	protected String jreArgs = "";

	public GenericModule(String name, String id, List<PrioritizedURL> url, String depends, boolean required, boolean inJar, int jarOrder, boolean keepMeta, boolean extract, boolean inRoot, boolean isDefault, boolean coreMod, String md5, String side, String path, HashMap<String, String> meta, boolean isLibrary, boolean litemod, String launchArgs, String jreArgs){
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
		this.setIsLibrary(isLibrary);
		this.setLaunchArgs(launchArgs);
		this.setJreArgs(jreArgs);
		this.setLitemod(litemod);
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

	public String getJreArgs() {
		return jreArgs;
	}

	public void setJreArgs(String jreArgs) {
		this.jreArgs = jreArgs;
	}

	public boolean isLitemod() {
		return litemod;
	}

	public void setLitemod(boolean litemod) {
		this.litemod = litemod;
	}
}
