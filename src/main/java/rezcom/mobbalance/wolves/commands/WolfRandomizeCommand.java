package rezcom.mobbalance.wolves.commands;

import org.bukkit.DyeColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.NotNull;
import rezcom.mobbalance.wolves.WolfColorHandler;

public class WolfRandomizeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        if (!(sender.hasPermission("MobBalance.WolfRandomize"))){
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
                        DyeColor dyeColor = WolfColorHandler.rollColor(WolfColorHandler.wildProbs);
                        wolf.setCollarColor(dyeColor);
                        sender.sendMessage(wolf.getName() + " was randomized to " + dyeColor);
                        count += 1;
                    }
                }
            }
            sender.sendMessage("Randomized " + count + " wolves.");
        } else {
            sender.sendMessage("You must be a player to do this.");
            return false;
        }
        return true;
    }
}
