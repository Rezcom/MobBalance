package rezcom.mobbalance.wolves;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rezcom.mobbalance.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class WolfCandleHandler implements Listener {

    public static final NamespacedKey CandleWolfID = new NamespacedKey(Main.thisPlugin,"CandleWolfID");

    // Prevent stacking
    public static final NamespacedKey CandleRandomID = new NamespacedKey(Main.thisPlugin, "CandleRandomID");

    public static final int minActivationLevel = 12;

    public static final TextComponent nameText = Component.text("Wolf Spirit's Primitive Protection Candle").color(TextColor.color(0xe3cfff)).decoration(TextDecoration.ITALIC,false);
    public static final ArrayList<Component> loreText = new ArrayList<>(Arrays.asList(
            Component.text("A primitive spiritual protection candle. Right click a").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text("wolf to bind/unbind it to this charm. Prevents selected").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text("wolf's death at the cost of " + minActivationLevel + " levels. Keep in").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text("inventory to use. Inactive under Level " + minActivationLevel + ".").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text(""),
            Component.text("Currently Selected Wolf:").color(TextColor.color(0xffffff)).decorate(TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC,false)
    ));

    public static final ArrayList<Component> flavorText = new ArrayList<>(Arrays.asList(
            Component.text(""),
            Component.text("Even wolf spirits are guided by scent. This charm").color(TextColor.color(0x338AF5)).decoration(TextDecoration.ITALIC,true),
            Component.text("guides them to protect your loved ones.").color(TextColor.color(0x338AF5)).decoration(TextDecoration.ITALIC,true)
    ));

    public static final PotionEffect maxResistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,200,254);
    public static final PotionEffect fireResistance = new PotionEffect(PotionEffectType.FIRE_RESISTANCE,200,0);
    public static final PotionEffect tempAbsorb = new PotionEffect(PotionEffectType.ABSORPTION,400,0);

    public static boolean isACandle(Material material){
        // In between 1125 and 1141 (inclusive)
        return Material.CANDLE.ordinal() <= material.ordinal() && material.ordinal() <= Material.BLACK_CANDLE.ordinal();
    }

    public static boolean isReviveCandle(ItemStack itemStack){
        if (itemStack == null || !isACandle(itemStack.getType())){
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        return itemPDC.has(CandleWolfID);
    }

    public static ItemStack generateCandle(){
        ItemStack itemStack = new ItemStack(Material.CANDLE);
        itemStack.addUnsafeEnchantment(Enchantment.DIG_SPEED,0);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(nameText);

        ArrayList<Component> newLore = new ArrayList<>(loreText);
        newLore.add(Component.text(""));
        newLore.add(WolfColorHandler.noWolfText);
        newLore.addAll(flavorText);
        itemMeta.lore(newLore);

        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        itemPDC.set(CandleWolfID, PersistentDataType.INTEGER, 0);

        // Prevent Candles from stacking
        Random random = new Random();
        itemPDC.set(CandleRandomID, PersistentDataType.INTEGER, random.nextInt());

        // Prevent player from placing Candles
        itemPDC.set(WolfGeneralHandler.unplaceableWolfItem, PersistentDataType.INTEGER, 0);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @EventHandler
    void onAnvilEvent(PrepareAnvilEvent event){
        AnvilInventory anvilInventory = event.getInventory();
        ItemStack first = anvilInventory.getFirstItem();
        ItemStack second = anvilInventory.getSecondItem();
        if (first == null || second == null){
            return;
        }
        if (isACandle(first.getType()) && !isReviveCandle(first) && first.getAmount() == 1 && second.getType() == Material.BONE && second.getAmount() == 1){
            anvilInventory.setRepairCost(20);
             event.setResult(generateCandle());
        }
    }

    @EventHandler
    void onPlayerRightClick(PlayerInteractEntityEvent event){
        if (!(event.getRightClicked() instanceof Wolf) || !(((Wolf) event.getRightClicked()).isTamed())){
            return;
        }

        Player player = event.getPlayer();
        EquipmentSlot equipmentSlot = event.getHand();
        ItemStack itemStack = null;
        if (equipmentSlot == EquipmentSlot.HAND){
            itemStack = player.getInventory().getItemInMainHand();
        } else if (equipmentSlot == EquipmentSlot.OFF_HAND){
            itemStack = player.getInventory().getItemInOffHand();
        } else {
            Main.logger.log(Level.WARNING, "Player right clicked a wolf with neither the main nor offhand? Wolf Protection Candle Error");
        }
        if (!isReviveCandle(itemStack)){return;}

        Wolf wolf = (Wolf) event.getRightClicked();
        DyeColor dyeColor = wolf.getCollarColor();
        int wolfID = WolfGeneralHandler.getWolfID(wolf);

        ItemMeta itemMeta = itemStack.getItemMeta();

        ArrayList<Component> newLore = new ArrayList<>(loreText);

        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        if (itemPDC.get(CandleWolfID, PersistentDataType.INTEGER) == wolfID){
            // Same Wolf - Unselect
            itemPDC.set(CandleWolfID, PersistentDataType.INTEGER, 0);

            newLore.add(Component.text(""));
            newLore.add(WolfColorHandler.noWolfText);
            itemMeta.displayName(nameText);
            itemStack.setType(Material.CANDLE);

            player.sendMessage(Component.text("Unselected ").color(TextColor.color(0xffffff)).append(
                    Component.text(wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(dyeColor))).append(
                            Component.text(" with the Protection Candle").color(TextColor.color(0xffffff)))));
        } else {
            // Different Wolf - Select
            itemPDC.set(CandleWolfID, PersistentDataType.INTEGER, wolfID);
            newLore.addAll(WolfColorHandler.generateSelectionLoreText(wolf));

            itemMeta.displayName(Component.text("Protection Candle - " + wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(dyeColor))).decoration(TextDecoration.ITALIC,false));
            itemStack.setType(Material.valueOf(dyeColor.name() + "_CANDLE"));

            player.sendMessage(Component.text("Selected ").color(TextColor.color(0xffffff)).append(
                    Component.text(wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(dyeColor))).append(
                            Component.text(" with the Protection Candle").color(TextColor.color(0xffffff)))));
        }
        newLore.addAll(flavorText);
        itemMeta.lore(newLore);
        itemStack.setItemMeta(itemMeta);

    }

    @EventHandler
    void onWolfDeath(EntityDeathEvent event){
        if (!(event.getEntity() instanceof Wolf) || !(((Wolf) event.getEntity()).isTamed())){
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        int wolfID = WolfGeneralHandler.getWolfID(wolf);

        World world = wolf.getWorld();
        List<Player> worldPlayers = world.getPlayers();

        boolean shouldRevive = false;
        Player revivePlayer = null;

        for (Player p : worldPlayers){
            PlayerInventory playerInventory = p.getInventory();
            if (p.getLevel() >= minActivationLevel){
                for (ItemStack itemStack : playerInventory){
                    if (isReviveCandle(itemStack)){
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
                        if (itemPDC.get(CandleWolfID, PersistentDataType.INTEGER) == wolfID){
                            shouldRevive = true;
                            revivePlayer = p;
                            break; // There's gotta be a cleaner way to do this lol
                        }
                    }
                }
            }
            if (shouldRevive){break;} // Seriously...
        }

        if (!shouldRevive){return;}

        // Revive that sucker!
        event.setCancelled(true);
        revivePlayer.setLevel(revivePlayer.getLevel() - minActivationLevel);

        wolf.setHealth(20.0);
        wolf.addPotionEffect(maxResistance);
        wolf.addPotionEffect(fireResistance);
        wolf.addPotionEffect(tempAbsorb);

        // Play scary noises
        world.playSound(wolf.getLocation(), Sound.ITEM_TRIDENT_THUNDER,2.5f,0.50f);
        world.playSound(wolf.getLocation(), Sound.ENTITY_WOLF_HOWL,0.4f,1.25f);
        world.playSound(wolf.getLocation(), Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, 2.0f, 1.3f);
        world.spawnParticle(Particle.ELECTRIC_SPARK,wolf.getLocation(),50);
        world.strikeLightning(wolf.getLocation());
        revivePlayer.sendMessage(Component.text(wolf.getName() + "'s").color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(wolf.getCollarColor()))).append(
                Component.text(" death was prevented by the spirits! Lost " + minActivationLevel + " levels.").color(TextColor.color(0x75ff9c))));
    }

}
