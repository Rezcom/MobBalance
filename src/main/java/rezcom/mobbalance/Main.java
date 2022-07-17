package rezcom.mobbalance;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import rezcom.mobbalance.moblevels.CreeperHandler;
import rezcom.mobbalance.moblevels.SkeletonHandler;
import rezcom.mobbalance.moblevels.SpiderHandler;
import rezcom.mobbalance.moblevels.ZombieHandler;
import rezcom.mobbalance.wolves.WolfColorHandler;
import rezcom.mobbalance.wolves.WolfDebugCommand;
import rezcom.mobbalance.wolves.WolfEXPCommand;
import rezcom.mobbalance.wolves.WolfGeneralHandler;
import rezcom.mobbalance.wolves.colors.RedWolfHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {


	public static Logger logger;
	public static Plugin thisPlugin;
	@Override
	public void onEnable() {
		// Plugin startup logic
		thisPlugin = this;

		logger = this.getLogger();
		logger.log(Level.INFO,"Initializing Plugin");

		this.saveDefaultConfig();

		// Register Events

		getServer().getPluginManager().registerEvents(new ZombieHandler(), this);
		getServer().getPluginManager().registerEvents(new SkeletonHandler(),this);
		getServer().getPluginManager().registerEvents(new SpiderHandler(),this);
		getServer().getPluginManager().registerEvents(new CreeperHandler(),this);

		// Wolf Event
		getServer().getPluginManager().registerEvents(new WolfGeneralHandler(), this);
		getServer().getPluginManager().registerEvents(new WolfColorHandler(),this);

		getServer().getPluginManager().registerEvents(new RedWolfHandler(),this);

		// Register Commands
		try {
			this.getCommand("MBWolfDebug").setExecutor(new WolfDebugCommand());
			this.getCommand("MBWolfEXP").setExecutor(new WolfEXPCommand());
		} catch (NullPointerException e){
			logger.log(Level.SEVERE,"Commands weren't initialized correctly! It returned a nullpointer exception.");
		}

		logger.log(Level.INFO,"Plugin Initialized");

	}

	public static void sendDebugMessage(String message, boolean send){
		if (send){
			logger.log(Level.INFO,message);
		}

	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
