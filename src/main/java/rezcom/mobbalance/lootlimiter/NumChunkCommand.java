package rezcom.mobbalance.lootlimiter;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class NumChunkCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
		if (sender instanceof Player && !sender.hasPermission("MobBalance.NumChunk")){
			sender.sendMessage("You don't have permission.");
			return false;
		}
		sender.sendMessage("=== NUMBER OF CHUNKS ===");
		for (Map.Entry<NamespacedKey,Map<Long,Integer>> entryWorld : ChunkHandler.chunkCounters.entrySet()){
			sender.sendMessage(entryWorld.getKey() +
					"\nChunks: " + entryWorld.getValue().size());
		}
		return true;
	}
}
