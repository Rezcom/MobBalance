package rezcom.mobbalance.wolves;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import rezcom.mobbalance.Main;

import java.util.HashMap;
import java.util.Map;

public class WolfLevelHandler implements Listener {

    public static final Map<Integer,Integer> wolfLevels = new HashMap<Integer,Integer>(){{
        put(0,0);
        put(1,10);
        put(2,25);
        put(3,47);
        put(4,77);
        put(5,120);
        put(6,178);
        put(7,250);
        put(8,345);
        put(9,463);
        put(10,608);
        put(11,785);
        put(12,1000);
    }};

    static void wolfDebugMessage(Wolf wolf, String message) {
        if (WolfDebugCommand.wolfDebug) {
            for (Player player : wolf.getLocation().getNearbyPlayers(1000)){
                if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.BONE){
                    player.sendMessage(message);
                }
            }
        }
    }

    @EventHandler
    void onPlayerHurtWolf(EntityDamageByEntityEvent event){

        // If a player attacks a wolf they do not own, the damage is negated and the player receives damage instead.

        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Wolf) || !(((Wolf) event.getEntity()).isTamed())){
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        Player player = (Player) event.getDamager();
        if (wolf.isTamed() && (wolf.getOwnerUniqueId() != player.getUniqueId())){
            event.setCancelled(true);
            player.damage(0.5);
            wolfDebugMessage(wolf, wolf.getName() + " was attacked by " + player.getName() + ", but the damage was negated.");
        }
    }

    @EventHandler
    void checkWolfStats(PlayerInteractEntityEvent event){

        // When a player right-clicks a tamed wolf with a bone, it should tell the players its stats.

        if (!(event.getRightClicked() instanceof Wolf) || !(((Wolf) event.getRightClicked()).isTamed())){
            return;
        }

        Player player = event.getPlayer();
        EquipmentSlot equipmentSlot = event.getHand();
        if ((equipmentSlot == EquipmentSlot.HAND && player.getInventory().getItemInMainHand().getType() != Material.BONE)
                || (equipmentSlot == EquipmentSlot.OFF_HAND && player.getInventory().getItemInOffHand().getType() != Material.BONE)){
            return;
        }
        // Player right-clicked with a Bone.
        Wolf wolf = (Wolf) event.getRightClicked();
        if (!wolf.hasMetadata("EXP")){
            wolf.setMetadata("EXP",new FixedMetadataValue(Main.thisPlugin,0));
        }

        int exp = wolf.getMetadata("EXP").get(0).asInt();

        if (!wolf.hasMetadata("Level")){
            wolf.setMetadata("Level",new FixedMetadataValue(Main.thisPlugin,convertEXPtoLevel(exp)));
        }

        int level = wolf.getMetadata("Level").get(0).asInt();

        player.sendMessage( wolf.getName()+ " is a Level " + level + " " + wolf.getCollarColor() + " wolf, with " + exp + " total EXP.");
        if (level < 12 && exp >= wolfLevels.get(level + 1)){
            player.sendMessage( wolf.getName() + " is eager to Level up! Give them a " + WolfColorHandler.favoriteItems.get(wolf.getCollarColor()) + " soon!");
        }
    }

    static int convertEXPtoLevel(int exp){
        if (exp < wolfLevels.get(1)){
            return 0;
        } else if (exp < wolfLevels.get(2)){
            return 1;
        } else if (exp < wolfLevels.get(3)){
            return 2;
        } else if (exp < wolfLevels.get(4)){
            return 3;
        } else if (exp < wolfLevels.get(5)){
            return 4;
        } else if (exp < wolfLevels.get(6)){
            return 5;
        } else if (exp < wolfLevels.get(7)){
            return 6;
        } else if (exp < wolfLevels.get(8)){
            return 7;
        } else if (exp < wolfLevels.get(9)){
            return 8;
        } else if (exp < wolfLevels.get(10)){
            return 9;
        } else if (exp < wolfLevels.get(11)){
            return 10;
        } else if (exp < wolfLevels.get(12)){
            return 11;
        } else {
            return 12;
        }
    }


    static void applyPackWithEffect(Wolf wolf, PotionEffect potionEffect, double radius, boolean includePlayers, boolean includeWolves, boolean includingOwner){
        String wolfName = wolf.getName();
        for (Entity entity : wolf.getNearbyEntities(radius,radius,radius)){
            if (entity instanceof Player && includePlayers){
                Player player = (Player) entity;
                if (wolf.getOwnerUniqueId() != null && player.getUniqueId() == wolf.getOwnerUniqueId()) {
                    // Owner found
                    if (includingOwner){
                        // Include Owner
                        player.addPotionEffect(potionEffect);
                        wolfDebugMessage(wolf,wolfName + " imbued " + player.getName() + " with " + potionEffect);
                    }
                } else {
                    // Not the owner
                    player.addPotionEffect(potionEffect);
                    wolfDebugMessage(wolf,wolfName + " imbued " + player.getName() + " with " + potionEffect);
                }
            } else if (entity instanceof Wolf && includeWolves){
                Wolf otherWolf = (Wolf) entity;
                if (otherWolf.isTamed()){
                    wolf.addPotionEffect(potionEffect);
                    wolfDebugMessage(wolf,wolfName + " imbued " + otherWolf.getName() + " with " + potionEffect);
                }
            }
        }
    }

    /*
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
        double chanceResult;

        Random random = new Random();
        UUID uuid = wolf.getOwnerUniqueId();

        switch (dyeColor){
            case BLACK:
                // Increases damage done at nighttime
                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && owner.getHealth() <= 5 && owner.getLevel() >= 25 && !world.isDayTime()){
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
                chanceResult = random.nextDouble();
                PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,120,0);
                PotionEffect weakerResistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,60,0);
                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner)){
                        if (chanceResult <= 0.33 && owner.getLevel() >= 25){
                            owner.addPotionEffect(resistance);
                            wolf.addPotionEffect(resistance);
                            applyPackWithEffect(wolf,weakerResistance,16,true,true,false);
                            wolfDebugMessage(wolf, wolfName + " applied Damage Resist to itself and " + owner.getName());
                        } else if (chanceResult <= 0.18){
                            owner.addPotionEffect(resistance);
                            wolf.addPotionEffect(resistance);
                            applyPackWithEffect(wolf,weakerResistance,16,true,true,false);
                            wolfDebugMessage(wolf, wolfName + " applied Damage Resist to itself and " + owner.getName());
                        }
                    }
                } else if (chanceResult <= 0.18){
                    wolf.addPotionEffect(resistance);
                    applyPackWithEffect(wolf,weakerResistance,16,true,true,false);
                    wolfDebugMessage(wolf, wolfName + " applied Damage Resist to itself.");
                }
                break;

            case BROWN:
                damage = event.getDamage() * 1.25;
                break;

            case CYAN:
                // Provides mending
                if (random.nextDouble() <= 0.33 && uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(32).contains(owner)){
                        owner.applyMending(50);
                        wolfDebugMessage(wolf, wolfName + " applied Mending to " + owner.getName());
                    }
                }
                break;

            case YELLOW:
                // Increases damage done during thunderstorms.
                // Can cast lightning.
                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && world.isThundering()){
                        damage = event.getDamage() * 2.25;
                        if (random.nextDouble() <= 0.20){world.strikeLightning(damagedEntity.getLocation());}

                    }
                } else if (world.isThundering()){
                    damage = event.getDamage() * 1.5;
                    if (random.nextDouble() <= 0.20){world.strikeLightning(damagedEntity.getLocation());}
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
                damage = event.getDamage() * 1.25;
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
                damage = event.getDamage() * 1.25;
                break;

            case LIGHT_BLUE:
                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && world.isDayTime() && owner.getLevel() >= 25){
                        damage = event.getDamage() * 2;
                    } else if (world.isDayTime()){
                        damage = event.getDamage() * 1.5;
                    }
                } else if (world.isDayTime()) {
                    damage = event.getDamage() * 1.5;
                }
                break;

            case MAGENTA:
                PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 75,4);
                chanceResult = random.nextDouble();
                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && owner.getLevel() >= 20 && chanceResult <= 0.33){
                        damagedEntity.addPotionEffect(weakness);
                        wolfDebugMessage(wolf, wolfName + " applied Weakness to " + victimName);
                    } else if (chanceResult <= 0.166){
                        damagedEntity.addPotionEffect(weakness);
                        wolfDebugMessage(wolf, wolfName + " applied Weakness to " + victimName);
                    }
                } else if (chanceResult <= 0.166){
                    damagedEntity.addPotionEffect(weakness);
                    wolfDebugMessage(wolf, wolfName + " applied Weakness to " + victimName);
                }
                damage = event.getDamage() * 1.25;
                break;

            case ORANGE:
                chanceResult = random.nextDouble();
                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && owner.getLevel() >= 25 && chanceResult <= 0.40){
                        damagedEntity.setFireTicks(155);
                    } else if (chanceResult <= 0.20){
                        damagedEntity.setFireTicks(155);
                    }
                } else if (chanceResult <= 0.20){
                    damagedEntity.setFireTicks(155);
                }
                if (random.nextDouble() <= 0.33){
                    damage = event.getDamage() * 1.75;
                } else {
                    damage = event.getDamage() * 1.25;
                }
                break;

            case RED:
                // Red increases critical damage chance

                if (uuid != null){
                    Player owner = wolf.getServer().getPlayer(uuid);
                    if (owner != null && wolf.getLocation().getNearbyPlayers(64).contains(owner) && random.nextDouble() <= 0.50 && owner.getLevel() >= 25){
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
    }*/


    /*
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
        double chanceResult;

        UUID uuid = wolf.getOwnerUniqueId();

        switch (dyeColor){
            case BLACK:
                if (event.getCause() == EntityDamageEvent.DamageCause.WITHER){
                    event.setCancelled(true);
                }
                break;
            case BLUE:
            case LIGHT_GRAY:
            case CYAN:
                event.setDamage(event.getDamage() * 0.10);
                wolfDebugMessage(wolf, wolfName + " took 90% reduced damage.");
                break;
            case BROWN:

                if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION){
                    if (event.getDamage() <= 4){
                        wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,200,0));
                        wolfDebugMessage(wolf,wolfName + " deflected an explosion and was granted Strength I.");
                    } else if (event.getDamage() <= 8){
                        wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,200,1));
                        wolfDebugMessage(wolf,wolfName + " deflected an explosion and was granted Strength II.");
                    } else if (event.getDamage() <= 12){
                        wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,200,2));
                        wolfDebugMessage(wolf,wolfName + " deflected an explosion and was granted Strength III.");
                    } else if (event.getDamage() <= 16){
                        wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,200,3));
                        wolfDebugMessage(wolf,wolfName + " deflected an explosion and was granted Strength IV.");
                    } else if (event.getDamage() > 16){
                        wolf.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,200,4));
                        wolfDebugMessage(wolf,wolfName + " deflected an explosion and was granted Strength V.");
                    }

                    applyPackWithEffect(wolf,new PotionEffect(PotionEffectType.INCREASE_DAMAGE,200,0),32,false,true,false);
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
                chanceResult = random.nextDouble();
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

            case LIGHT_BLUE:
                // Negates fall damage.
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL){
                    event.setCancelled(true);
                    wolfDebugMessage(wolf, wolfName + " negated fall damage.");
                }
                break;

            case ORANGE:
                if (event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK){
                    event.setCancelled(true);
                    wolfDebugMessage(wolf, wolfName + " negated fire damage.");
                }


        }
        if (!event.isCancelled()){
            wolfDebugMessage(wolf, wolfName + " took " + event.getDamage() + " damage, and now has " + wolf.getHealth() + " Health");
        }
    }*/



    /*
    @EventHandler
    void onHighLevelMobSpawn(CreatureSpawnEvent event){

        // Light gray wolves can sense the spawn of high level creatures

        LivingEntity livingEntity = event.getEntity();
        for (Entity entity : livingEntity.getNearbyEntities(32,32,32)){
            if (entity instanceof Wolf){
                Wolf wolf = (Wolf) entity;
                if (wolf.isTamed() && wolf.getCollarColor() == DyeColor.LIGHT_GRAY){
                    boolean highLevel = false;
                    for (MetadataValue metadataValue : livingEntity.getMetadata("Level")){
                        if (metadataValue.asInt() >= 6){
                            highLevel = true;
                        }
                    }
                    if (livingEntity.getType() == EntityType.WITHER || livingEntity.getType() == EntityType.ENDER_DRAGON
                            || highLevel){
                        wolfDebugMessage(wolf, wolf.getName() + " has sensed a high level " + livingEntity.getName());
                        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,2400,0));
                    }

                }
            }
        }
    }*/

}
