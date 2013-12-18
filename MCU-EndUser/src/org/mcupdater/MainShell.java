package org.mcupdater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.mcupdater.MCUConsole.LineStyle;
import org.mcupdater.instance.Instance;
import org.mcupdater.model.ConfigFile;
import org.mcupdater.model.GenericModule;
import org.mcupdater.model.ModSide;
import org.mcupdater.model.Module;
import org.mcupdater.model.ServerList;
import org.mcupdater.mojang.AssetManager;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.translate.TranslateProxy;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.ServerPackParser;
import org.mcupdater.translate.Languages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainShell extends MCUApp {

	private static MainShell INSTANCE;
	private Shell shell;
	private ServerList selected;
	private MCUBrowser browser;
	private List<Module> modList;
	private MCUConsole console;
	private MCUProgress progress;
	private MCUModules modules;
	public TranslateProxy translate;
	private Label lblStatus;
	private InstanceList iList;
	private MCUClientTracker tracker;
	private String defaultUrl;
	public MCULogin login;
	private Button btnUpdate;
	private Button btnLaunch;
	private final Display display;
	private MCUSettings cmpSettings;
	private boolean playing;
	private Composite cmpStatus;
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();	

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			OptionParser optParser = new OptionParser();
			ArgumentAcceptingOptionSpec<String> packSpec = optParser.accepts("ServerPack").withRequiredArg().ofType(String.class);
			ArgumentAcceptingOptionSpec<File> rootSpec = optParser.accepts("MCURoot").withRequiredArg().ofType(File.class);
			OptionSet options = optParser.parse(args);
			MCUpdater mcu = MCUpdater.getInstance(options.valueOf(rootSpec));
			INSTANCE = new MainShell();
			INSTANCE.setDefaultPack(options.valueOf(packSpec));
			mcu.setParent(INSTANCE);
			SettingsManager.getInstance();
			INSTANCE.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private MainShell() {
		display = Display.getDefault();
		this.baseLogger = Logger.getLogger("MCUpdater");
		baseLogger.setLevel(Level.ALL);
		FileHandler mcuHandler;
		try {
			mcuHandler = new FileHandler(MCUpdater.getInstance().getArchiveFolder().resolve("MCUpdater.log").toString(), 0, 3);
			mcuHandler.setFormatter(new FMLStyleFormatter());
			baseLogger.addHandler(mcuHandler);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Version.setApp(this);
	}
	
	private void setDefaultPack(String packUrl) {
		this.defaultUrl = packUrl;
	}

	/**
	 * Open the window.
	 */
	public void open() {
		try {
			translate = Languages.valueOf(Languages.getLocale()).getProxy();
		} catch (Exception e) {
			System.out.println("No translation for " + Languages.getLocale() + "!");
			translate = Languages.en_US.getProxy();
		}		
		createContents();
		processSettings();
		getShell().open();
		getShell().layout();
		tracker = new MCUClientTracker(display, progress); 
				
		int activeJobs = 0;
		ServerList currentSelection = null;
		if (SettingsManager.getInstance().getSettings().getProfiles().size() == 0) {
			try {
				Profile newProfile = LoginDialog.doLogin(getShell(), translate, "");
				if (newProfile.getStyle().equals("Yggdrasil")) {
					SettingsManager.getInstance().getSettings().addOrReplaceProfile(newProfile);
					SettingsManager.getInstance().getSettings().setLastProfile(newProfile.getName());
					if (!SettingsManager.getInstance().isDirty()) {
						SettingsManager.getInstance().saveSettings();
					}
					refreshProfiles();
					cmpSettings.reloadProfiles();
					login.setSelectedProfile(newProfile.getName());
				}
			} catch (Exception e) {
			}

		}
		boolean playState = false;
		while (!getShell().isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
			if (activeJobs != progress.getActiveCount() || currentSelection != selected || playState != isPlaying()) {
				currentSelection = selected;
				activeJobs = progress.getActiveCount();
				playState = isPlaying();
				lblStatus.setText("Active jobs: " + activeJobs);
				if (activeJobs > 0) {
					btnLaunch.setEnabled(false);
				} else {
					if (!(currentSelection == null) && !playState){
						btnLaunch.setEnabled(true);
					} else {
						btnLaunch.setEnabled(false);
					}
				}
				if (!(currentSelection == null)) {
					if (progress.getActiveById(currentSelection.getServerId()) > 0){
						btnUpdate.setEnabled(false);
					} else {
						btnUpdate.setEnabled(true);
					}
				} else {
					btnUpdate.setEnabled(false);
				}
			}
		}
		display.dispose();
	}

	public boolean isPlaying() {
		return this.playing;
	}

	public void setPlaying(boolean state) {
		this.playing = state;
	}
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		setShell(new Shell());
		getShell().setSize(1175, 592);
		getShell().setText("MCUpdater " + Version.VERSION);
		getShell().setLayout(new GridLayout(1,false));
		
		getShell().addListener(SWT.RESIZE, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				getShell().layout();
			}
		});
		
		final Composite mainArea = new Composite(getShell(), SWT.NONE);
		mainArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainArea.setLayout(new FormLayout());
		{
			final int limit = 20;
			final Sash sashLeft = new Sash(mainArea, SWT.VERTICAL);

			Group grpInstances = new Group(mainArea, SWT.NONE);
			grpInstances.setText(translate.instances);
			grpInstances.setLayout(new FillLayout());
			FormData grpInstancesData = new FormData();
			{
				grpInstancesData.left = new FormAttachment(0,0);
				grpInstancesData.right = new FormAttachment(sashLeft, 0);
				grpInstancesData.top = new FormAttachment(0,0);
				grpInstancesData.bottom = new FormAttachment(100,0);
			}
			grpInstances.setLayoutData(grpInstancesData);			
			{
				/*				
				Composite foo = new Composite(grpInstances, SWT.NONE);
				foo.setLayout(new RowLayout(SWT.VERTICAL));
				List<ServerList> list = MCUpdater.getInstance().loadServerList("http://files.mcupdater.com/example/SamplePack.xml");
				Collections.sort(list);
				for (ServerList entry : list) {
					Label bar = new Label(foo, SWT.NONE);
					bar.setText("Entry: " + entry.getName());
				}
				*/
				iList = new InstanceList(grpInstances);
				//iList.setInstances(MCUpdater.getInstance().loadServerList("http://files.mcupdater.com/example/SamplePack.xml"));
				//iList.setInstances(MCULogic.loadServerList(defaultUrl));
				grpInstances.pack();
			}
			final FormData sashLeftData = new FormData();
			{
				sashLeftData.left = new FormAttachment(20, 0);
				sashLeftData.top = new FormAttachment(0, 0);
				sashLeftData.bottom = new FormAttachment(100, 0);
			}
			sashLeft.setLayoutData(sashLeftData);
			sashLeft.addListener(SWT.Selection, new Listener(){
				@Override
				public void handleEvent(Event e) {
					Rectangle sashRect = sashLeft.getBounds();
					Rectangle viewRect = mainArea.getClientArea();
					int right = viewRect.width - sashRect.width - limit;
					e.x = Math.max(Math.min(e.x, right), limit);
					if (e.x != sashRect.x) {
						sashLeftData.left = new FormAttachment(0, e.x);
						mainArea.layout();
					}
				}
			});
			
			final TabFolder tabFolder = new TabFolder(mainArea, SWT.NONE);
			final FormData grpTabData = new FormData();
			{
				grpTabData.left = new FormAttachment(sashLeft,0);
				grpTabData.right = new FormAttachment(100,0);
				grpTabData.top = new FormAttachment(0,0);
				grpTabData.bottom = new FormAttachment(100,0);
			}
			tabFolder.setLayoutData(grpTabData);
			{
				TabItem tbtmNews = new TabItem(tabFolder, SWT.V_SCROLL);
				{
					tbtmNews.setText(translate.news);
					browser = new MCUBrowser(tabFolder, SWT.NONE);
					tbtmNews.setControl(browser);
				}
				TabItem tbtmConsole = new TabItem(tabFolder, SWT.NONE);
				{
					tbtmConsole.setText(translate.console);
					console = new MCUConsole(tabFolder);
					tbtmConsole.setControl(console);
					ConsoleHandler consoleHandler = new ConsoleHandler(console);
					consoleHandler.setLevel(Level.INFO);
					baseLogger.addHandler(consoleHandler);
					MCUpdater.apiLogger.addHandler(consoleHandler);
				}
				TabItem tbtmSettings = new TabItem(tabFolder, SWT.NONE);
				{
					tbtmSettings.setText(translate.settings);
					cmpSettings = new MCUSettings(tabFolder);
					tbtmSettings.setControl(cmpSettings);
				}
				TabItem tbtmModules = new TabItem(tabFolder, SWT.NONE);
				{
					tbtmModules.setText(translate.modules);
					modules = new MCUModules(tabFolder);
					tbtmModules.setControl(modules);
				}
				TabItem tbtmProgress = new TabItem(tabFolder, SWT.NONE);
				{
					tbtmProgress.setText(translate.progress);
					progress = new MCUProgress(tabFolder);
					tbtmProgress.setControl(progress); 
				}
			}
			
			/*
			Group grpModules = new Group(mainArea, SWT.NONE);
			grpModules.setText("Modules");
			final FormData grpModulesData = new FormData();
			{
				grpModulesData.left = new FormAttachment(sashRight,0);
				grpModulesData.right = new FormAttachment(100, 0);
				grpModulesData.top = new FormAttachment(0,0);
				grpModulesData.bottom = new FormAttachment(100,0);
			}
			grpModules.setLayoutData(grpModulesData);
			grpModules.setLayout(new FillLayout(SWT.VERTICAL));
			ScrolledComposite modScroller = new ScrolledComposite(grpModules, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
			modContainer = new Composite(modScroller, SWT.FILL);
			modContainer.setLayout(new RowLayout(SWT.VERTICAL));
			modScroller.setContent(modContainer);
			*/
			
//			final FormData sashRightData = new FormData();
//			{
//				sashRightData.right = new FormAttachment(80,0);
//				sashRightData.top = new FormAttachment(0, 0);
//				sashRightData.bottom = new FormAttachment(100, 0);
//			}
//			sashRight.setLayoutData(sashRightData);
//			sashRight.addListener(SWT.Selection, new Listener(){
//				@Override
//				public void handleEvent(Event e) {
//					Rectangle sashRect = sashRight.getBounds();
//					Rectangle viewRect = mainArea.getClientArea();
//					int right = viewRect.width - sashRect.width - limit;
//					e.x = Math.max(Math.min(e.x, right), limit);
//					if (e.x != sashRect.x) {
//						sashRightData.right = new FormAttachment(0, e.x);
//						mainArea.layout();
//					}
//				}
//			});
		}
		
		cmpStatus = new Composite(getShell(), SWT.NONE);
		cmpStatus.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		{
			cmpStatus.setLayout(new GridLayout(4, false));
						
			lblStatus = new Label(cmpStatus, SWT.NONE);
			lblStatus.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,1,1));
			lblStatus.setText("Ready");
			
			login = new MCULogin(cmpStatus);
			login.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,false,false,1,1));

			btnUpdate = new Button(cmpStatus, SWT.PUSH);
			btnUpdate.setEnabled(false);
			btnUpdate.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false,1,1));
			btnUpdate.setText(translate.update);
			btnUpdate.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					btnUpdate.setEnabled(false);
					MCUpdater.getInstance().getInstanceRoot().resolve(selected.getServerId()).toFile().mkdirs();

					Instance instData;
					final Path instanceFile = MCUpdater.getInstance().getInstanceRoot().resolve(selected.getServerId()).resolve("instance.json");
					try {
						BufferedReader reader = Files.newBufferedReader(instanceFile, StandardCharsets.UTF_8);
						instData = gson.fromJson(reader, Instance.class);
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
						instData = new Instance();
					}

					final List<GenericModule> selectedMods = new ArrayList<GenericModule>();
					final List<ConfigFile> selectedConfigs = new ArrayList<ConfigFile>();
					Iterator<ModuleCheckbox> it = modules.getModules().iterator();
					while (it.hasNext()){
						ModuleCheckbox entry = it.next();
						System.out.println("Module: " + entry.getModule().getName());
						if (entry.isSelected()) {
							selectedMods.add(entry.getModule());
							if (entry.getModule().hasConfigs()){
								selectedConfigs.addAll(entry.getModule().getConfigs());
							}
							if (entry.getModule().hasSubmodules()){
								selectedMods.addAll(entry.getModule().getSubmodules());
							}
						}
						if (!entry.getModule().getRequired()) {
							instData.setModStatus(entry.getModule().getId(), entry.isSelected());
						}
					}

//					try {
//						if (!instanceFile.toFile().exists()) { instanceFile.toFile().createNewFile(); }
//						instData.load(Files.newInputStream(instanceFile));		
//					} catch (IOException e1) {
//						//baseLogger.log(Level.SEVERE, "I/O error", e1);
//						e1.printStackTrace();
//					}
					
					
					try {
						MCUpdater.getInstance().installMods(selected , selectedMods, selectedConfigs, false, instData, ModSide.CLIENT);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) { widgetSelected(arg0); }
			});
			
			btnLaunch = new Button(cmpStatus, SWT.PUSH);
			btnLaunch.setEnabled(false);
			btnLaunch.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false,1,1));
			btnLaunch.setText(translate.launchMinecraft);
			btnLaunch.addSelectionListener(new SelectionListener()
			{

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					widgetSelected(arg0);
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					btnLaunch.setEnabled(false);
					Profile launchProfile = login.getSelectedProfile();
					if (!(launchProfile == null)) {
						SettingsManager.getInstance().getSettings().setLastProfile(launchProfile.getName());
						SettingsManager.getInstance().getSettings().findProfile(launchProfile.getName()).setLastInstance(selected.getServerId());
						if (!SettingsManager.getInstance().isDirty()) {
							SettingsManager.getInstance().saveSettings();
						}
						try {
							MCULogic.doLaunch(selected, modules.getModules(), launchProfile);
						} catch (Exception e) {
							MessageBox msg = new MessageBox(getShell(), SWT.ICON_WARNING);
							msg.setMessage(e.getMessage() + "\n\nNote: An authentication error can occur if your profile is out of sync with Mojang's servers.\nRe-add your profile in the Settings tab to resync with Mojang.");
							log(e.getMessage());
							msg.open();
						}
					}
				}
			});
		}
	}
	
	public void changeSelectedInstance(ServerList entry) {
		this.selected = entry;
		browser.setUrl(selected.getNewsUrl());
		modList = ServerPackParser.loadFromURL(selected.getPackUrl(), selected.getServerId());
		Instance instData = new Instance();
		final Path instanceFile = MCUpdater.getInstance().getInstanceRoot().resolve(entry.getServerId()).resolve("instance.json");
		try {
			BufferedReader reader = Files.newBufferedReader(instanceFile, StandardCharsets.UTF_8);
			instData = gson.fromJson(reader, Instance.class);
			reader.close();
		} catch (IOException e) {
			MainShell.getInstance().baseLogger.log(Level.WARNING, "instance.json file not found.");
		}
		refreshModList(instData.getOptionalMods());
	}

	private void refreshModList(Map<String, Boolean> optionalSelections) {
		modules.reload(modList, optionalSelections);
	}

	public static MainShell getInstance() {
		return INSTANCE;
	}

	public void refreshInstances() {
		iList.setInstances(MCULogic.loadServerList(defaultUrl));
	}
	@Override
	public void setStatus(String string) {
		// TODO Auto-generated method stub
		//lblStatus.setText(string);
	}

	@Override
	public void log(String msg) {
		baseLogger.info(msg);
	}

	@Override
	public boolean requestLogin() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addServer(ServerList entry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addProgressBar(String title, String parent) {
		progress.addProgressBar(title, parent);
	}

	@Override
	public DownloadQueue submitNewQueue(String queueName, String parent, Collection<Downloadable> files, File basePath, File cachePath) {
		progress.addProgressBar(queueName, parent);
		if (login.getSelectedProfile() != null) {
			return new DownloadQueue(queueName, parent, tracker, files, basePath, cachePath, login.getSelectedProfile().getName());
		} else {
			return new DownloadQueue(queueName, parent, tracker, files, basePath, cachePath);
		}
	}
	
	@Override
	public DownloadQueue submitAssetsQueue(String queueName, String parent, MinecraftVersion version) {
		progress.addProgressBar(queueName, parent);
		return AssetManager.downloadAssets(queueName, parent, MCUpdater.getInstance().getArchiveFolder().resolve("assets").toFile(), tracker, version);
	}

	public void refreshProfiles() {
		login.refreshProfiles(SettingsManager.getInstance().getSettings());
		cmpStatus.layout();
	}

	public void processSettings() {
		Settings settings = SettingsManager.getInstance().getSettings();
		MCUpdater.getInstance().setInstanceRoot(new File(settings.getInstanceRoot()).toPath());
		MCUpdater.getInstance().getInstanceRoot().toFile().mkdirs();
		refreshProfiles();
		refreshInstances();
		login.setSelectedProfile(settings.getLastProfile());
	}

	public void setSelectedInstance(String lastInstance) {
		iList.changeSelection(lastInstance);
	}

	public String getDefaultPack() {
		return this.defaultUrl;
	}

	public Display getDisplay() {
		return display;
	}

	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	public void consoleWrite(final String line) {
		display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				console.appendLine(line, LineStyle.NORMAL);				
			}
		});
	}

	public ServerList getSelectedInstance() {
		return this.selected;
	}
}
