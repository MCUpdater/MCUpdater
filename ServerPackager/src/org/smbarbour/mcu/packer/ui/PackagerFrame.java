package org.smbarbour.mcu.packer.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.smbarbour.mcu.packer.Version;

public class PackagerFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = 7909185882605437761L;
	
	private JPanel _panel;
	
	private JMenuItem _newItem;
	private JMenuItem _openItem;
	private JMenuItem _importFromDotMinecraft;
	private JMenuItem _importFromMultiMC;
	private JMenuItem _saveItem;
	private JMenuItem _saveAsItem;
	private JMenuItem _quitItem;

	public PackagerFrame() {
		this.setTitle("MCU ServerPackager - v"+Version.VERSION);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(100, 100, 1200, 700);
		
		_panel = new JPanel();
		this.add(_panel);
		
		initMenu();
		
		this.setVisible(true);
	}
	
	private void initMenu() {
		_newItem = new JMenuItem("New");
		_newItem.addActionListener(this);
		
		_openItem = new JMenuItem("Open Server Pack...");
		_openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		_openItem.addActionListener(this);
		
		JMenu importMenu = new JMenu("Import");
		_importFromDotMinecraft = new JMenuItem("From .minecraft...");
		_importFromDotMinecraft.setEnabled(false);
		_importFromDotMinecraft.addActionListener(this);
		_importFromMultiMC = new JMenuItem("From MultiMC instance...");
		_importFromMultiMC.addActionListener(this);
		importMenu.add(_importFromDotMinecraft);
		importMenu.add(_importFromMultiMC);
		
		_saveItem = new JMenuItem("Save");
		_saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		_saveItem.setEnabled(false);
		_saveItem.addActionListener(this);
		
		_saveAsItem = new JMenuItem("Save As...");
		_saveAsItem.setEnabled(false);
		_saveAsItem.addActionListener(this);
		
		_quitItem = new JMenuItem("Quit");
		_quitItem.addActionListener(this);
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(_newItem);
		fileMenu.add(_openItem);
		fileMenu.add(importMenu);
		fileMenu.addSeparator();
		fileMenu.add(_saveItem);
		fileMenu.add(_saveAsItem);
		fileMenu.addSeparator();
		fileMenu.add(_quitItem);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		this.setJMenuBar(menuBar);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object target = e.getSource();
		System.out.println(((JMenuItem)target).getText());
		if( target.equals(_newItem) ) {
			// Discard all changes and create a new server pack
			// TODO: check for unsaved pending changes
		} else if( target.equals(_openItem) ) {
			// Open an existing server pack xml
			// TODO: check for unsaved pending changes
		} else if( target.equals(_saveItem) ) {
			// Save current changes
		} else if( target.equals(_saveAsItem) ) {
			// Save with a new name
		} else if( target.equals(_importFromDotMinecraft) ) {
			// Import configuration from ~/.minecraft
		} else if( target.equals(_importFromMultiMC) ) {
			// Import configuration from a MultiMC instance
		} else if( target.equals(_quitItem) ) {
			// TODO: check for unsaved pending changes
			System.exit(NORMAL);
		}
	}
}
