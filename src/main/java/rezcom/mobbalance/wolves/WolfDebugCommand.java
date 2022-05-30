package rezcom.mobbalance.wolves;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class WolfDebugCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        if (WolfHandler.wolfDebug){
            sender.sendMessage("Wolf debug toggled off.");
            WolfHandler.wolfDebug = false;
        } else {
            sender.sendMessage("Wolf debug toggled on. Players with bones in their mouths will see wolf debug messages. Toggle off by using command again.");
            WolfHandler.wolfDebug = true;
        }
        return true;
    }

}
