package rezcom.mobbalance.wolves.colors;

import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import rezcom.mobbalance.Main;
import rezcom.mobbalance.wolves.WolfDebugCommand;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

import java.util.*;

public class GreenWolfHandler implements Listener {

    // Base Damage
    private static final Map<Integer,Double> baseDamage = new HashMap<Integer,Double>(){{
        put(0, 1.0);
        put(1, 1.0);
        put(2, 1.0);
        put(3, 1.0);
        put(4, 1.0);
        put(5, 1.25);
        put(6, 1.25);
        put(7, 1.25);
        put(8, 1.25);
        put(9, 1.25);
        put(10,1.5);
        put(11,1.5);
        put(12,1.5);
    }};

    // Damage Resistance
    // 0.60 means 60% resist, so takes 40% damage
    private static final Map<Integer,Double> damageResist = new HashMap<Integer,Double>(){{
        put(0, 0.60);
        put(1, 0.60);
        put(2, 0.60);
        put(3, 0.60);
        put(4, 0.60);
        put(5, 0.60);
        put(6, 0.65);
        put(7, 0.65);
        put(8, 0.65);
        put(9, 0.70);
        put(10,0.70);
        put(11,0.70);
        put(12,0.75);
    }};

    // Speed Chance for the self buff
    private static final Map<Integer,Double> selfSpeedChanceMap = new HashMap<Integer,Double>(){{
        put(0, 0.10);
        put(1, 0.25);
        put(2, 0.25);
        put(3, 0.30);
        put(4, 0.35);
        put(5, 0.40);
        put(6, 0.45);
        put(7, 0.50);
        put(8, 0.50);
        put(9, 0.50);
        put(10,0.50);
        put(11,0.50);
        put(12,0.50);
    }};

    // Speed Amp for self buff
    private static final Map<Integer,Integer> speedAmpMap = new HashMap<Integer,Integer>(){{
       put(0,0);
       put(1,0);
       put(2,0);
       put(3,0);
       put(4,1);
       put(5,1);
       put(6,1);
       put(7,1);
       put(8,1);
       put(9,1);
       put(10,2);
       put(11,2);
       put(12,2);
    }};


    // Evade Chance
    private static final Map<Integer,Double> evadeChanceMap = new HashMap<Integer,Double>(){{
       put(0, 0.0);
       put(1, 0.04);
       put(2, 0.10);
       put(3, 0.16);
       put(4, 0.22);
       put(5, 0.28);
       put(6, 0.34);
       put(7, 0.40);
       put(8, 0.40);
       put(9, 0.40);
       put(10,0.45);
       put(11,0.45);
       put(12,0.45);

    }};

    // Chance to dash for players
    private static final Map<Integer,Double> dashChanceMap = new HashMap<Integer,Double>(){{
       put(0, 0.0);
       put(1, 0.02);
       put(2, 0.02);
       put(3, 0.04);
       put(4, 0.04);
       put(5, 0.06);
       put(6, 0.06);
       put(7, 0.08);
       put(8, 0.08);
       put(9, 0.08);
       put(10,0.10);
       put(11,0.10);
       put(12,0.10);

    }};

    private static final Set<UUID> currentlyDashingPlayers = new HashSet<>();
    private static final Set<UUID> currentlyInvulPlayers = new HashSet<>();


    // Green wolves get speed on attacking
    @EventHandler
    void onWolfAttackEnemy(EntityDamageByEntityEvent event){

        if (!WolfGeneralHandler.isCorrectWolf(event.getDamager(), DyeColor.GREEN)){
            return;
        }

        Wolf wolf = (Wolf) event.getDamager();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        Double selfSpeedChance = selfSpeedChanceMap.get(level);
        int selfSpeedAmp = speedAmpMap.get(level);
        Random random = new Random();

        WolfDebugCommand.wolfDebugMessage(wolf, "Attempting speed buff of chance " + selfSpeedChance + " with amp " + selfSpeedAmp);
        if (random.nextDouble() <= selfSpeedChance){
            wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,800,selfSpeedAmp));
            WolfDebugCommand.wolfDebugMessage(wolf, "Applied speed buff to self");
        }

        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * baseDamage.get(level));

    }

    // Green wolves evade hits
    @EventHandler
    void onWolfGetsHit(EntityDamageByEntityEvent event){
        if ((!WolfGeneralHandler.isCorrectWolf(event.getEntity(),DyeColor.GREEN)) || event.getDamager() instanceof Player){
            return;
        }
        Wolf wolf = (Wolf) event.getEntity();
        int level = WolfGeneralHandler.getWolfLevel(wolf);

        if (event.getDamager() instanceof LivingEntity){
            double evadeChance = evadeChanceMap.get(level);
            Random random = new Random();
            WolfDebugCommand.wolfDebugMessage(wolf, "Attempting to evade with chance of " + evadeChance);
            if (random.nextDouble() <= evadeChance){
                WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + " successfully evaded.");
                wolf.getWorld().playSound(wolf.getLocation(),Sound.ITEM_ARMOR_EQUIP_ELYTRA,1.2f,2.1f);
                event.setDamage(0.0);
                return;
            }
        }

        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * (1 - damageResist.get(level)));
        WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + " took " + event.getDamage() + " damage, and now has " + wolf.getHealth() + " HP.");

    }

    @EventHandler
    void onEnemyKilled(EntityDeathEvent event){
        if (event.getEntity().getKiller() == null){
            // No player was the killer
            return;
        }
        Player player = event.getEntity().getKiller();
        Wolf wolf = WolfGeneralHandler.isNearbyOwnedWolf(player,0, DyeColor.GREEN,32);
        if (wolf == null){
            if (WolfDebugCommand.wolfDebug){player.sendMessage("No wolf was found.");}
            return;
        }
        applyDashToPlayer(player);
    }

    // Players can dash upon striking an enemy if a green wolf is nearby
    @EventHandler
    void onPlayerStrike(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player)){
            return;
        }
        Player player = (Player) event.getDamager();
        Wolf wolf = WolfGeneralHandler.isNearbyOwnedWolf(player,0,DyeColor.GREEN,32);
        if (wolf == null){
            return;
        }
        Random random = new Random();
        int level = WolfGeneralHandler.getWolfLevel(wolf);
        double dashChance = dashChanceMap.get(level);
        if (random.nextDouble() <= dashChance){
            applyDashToPlayer(player);
        }
    }

    // Level 30+ players who do crits get a dash
    @EventHandler
    void onPlayerCrit(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player) || !(event.isCritical())){
            return;
        }

        Player player = (Player) event.getDamager();
        Wolf wolf = WolfGeneralHandler.isNearbyOwnedWolf(player,30,DyeColor.GREEN,32);
        if (wolf == null){
            if (WolfDebugCommand.wolfDebug){player.sendMessage("No wolf was found.");}
            return;
        }
        applyDashToPlayer(player);

    }

    // Player evades all damage if they are dashing.
    @EventHandler
    void onPlayerGetsHit(EntityDamageEvent event){
        if (!(event.getEntity() instanceof Player)){
            return;
        }

        Player player = (Player) event.getEntity();
        if (currentlyInvulPlayers.contains(player.getUniqueId())){
            event.setDamage(0.0);
            player.getWorld().playSound(player.getLocation(),Sound.ITEM_ARMOR_EQUIP_ELYTRA,1.2f,2.1f);
            if (WolfDebugCommand.wolfDebug){player.sendMessage("Evaded an attack!");}
        }

    }

    @EventHandler
    void onPlayerMove(PlayerMoveEvent event){
        if (currentlyDashingPlayers.isEmpty() || !currentlyDashingPlayers.contains(event.getPlayer().getUniqueId())){
            return;
        }
        Vector toVector = event.getTo().toVector();
        Vector fromVector = event.getFrom().toVector();
        Vector newVector = toVector.subtract(fromVector);

        Player player = event.getPlayer();
        Vector playerVelocity = player.getVelocity();
        playerVelocity.setX((newVector.getX() * 6.5) + playerVelocity.getX());
        playerVelocity.setZ((newVector.getZ() * 6.5) + playerVelocity.getZ());
        player.setVelocity(playerVelocity);
        currentlyDashingPlayers.remove(player.getUniqueId());
    }

    void applyDashToPlayer(Player player){

        currentlyInvulPlayers.add(player.getUniqueId());
        currentlyDashingPlayers.add(player.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run(){
                currentlyDashingPlayers.remove(player.getUniqueId());
                currentlyInvulPlayers.remove(player.getUniqueId());
                if (WolfDebugCommand.wolfDebug){player.sendMessage("Removed from hashset, no longer dashing");}
            }
        }.runTaskLater(Main.thisPlugin,15); // Dash Ends after this many ticks
    }
}
