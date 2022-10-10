package rezcom.mobbalance.wolves.colors;

import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import rezcom.mobbalance.Main;
import rezcom.mobbalance.wolves.WolfDebugCommand;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

import javax.management.InstanceNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BrownWolfHandler implements Listener {

    public static final NamespacedKey inAirExtraDamage = new NamespacedKey(Main.thisPlugin, "inAirExtraDamage");

    private static final Map<Integer,Double> baseDamage = new HashMap<Integer,Double>(){{
        put(0, 1.0);
        put(1, 1.25);
        put(2, 1.25);
        put(3, 1.25);
        put(4, 1.25);
        put(5, 1.5);
        put(6, 1.5);
        put(7, 1.5);
        put(8, 1.5);
        put(9, 1.5);
        put(10,1.5);
        put(11,1.5);
        put(12,1.75);
    }};

    // Damage Resistance
    // Each double refers to how much damage is RESISTED, so 0.6 means the wolf resists
    // 60% of the damage; aka takes only 40% damage.
    private static final Map<Integer,Double> damageResist = new HashMap<Integer,Double>() {{
        put(0, 0.60);
        put(1, 0.60);
        put(2, 0.65);
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

    private static final Map<Integer,Double> throwChanceMap = new HashMap<Integer,Double>(){{
       put(0 ,0.05);
       put(1, 0.10);
       put(2, 0.15);
       put(3, 0.20);
       put(4, 0.30);
       put(5, 0.40);
       put(6, 0.50);
       put(7, 0.60);
       put(8, 0.65);
       put(9, 0.70);
       put(10, 0.72);
       put(11, 0.74);
       put(12, 0.76);
    }};

    @EventHandler
    void onWolfAttackEnemy(EntityDamageByEntityEvent event){
        if (!WolfGeneralHandler.isCorrectWolf(event.getDamager(), DyeColor.BROWN)){
            return;
        }

        Wolf wolf = (Wolf) event.getDamager();
        Entity victim = event.getEntity();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        Double throwChance = throwChanceMap.get(level);
        Random random = new Random();
        Double result = random.nextDouble();
        if (result <= throwChance){
            Vector victimCurVelocity = victim.getVelocity();
            Main.sendDebugMessage("Current Velocity: " + victimCurVelocity,WolfDebugCommand.wolfDebug);
            victim.setVelocity(victim.getLocation().getDirection().multiply(3).setY(2));
            Main.sendDebugMessage("New Velocity: " + victim.getVelocity(),WolfDebugCommand.wolfDebug);


            PersistentDataContainer victimPDC = victim.getPersistentDataContainer();
            victimPDC.set(inAirExtraDamage, PersistentDataType.INTEGER,1);

            Main.sendDebugMessage("Thrown in air", WolfDebugCommand.wolfDebug);

        }

        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * baseDamage.get(level));
    }

    @EventHandler
    void onWolfGetsHit(EntityDamageEvent event){
        if (!WolfGeneralHandler.isCorrectWolf(event.getEntity(),DyeColor.BROWN)){
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION){
            event.setCancelled(true);
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * (1 - damageResist.get(level)));
    }

    @EventHandler
    void EntityGetsHitInAir(EntityDamageEvent event){
        Entity entity = event.getEntity();
        PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
        double eventDamage = event.getDamage();

        if (entityPDC.get(inAirExtraDamage, PersistentDataType.INTEGER) == null){
            entityPDC.set(inAirExtraDamage, PersistentDataType.INTEGER, 0);
        } else if (entityPDC.get(inAirExtraDamage,PersistentDataType.INTEGER) == 1){
            //Main.sendDebugMessage("Event damage before: " + eventDamage, WolfDebugCommand.wolfDebug);
            int mult;
            if (entity.isOnGround()){
                // On the ground, only extra damage
                mult = 1;
            } else if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE){
                // Projectile damage in air, deal triple.
                mult = 3;
            } else{
                // Normal damage in air, deal double
                mult = 2;
            }
            event.setDamage((eventDamage + 2) * mult);
            //Main.sendDebugMessage("Extra damage mult: " + mult, WolfDebugCommand.wolfDebug);
            //Main.sendDebugMessage("Event damage after: " + eventDamage, WolfDebugCommand.wolfDebug);
        }
        entityPDC.set(inAirExtraDamage, PersistentDataType.INTEGER, 0);

    }
}
