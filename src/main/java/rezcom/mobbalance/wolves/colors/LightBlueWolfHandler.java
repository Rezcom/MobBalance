package rezcom.mobbalance.wolves.colors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import rezcom.mobbalance.wolves.WolfColorHandler;
import rezcom.mobbalance.wolves.WolfEvalSignetHandler;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

import java.util.*;

public class LightBlueWolfHandler implements Listener {

    public static final Random random = new Random();

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

    private static final Map<Integer,Integer> numOtherTargetsMap = new HashMap<Integer, Integer>(){{
       put(0,1);
       put(1,1);
       put(2,1);
       put(3,2);
       put(4,3);
       put(5,4);
       put(6,5);
       put(7,6);
       put(8,6);
       put(9,6);
       put(10,7);
       put(11,7);
       put(12,8);
    }};

    private static final Map<Integer,Double> tauntProbMap = new HashMap<Integer, Double>(){{
       put(0, 0.15);
       put(1, 0.20);
       put(2, 0.25);
       put(3, 0.30);
       put(4, 0.35);
       put(5, 0.40);
       put(6, 0.45);
       put(7, 0.50);
       put(8, 0.55);
       put(9, 0.60);
       put(10,0.65);
       put(11,0.70);
       put(12,0.75);
    }};

    @EventHandler
    void onWolfAttackEnemy(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Wolf) || !(WolfGeneralHandler.isCorrectWolf(event.getDamager(), DyeColor.LIGHT_BLUE))){
            return;
        }

        Wolf wolf = (Wolf) event.getDamager();
        Entity victim = event.getEntity();

        int level = WolfGeneralHandler.getWolfLevel(wolf);
        int numOtherTargets = numOtherTargetsMap.get(level);

        Collection<Monster> monsters = wolf.getLocation().getNearbyEntitiesByType(Monster.class,16);
        monsters.remove(victim);

        List<Monster> tauntedMonsters = new ArrayList<>();

        double result = random.nextDouble();
        double chance = tauntProbMap.get(level);

        for (int i = 0; i < numOtherTargets && !monsters.isEmpty() && result <= chance; i++){
            // Choose a random nearby monster
            Optional<Monster> chosenMonster = monsters.stream()
                    .skip((int) (monsters.size() * Math.random()))
                    .findFirst();
            if (chosenMonster.isPresent()){
                monsters.remove(chosenMonster.get());
                tauntedMonsters.add(chosenMonster.get());
            }
            chance = chance - 0.05;
        }



        for (Monster monster : tauntedMonsters){
            World world = victim.getWorld();
            Snowball ball = world.spawn(monster.getLocation().add(0,2.5,0), Snowball.class);
            ball.setHasLeftShooter(true);
            ball.setHasBeenShot(true);
            ball.setShooter((ProjectileSource) victim);
            ball.setVelocity(new Vector(0,-1.2f,0));
            ball.setSilent(true);

            WolfEvalSignetHandler.broadcastSignetMessage(wolf, Component.text(wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(DyeColor.LIGHT_BLUE))).append(
                    Component.text(" instigated " + victim.getName() + " towards " + victim.getName()).color(TextColor.color(0x3c85bd))));
        }
        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * baseDamage.get(level));

    }


    @EventHandler
    void onWolfGetsHit(EntityDamageByEntityEvent event){
        if (!WolfGeneralHandler.isCorrectWolf(event.getEntity(), DyeColor.LIGHT_BLUE)){
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        int level = WolfGeneralHandler.getWolfLevel(wolf);
        double eventDamage = event.getDamage();

        event.setDamage(eventDamage * damageResist.get(level));


    }

    @EventHandler
    void onWolfFallDamage(EntityDamageEvent event){
        if (!WolfGeneralHandler.isCorrectWolf(event.getEntity(), DyeColor.LIGHT_BLUE)){
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL){
            WolfEvalSignetHandler.broadcastSignetMessage(wolf, Component.text(wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(DyeColor.LIGHT_BLUE))).append(
                    Component.text(" avoided fall damage.").color(TextColor.color(0x3c85bd))));
            event.setCancelled(true);
        }
    }

}
