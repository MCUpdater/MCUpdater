package org.smbarbour.mcu;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.text.Style;

public class CALogHandler extends Handler {

	private ConsoleArea console;
	private SimpleDateFormat sdFormat = new SimpleDateFormat("[HH:mm:ss.SSS] "); 

	public CALogHandler(ConsoleArea console) {
		this.console = console;
	}
	
	@Override
	public void publish(LogRecord record) {
		Calendar recordDate = Calendar.getInstance();
		recordDate.setTimeInMillis(record.getMillis());
		Style a = null;
		if (record.getLevel() == Level.INFO) { a = console.infoStyle; }
		if (record.getLevel() == Level.WARNING) { a = console.warnStyle; }
		if (record.getLevel() == Level.SEVERE) { a = console.errorStyle; } 
		console.log(sdFormat.format(recordDate.getTime()) + record.getLevel().getLocalizedName() + ": " + record.getMessage() + "\n", a);
	}

	@Override
	public void flush() {}

	@Override
	public void close() throws SecurityException {}

}
