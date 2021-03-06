package com.poixson.webxbukkit;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsManager;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.pxdb.dbQuery;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.console.xNoConsole;
import com.poixson.webxbukkit.webLink.LinkManager;
import com.poixson.webxbukkit.webSettings.SettingsManager;
import com.poixson.webxbukkit.xLogger.logBukkit;


public class WebAPI extends JavaPlugin {

	private static volatile WebAPI instance = null;
	private static final Object lock = new Object();

	// objects
	private volatile webConfig config = null;
@SuppressWarnings("unused")
	private volatile SettingsManager settings = null;
	private volatile PluginVersion version = null;
	private final webLanguage lang = new webLanguage();
	// web link
	private volatile LinkManager link = null;

	// database key
	private volatile String dbKey = null;

	// null=unloaded false=failed true=loaded
	private static volatile Boolean isOk = null;


	// get instance
	public static WebAPI get() {
		synchronized(lock) {
			if(instance == null) throw new RuntimeException("WebAPI is not enabled! Plugin instance cannot be obtained!");
			return instance;
		}
	}
	public WebAPI() {
		super();
//xVars.debug(true);
		// init logger
		logBukkit.init();
		synchronized(lock) {
			if(instance != null) throw new RuntimeException("API already loaded?!");
			instance = this;
		}
		// disable console (let bukkit handle this)
		xLog.setConsole(new xNoConsole());
	}
	public static boolean isOk() {
		return isOk;
	}


	// enable api plugin
	@Override
	public void onEnable() {
xLog.getRoot().setLevel(xLevel.FINEST);
xLog.getRoot().get("db").setLevel(xLevel.INFO);
		synchronized(lock) {
			if(isOk != null) {
				getServer().getConsoleSender().sendMessage(ChatColor.RED+"************************************");
				getServer().getConsoleSender().sendMessage(ChatColor.RED+"*** WebAPI is already running!!! ***");
				getServer().getConsoleSender().sendMessage(ChatColor.RED+"************************************");
				return;
			}
			isOk = false;
			if(instance == null)
				instance = this;
		}
		// load vault economy
		if(Plugins3rdParty.get().getEconomy() == null)
			log().info("Economy plugin not found");
		else
			log().info("Economy plugin found");
		// load world guard
		if(Plugins3rdParty.get().getWorldGuard() == null)
			log().info("WorldGuard plugin not found");
		else
			log().info("WorldGuard plugin found");

		// plugin version
		version = PluginVersion.get(this);
		version.update();
		// config.yml
		config = new webConfig(this);
		// connect to db
		dbKey = config.dbKey();
		// shared settings
		settings = SettingsManager.get(dbKey);
		// language
		lang.load(this, "en");

		// web link
		link = LinkManager.get(dbKey);
		// stand-alone web economy
		link.setEnabled(
			"economy",
			config.getBool(webConfig.PATH_Standalone_WebEconomy_Enabled)
		);
		// stand-alone web inventory
		link.setEnabled(
			"inventory",
			config.getBool(webConfig.PATH_Standalone_WebInventory_Enabled)
		);
		// stand-alone web permissions
		link.setEnabled(
			"perms",
			config.getBool(webConfig.PATH_Standalone_WebPermissions_Enabled)
		);
		// stand-alone web worldguard
		link.setEnabled(
			"worldguard",
			config.getBool(webConfig.PATH_Standalone_WebWorldGuard_Enabled)
		);

		isOk = true;








//		log = xLog.getRoot().get("WebAPI");
//		log.info("Loaded API "+this.getDescription().getVersion());
		try {
			MetricsManager.get(this).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// prepare to end
	@Override
	public void onDisable() {
		isOk = false;
		// stop schedulers
		try {
			Bukkit.getScheduler().cancelTasks(this);
		} catch (Exception ignore) {}
		// stop web link updates
		LinkManager.shutdown();


		isOk = null;
	}


	// get plugins/name/ dir
	public static String getPluginDir() {
		return getPluginDir(get());
	}
	public static String getPluginDir(Plugin plugin) {
		if(plugin == null) return null;
		return plugin.getDataFolder().toString();
	}
	// get plugins/ dir
	public static String getPluginsDir() {
		return getPluginsDir(get());
	}
	public static String getPluginsDir(Plugin plugin) {
		if(plugin == null) return null;
		return plugin.getDataFolder().getParentFile().toString();
	}


	// get objects
	public String dbKey() {
		return dbKey;
	}
	public dbQuery getDB() {
		return dbQuery.get(dbKey);
	}


	// logger
	private static volatile xLog _log = null;
	private static final Object logLock = new Object();
	public static xLog log() {
		if(_log == null) {
			synchronized(logLock) {
				if(_log == null)
					_log = xVars.log("WebAPI");
			}
		}
		return _log;
	}
	public static xLog log(String name) {
		return log().get(name);
	}
	public static void setLog(xLog log) {
		synchronized(logLock) {
			_log = log;
		}
	}


}
