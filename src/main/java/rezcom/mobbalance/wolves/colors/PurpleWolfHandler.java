package rezcom.mobbalance.wolves.colors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rezcom.mobbalance.wolves.WolfColorHandler;
import rezcom.mobbalance.wolves.WolfEvalSignetHandler;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PurpleWolfHandler implements Listener {

    public static Random random = new Random();
    // Base Damage
    private static final Map<Integer,Double> baseDamage = new HashMap<Integer, Double>(){{
        put(0, 1.0);
        put(1, 1.0);
        put(2, 1.0);
        put(3, 1.0);
        put(4, 1.0);
        put(5, 1.0);
        put(6, 1.0);
        put(7, 1.0);
        put(8, 1.0);
        put(9, 1.0);
        put(10,1.0);
        put(11,1.0);
        put(12,1.0);
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

    private static final Map<Integer,Double> buffDamageMap = new HashMap<Integer, Double>(){{
       put(0, 0.03);
       put(1, 0.04);
       put(2, 0.05);
       put(3, 0.06);
       put(4, 0.07);
       put(5, 0.08);
       put(6, 0.09);
       put(7, 0.10);
       put(8, 0.10);
       put(9, 0.10);
       put(10,0.11);
       put(11,0.11);
       put(12,0.12);
    }};

    private static final Map<Integer,Integer> hasteAmpMap = new HashMap<Integer, Integer>(){{
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

    private static final Map<Integer,Double> hasteChanceMap = new HashMap<Integer, Double>(){{
       put(0, 0.07);
       put(1, 0.09);
       put(2, 0.11);
       put(3, 0.13);
       put(4, 0.15);
       put(5, 0.17);
       put(6, 0.18);
       put(7, 0.19);
       put(8, 0.20);
       put(9, 0.21);
       put(10,0.22);
       put(11,0.23);
       put(12,0.24);
    }};

    @EventHandler
    void onWolfAttackEnemy(EntityDamageByEntityEvent event){
        if (!WolfGeneralHandler.isCorrectWolf(event.getDamager(), DyeColor.PURPLE)){
            return;
        }

        Wolf wolf = (Wolf) event.getDamager();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        double eventDamage = event.getDamage();

        // XYZ values are radius, in that they are half the size of bounding box in that direction
        // Intended bounding box is 24, the size of a chunk and a half
        List<Entity> nearbyEntities = wolf.getNearbyEntities(12,12,12);
        int allies = 0;
        for (Entity e : nearbyEntities){
            if (e instanceof Player || (e instanceof Wolf && ((Wolf) e).isTamed())){
                allies = allies + 1;
            }
        }

        if (allies > 0){
            WolfEvalSignetHandler.broadcastSignetMessage(wolf, Component.text(wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(DyeColor.PURPLE))).append(
                    Component.text(" dealt " + (buffDamageMap.get(level) * allies) + "x bonus damage. (" + buffDamageMap.get(level) + "x * " + allies + " nearby allies)").color(TextColor.color(0x8c00ff))));
        }

        if (random.nextDouble() <= hasteChanceMap.get(level)){
            int ampLevel = hasteAmpMap.get(level);
            PotionEffect haste = new PotionEffect(PotionEffectType.FAST_DIGGING,90,ampLevel);
            WolfGeneralHandler.applyPackWithEffect(wolf, haste, 16, true, true, true);
            WolfEvalSignetHandler.broadcastSignetMessage(wolf, Component.text(wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(DyeColor.PURPLE))).append(
                    Component.text(" applied Haste " + (ampLevel + 1) + " to the pack.").color(TextColor.color(0x8c00ff))));
        }

        event.setDamage(eventDamage * (baseDamage.get(level) + (buffDamageMap.get(level) * allies)));

    }

    @EventHandler
    void onWolfGetsHit(EntityDamageByEntityEvent event){
        if (!WolfGeneralHandler.isCorrectWolf(event.getEntity(),DyeColor.PURPLE)){
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * (1 - damageResist.get(level)));

    }
}
