package rezcom.mobbalance.wolves.commands;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import rezcom.mobbalance.Main;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

public class WolfEXPCommand implements CommandExecutor {

    // Command that sets the closest tamed wolf's EXP to a certain value.

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        if (!(sender.hasPermission("MobBalance.WolfEXP"))){
            sender.sendMessage("You do not have permission");
            return false;
        }
        if (!(sender instanceof Player)){
            sender.sendMessage("Only players can use this command.");
            return false;
        }
        if (args.length < 1 || args.length > 2){
            sender.sendMessage("Enter an EXP amount and LevelUp Option. Usage: /MBWolfEXP <XP Amount> [LevelUp]. Use LevelUp if you want the wolf to Level Up with the EXP change.");
            return false;
        }
        try {
            int exp = Integer.parseInt(args[0]);
            boolean levelup = args.length == 2 && args[1].equals("LevelUp");
            Player player = (Player) sender;
            Wolf closestWolf = WolfGeneralHandler.getClosestTamedWolf(player);
            if (closestWolf == null){
                sender.sendMessage("No tamed wolf was found nearby.");
                return false;
            }

            PersistentDataContainer closestPDC = closestWolf.getPersistentDataContainer();
            closestPDC.set(WolfGeneralHandler.WolfEXP, PersistentDataType.INTEGER,exp);
            if (levelup){
                closestPDC.set(WolfGeneralHandler.WolfLevel,PersistentDataType.INTEGER,WolfGeneralHandler.convertEXPtoLevel(exp));
            }


            sender.sendMessage("EXP change applied to " + closestWolf.getName());
            return true;
        } catch (NumberFormatException e){
            sender.sendMessage("EXP must be an integer.");
            return false;
        }
    }
}
