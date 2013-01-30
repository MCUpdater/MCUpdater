package org.smbarbour.mcu;

import java.util.logging.*;

public abstract class MCUApp {
	
	public Logger baseLogger;

	public abstract void setLblStatus(String string);
	public abstract void setProgressBar(int i);
	public abstract void log(String msg);
	public abstract boolean requestLogin();
}
