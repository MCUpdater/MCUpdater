package org.mcupdater;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.mcupdater.util.ModSide;
import org.mcupdater.util.ServerList;

public class MCUCLI extends MCUApp {
	@Override
	public void setStatus(String string) {
		log("Status: "+string);
	}
	@Override
	public void log(String msg) {
		System.out.println(msg);
	}
	@Override
	public boolean requestLogin() {
		return false;
	}

	public static void main( String args[] ) {
		MCUCLI instance = new MCUCLI();
		instance.parse( args );
	}

	public static boolean DEBUG = false;
	private void parse( String args[] ) {
		Map<String,String> opts = new HashMap<String,String>();
		boolean showHelp = true;
		// grab our provided options
		for( int k = 0; k < args.length; ++k ) {
			String[] param = args[k].split("=",2);
			if( !param[0].startsWith("--") || param[0].equals("--help") ) {
				showHelp = true;
				break;
			}
			showHelp = false;
			param[0] = param[0].substring(2);	// crop the --'s
			if( param.length > 1 ) {
				opts.put(param[0], param[1]);
			} else {
				opts.put(param[0], "");
			}
		}
		// show help
		if( showHelp ) {
			displayHelp();
			return;
		}
		
		if( opts.containsKey("debug") ) {
			DEBUG = true;
			dumpOpts(opts);
		}
		
		// make sure we have required arguments
		if( !(opts.containsKey("pack") && opts.containsKey("server")) ) {
			System.out.println("Error: You must specify both --pack and --server");
			return;
		}
		final String packURL = opts.get("pack");
		final String serverID = opts.get("server");
		
		// grab optional arguments
		ModSide side = ModSide.SERVER;
		if( opts.containsKey("side") && opts.get("side").equals("client") )
			side = ModSide.CLIENT;
		final boolean backup = opts.containsKey("backup");
		final boolean launch = opts.containsKey("launch");
		
		if( backup ) {
			doBackup();
		}
		
		doUpdate( packURL, serverID, side );
		
		if( launch ) {
			doLaunch(side);
		}
	}
	
	private void doUpdate(String packURL, String serverID, ModSide side) {
		log("Updating "+side+" for "+serverID+" from "+packURL+"...");
		
	}
	private void doLaunch(ModSide side) {
		log("Launching "+side+"...");
		
	}
	private void doBackup() {
		log("Backing up instance...");
	}
	
	private void dumpOpts( Map<String,String> opts ) {
		Iterator<String> it = opts.keySet().iterator();
		while( it.hasNext() ) {
			String key = it.next();
			System.out.println(key+": "+opts.get(key));
		}
	}
	
	private void displayVersion() {
		System.out.println("MCUpdater CLI - "+Version.VERSION);
	}
	
	private void displayHelp() {
		displayVersion();
		System.out.println(
			"Commands:\n\n"+
			"--help                    Display this help\n"+
			"--pack=<url>              URL to the ServerPack.XML\n"+
			"--server=<id>             Server ID of the server definition to use\n"+
			"--side=(server|client)    Which side should we update for?\n"+
			"--backup                  Save a timestamped backup of the world, mods, and configs\n"+
			"--launch                  Attempt to launch the world after updating"
		);
	}
	@Override
	public void addServer(ServerList entry) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addProgressBar(String title) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public DownloadQueue submitNewQueue(String queueName,
			Collection<Downloadable> files, File basePath) {
		// TODO Auto-generated method stub
		return null;
	}
}
