package rezcom.mobbalance.wolves.colors;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import rezcom.mobbalance.Main;
import rezcom.mobbalance.wolves.commands.WolfDebugCommand;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class YellowWolfHandler implements Listener {

    private static final Map<Integer,Double> baseDamage = new HashMap<Integer, Double>(){{
        put(0, 1.0);
        put(1, 1.0);
        put(2, 1.0);
        put(3, 1.25);
        put(4, 1.25);
        put(5, 1.25);
        put(6, 1.25);
        put(7, 1.5);
        put(8, 1.5);
        put(9, 1.5);
        put(10,1.5);
        put(11,1.5);
        put(12,1.5);
    }};

    private static final Map<Integer,Double> damageResist = new HashMap<Integer,Double>(){{
        put(0, 0.60);
        put(1, 0.60);
        put(2, 0.60);
        put(3, 0.65);
        put(4, 0.65);
        put(5, 0.65);
        put(6, 0.70);
        put(7, 0.70);
        put(8, 0.70);
        put(9, 0.70);
        put(10,0.75);
        put(11,0.75);
        put(12,0.75);
    }};

    private static final Map<Integer,Double> critDamage = new HashMap<Integer,Double>(){{
        put(0, 1.0);
        put(1, 1.25);
        put(2, 1.25);
        put(3, 1.5);
        put(4, 2.0);
        put(5, 2.0);
        put(6, 2.25);
        put(7, 2.25);
        put(8, 2.5);
        put(9, 3.0);
        put(10, 3.5);
        put(11, 3.5);
        put(12, 3.5);
    }};

    private static final Map<Integer,Double> critChance = new HashMap<Integer,Double>(){{
       put(0, 0.0);
       put(1, 0.10);
       put(2, 0.15);
       put(3, 0.15);
       put(4, 0.20);
       put(5, 0.25);
       put(6, 0.25);
       put(7, 0.30);
       put(8, 0.35);
       put(9, 0.35);
       put(10, 0.40);
       put(11, 0.45);
       put(12, 0.50);
    }};


    @EventHandler
    void onWolfGetsHit(EntityDamageEvent event){
        if (!WolfGeneralHandler.isCorrectWolf(event.getEntity(), DyeColor.YELLOW)){
            return;
        }

        // Immune to lightning
        if (event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING || event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK){
            event.setCancelled(true);
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * (1 - damageResist.get(level)));
    }

    @EventHandler
    void onWolfAttackEnemy(EntityDamageByEntityEvent event){
        if (!WolfGeneralHandler.isCorrectWolf(event.getDamager(), DyeColor.YELLOW)){
            return;
        }

        Wolf wolf = (Wolf) event.getDamager();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        boolean thundering = wolf.getWorld().isThundering() && wolf.isInRain();

        double eventDamage = event.getDamage();
        if (thundering){
            Random random = new Random();
            double result = random.nextDouble();
            Main.sendDebugMessage("It's thundering! Result: " + result, WolfDebugCommand.wolfDebug);
            if (result <= critChance.get(level)){
                // STRIKE LIGHTNING AND DEAL CRITS!
                Main.sendDebugMessage("Was under " + critChance.get(level) + ", deal lightning!",WolfDebugCommand.wolfDebug);
                event.setDamage(eventDamage * critDamage.get(level));

                Entity victim = event.getEntity();
                victim.getWorld().strikeLightning(victim.getLocation());

                Main.sendDebugMessage("Dealt " + event.getDamage() + " damage.", WolfDebugCommand.wolfDebug);
            } else {
                // Crit didn't land
                event.setDamage(eventDamage * baseDamage.get(level));
                Main.sendDebugMessage("Wasn't underneath " + critChance.get(level) + ", dealing " + event.getDamage() + " damage.",WolfDebugCommand.wolfDebug);
            }
        } else {
            // Not thundering, deal normal damage
            event.setDamage(eventDamage * baseDamage.get(level));
            Main.sendDebugMessage("Not thundering. Dealing " + event.getDamage() + " damage.",WolfDebugCommand.wolfDebug);
        }
    }

    @EventHandler
    void feedEyeOfEnder(PlayerInteractEntityEvent event){
        if (!(WolfGeneralHandler.isCorrectWolf(event.getRightClicked(), DyeColor.YELLOW))){
            return;
        }
        Wolf wolf = (Wolf) event.getRightClicked();
        Player player = event.getPlayer();
        EquipmentSlot equipmentSlot = event.getHand();
        ItemStack itemStack = equipmentSlot == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        if (itemStack.getType() != Material.ENDER_EYE){
            return;
        }

        if (wolf.isInRain() && !wolf.getWorld().isThundering()){
            player.sendMessage(wolf.getName() + " has eaten the Eye. A thunderstorm has begun.");
            itemStack.subtract();
            player.getWorld().setThundering(true);
        }
    }


}
