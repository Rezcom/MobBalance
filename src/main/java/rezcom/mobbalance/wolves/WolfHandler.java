package rezcom.mobbalance.wolves;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.UUID;

public class WolfHandler implements Listener {

    public static boolean wolfDebug = false;

    @EventHandler
    void onWolfDealingDamage(EntityDamageByEntityEvent event){

        if (!(event.getDamager() instanceof Wolf) || !(event.getEntity() instanceof LivingEntity) || !(((Wolf) event.getDamager()).isTamed())){
            return;
        }

        Wolf wolf = (Wolf) event.getDamager();
        String wolfName = wolf.getName();

        LivingEntity damagedEntity = (LivingEntity) event.getEntity();
        String victimName = damagedEntity.getName();

        DyeColor dyeColor = wolf.getCollarColor();

        World world = wolf.getLocation().getWorld();

        double damage = event.getDamage();

        Random random = new Random();
        UUID uuid = wolf.getOwnerUniqueId();

        switch (dyeColor){
            case BLACK:
                // Increases damage done at nighttime
                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && owner.getHealth() <= 3 && owner.getLevel() >= 25 && !world.isDayTime()){
                        damagedEntity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,55,0));
                        wolfDebugMessage(wolf, wolfName + " applied Wither to " + victimName);
                    }
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && owner.getLevel() >= 30 && !world.isDayTime()){
                        damage = event.getDamage() * 2.5;
                    } else if (!world.isDayTime()){
                        damage = event.getDamage() * 1.5;
                    }
                } else if (!world.isDayTime()) {
                    damage = event.getDamage() * 1.5;
                }
                break;
            case BLUE:
                // Small chance to imbue owner player with resistance for a short duration.
                double chanceResult = random.nextDouble();
                PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,200,1);
                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner)){
                        if (chanceResult <= 0.33 && owner.getLevel() >= 25){
                            owner.addPotionEffect(resistance);
                            wolf.addPotionEffect(resistance);
                            wolfDebugMessage(wolf, wolfName + " applied Damage Resist to itself and " + owner.getName());
                        } else if (chanceResult <= 0.18){
                            owner.addPotionEffect(resistance);
                            wolf.addPotionEffect(resistance);
                            wolfDebugMessage(wolf, wolfName + " applied Damage Resist to itself and " + owner.getName());
                        }
                    }
                } else if (chanceResult <= 0.18){
                    wolf.addPotionEffect(resistance);
                    wolfDebugMessage(wolf, wolfName + " applied Damage Resist to itself.");
                }
                break;

            case CYAN:
                // Provides mending
                if (random.nextDouble() <= 0.14 && uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(32).contains(owner)){
                        owner.applyMending(50);
                        wolfDebugMessage(wolf, wolfName + " applied Mending to " + owner.getName());
                    }
                }
                break;

            case YELLOW:
                // Increases damage done during thunderstorms.

                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && world.isThundering()){
                        damage = event.getDamage() * 2.5;
                    }
                } else if (world.isThundering()){
                    damage = event.getDamage() * 1.5;
                }
                break;

            case LIME:
                // Chance to deal slow and poison.
                chanceResult = random.nextDouble();
                PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW,60,3);
                PotionEffect poison = new PotionEffect(PotionEffectType.POISON,60,0);
                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner)){
                        if (chanceResult <= 0.66 && owner.getLevel() >= 20){
                            damagedEntity.addPotionEffect(slowness);
                            damagedEntity.addPotionEffect(poison);
                            wolfDebugMessage(wolf, wolfName + " applied slowness and poison to " + victimName);
                        } else if (chanceResult <= 0.33){
                            damagedEntity.addPotionEffect(slowness);
                            damagedEntity.addPotionEffect(poison);
                            wolfDebugMessage(wolf, wolfName + " applied slowness and poison to " + victimName);
                        }
                    }
                } else if (chanceResult <= 0.33){
                    damagedEntity.addPotionEffect(slowness);
                    damagedEntity.addPotionEffect(poison);
                    wolfDebugMessage(wolf, wolfName + " applied slowness and poison to " + victimName);
                }
                break;

            case GREEN:
                wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,300,3));
                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && random.nextDouble() <= 0.20){
                        owner.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,100,1));
                        wolfDebugMessage(wolf, wolfName + " applied Speed to " + owner.getName());
                    }
                }


            case RED:
                // Red increases critical damage chance

                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && random.nextDouble() <= 0.50 && owner.getLevel() >= 20){
                        damage = event.getDamage() * 2.5;
                    } else {
                        damage = event.getDamage() * 1.5;
                    }
                } else {
                    if (random.nextDouble() <= 0.30){
                        damage = event.getDamage() * 2.5;
                    } else {
                        damage = event.getDamage() * 1.5;
                    }
                }


                break;
                // Does x2.375 on average
        }

        event.setDamage(damage);
        wolfDebugMessage(wolf, wolfName + " dealt " + event.getDamage() + " damage to " + victimName);
    }

    static void wolfDebugMessage(Wolf wolf, String message) {
        if (wolfDebug) {
            for (Player player : wolf.getLocation().getNearbyPlayers(1000)){
                if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.BONE){
                    player.sendMessage(message);
                }
            }
        }
    }

    @EventHandler
    void onWolfTakingDamage(EntityDamageEvent event){

        if (!(event.getEntity() instanceof Wolf) || !(((Wolf) event.getEntity()).isTamed())){
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        String wolfName = wolf.getName();
        DyeColor dyeColor = wolf.getCollarColor();

        event.setDamage(event.getDamage() * 0.20);

        Random random = new Random();

        UUID uuid = wolf.getOwnerUniqueId();

        switch (dyeColor){
            case BLACK:
                if (event.getCause() == EntityDamageEvent.DamageCause.WITHER){
                    event.setCancelled(true);
                }
                break;
            case BLUE:
            case CYAN:
                event.setDamage(event.getDamage() * 0.10);
                wolfDebugMessage(wolf, wolfName + " took 90% reduced damage.");
                break;
            case BROWN:
                if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION){
                    if (event.getDamage() <= 10){
                        wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,60,0));
                        wolfDebugMessage(wolf,wolfName + " deflected an explosion and was granted Strength I.");
                    } else if (event.getDamage() <= 15){
                        wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,60,1));
                        wolfDebugMessage(wolf,wolfName + " deflected an explosion and was granted Strength II.");
                    } else if (event.getDamage() <= 20){
                        wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,60,2));
                        wolfDebugMessage(wolf,wolfName + " deflected an explosion and was granted Strength III.");
                    } else if (event.getDamage() <= 25){
                        wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,60,3));
                        wolfDebugMessage(wolf,wolfName + " deflected an explosion and was granted Strength IV.");
                    } else if (event.getDamage() <= 30){
                        wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,60,4));
                        wolfDebugMessage(wolf,wolfName + " deflected an explosion and was granted Strength V.");
                    }
                    wolfDebugMessage(wolf,wolfName + " took no Explosion damage.");
                    event.setCancelled(true);
                }
                break;
            case LIME:
                if (event.getCause() == EntityDamageEvent.DamageCause.POISON){
                    wolfDebugMessage(wolf,wolfName + " took no Poison damage.");
                    event.setCancelled(true);
                }
                break;

            case GREEN:
                double chanceResult = random.nextDouble();
                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && owner.getLevel() >= 20 && chanceResult <= 0.40){
                        wolfDebugMessage(wolf,wolfName + " evaded damage.");
                        event.setCancelled(true);
                    } else if (chanceResult <= 0.20){
                        wolfDebugMessage(wolf,wolfName + " evaded damage.");
                        event.setCancelled(true);
                    }
                } else if (chanceResult <= 0.20){
                    wolfDebugMessage(wolf,wolfName + " evaded damage.");
                    event.setCancelled(true);
                }
                break;
        }
        if (!event.isCancelled()){
            wolfDebugMessage(wolf, wolfName + " took " + event.getDamage() + " damage, and now has " + wolf.getHealth() + " Health");
        }
    }

    @EventHandler
    void onPlayerHurtWolf(EntityDamageByEntityEvent event){

        // If a player attacks a wolf, the damage is negated and the player receives damage instead.

        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Wolf) || !(((Wolf) event.getEntity()).isTamed())){
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        Player player = (Player) event.getDamager();
        if (wolf.isTamed()){
            event.setCancelled(true);
            player.damage(0.5);
            wolfDebugMessage(wolf, wolf.getName() + " was attacked by " + player.getName() + ", but the damage was negated.");
        }

    }


}
