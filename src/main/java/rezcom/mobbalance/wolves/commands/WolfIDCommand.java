package rezcom.mobbalance.wolves.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.NotNull;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

public class WolfIDCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        if (!(sender.hasPermission("MobBalance.WolfID"))){
            sender.sendMessage("You do not have permission");
            return false;
        }
        if (!(sender instanceof Player)){
            sender.sendMessage("Only players can use this command.");
            return false;
        }

        Player player = (Player) sender;
        Wolf closestWolf = WolfGeneralHandler.getClosestTamedWolf(player);
        player.sendMessage(closestWolf.getName() + "'s ID: " + WolfGeneralHandler.getWolfID(closestWolf));
        return true;
    }

}
