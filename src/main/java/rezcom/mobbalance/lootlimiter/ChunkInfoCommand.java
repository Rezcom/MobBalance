package rezcom.mobbalance.lootlimiter;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChunkInfoCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
		if (!(sender instanceof Player)){
			sender.sendMessage("Only players can execute this command.");
			return false;
		}

		Player player = (Player) sender;
		if (player.hasPermission("MobBalance.ChunkInfo")){
			NamespacedKey worldKey = player.getWorld().getKey();
			long chunkKey = player.getChunk().getChunkKey();
			player.sendMessage("=== CHUNK INFO ===\nWorld: " + worldKey +
					"\nChunk: " + chunkKey + "\nCurrent Counter: " +
					ChunkHandler.chunkCounters.get(worldKey).get(chunkKey));
			return true;
		} else {
			sender.sendMessage("You don't have permission");
			return false;
		}



	}
}
