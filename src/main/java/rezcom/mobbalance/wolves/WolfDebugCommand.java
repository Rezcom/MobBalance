package rezcom.mobbalance.wolves;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.NotNull;

public class WolfDebugCommand implements CommandExecutor {

    public static boolean wolfDebug = false;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        if (!(sender.hasPermission("MobBalance.WolfDebug"))){
            sender.sendMessage("You do not have permission.");
            return false;
        }
        if (wolfDebug){
            sender.sendMessage("Wolf debug toggled off.");
            wolfDebug = false;
        } else {
            sender.sendMessage("Wolf debug toggled on. Players with bones in their mouths will see wolf debug messages. Toggle off by using command again.");
            wolfDebug = true;
        }
        return true;
    }

    public static void wolfDebugMessage(Wolf wolf, String message) {
        if (wolfDebug) {
            for (Player player : wolf.getLocation().getNearbyPlayers(1000)){
                if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.BONE){
                    player.sendMessage(message);
                }
            }
        }
    }

}
