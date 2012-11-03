package org.smbarbour.mcu;

import javax.swing.JTextArea;

public class ConsoleArea extends JTextArea {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3568865213747832678L;

	public void log(String msg) {
		append(msg);
		setCaretPosition(getText().length());
	}
}
