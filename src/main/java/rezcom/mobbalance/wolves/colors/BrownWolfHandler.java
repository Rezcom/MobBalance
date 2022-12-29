package rezcom.mobbalance.wolves.colors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import rezcom.mobbalance.Main;
import rezcom.mobbalance.wolves.WolfEvalCandleHandler;
import rezcom.mobbalance.wolves.commands.WolfDebugCommand;
import rezcom.mobbalance.wolves.WolfGeneralHandler;

import java.util.*;

public class BrownWolfHandler implements Listener {

    public static final NamespacedKey inAirExtraDamage = new NamespacedKey(Main.thisPlugin, "inAirExtraDamage");

    private static final ArrayList<Material> diamondArmor = new ArrayList<>(Arrays.asList(
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS
    ));

    private static final ArrayList<Material> netheriteArmor = new ArrayList<>(Arrays.asList(
            Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS
    ));

    private static final Map<Integer,Double> baseDamage = new HashMap<Integer,Double>(){{
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
        put(10,1.25);
        put(11,1.25);
        put(12,1.25);
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
       put(10, 0.71);
       put(11, 0.72);
       put(12, 0.73);
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
        double result = random.nextDouble();
        if (result <= throwChance){
            Vector victimCurVelocity = victim.getVelocity();
            Main.sendDebugMessage("Current Velocity: " + victimCurVelocity,WolfDebugCommand.wolfDebug);
            victim.setVelocity(victim.getLocation().getDirection().multiply(3).setY(2));
            Main.sendDebugMessage("New Velocity: " + victim.getVelocity(),WolfDebugCommand.wolfDebug);


            PersistentDataContainer victimPDC = victim.getPersistentDataContainer();
            victimPDC.set(inAirExtraDamage, PersistentDataType.INTEGER,1);

            WolfEvalCandleHandler.broadcastCandleMessage(wolf, Component.text(wolf.getName()).color(TextColor.color(WolfEvalCandleHandler.dyeColorLightTextMap.get(DyeColor.BROWN))).append(
                    Component.text(" launched " + victim.getName() + " into the air!").color(TextColor.color(0x874f00))));

            //Main.sendDebugMessage("Thrown in air", WolfDebugCommand.wolfDebug);

            // Should armor be ripped off?
            if (random.nextDouble() <= 0.20 && (victim instanceof LivingEntity)){
                // Yessir!
                LivingEntity livingVictim = (LivingEntity) victim;
                EntityEquipment entityEquipment = livingVictim.getEquipment();
                double whichArmorResult = random.nextDouble();
                if (whichArmorResult <= 0.25){
                    // Remove helmet
                    ItemStack helmet = entityEquipment.getHelmet();
                    if (helmet == null || helmet.getType() == Material.AIR){event.setDamage(1.0);return;}
                    dropArmorItem(helmet,livingVictim);
                    entityEquipment.setHelmet(null);

                } else if (whichArmorResult <= 0.50){
                    // Remove chestplate
                    ItemStack chestplate = entityEquipment.getChestplate();
                    if (chestplate == null || chestplate.getType() == Material.AIR){event.setDamage(1.0);return;}

                    dropArmorItem(chestplate,livingVictim);
                    entityEquipment.setChestplate(null);

                } else if (whichArmorResult <= 0.75){
                    // Remove leggings
                    ItemStack leggings = entityEquipment.getLeggings();
                    if (leggings == null || leggings.getType() == Material.AIR){event.setDamage(1.0);return;}
                    dropArmorItem(leggings,livingVictim);
                    entityEquipment.setLeggings(null);

                } else {
                    // Remove boots
                    ItemStack boots = entityEquipment.getBoots();
                    if (boots == null || boots.getType() == Material.AIR){event.setDamage(1.0);return;}
                    dropArmorItem(boots, livingVictim);
                    entityEquipment.setBoots(null);
                }


            }


        }

        double eventDamage = event.getDamage();
        event.setDamage(eventDamage * baseDamage.get(level));
    }

    private static void dropArmorItem(ItemStack armorItem, LivingEntity defender){
        Random rand = new Random();
        if (armorItem == null || armorItem.getType() == Material.AIR){return;}
        if (!(defender instanceof Player)){
            playScaryNoises(defender);
            double dropChance = rand.nextDouble();
            double resultToGoUnder = 1.0;
            if (diamondArmor.contains(armorItem.getType())){
                resultToGoUnder = 0.005;
            } else if (netheriteArmor.contains(armorItem.getType())){
                resultToGoUnder = 0.0001;
            }
            if (dropChance <= resultToGoUnder){
                ItemStack itemToDrop = new ItemStack(armorItem.getType());
                ItemMeta itemMeta = itemToDrop.getItemMeta();
                Damageable damageable = (Damageable) itemMeta;
                int damage = itemToDrop.getType().getMaxDurability() - rand.nextInt(5) + 1;
                damageable.setDamage(damage);
                itemToDrop.setItemMeta(damageable);
                defender.getWorld().dropItemNaturally(defender.getLocation(),itemToDrop);
            }

        } else {
            Damageable damageable = (Damageable) armorItem.getItemMeta();
            armorItem.setItemMeta(damageable);
            defender.getWorld().dropItemNaturally(defender.getLocation(),armorItem);
            playScaryNoises(defender);
        }

    }

    public static void playScaryNoises(LivingEntity livingEntity){
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.35f, 0.55f);
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.35f, 0.55f);
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
