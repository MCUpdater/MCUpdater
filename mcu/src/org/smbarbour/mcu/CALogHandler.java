package org.smbarbour.mcu;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class CALogHandler extends Handler {

	private ConsoleArea console;

	public CALogHandler(ConsoleArea console) {
		this.console = console;
	}
	
	@Override
	public void publish(LogRecord record) {
		console.log(record.getMessage());
	}

	@Override
	public void flush() {}

	@Override
	public void close() throws SecurityException {}

}
