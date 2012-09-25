package org.smbarbour.mcu;

import javax.swing.JTextArea;

public class ConsoleArea extends JTextArea {
	public void log(String msg) {
		append(msg);
		setCaretPosition(getText().length());
	}
}
