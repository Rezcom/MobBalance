package rezcom.mobbalance.XPView;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XPViewCommand implements CommandExecutor {

	public static Map<UUID,Boolean> XPViewPlayers = new HashMap<>();

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can execute this command.");
			return false;
		}

		Player player = (Player) sender;
		if (player.hasPermission("MobBalance.XPView")) {
			if (XPViewPlayers.containsKey(player.getUniqueId())) {
				Boolean result = XPViewPlayers.get(player.getUniqueId());
				XPViewPlayers.replace(player.getUniqueId(), !result);
			} else {
				// Add it
				XPViewPlayers.put(player.getUniqueId(), true);
			}
			player.sendMessage("Toggled XP View");
			return true;
		} else {
			player.sendMessage("You don't have permission");
			return false;
		}
	}
}

