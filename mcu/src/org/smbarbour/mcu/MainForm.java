package org.smbarbour.mcu;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.smbarbour.mcu.MCUApp;
import org.smbarbour.mcu.util.InstanceManager;
import org.smbarbour.mcu.util.MCUpdater;
import org.smbarbour.mcu.util.Module;
import org.smbarbour.mcu.util.ServerList;
import org.smbarbour.mcu.util.ServerListPacket;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.JProgressBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.util.ResourceBundle;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JToolBar;
import javax.swing.ImageIcon;

public class MainForm extends MCUApp {
	private static final ResourceBundle Customization = ResourceBundle.getBundle("customization"); //$NON-NLS-1$
	private static final String VERSION = "v1.36";
	private static MainForm window;
	private Properties config = new Properties();
	private JFrame frmMain;
	final MCUpdater mcu = new MCUpdater();
	
	private JTabbedPane tabs;
	private final JTextPane browser = new JTextPane();
	private final ConsoleArea console = new ConsoleArea();
	private final SimpleDateFormat logSDF = new SimpleDateFormat("[HH:mm:ss.SSS] ");
	
	private ServerList selected;
	private final JPanel pnlModList = new JPanel();
	private JLabel lblStatus;
	private JProgressBar progressBar;
	private JList<ServerListPacket> serverList;
	private SLListModel slModel;
	
	private JButton btnUpdate;
	private JButton btnLaunchMinecraft;
	
	private boolean minimized;
	private TrayIcon trayIcon;
	private ImageIcon mcuIcon;
	
	public ResourceBundle getCustomization(){
		return Customization;
	}
	/**
	 * Create the application.
	 */
	public MainForm() {
		this.baseLogger = Logger.getLogger(MainForm.class);
		PropertyConfigurator.configure(config);
		window = this;
		initialize();
		window.frmMain.setVisible(true);
		mcu.setParent(window);
	}

	public Properties getConfig()
	{
		return config;
	}
	
	public void writeConfig(Properties newConfig)
	{
		System.out.println("Writing configuration file");
		File configFile = new File(mcu.getArchiveFolder() + MCUpdater.sep + "config.properties");
		try {
			configFile.getParentFile().mkdirs();
			newConfig.store(new FileOutputStream(configFile), "User-specific configuration options");
			config = newConfig;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private void createDefaultConfig(File configFile) {
		System.out.println("Creating default configuration file");
		Properties newConfig = new Properties();
		newConfig.setProperty("minimumMemory", "512M");
		newConfig.setProperty("maximumMemory", "1G");
		newConfig.setProperty("currentConfig", "");
		newConfig.setProperty("packRevision","");
		newConfig.setProperty("suppressUpdates", "false");
		try {
			configFile.getParentFile().mkdirs();
			newConfig.store(new FileOutputStream(configFile), "User-specific configuration options");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean validateConfig(Properties current)
	{
		boolean hasChanged = false;
		if (current.getProperty("minimumMemory") == null) {	current.setProperty("minimumMemory", "512M"); hasChanged = true; }
		if (current.getProperty("maximumMemory") == null) {	current.setProperty("maximumMemory", "1G"); hasChanged = true; }
		if (current.getProperty("currentConfig") == null) {	current.setProperty("currentConfig", ""); hasChanged = true; }
		if (current.getProperty("packRevision") == null) {	current.setProperty("packRevision",""); hasChanged = true; }
		if (current.getProperty("suppressUpdates") == null) { current.setProperty("suppressUpdates", "false"); hasChanged = true; }
		return hasChanged;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	void initialize() {
		File configFile = new File(mcu.getArchiveFolder() + MCUpdater.sep + "config.properties");
		if (!configFile.exists())
		{
			createDefaultConfig(configFile);
		}
		try {
			config.load(new FileInputStream(configFile));
			if (validateConfig(config))
			{
				writeConfig(config);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		frmMain = new JFrame();
		frmMain.setTitle("[No Server Selected] - MCUpdater " + MainForm.VERSION);
		frmMain.setResizable(false);
		frmMain.setBounds(100, 100, 1175, 592);
		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		mcuIcon = new ImageIcon(MainForm.class.getResource("/art/mcu-icon.png"));	// was "/icons/briefcase.png"
		frmMain.setIconImage(mcuIcon.getImage());

		JPanel pnlFooter = new JPanel();
		frmMain.getContentPane().add(pnlFooter, BorderLayout.SOUTH);
		pnlFooter.setLayout(new BorderLayout(0, 0));

		JPanel pnlButtons = new JPanel();
		pnlFooter.add(pnlButtons, BorderLayout.EAST);

		btnUpdate = new JButton("Update");
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread() {
					public void run() {
						Path instancePath;
						InstanceManager instance = new InstanceManager(mcu);
						if ( Files.notExists( mcu.getInstanceRoot().toPath().resolve(selected.getServerId()) ) ) {
							instancePath = instance.createInstance(selected.getServerId());
						} else {
							instancePath = mcu.getInstanceRoot().toPath().resolve(selected.getServerId());
						}
						try {
							Path MCPath = new File(mcu.getMCFolder()).toPath();
							if (Files.exists(MCPath)) { Files.delete(MCPath); }
							instance.createLink(MCPath, instancePath);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return;
						}
						mcu.getMCVersion();
						int saveConfig = JOptionPane.showConfirmDialog(null, "Do you want to save a backup of your existing configuration?", "MCUpdater", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
						if(saveConfig == JOptionPane.YES_OPTION){
							setLblStatus("Creating backup");
							setProgressBar(10);
							Calendar cal = Calendar.getInstance();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String backDesc = (String) JOptionPane.showInputDialog(null,"Enter description for backup:", "MCUpdater", JOptionPane.QUESTION_MESSAGE, null, null, ("Automatic backup: " + sdf.format(cal.getTime())));
							log("Creating backup ("+backDesc+") ...");
							mcu.saveConfig(backDesc);
							log("Backup complete.");
						} else if(saveConfig == JOptionPane.CANCEL_OPTION){
							return;
						}
						btnUpdate.setEnabled(false);
						tabs.setSelectedIndex(tabs.getTabCount()-1);
						config.setProperty("currentConfig", selected.getServerId());
						config.setProperty("packRevision", selected.getRevision());
						writeConfig(config);
						List<Module> toInstall = new ArrayList<Module>();
						List<Component> selects = new ArrayList<Component>(Arrays.asList(pnlModList.getComponents()));
						Iterator<Component> it = selects.iterator();
						setLblStatus("Preparing module list");
						log("Preparing module list...");
						setProgressBar(20);
						while(it.hasNext()) {
							Component baseEntry = it.next();
							//System.out.println(baseEntry.getClass().toString());
							if(baseEntry.getClass().toString().equals("class org.smbarbour.mcu.JModuleCheckBox")) {
								JModuleCheckBox entry = (JModuleCheckBox) baseEntry;
								if(entry.isSelected()){
									toInstall.add(entry.getModule());
								}
							}
						}
						try {
							setLblStatus("Installing mods");
							log("Installing mods...");
							setProgressBar(25);
							mcu.installMods(selected, toInstall);
							if (selected.isGenerateList()) {
								setLblStatus("Writing servers.dat");
								log("Writing servers.dat");
								setProgressBar(90);
								mcu.writeMCServerFile(selected.getName(), selected.getAddress());
							}
							setLblStatus("Finished");
							setProgressBar(100);
						} catch (FileNotFoundException fnf) {
							log("! Error: "+fnf.getMessage());
							JOptionPane.showMessageDialog(null, fnf.getMessage(), "MCUpdater", JOptionPane.ERROR_MESSAGE);
						}
						log("Update complete.");
						JOptionPane.showMessageDialog(frmMain, "Your update is complete.", "Update Complete", JOptionPane.INFORMATION_MESSAGE);
						btnUpdate.setEnabled(true);
					}						
				}.start();
			}
		});
		pnlButtons.add(btnUpdate);

		btnLaunchMinecraft = new JButton("Launch Minecraft");
		btnLaunchMinecraft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File launcher = new File(mcu.getMCFolder() + MCUpdater.sep + "minecraft.jar");
				if(!launcher.exists())
				{
					try {
						URL launcherURL = new URL("http://s3.amazonaws.com/MinecraftDownload/launcher/minecraft.jar");
						ReadableByteChannel rbc = Channels.newChannel(launcherURL.openStream());
						FileOutputStream fos = new FileOutputStream(launcher);
						fos.getChannel().transferFrom(rbc, 0, 1 << 24);
						fos.close();
					} catch (MalformedURLException mue) {
						mue.printStackTrace();

					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				File outFile = new File(mcu.getArchiveFolder() + MCUpdater.sep + "client-log.txt");
				outFile.delete();
				btnLaunchMinecraft.setEnabled(false);
				tabs.setSelectedIndex(tabs.getTabCount()-1);
				final PopupMenu menu = trayIcon.getPopupMenu();
				MenuItem killItem = null;
				for( int k = 0; k < menu.getItemCount(); ++k ) {
					final MenuItem item = menu.getItem(k);
					if( item.getLabel().equals("Kill Minecraft") ) {
						killItem = item;
						break;
					}
				}
				LauncherThread thread = LauncherThread.launch(launcher, config.getProperty("minimumMemory"), config.getProperty("maximumMemory"), Boolean.parseBoolean(config.getProperty("suppressUpdates")), outFile, console);
				thread.register( window, btnLaunchMinecraft, killItem );
				thread.start();
			}
		});
		pnlButtons.add(btnLaunchMinecraft);

		JPanel pnlStatus = new JPanel();
		pnlFooter.add(pnlStatus, BorderLayout.CENTER);
		pnlStatus.setLayout(new BorderLayout(0, 0));

		lblStatus = new JLabel("Idle");
		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
		pnlStatus.add(lblStatus);
		
		JPanel panel = new JPanel();
		pnlStatus.add(panel, BorderLayout.EAST);
		panel.setLayout(new BorderLayout(0, 0));
		
		progressBar = new JProgressBar();
		panel.add(progressBar);
		progressBar.setStringPainted(true);
		
		Component TopStrut = Box.createVerticalStrut(5);
		panel.add(TopStrut, BorderLayout.NORTH);
		
		Component BottomStrut = Box.createVerticalStrut(5);
		panel.add(BottomStrut, BorderLayout.SOUTH);

		Component horizontalStrut = Box.createHorizontalStrut(5);
		pnlFooter.add(horizontalStrut, BorderLayout.WEST);
		
		JPanel pnlLeft = new JPanel();
		frmMain.getContentPane().add(pnlLeft, BorderLayout.WEST);
		pnlLeft.setLayout(new BorderLayout(0, 0));
		
		JLabel lblServers = new JLabel("Servers");
		lblServers.setHorizontalAlignment(SwingConstants.CENTER);
		lblServers.setFont(new Font("Dialog", Font.BOLD, 14));
		pnlLeft.add(lblServers, BorderLayout.NORTH);
		
		slModel = new SLListModel();
		serverList = new JList<ServerListPacket>();
		serverList.setModel(slModel);
		serverList.setCellRenderer(new ServerListCellRenderer());
		serverList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting())
				{
					changeSelectedServer(((ServerListPacket)serverList.getSelectedValue()).getEntry());
					if (selected.getServerId().equals(config.getProperty("currentConfig")) && !(selected.getRevision().equals(config.getProperty("packRevision")))) {
						JOptionPane.showMessageDialog(null, "Your configuration is out of sync with the server. Updating is necessary.", "MCUpdater", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
			
		});
				
		JScrollPane serverScroller = new JScrollPane(serverList);
		pnlLeft.add(serverScroller, BorderLayout.CENTER);
						
		JPanel pnlRight = new JPanel();
		frmMain.getContentPane().add(pnlRight, BorderLayout.EAST);
		pnlRight.setLayout(new BorderLayout(0, 0));

		JPanel pnlChangesTitle = new JPanel();
		pnlChangesTitle.setLayout(new BorderLayout(0,0));
		JLabel lblChanges = new JLabel("Modules");
		lblChanges.setHorizontalAlignment(SwingConstants.CENTER);
		lblChanges.setFont(new Font("Dialog", Font.BOLD, 14));
		pnlChangesTitle.add(lblChanges, BorderLayout.CENTER);
		pnlRight.add(pnlChangesTitle, BorderLayout.NORTH);
		
		Component hstrut_ChangesLeft = Box.createHorizontalStrut(75);
		pnlChangesTitle.add(hstrut_ChangesLeft, BorderLayout.WEST);
		
		Component hstrut_ChangesRight = Box.createHorizontalStrut(75);
		pnlChangesTitle.add(hstrut_ChangesRight, BorderLayout.EAST);

		JScrollPane modScroller = new JScrollPane(pnlModList);
		pnlRight.add(modScroller, BorderLayout.CENTER);
		pnlModList.setLayout(new BoxLayout(pnlModList, BoxLayout.Y_AXIS));
		browser.setEditable(false);
		browser.setContentType("text/html");
		browser.addHyperlinkListener(new HyperlinkListener(){

			@Override
			public void hyperlinkUpdate(HyperlinkEvent he) {
				if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
					try {
						MCUpdater.openLink(he.getURL().toURI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		tabs = new JTabbedPane();

		browser.setText("<HTML><BODY>There are no servers currently defined.</BODY></HTML>");
		JScrollPane browserScrollPane = new JScrollPane(browser);
		browserScrollPane.setViewportBorder(null);
		browser.setBorder(null);
		tabs.add("News",browserScrollPane);
		
		console.setBorder(null);
		console.setLineWrap(true);
		console.setEditable(false);
		Font f = new Font("Monospaced",Font.PLAIN,11);
		console.setFont(f);
		JScrollPane consoleScrollPane = new JScrollPane(console);
		consoleScrollPane.setViewportBorder(null);
		consoleScrollPane.setAutoscrolls(true);
		consoleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		consoleScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tabs.add("Console",consoleScrollPane);
		log("MCUpdater starting...");
		
		frmMain.getContentPane().add(tabs, BorderLayout.CENTER);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		frmMain.getContentPane().add(toolBar, BorderLayout.NORTH);
		
		JButton btnManageServers = new JButton("");
		btnManageServers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ServerManager sm = new ServerManager(window);
				sm.setVisible(true);
			}
		});
		btnManageServers.setToolTipText("Manage Servers");
		btnManageServers.setIcon(new ImageIcon(MainForm.class.getResource("/icons/server_database.png")));
		toolBar.add(btnManageServers);
		
		JButton btnOptions = new JButton("");
		btnOptions.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ClientConfig cc = new ClientConfig(window);
				cc.setVisible(true);
			}
		});
		btnOptions.setToolTipText("Options");
		btnOptions.setIcon(new ImageIcon(MainForm.class.getResource("/icons/application_edit.png")));
		toolBar.add(btnOptions);
		
		JButton btnBackups = new JButton("");
		btnBackups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BackupManager bm = new BackupManager(window);
				bm.setVisible(true);
			}
		});
		btnBackups.setIcon(new ImageIcon(MainForm.class.getResource("/icons/folder_database.png")));
		btnBackups.setToolTipText("Backups");
		toolBar.add(btnBackups);
		
		JButton btnInsttest = new JButton("InstTest");
		btnInsttest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InstanceManager inst = new InstanceManager(mcu);
				inst.createInstance("Test");
			}
		});
		toolBar.add(btnInsttest);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		toolBar.add(horizontalGlue);
		
		JLabel lblNewLabel = new JLabel("minecraft.jar version: " + mcu.getMCVersion());
		toolBar.add(lblNewLabel);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(5);
		toolBar.add(horizontalStrut_1);

		File serverFile = new File(mcu.getArchiveFolder() + MCUpdater.sep + "mcuServers.dat");
		String packUrl = Customization.getString("InitialServer.text");
		while(!serverFile.exists() && !(serverFile.length() > 0)){
			if(packUrl.isEmpty()) {
				packUrl = (String) JOptionPane.showInputDialog(null, "No default server defined.\nPlease enter URL to ServerPack.xml: ", "MCUpdater", JOptionPane.INFORMATION_MESSAGE, null, null, "http://www.example.com/ServerPack.xml");
			}
			try {
				Document serverHeader = MCUpdater.readXmlFromUrl(packUrl);
				Element docEle = serverHeader.getDocumentElement();
				ServerList sl = new ServerList(docEle.getAttribute("id"), docEle.getAttribute("name"), packUrl, docEle.getAttribute("newsUrl"), docEle.getAttribute("iconUrl"), docEle.getAttribute("version"), docEle.getAttribute("serverAddress"), mcu.parseBoolean(docEle.getAttribute("generateList")), docEle.getAttribute("revision"));
				List<ServerList> servers = new ArrayList<ServerList>();
				servers.add(sl);
				mcu.writeServerList(servers);
			} catch (Exception x) {
				x.printStackTrace();
				packUrl = "";
			}
		}

		updateServerList();
		int selectIndex = ((SLListModel)serverList.getModel()).getEntryIdByTag(config.getProperty("currentConfig"));
		serverList.setSelectedIndex(selectIndex);
		
		initTray();
	}

	protected void changeSelectedServer(ServerList entry) {
		try {
			selected = entry;
			browser.setPage(entry.getNewsUrl());
			frmMain.setTitle(entry.getName() + " - MCUpdater " + MainForm.VERSION);
			List<Module> modules = mcu.loadFromURL(entry.getPackUrl(), entry.getServerId());
			Iterator<Module> itMods = modules.iterator();
			pnlModList.setVisible(false);
			pnlModList.removeAll();
			while(itMods.hasNext())
			{
				Module modEntry = itMods.next();
				JModuleCheckBox chkModule = new JModuleCheckBox(modEntry.getName());
				if(modEntry.getInJar())
				{
					chkModule.setFont(chkModule.getFont().deriveFont(Font.BOLD));
				}
				chkModule.setModule(modEntry);
				if(modEntry.getRequired())
				{
					chkModule.setSelected(true);
					chkModule.setEnabled(false);
				}
				if(modEntry.getIsDefault())
				{
					chkModule.setSelected(true);
				}
				pnlModList.add(chkModule);
			}
			pnlModList.setVisible(true);
			
			// switching servers should show news
			if( tabs != null ) {
				tabs.setSelectedIndex(0);
			}
			
			this.frmMain.repaint();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	public void updateServerList()
	{
		serverList.setVisible(false);
		slModel.clear();
		List<ServerList> servers = mcu.loadServerList(Customization.getString("InitialServer.text"));
		if (servers != null)
		{
			Iterator<ServerList> it = servers.iterator();
			
			//boolean flag = false;
			while(it.hasNext())
			{
				ServerList entry = it.next();
				slModel.add(new ServerListPacket(entry, mcu));
			}
		}
		serverList.setVisible(true);
	}
		
	@Override
	public void setLblStatus(String text) {
		lblStatus.setText(text);
	}
	
	@Override
	public void setProgressBar(int value) {
		progressBar.setValue(value);
	}
	
	@Override
	public void log(String msg) {
		final StringBuilder str = new StringBuilder(logSDF.format(new Date()));
		str.append(msg);
		str.append('\n');
		console.log(str.toString());
	}
	
	private void initTray() {
		if( !SystemTray.isSupported() ) {
			System.out.println("System tray is NOT supported :(");
			return;
		}
		
		// change minimize behavior to go to tray
		frmMain.addWindowListener(new WindowAdapter() {
			public void windowIconified(WindowEvent e) {
				minimize(false);	// this is a manual minimize event
			}
		});
		
		final String label = "MCUpdater "+VERSION;
		
		trayIcon = new TrayIcon(mcuIcon.getImage(),label);
		trayIcon.setImageAutoSize(true);
		final SystemTray tray = SystemTray.getSystemTray();
		final PopupMenu menu = new PopupMenu();
		
		final MenuItem restoreItem = new MenuItem("Restore MCU");
		restoreItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				restore();
			}
		});
		final MenuItem killItem = new MenuItem("Kill Minecraft");
		killItem.setEnabled(false);
		
		menu.add(restoreItem);
		menu.add(killItem);
		
		trayIcon.setPopupMenu(menu);
		
		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			trayIcon = null;
			e.printStackTrace();
		}		
	}
	public void minimize(boolean auto) {
		if( trayIcon == null ) {
			// only minimize if we have a way to come back
			return;
		}
		// TODO: add preference whether to autohide or not
		frmMain.setVisible(false);
		frmMain.setState(Frame.ICONIFIED);
		minimized = true;
	}
	public void restore() {
		if( !minimized ) {
			return;
		}
		frmMain.setVisible(true);
		frmMain.setState(Frame.NORMAL);
	}
}

class JModuleCheckBox extends JCheckBox
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8124564072878896685L;
	private Module entry;

	public JModuleCheckBox(String name) {
		super(name);
	}

	public void setModule(Module entry)
	{
		this.entry=entry;
	}

	public Module getModule()
	{
		return entry;
	}
}

class SLListModel extends AbstractListModel<ServerListPacket>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6829288390151952427L;
	List<ServerListPacket> model;
	
	public SLListModel()
	{
		model = new ArrayList<ServerListPacket>();
	}
	
	public int getEntryIdByTag(String tag) {
		int foundId = 0;
		Iterator<ServerListPacket> it = model.iterator();
		int searchId = 0;
		while (it.hasNext()) {
			ServerListPacket entry = it.next();
			if (tag.equals(entry.getEntry().getServerId())) {
				foundId = searchId;
				break;
			}
			searchId++;
		}
		return foundId;
	}

	@Override
	public int getSize() {
		return model.size();
	}

	@Override
	public ServerListPacket getElementAt(int index) {
		return model.get(index);
	}

	public void add(ServerListPacket element)
	{
		model.add(element);
		fireContentsChanged(this, 0, getSize());
	}
	
	public Iterator<ServerListPacket> iterator()
	{
		return model.iterator();
	}
	
	public boolean removeElement(ServerListPacket element)
	{
		boolean removed = model.remove(element);
		if (removed) {
			fireContentsChanged(this, 0, getSize());
		}
		return removed;
	}
	
	public void clear()
	{
		model.clear();
		fireContentsChanged(this, 0, getSize());
	}

}
