package rezcom.mobbalance.wolves.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

public class WolfIDRandomizeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        if (!(sender.hasPermission("MobBalance.WolfIDRandomize"))){
            sender.sendMessage("You do not have permission.");
            return false;
        }
        if (sender instanceof Player){
            Player player = (Player) sender;
            int count = 0;
            for (Entity e : player.getWorld().getEntities()){
                if (e instanceof Wolf){
                    Wolf wolf = (Wolf) e;
                    if (wolf.isTamed()){
                        PersistentDataContainer wolfPDC = wolf.getPersistentDataContainer();
                        wolfPDC.set(WolfGeneralHandler.WolfID, PersistentDataType.INTEGER,WolfGeneralHandler.random.nextInt());
                        count += 1;
                    }
                }
            }
            sender.sendMessage("Randomized " + count + " wolf ID's.");
        } else {
            sender.sendMessage("You must be a player to do this.");
            return false;
        }
        return true;
    }
}
