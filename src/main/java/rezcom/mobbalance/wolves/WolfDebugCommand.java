package rezcom.mobbalance.wolves;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class WolfDebugCommand implements CommandExecutor {

    public static boolean wolfDebug = false;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        if (wolfDebug){
            sender.sendMessage("Wolf debug toggled off.");
            wolfDebug = false;
        } else {
            sender.sendMessage("Wolf debug toggled on. Players with bones in their mouths will see wolf debug messages. Toggle off by using command again.");
            wolfDebug = true;
        }
        return true;
    }

}
