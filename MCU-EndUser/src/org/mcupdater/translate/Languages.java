package org.mcupdater.translate;

public enum Languages {
	en_US(new USEnglish());
	
	private TranslateProxy proxy;

	Languages(TranslateProxy proxy){
		this.proxy = proxy;
	}
	
	public TranslateProxy getProxy() {
		return this.proxy;
	}
	
	public static String getLocale() {
		return System.getProperty("user.language") + "_" + System.getProperty("user.country");
	}
}
