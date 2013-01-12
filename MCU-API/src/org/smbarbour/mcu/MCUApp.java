package org.smbarbour.mcu;

import org.apache.log4j.Logger;

public abstract class MCUApp {
	
	public Logger baseLogger;

	public abstract void setLblStatus(String string);
	public abstract void setProgressBar(int i);
	public abstract void log(String msg);
	public abstract boolean requestLogin();
}
