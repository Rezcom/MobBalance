package rezcom.mobbalance.wolves.colors;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import rezcom.mobbalance.wolves.WolfDebugCommand;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RedWolfHandler implements Listener {

    // Base Damage
    private static final Map<Integer,Double> baseDamage = new HashMap<Integer,Double>(){{
        put(0, 1.0);
        put(1, 1.0);
        put(2, 1.0);
        put(3, 1.25);
        put(4, 1.25);
        put(5, 1.5);
        put(6, 1.5);
        put(7, 1.5);
        put(8, 1.5);
        put(9, 1.5);
        put(10,1.75);
        put(11,1.75);
        put(12,1.75);
    }};

    // Damage Resistance
    // Each double refers to how much damage is RESISTED, so 0.6 means the wolf resists
    // 60% of the damage; aka takes only 40% damage.
    private static final Map<Integer,Double> damageResist = new HashMap<Integer,Double>(){{
        put(0, 0.60);
        put(1, 0.60);
        put(2, 0.60);
        put(3, 0.65);
        put(4, 0.65);
        put(5, 0.70);
        put(6, 0.70);
        put(7, 0.75);
        put(8, 0.75);
        put(9, 0.75);
        put(10,0.80);
        put(11,0.80);
        put(12,0.80);
    }};

    // Critical Damage - How much damage the wolf does upon getting a critical proc.
    private static final Map<Integer,Double> critDamage = new HashMap<Integer,Double>(){{
        put(0, 1.0);
        put(1, 1.25);
        put(2, 1.25);
        put(3, 1.5);
        put(4, 2.0);
        put(5, 2.25);
        put(6, 2.25);
        put(7, 2.5);
        put(8, 2.5);
        put(9, 2.5);
        put(10,2.5);
        put(11,2.5);
        put(12,2.5);
    }};

    // Critical Chance WITHOUT having nearby high level owner.
    private static final Map<Integer,Double> critChance = new HashMap<Integer,Double>(){{
        put(0, 0.0);
        put(1, 0.10);
        put(2, 0.10);
        put(3, 0.15);
        put(4, 0.20);
        put(5, 0.25);
        put(6, 0.25);
        put(7, 0.30);
        put(8, 0.30);
        put(9, 0.35);
        put(10,0.35);
        put(11,0.37);
        put(12,0.40);
    }};

    // Critical Chance WITH nearby high level owner.
    private static final Map<Integer,Double> critChanceWithOwner = new HashMap<Integer,Double>(){{
       put(0, 0.0);
       put(1, 0.15);
       put(2, 0.15);
       put(3, 0.20);
       put(4, 0.25);
       put(5, 0.30);
       put(6, 0.40);
       put(7, 0.50);
       put(8, 0.50);
       put(9, 0.50);
       put(10,0.50);
       put(11,0.50);
       put(12,0.50);
    }};

    // When the wolf attacks something.
    @EventHandler
    void onWolfAttackEnemy(EntityDamageByEntityEvent event){

        if (!(event.getDamager() instanceof Wolf) || !(((Wolf) event.getDamager()).isTamed()) || !(((Wolf) event.getDamager()).getCollarColor() == DyeColor.RED)){
            // Not a tamed, red wolf.
            return;
        }

        Wolf wolf = (Wolf) event.getDamager();
        int level = WolfGeneralHandler.getWolfLevel(wolf);
        boolean nearbyOwner = WolfGeneralHandler.isNearbyOwner(wolf,25, 32);

        Double actualCritChance = nearbyOwner ? critChanceWithOwner.get(level) : critChance.get(level);

        double eventDamage = event.getDamage();
        Random random = new Random();
        if (random.nextDouble() <= actualCritChance){
            // Crit lands
            event.setDamage(eventDamage * critDamage.get(level));
            WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + ": Crit Chance: " + actualCritChance + "; landed a crit and dealt " + event.getDamage());
        } else {
            // No crit
            event.setDamage(eventDamage * baseDamage.get(level));
            WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + ": Crit Chance: " + actualCritChance + "; missed a crit and dealt " + event.getDamage());
        }

    }

    // When a wolf gets attacked.
    @EventHandler
    void onWolfGetsHit(EntityDamageByEntityEvent event){

        if (!(event.getEntity() instanceof Wolf) || (event.getDamager() instanceof Player) || !(((Wolf) event.getEntity()).getCollarColor() == DyeColor.RED)){
            return;
        }
        Wolf wolf = (Wolf) event.getEntity();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * (1 - damageResist.get(level)));
        WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + " took " + event.getDamage() + " damage, and now has " + wolf.getHealth() + " HP.");
    }


}
