package org.smbarbour.mcu;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ConsoleArea extends JTextPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3568865213747832678L;
	
	private StyledDocument doc = this.getStyledDocument();
	public Style infoStyle = doc.addStyle("Info", null);
	public Style warnStyle = doc.addStyle("Warning", null);
	public Style errorStyle = doc.addStyle("Error", null);

	public ConsoleArea() {
		this.setBackground(Color.white);
		StyleConstants.setForeground(infoStyle, new Color(0x007700));
		StyleConstants.setForeground(warnStyle, new Color(0xaaaa00));
		StyleConstants.setForeground(errorStyle, Color.red);
	}
	public void log(String msg) {
		try {
			doc.insertString(doc.getLength(), msg, null);
			setCaretPosition(getText().length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public void log(String msg, Style a) {
		try {
			doc.insertString(doc.getLength(), msg, a);
			setCaretPosition(getText().length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
