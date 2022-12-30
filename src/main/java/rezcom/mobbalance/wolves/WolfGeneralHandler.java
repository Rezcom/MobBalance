package rezcom.mobbalance.wolves;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import rezcom.mobbalance.Main;
import rezcom.mobbalance.wolves.commands.WolfDebugCommand;

import java.util.*;
import java.util.logging.Level;

public class WolfGeneralHandler implements Listener {

    public static final NamespacedKey WolfLevel = new NamespacedKey(Main.thisPlugin, "WolfLevel");
    public static final NamespacedKey WolfEXP = new NamespacedKey(Main.thisPlugin, "WolfEXP");
    public static final NamespacedKey hurtByWolf = new NamespacedKey(Main.thisPlugin, "hurtByWolf");
    public static final NamespacedKey WolfID = new NamespacedKey(Main.thisPlugin, "WolfID");

    // Wolf Items that shouldn't be placed
    public static final NamespacedKey unplaceableWolfItem = new NamespacedKey(Main.thisPlugin,"unplaceableWolfItem");

    public static boolean wolfIDDebug = false;

    public static final Random random = new Random();

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
            WolfDebugCommand.wolfDebugMessage(wolf, wolf.getName() + " was attacked by " + player.getName() + ", but the damage was negated.");
        }
    }

    @EventHandler
    void onWolfHurtEnemy(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Wolf) || !(((Wolf) event.getDamager()).isTamed()) || (event.getEntity() instanceof Player) || !(event.getEntity() instanceof LivingEntity)){
            return;
        }

        LivingEntity victim = (LivingEntity) event.getEntity();
        PersistentDataContainer victimPDC = victim.getPersistentDataContainer();
        if (!victimPDC.has(hurtByWolf)){
            int[] initial = {};
            victimPDC.set(hurtByWolf,PersistentDataType.INTEGER_ARRAY,initial);
            Main.sendDebugMessage(victim.getName() + "'s hurtByWolfArray initialized.", wolfIDDebug);
        }

        Wolf wolf = (Wolf) event.getDamager();
        int wolfID = WolfGeneralHandler.getWolfID(wolf);
        Main.sendDebugMessage(wolf.getName() + "'s ID: " + wolfID,wolfIDDebug);

        int[] hurtByWolfArray = victimPDC.get(hurtByWolf,PersistentDataType.INTEGER_ARRAY);
        victimPDC.set(hurtByWolf,PersistentDataType.INTEGER_ARRAY,addToArrayIfNoIDExists(hurtByWolfArray,wolfID));

        if (victimPDC.get(hurtByWolf,PersistentDataType.INTEGER_ARRAY) == null){
            Main.sendDebugMessage("PDC was null?",wolfIDDebug);
        }

        if (wolfIDDebug){
            for (int i : Objects.requireNonNull(victimPDC.get(hurtByWolf, PersistentDataType.INTEGER_ARRAY))){
                Main.sendDebugMessage(victim.getName() + " hit by ID: " + i,true);
            }
        }

    }

    boolean idExists(int[] array, int ID){
        for (int i : array){
            if (i == ID){
                return true;
            }
        }
        return false;
    }

    int[] addToArrayIfNoIDExists(int[] array, int ID){
        if (idExists(array,ID)){
            return array;
        }
        int size = array.length;
        int[] newArray = new int[size + 1];
        System.arraycopy(array, 0, newArray, 0, size);
        newArray[size] = ID;
        return newArray;
    }

    @EventHandler
    void onEnemyDeathWolfEXP(EntityDeathEvent event){
        LivingEntity victim = event.getEntity();
        PersistentDataContainer victimPDC = victim.getPersistentDataContainer();
        if (!victimPDC.has(hurtByWolf)){
            // Wasn't hurt by a wolf
            return;
        }

        int[] hurtByArray = victimPDC.get(hurtByWolf,PersistentDataType.INTEGER_ARRAY);
        if (hurtByArray == null){
            Main.sendDebugMessage("On Enemy Death, hurtByWolf was null??",wolfIDDebug);
            return;
        }

        for (LivingEntity entity : victim.getLocation().getNearbyLivingEntities(16)){
            // Level up wolves
            if (entity instanceof Wolf){
                Wolf wolf = (Wolf) entity;
                if (!wolf.isTamed()){return;}
                if (idExists(hurtByArray,WolfGeneralHandler.getWolfID(wolf))){
                    increaseWolfEXP(wolf,1);
                }
            }
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
        Material material = null;
        if (equipmentSlot == EquipmentSlot.HAND){
            material = player.getInventory().getItemInMainHand().getType();
        } else if (equipmentSlot == EquipmentSlot.OFF_HAND){
            material = player.getInventory().getItemInOffHand().getType();
        } else {
            Main.logger.log(Level.WARNING,"Player right clicked a wolf with neither the main nor off hand? WolfGeneralHandler.CheckWolfStats error");
        }
        if (material == null){return;}

        Wolf wolf = (Wolf) event.getRightClicked();
        int level = getWolfLevel(wolf);
        int exp = getWolfEXP(wolf);
        DyeColor color = wolf.getCollarColor();

        if (material == Material.BONE){
            // Player right-clicked with a Bone.
            player.sendMessage( wolf.getName()+ " is a Level " + level + " " + wolf.getCollarColor() + " wolf, with " + exp + " total EXP.");
            if (level < 12 && exp >= wolfLevels.get(level + 1)){
                player.sendMessage( wolf.getName() + " is eager to Level up! Give them a " + WolfColorHandler.favoriteItems.get(color) + " soon!");
            }
            return;
        }

        Material favorite = WolfColorHandler.favoriteItems.get(color);
        if (exp >= wolfLevels.get(level + 1) && (material == favorite)){
            ItemStack itemStack;
            if (equipmentSlot == EquipmentSlot.HAND){
                itemStack = player.getInventory().getItemInMainHand();
            } else {
                itemStack = player.getInventory().getItemInOffHand();
            }
            itemStack.subtract();
            PersistentDataContainer wolfPDC = wolf.getPersistentDataContainer();
            wolfPDC.set(WolfLevel,PersistentDataType.INTEGER,level + 1);
            player.sendMessage(wolf.getName() + " leveled up! " + wolf.getName() + " is now level " + (level + 1) + "!");
        }
    }

    public static int getWolfLevel(Wolf wolf){
        // Returns the level of the Wolf.
        // If the wolf doesn't have a level or EXP, sets them to 0.

        PersistentDataContainer wolfPDC = wolf.getPersistentDataContainer();
        if (!wolfPDC.has(WolfLevel)){
            wolfPDC.set(WolfID, PersistentDataType.INTEGER, random.nextInt());
            wolfPDC.set(WolfLevel, PersistentDataType.INTEGER,0);
            wolfPDC.set(WolfEXP, PersistentDataType.INTEGER, 0);
            return 0;
        } else {
            return wolfPDC.get(WolfLevel, PersistentDataType.INTEGER);
        }

    }

    public static Wolf getClosestTamedWolf(Player player){
        Location playerLocation = player.getLocation();
        double shortest = 1000;
        Entity closestWolf = null;
        for (Entity entity : player.getNearbyEntities(10,10,10)){
            double cur_dist = playerLocation.distance(entity.getLocation());
            if ((entity instanceof Wolf) && (((Wolf) entity).isTamed()) && (cur_dist < shortest)){
                shortest = cur_dist;
                closestWolf = entity;
            }
        }
        return (Wolf) closestWolf;
    }

    public static int getWolfEXP(Wolf wolf){
        // Returns the EXP of the wolf.
        // If the wolf doesn't have a level or EXP, sets them to 0.
        PersistentDataContainer wolfPDC = wolf.getPersistentDataContainer();
        if (!wolfPDC.has(WolfLevel) || !wolfPDC.has(WolfEXP)){
            wolfPDC.set(WolfID, PersistentDataType.INTEGER, random.nextInt());
            wolfPDC.set(WolfLevel, PersistentDataType.INTEGER,0);
            wolfPDC.set(WolfEXP, PersistentDataType.INTEGER, 0);
            return 0;
        }

        return wolfPDC.get(WolfEXP, PersistentDataType.INTEGER);
    }

    public static int getWolfID(Wolf wolf){
        PersistentDataContainer wolfPDC = wolf.getPersistentDataContainer();
        if (!wolfPDC.has(WolfID)){
            wolfPDC.set(WolfID, PersistentDataType.INTEGER, random.nextInt());
        }
        return wolfPDC.get(WolfID, PersistentDataType.INTEGER);
    }

    public static void increaseWolfEXP(Wolf wolf, int amount){

        // Increases a Wolf's total EXP by amount passed

        PersistentDataContainer wolfPDC = wolf.getPersistentDataContainer();
        int curEXP = getWolfEXP(wolf);
        if (random.nextDouble() <= 0.04){
            wolfPDC.set(WolfEXP,PersistentDataType.INTEGER,curEXP + amount);
        }

    }

    public static int convertEXPtoLevel(int exp){
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

    // Surrounding the current wolf, applies a potionEffect to allies in the raidus.
    // includeWolves will include allied wolves.
    // includePlayers will apply the effect to players as well, but not the owner.
    // includeOwner includes the owner.

    public static void applyPackWithEffect(Wolf wolf, PotionEffect potionEffect, double radius, boolean includePlayers, boolean includeWolves, boolean includingOwner){
        String wolfName = wolf.getName();
        for (Entity entity : wolf.getNearbyEntities(radius,radius,radius)){
            if (entity instanceof Player && includePlayers){
                Player player = (Player) entity;
                if (wolf.getOwnerUniqueId() != null && player.getUniqueId() == wolf.getOwnerUniqueId()) {
                    // Owner found
                    if (includingOwner){
                        // Include Owner
                        player.addPotionEffect(potionEffect);
                        WolfDebugCommand.wolfDebugMessage(wolf,wolfName + " imbued " + player.getName() + " with " + potionEffect);
                    }
                } else {
                    // Not the owner
                    player.addPotionEffect(potionEffect);
                    WolfDebugCommand.wolfDebugMessage(wolf,wolfName + " imbued " + player.getName() + " with " + potionEffect);
                }
            } else if (entity instanceof Wolf && includeWolves){
                Wolf otherWolf = (Wolf) entity;
                if (otherWolf.isTamed()){
                    wolf.addPotionEffect(potionEffect);
                    WolfDebugCommand.wolfDebugMessage(wolf,wolfName + " imbued " + otherWolf.getName() + " with " + potionEffect);
                }
            }
        }
    }

    public static boolean isNearbyOwner(Wolf wolf, int minLevel, double radius){
        // Returns if the wolf is nearby its owner within a given radius, and the owner is at least the minimum level.

        if (!wolf.isTamed()){
            return false;
        }

        UUID ownerID = wolf.getOwnerUniqueId();

        for (Player player : wolf.getLocation().getNearbyPlayers(radius)){
            UUID playerID = player.getUniqueId();
            if (playerID.equals(ownerID)){
                WolfDebugCommand.wolfDebugMessage(wolf, "Found owner; level is " + player.getLevel() + " and minLevel is " + minLevel);
                return player.getLevel() >= minLevel;
            }
        }
        WolfDebugCommand.wolfDebugMessage(wolf, "Didn't find owner.");
        return false;
    }


    // Checks if a player owns a wolf nearby and is the correct level, and returns the wolf.
    // If not, returns null.
    public static List<Wolf> nearbyOwnedWolves(Player player, int minLevel, DyeColor dyeColor, int radius){
        List<Wolf> outputList = new ArrayList<>();
        if (player.getLevel() < minLevel){return outputList;}

        for (LivingEntity livingEntity : player.getLocation().getNearbyLivingEntities(radius)){
            if (isCorrectWolf(livingEntity,dyeColor)){
                // We found a tamed wolf of the correct color.
                Wolf wolf = (Wolf) livingEntity;
                if (wolf.getOwnerUniqueId() != null && wolf.getOwnerUniqueId().equals(player.getUniqueId())){
                    outputList.add(wolf);
                } else {
                    if (WolfDebugCommand.wolfDebug){Main.logger.log(Level.INFO,"No living entity nearby was a correct wolf.");}
                }
            }
        }
        return outputList;
    }

    public static boolean isCorrectWolf(Entity entity, DyeColor color){
        if (!(entity instanceof Wolf)){
            return false;
        }
        Wolf wolf = (Wolf) entity;
        return wolf.isTamed() && (wolf.getCollarColor() == color);
    }

    @EventHandler
    void onPlayerPlaceUnplaceable(BlockPlaceEvent event){
        // Prevent a player from actually placing the eval candles
        ItemStack item = event.getItemInHand();
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        if (itemPDC.has(unplaceableWolfItem)){
            event.setCancelled(true);
        }
    }

}
