package rezcom.mobbalance.wolves.colors;

import org.bukkit.DyeColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rezcom.mobbalance.Main;
import rezcom.mobbalance.wolves.commands.WolfDebugCommand;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlackWolfHandler implements Listener {

    private static final Map<Integer,Double> baseDamage = new HashMap<Integer,Double>(){{
        put(0, 1.0);
        put(1, 1.0);
        put(2, 1.0);
        put(3, 1.25);
        put(4, 1.25);
        put(5, 1.25);
        put(6, 1.25);
        put(7, 1.25);
        put(8, 1.25);
        put(9, 1.25);
        put(10,1.25);
        put(11,1.25);
        put(12,1.25);
    }};

    private static final Map<Integer,Double> damageResist = new HashMap<Integer,Double>(){{
        put(0, 0.60);
        put(1, 0.60);
        put(2, 0.65);
        put(3, 0.65);
        put(4, 0.70);
        put(5, 0.70);
        put(6, 0.70);
        put(7, 0.75);
        put(8, 0.75);
        put(9, 0.80);
        put(10,0.80);
        put(11,0.85);
        put(12,0.85);
    }};

    private static final Map<Integer,Double> nightDamage = new HashMap<Integer,Double>(){{
        put(0, 1.25);
        put(1, 1.25);
        put(2, 1.5);
        put(3, 1.5);
        put(4, 1.5);
        put(5, 1.75);
        put(6, 1.75);
        put(7, 1.75);
        put(8, 2.0);
        put(9, 2.0);
        put(10,2.0);
        put(11,2.25);
        put(12,2.25);
    }};

    private static final Map<Integer,Double> nightCritDamage = new HashMap<Integer,Double>(){{
        put(0, 1.5);
        put(1, 1.5);
        put(2, 1.75);
        put(3, 1.75);
        put(4, 1.75);
        put(5, 2.0);
        put(6, 2.0);
        put(7, 2.0);
        put(8, 2.25);
        put(9, 2.25);
        put(10,2.25);
        put(11,2.5);
        put(12,2.5);
    }};

    private static final Map<Integer,Integer> witherLvlAmp = new HashMap<Integer,Integer>(){{
        put(0, 0);
        put(1, 0);
        put(2, 0);
        put(3, 0);
        put(4, 0);
        put(5, 0);
        put(6, 0);
        put(7, 1);
        put(8, 1);
        put(9, 1);
        put(10,1);
        put(11,1);
        put(12,2);
    }};

    @EventHandler
    void onWolfGetsHit(EntityDamageEvent event){
        if (!WolfGeneralHandler.isCorrectWolf(event.getEntity(), DyeColor.BLACK)){
            return;
        }

        // Immune to Wither
        if (event.getCause() == EntityDamageEvent.DamageCause.WITHER){
            event.setCancelled(true);
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * (1 - damageResist.get(level)));

        Main.sendDebugMessage("Damage Received: " + event.getDamage(),WolfDebugCommand.wolfDebug);

    }

    @EventHandler
    void onWolfAttackEnemy(EntityDamageByEntityEvent event){
        if (!WolfGeneralHandler.isCorrectWolf(event.getDamager(),DyeColor.BLACK)){
            return;
        }

        Wolf wolf = (Wolf) event.getDamager();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        boolean isNight = !wolf.getWorld().isDayTime();
        double eventDamage = event.getDamage();

        if (isNight){
            // It's night!
            Main.sendDebugMessage("It's nighttime!", WolfDebugCommand.wolfDebug);
            double health = wolf.getHealth();
            if (health < 10){
                // Crit
                Main.sendDebugMessage("Crit should be applied",WolfDebugCommand.wolfDebug);
                event.setDamage(eventDamage * nightCritDamage.get(level));
            } else {
                Main.sendDebugMessage("Crit shouldn't be applied",WolfDebugCommand.wolfDebug);
                event.setDamage(eventDamage * nightDamage.get(level));
            }

            // Check to apply wither
            boolean applyWither = false;
            UUID ownerID = wolf.getOwnerUniqueId();
            for (Player player : wolf.getLocation().getNearbyPlayers(8)){
                UUID playerID = player.getUniqueId();
                if (playerID.equals(ownerID)){
                    if (player.getHealth() < 7){applyWither = true;}
                }
            }

            if (applyWither){
                Main.sendDebugMessage("Wither should be applied",WolfDebugCommand.wolfDebug);
                LivingEntity victim = (LivingEntity) event.getEntity();
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,60,witherLvlAmp.get(level)));
            }

        } else {
            // It's day
            Main.sendDebugMessage("It's daytime",WolfDebugCommand.wolfDebug);
            event.setDamage(eventDamage * baseDamage.get(level));
        }

        Main.sendDebugMessage("Damage dealt: " + event.getDamage(),WolfDebugCommand.wolfDebug);

    }


}
