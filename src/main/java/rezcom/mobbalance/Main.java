package rezcom.mobbalance;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import rezcom.mobbalance.lootlimiter.*;

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
		getServer().getPluginManager().registerEvents(new ChunkHandler(),this);
		getServer().getPluginManager().registerEvents(new LootLimiter(),this);

		// Register Commands
		try {
			this.getCommand("MBNumChunk").setExecutor(new NumChunkCommand());
			this.getCommand("MBForcePurge").setExecutor(new ForcePurgeCommand());
			this.getCommand("MBChunkInfo").setExecutor(new ChunkInfoCommand());
		} catch (NullPointerException e){
			logger.log(Level.SEVERE,"Commands weren't initialized correctly! It returned a nullpointer exception.");
		}

		logger.log(Level.INFO,"Plugin Initialized");

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			@Override
			public void run(){
				logger.log(Level.INFO, "Purging Unloaded Chunks, Refreshing Chunks");
				ChunkHandler.purgeChunks(getServer());
				ChunkHandler.refreshAllChunks();
			}
		},0,20 * 60 * 30); // 20 Ticks per second, 60 seconds per minute
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
