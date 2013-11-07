package org.mcupdater;

import j7compat.Files;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.mcupdater.model.ServerList;
import org.mcupdater.util.MCUpdater;

public class InstanceList extends ScrolledComposite {

	private Color normal_bg;
	private Composite listBase;
	private List<ServerList> instances = new ArrayList<ServerList>();
	private String selected = "";

	public InstanceList(Composite parent) {
		super(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		normal_bg = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		listBase = new Composite(this, SWT.FILL);
		listBase.setLayout(new RowLayout(SWT.VERTICAL));
		//this.setContent(listBase);
		this.setLayout(new FillLayout(SWT.HORIZONTAL));
		listBase.setBackground(normal_bg);
		this.setBackground(normal_bg);
		this.setContent(listBase);
	}
	
	public void setInstances(List<ServerList> newList){
		this.instances = newList;
		Thread.yield();
		refresh();
	}
		
	public void refresh() {
		for (Control c : listBase.getChildren()) {
			c.dispose();
		}
		Thread.yield();
		//listBase.pack(true);
		//this.pack(true);
		Collections.sort(instances);
		for (ServerList entry : this.instances) {
			if (entry.isFakeServer()) { continue; }
			InstanceCell newCell = new InstanceCell(listBase,SWT.NONE,entry.getServerId(), this);
			ImageData iData = null;
			InputStream stream = null;
			if (entry.getIconUrl().isEmpty()){
				try {
					stream = MCUpdater.class.getResource("/minecraft.png").openStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					URL iconURL = new URL(entry.getIconUrl());
					stream = iconURL.openStream();
				} catch (IOException ioe) {
					try {
						stream = MCUpdater.class.getResource("/minecraft.png").openStream();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			iData = new ImageData(stream);
			if (iData.transparentPixel > 0) {
				newCell.setIcon(new Image(Display.getCurrent(), iData, iData.getTransparencyMask()));				
			} else {
				newCell.setIcon(new Image(Display.getCurrent(), iData));
			}
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			newCell.setServerName(entry.getName());
			newCell.setVersion("Version: " + entry.getVersion());
			newCell.setRevision("Revision: " + entry.getRevision());
			newCell.pack();
			//break;
		}
		Thread.yield();
		listBase.pack(true);
		//this.pack(true);
	}
	
	public void changeSelection(String serverId) {
		selected = serverId;
		for(Control c : listBase.getChildren()) {
			if (c instanceof InstanceCell) {
				InstanceCell cell = (InstanceCell) c;
				if (cell.getServerId().equals(selected)) {
					cell.setSelected(true);
				} else {
					cell.setSelected(false);
				}
			}
		}
		for (ServerList entry : this.instances ) {
			if (entry.getServerId().equals(selected)) {
				MainShell.getInstance().changeSelectedInstance(entry);
				Properties instData = new Properties();
				try {
					instData.load(Files.newInputStream(MCUpdater.getInstance().getInstanceRoot().resolve(entry.getServerId()).resolve("instance.dat")));
				} catch (IOException e1) {
					MainShell.getInstance().baseLogger.log(Level.WARNING, "instance.dat file not found.");
				}
				final boolean needUpdate = !entry.getRevision().equals(instData.getProperty("revision"));
				// check for mcu version update
				final boolean needMCUUpgrade = Version.isVersionOld(entry.getMCUVersion());

				String warningMessage = null;
				if( needUpdate ) {
					warningMessage = MainShell.getInstance().translate.updateRequired;
				} else if( needMCUUpgrade ) {
					warningMessage = MainShell.getInstance().translate.oldMCUpdater;
				}

				if (warningMessage != null) {
					MessageBox msg = new MessageBox(getShell(), SWT.ICON_WARNING);
					msg.setMessage(warningMessage);
					msg.open();
				}
			}
		}
	}
}
