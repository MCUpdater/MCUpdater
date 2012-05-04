package cah.melonar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;


public class MainSWT {

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		final MCUpdater mcu = new MCUpdater();
		ResourceBundle config = ResourceBundle.getBundle("Config");
		Display display = Display.getDefault();
		final Shell shell = new Shell(SWT.TITLE + SWT.MENU + SWT.MIN + SWT.BORDER + SWT.MODELESS);
		shell.setSize(834, 592);
		shell.setText(config.getString("title"));
		shell.setLayout(new BorderLayout(0, 0));

		Composite cmpFooter = new Composite(shell, SWT.NONE);
		cmpFooter.setLayoutData(BorderLayout.SOUTH);
		cmpFooter.setLayout(null);

		Button btnUpdate = new Button(cmpFooter, SWT.NONE);
		btnUpdate.setBounds(595, 5, 100, 29);
		btnUpdate.setText("Update");
		
		Button btnLaunch = new Button(cmpFooter, SWT.NONE);
		btnLaunch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				File launcher = new File(mcu.getMCFolder() + mcu.sep + "minecraft.jar");
				if(!launcher.exists())
				{
					try {
						URL launcherURL = new URL("http://s3.amazonaws.com/MinecraftDownload/launcher/minecraft.jar");
						ReadableByteChannel rbc = Channels.newChannel(launcherURL.openStream());
						FileOutputStream fos = new FileOutputStream(launcher);
						fos.getChannel().transferFrom(rbc, 0, 1 << 24);
					} catch (MalformedURLException mue) {
						mue.printStackTrace();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				ProcessBuilder pb = new ProcessBuilder("java","-jar",launcher.getPath());
				pb.redirectErrorStream(true);
				try {
					pb.start();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		});
		btnLaunch.setBounds(700, 5, 125, 29);
		btnLaunch.setText("Launch Minecraft");

		Label lblStatus = new Label(cmpFooter, SWT.HORIZONTAL | SWT.SHADOW_NONE);
		lblStatus.setBounds(5, 5, 580, 32);
		lblStatus.setText("Idle");

		final Browser browser = new Browser(shell, SWT.NONE);
		browser.setUrl("about:blank");
		browser.setLayoutData(BorderLayout.CENTER);

		Composite cmpChanges = new Composite(shell, SWT.NONE);
		cmpChanges.setLayoutData(BorderLayout.EAST);
		cmpChanges.setLayout(null);

		Label lblNewLabel = new Label(cmpChanges, SWT.CENTER);
		lblNewLabel.setFont(SWTResourceManager.getFont("Sans", 10, SWT.BOLD));
		lblNewLabel.setBounds(5, 5, 220, 17);
		lblNewLabel.setText(config.getString("Main.lblNewLabel.text"));

		final ScrolledComposite scrolledComposite = new ScrolledComposite(cmpChanges, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		scrolledComposite.setBounds(0, 27, 225, 390);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		final Composite cmpChangeList = new Composite(scrolledComposite, SWT.NONE);
		cmpChangeList.setLayout(new RowLayout(SWT.VERTICAL));

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem mnuServers = new MenuItem(menu, SWT.CASCADE);
		mnuServers.setText(config.getString("Main.mntmNewSubmenu.text")); //$NON-NLS-1$

		Menu smServers = new Menu(mnuServers);
		mnuServers.setMenu(smServers);

		MenuItem mnuSManage = new MenuItem(smServers, SWT.NONE);
		mnuSManage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ServerManagerSWT sm = new ServerManagerSWT(Display.getCurrent().getActiveShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				sm.open();
			}
		});
		mnuSManage.setText(config.getString("Main.mntmadd.text")); //$NON-NLS-1$

		MenuItem mnuSList = new MenuItem(smServers, SWT.CASCADE);
		mnuSList.setText(config.getString("Main.mntmList.text")); //$NON-NLS-1$

		final Menu smSList = new Menu(mnuSList);
		mnuSList.setMenu(smSList);
		
		MenuItem mntmInfo = new MenuItem(menu, SWT.CASCADE);
		mntmInfo.setText(config.getString("Main.mntmInfo.text")); //$NON-NLS-1$
		
		Menu menu_1 = new Menu(mntmInfo);
		mntmInfo.setMenu(menu_1);
		
		MenuItem mntmMinecraftjarVersion = new MenuItem(menu_1, SWT.NONE);
		mntmMinecraftjarVersion.setText(config.getString("Main.mntmMinecraftjarVersion.text")); //$NON-NLS-1$
		
		MenuItem mntmLastLoadedConfig = new MenuItem(menu_1, SWT.NONE);
		mntmLastLoadedConfig.setText(config.getString("Main.mntmLastLoadedConfig.text")); //$NON-NLS-1$

/*		
		MenuItem mnuConfigs = new MenuItem(menu, SWT.CASCADE);
		mnuConfigs.setText(config.getString("Main.mntmNewSubmenu.text_1")); //$NON-NLS-1$
		
		Menu smConfigs = new Menu(mnuConfigs);
		mnuConfigs.setMenu(smConfigs);
		
		MenuItem mnuCManage = new MenuItem(smConfigs, SWT.NONE);
		mnuCManage.setText(config.getString("Main.mntmmanage.text")); //$NON-NLS-1$
		
		MenuItem mnuCList = new MenuItem(smConfigs, SWT.CASCADE);
		mnuCList.setText(config.getString("Main.mntmlist.text")); //$NON-NLS-1$
		
		Menu menu_1 = new Menu(mnuCList);
		mnuCList.setMenu(menu_1);
*/

		List<ServerList> servers = mcu.loadServerList();
		if(servers != null)
		{
			Iterator<ServerList> it = servers.iterator();

			boolean flag = false;
			while(it.hasNext())
			{
				ServerList entry = it.next();
				MenuItem mnuServerEntry = new MenuItem(smSList, SWT.RADIO);
				mnuServerEntry.setText(entry.getName());
				mnuServerEntry.setData("Name",entry.getName());
				mnuServerEntry.setData("Pack",entry.getPackUrl());
				mnuServerEntry.setData("News",entry.getNewsUrl());
				mnuServerEntry.setData("Version",entry.getVersion());
				mnuServerEntry.setData("Address",entry.getAddress());
				mnuServerEntry.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						browser.setUrl((String) e.widget.getData("News"));
						shell.setText(e.widget.getData("Name") + " - Minecraft Client Updater");
						List<Module> modules = mcu.loadFromURL((String) e.widget.getData("Pack"));
						Iterator<Module> itMods = modules.iterator();
						Control[] modListChildren = cmpChangeList.getChildren();
						for(int i = 0; i < modListChildren.length; i++)
						{
							modListChildren[i].dispose();
						}
						while(itMods.hasNext())
						{
							Module modEntry = itMods.next();
							System.out.println(modEntry.getName());
							Button dynCheck = new Button(cmpChangeList, SWT.CHECK);
							if(modEntry.getInJar())
							{
								dynCheck.setFont(SWTResourceManager.getBoldFont(dynCheck.getFont()));
							}
							dynCheck.setText(modEntry.getName());
							dynCheck.setData("url",modEntry.getUrl());
							dynCheck.setData("inJar",modEntry.getInJar());
							dynCheck.setData("configs",modEntry.getConfigs());
							if(modEntry.getRequired())
							{
								dynCheck.setEnabled(false);
								dynCheck.setSelection(true);
							}						
						}
						scrolledComposite.setContent(cmpChangeList);
						scrolledComposite.setMinSize(cmpChangeList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					}
				});
				if(flag == false){
					mnuServerEntry.setSelection(true);
					flag = true;
					browser.setUrl((String) mnuServerEntry.getData("News"));
					shell.setText(mnuServerEntry.getData("Name") + " - Minecraft Client Updater");

					List<Module> modules = mcu.loadFromURL((String) mnuServerEntry.getData("Pack"));
					Iterator<Module> itMods = modules.iterator();
					Control[] modListChildren = cmpChangeList.getChildren();
					for(int i = 0; i < modListChildren.length; i++)
					{
						modListChildren[i].dispose();
					}
					while(itMods.hasNext())
					{
						Module modEntry = itMods.next();
						System.out.println(modEntry.getName());
						Button dynCheck = new Button(cmpChangeList, SWT.CHECK);
						if(modEntry.getInJar())
						{
							dynCheck.setFont(SWTResourceManager.getBoldFont(dynCheck.getFont()));
						}
						dynCheck.setText(modEntry.getName());
						dynCheck.setData("url",modEntry.getUrl());
						dynCheck.setData("inJar",modEntry.getInJar());
						dynCheck.setData("configs",modEntry.getConfigs());
						if(modEntry.getRequired())
						{
							dynCheck.setEnabled(false);
							dynCheck.setSelection(true);
						}						
					}
					scrolledComposite.setContent(cmpChangeList);
					scrolledComposite.setMinSize(cmpChangeList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
			}
		}
		shell.addListener(254, new Listener() {
			@Override
			public void handleEvent(Event event) {
				for(int i=0; i < smSList.getItemCount(); i++)
				{
					smSList.getItem(i).dispose();
				}
				List<ServerList> servers = mcu.loadServerList();
				if(servers != null)
				{
					Iterator<ServerList> it = servers.iterator();

					boolean flag = false;
					while(it.hasNext())
					{
						ServerList entry = it.next();
						MenuItem mnuServerEntry = new MenuItem(smSList, SWT.RADIO);
						mnuServerEntry.setText(entry.getName());
						mnuServerEntry.setData("Name",entry.getName());
						mnuServerEntry.setData("Pack",entry.getPackUrl());
						mnuServerEntry.setData("News",entry.getNewsUrl());
						mnuServerEntry.setData("Version",entry.getVersion());
						mnuServerEntry.setData("Address",entry.getAddress());
						mnuServerEntry.addSelectionListener(new SelectionAdapter() {
							public void widgetSelected(SelectionEvent e) {
								browser.setUrl((String) e.widget.getData("News"));
								shell.setText(e.widget.getData("Name") + " - Minecraft Client Updater");
								List<Module> modules = mcu.loadFromURL((String) e.widget.getData("Pack"));
								Iterator<Module> itMods = modules.iterator();
								Control[] modListChildren = cmpChangeList.getChildren();
								for(int i = 0; i < modListChildren.length; i++)
								{
									modListChildren[i].dispose();
								}
								while(itMods.hasNext())
								{
									Module modEntry = itMods.next();
									System.out.println(modEntry.getName());
									Button dynCheck = new Button(cmpChangeList, SWT.CHECK);
									if(modEntry.getInJar())
									{
										dynCheck.setFont(SWTResourceManager.getBoldFont(dynCheck.getFont()));
									}
									dynCheck.setText(modEntry.getName());
									dynCheck.setData("url",modEntry.getUrl());
									dynCheck.setData("inJar",modEntry.getInJar());
									dynCheck.setData("configs",modEntry.getConfigs());
									if(modEntry.getRequired())
									{
										dynCheck.setEnabled(false);
										dynCheck.setSelection(true);
									}						
								}
								scrolledComposite.setContent(cmpChangeList);
								scrolledComposite.setMinSize(cmpChangeList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
							}
						});
						if(flag == false){
							mnuServerEntry.setSelection(true);
							flag = true;
							browser.setUrl((String) mnuServerEntry.getData("News"));
							shell.setText(mnuServerEntry.getData("Name") + " - Minecraft Client Updater");

							List<Module> modules = mcu.loadFromURL((String) mnuServerEntry.getData("Pack"));
							Iterator<Module> itMods = modules.iterator();
							Control[] modListChildren = cmpChangeList.getChildren();
							for(int i = 0; i < modListChildren.length; i++)
							{
								modListChildren[i].dispose();
							}
							while(itMods.hasNext())
							{
								Module modEntry = itMods.next();
								System.out.println(modEntry.getName());
								Button dynCheck = new Button(cmpChangeList, SWT.CHECK);
								if(modEntry.getInJar())
								{
									dynCheck.setFont(SWTResourceManager.getBoldFont(dynCheck.getFont()));
								}
								dynCheck.setText(modEntry.getName());
								dynCheck.setData("url",modEntry.getUrl());
								dynCheck.setData("inJar",modEntry.getInJar());
								dynCheck.setData("configs",modEntry.getConfigs());
								if(modEntry.getRequired())
								{
									dynCheck.setEnabled(false);
									dynCheck.setSelection(true);
								}						
							}
							scrolledComposite.setContent(cmpChangeList);
							scrolledComposite.setMinSize(cmpChangeList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
						}
					}
				}
				
			}
		});

		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}	
		}
	}
	public static void updateLists(Shell shell)
	{
		shell.notifyListeners(254, new Event());
	}
}
