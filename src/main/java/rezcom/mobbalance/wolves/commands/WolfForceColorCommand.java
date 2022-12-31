package rezcom.mobbalance.wolves.commands;

import org.bukkit.DyeColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.NotNull;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

public class WolfForceColorCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        if (!(sender.hasPermission("MobBalance.WolfForceColor"))){
            sender.sendMessage("You do not have permission");
            return false;
        }
        if (!(sender instanceof Player)){
            sender.sendMessage("Only players can use this command.");
            return false;
        }

        if (args.length < 1){
            sender.sendMessage("Enter a dye color to change the wolf's collar color.");
            return false;
        }

        Player player = (Player) sender;
        Wolf closestWolf = WolfGeneralHandler.getClosestTamedWolf(player);
        if (closestWolf == null){
            sender.sendMessage("No tamed wolf was found nearby.");
            return false;
        }

        try {
            closestWolf.setCollarColor(DyeColor.valueOf(args[0].toUpperCase()));
        } catch (IllegalArgumentException e){
            sender.sendMessage("Not a valid dye color.");
            return false;
        }
        return true;
    }
}
