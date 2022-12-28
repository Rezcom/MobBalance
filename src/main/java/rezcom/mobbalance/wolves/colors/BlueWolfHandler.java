package rezcom.mobbalance.wolves.colors;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rezcom.mobbalance.wolves.commands.WolfDebugCommand;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlueWolfHandler implements Listener {

    // Base Damage
    private static final Map<Integer,Double> baseDamage = new HashMap<Integer,Double>(){{
        put(0, 1.0);
        put(1, 1.0);
        put(2, 1.0);
        put(3, 1.0);
        put(4, 1.0);
        put(5, 1.0);
        put(6, 1.0);
        put(7, 1.25);
        put(8, 1.25);
        put(9, 1.25);
        put(10,1.25);
        put(11,1.25);
        put(12,1.25);
    }};

    // Damage Resist
    // 0.60 means resist 60% damage, so only take 40% damage
    private static final Map<Integer,Double> damageResist = new HashMap<Integer,Double>(){{
       put(0, 0.60);
       put(1, 0.70);
       put(2, 0.70);
       put(3, 0.75);
       put(4, 0.75);
       put(5, 0.80);
       put(6, 0.80);
       put(7, 0.85);
       put(8, 0.85);
       put(9, 0.85);
       put(10,0.90);
       put(11,0.90);
       put(12,0.90);
    }};

    // Chance to give resistance upon striking an enemy.
    private static final Map<Integer,Double> giveResistChance = new HashMap<Integer,Double>(){{
        put(0, 0.01);
        put(1, 0.10);
        put(2, 0.10);
        put(3, 0.12);
        put(4, 0.12);
        put(5, 0.14);
        put(6, 0.14);
        put(7, 0.17);
        put(8, 0.17);
        put(9, 0.18);
        put(10,0.18);
        put(11,0.19);
        put(12,0.19);
    }};

    // Amplifier for the resistance effect for a given level.
    private static final Map<Integer,Integer> resistAmplifier = new HashMap<Integer,Integer>(){{
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
       put(10,2);
       put(11,2);
       put(12,2);
    }};

    // Chance to give fire resist upon striking an enemy.
    private static final Map<Integer,Double> giveFireResistChance = new HashMap<Integer,Double>(){{
        put(0, 0.0);
        put(1, 0.01);
        put(2, 0.02);
        put(3, 0.03);
        put(4, 0.05);
        put(5, 0.07);
        put(6, 0.09);
        put(7, 0.11);
        put(8, 0.11);
        put(9, 0.11);
        put(10,0.12);
        put(11,0.12);
        put(12,0.12);
    }};

    // Chance to get absorption upon getting hit
    // if the player is the owner and is level 25+
    private static final Map<Integer,Double> absorbHitChance = new HashMap<Integer,Double>(){{
       put(0, 0.0);
       put(1, 0.02);
       put(2, 0.04);
       put(3, 0.06);
       put(4, 0.08);
       put(5, 0.10);
       put(6, 0.10);
       put(7, 0.10);
       put(8, 0.10);
       put(9, 0.10);
       put(10,0.10);
       put(11,0.10);
       put(12,0.10);
    }};

    private static final PotionEffect absorbEffect = new PotionEffect(PotionEffectType.ABSORPTION, 15, 0);

    @EventHandler
    void onWolfAttackEnemy(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Wolf) || !(((Wolf) event.getDamager()).isTamed()) || !(((Wolf) event.getDamager()).getCollarColor() == DyeColor.BLUE)){
            // Wasn't a Blue Tamed Wolf who attacked.
            return;
        }
        Wolf wolf = (Wolf) event.getDamager();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * baseDamage.get(level));

        Random random = new Random();

        double resistChance = giveResistChance.get(level);
        double fireChance = giveFireResistChance.get(level);
        int resistLevelAmplifier = resistAmplifier.get(level);

        WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + " attempt ResistChance of " + resistChance + " with an amplifier of " + resistLevelAmplifier);
        if (random.nextDouble() <= resistChance){
            // Wolf applies resistance to the pack.
            PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,100,resistLevelAmplifier);
            WolfGeneralHandler.applyPackWithEffect(wolf, resistance, 32, true, true, true);
            WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + " succeeded.");
        }

        WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + " attempt FireResist of " + fireChance);
        if (random.nextDouble() <= fireChance){
            // Wolf applies fire resistance to the pack.
            PotionEffect fireResist = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0);
            WolfGeneralHandler.applyPackWithEffect(wolf,fireResist,32,true,true,true);
            WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + " succeeded.");
        }
    }

    @EventHandler
    void onWolfGetsHit(EntityDamageByEntityEvent event){
        if (!(event.getEntity() instanceof Wolf) || !(((Wolf) event.getEntity()).isTamed()) || !(((Wolf) event.getEntity()).getCollarColor() == DyeColor.BLUE)){
            // Do nothing
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * (1 - damageResist.get(level)));
        WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + " took " + event.getDamage() + " damage, and now has " + wolf.getHealth() + " HP.");
    }

    @EventHandler
    void onPlayerGetsHit(EntityDamageByEntityEvent event){
        if (!(event.getEntity() instanceof Player)){
            return;
        }
        Player player = (Player) event.getEntity();
        Wolf wolf = WolfGeneralHandler.isNearbyOwnedWolf(player,25,DyeColor.BLUE,32);
        if (wolf == null){
            return;
        }
        int level = WolfGeneralHandler.getWolfLevel(wolf);
        double absorbChance = absorbHitChance.get(level);

        Random random = new Random();
        WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + " attempting absorption chance of " + absorbChance);
        if (random.nextDouble() <= absorbChance){
            // Successful Absorption
            player.addPotionEffect(absorbEffect);
            event.setDamage(0.0);
            WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + " was successful");
        }
    }

}
