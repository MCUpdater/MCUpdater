package org.smbarbour.mcu.EndUser;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class MainShell {

	protected Shell shell;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainShell window = new MainShell();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(1175, 592);
		shell.setText("MCUpdater 3.1.0");
		shell.setLayout(new BorderLayout(0, 0));
		
		Composite cmpStatus = new Composite(shell, SWT.NONE);
		{
			cmpStatus.setLayoutData(BorderLayout.SOUTH);
			cmpStatus.setLayout(new GridLayout(4, false));
			
			Label lblStatus = new Label(cmpStatus, SWT.NONE);
			lblStatus.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,1,1));
			lblStatus.setText("Ready");
			
			ProgressBar progStatus = new ProgressBar(cmpStatus, SWT.SMOOTH | SWT.HORIZONTAL);
			progStatus.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false,1,1));
			progStatus.setSelection(50);
			
			Button btnUpdate = new Button(cmpStatus, SWT.PUSH);
			btnUpdate.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,false,false,1,1));
			btnUpdate.setText("Update");
			
			Button btnLaunch = new Button(cmpStatus, SWT.PUSH);
			btnLaunch.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,false,false,1,1));
			btnLaunch.setText("Launch Minecraft");
		}
		
		Group grpInstances = new Group(shell, SWT.V_SCROLL);
		grpInstances.setText("Instances");
		grpInstances.setLayoutData(BorderLayout.WEST);
		grpInstances.setLayout(new RowLayout());
		
		Label lblTest1 = new Label(grpInstances, SWT.NONE);
		lblTest1.setText("First instance in the list.");
		lblTest1.setSize(lblTest1.computeSize(400, SWT.DEFAULT));
		grpInstances.pack();
				
		Group grpModules = new Group(shell, SWT.V_SCROLL);
		grpModules.setText("Modules");
		grpModules.setLayoutData(BorderLayout.EAST);
		grpModules.setLayout(new RowLayout());
		
		Button btnTest2 = new Button(grpModules, SWT.CHECK);
		btnTest2.setText("Minecraft Forge");
		btnTest2.setSelection(true);
		btnTest2.setSize(btnTest2.computeSize(500, SWT.DEFAULT));
		grpModules.pack();
		
		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setLayoutData(BorderLayout.CENTER);
		
		TabItem tbtmNews = new TabItem(tabFolder, SWT.V_SCROLL);
		tbtmNews.setText("News");
		
		Browser browser = new Browser(tabFolder, SWT.NONE);
		browser.setUrl("http://files.mcupdater.com/example/SamplePack.xml");
		tbtmNews.setControl(browser);
		
		TabItem tbtmConsole = new TabItem(tabFolder, SWT.NONE);
		tbtmConsole.setText("Console");

		TabItem tbtmSettings = new TabItem(tabFolder, SWT.NONE);
		tbtmSettings.setText("Settings");
		
		SettingsPanel cmpSettings = new SettingsPanel(tabFolder, SWT.NONE);
		tbtmSettings.setControl(cmpSettings);
	}
}
