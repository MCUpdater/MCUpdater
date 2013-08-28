package org.mcupdater;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.mcupdater.MCUConsole.LineStyle;

public class ConsoleHandler extends Handler {
	private MCUConsole console;
	private final SimpleDateFormat sdFormat = new SimpleDateFormat("[HH:mm:ss.SSS] "); 

	public ConsoleHandler(MCUConsole console) {
		this.console = console;
	}
	
	@Override
	public void publish(final LogRecord record) {
		if (this.isLoggable(record)){
			final Calendar recordDate = Calendar.getInstance();
			recordDate.setTimeInMillis(record.getMillis());
			LineStyle a = LineStyle.NORMAL;
			//if (record.getLevel() == Level.INFO) { a = LineStyle.NORMAL; }
			if (record.getLevel() == Level.WARNING) { a = LineStyle.WARNING; }
			if (record.getLevel() == Level.SEVERE) { a = LineStyle.ERROR; }
			final LineStyle style = a;
			final Throwable thrown = record.getThrown();
			try {
				final String msg = sdFormat.format(recordDate.getTime()) + record.getMessage() + (thrown != null ? " (stacktrace in " + record.getLoggerName() + " log)" : "");
				MainShell.getInstance().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						console.appendLine(msg, style);						
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void flush() {}

	@Override
	public void close() throws SecurityException {}

}
