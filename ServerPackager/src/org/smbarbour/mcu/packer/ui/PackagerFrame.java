package org.smbarbour.mcu.packer.ui;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.smbarbour.mcu.packer.Version;

public class PackagerFrame extends JFrame {
	private static final long serialVersionUID = 7909185882605437761L;
	
	private JPanel _panel;

	public PackagerFrame() {
		this.setTitle("MCU ServerPackager - v"+Version.VERSION);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(100, 100, 1200, 700);
		
		_panel = new JPanel();
		this.add(_panel);
		
		JMenu fileMenu = new JMenu("File");
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		this.setJMenuBar(menuBar);
		
		this.setVisible(true);
	}
}
