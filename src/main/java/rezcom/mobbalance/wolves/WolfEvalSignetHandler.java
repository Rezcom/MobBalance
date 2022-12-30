package rezcom.mobbalance.wolves;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import rezcom.mobbalance.Main;

import java.util.*;
import java.util.logging.Level;

public class WolfEvalSignetHandler implements Listener {

    public static final NamespacedKey EvalSignetWolfID = new NamespacedKey(Main.thisPlugin,"EvalSignetWolfID");

    // This is to prevent Signets targeted on the same wolf from stacking.
    public static final NamespacedKey EvalSignetRandomID = new NamespacedKey(Main.thisPlugin, "EvalSignetRandomID");

    public static final TextComponent nameText = Component.text("Wolf Evaluation Signet").color(TextColor.color(0xb0f7dd)).decoration(TextDecoration.ITALIC,false);
    public static final ArrayList<Component> loreText = new ArrayList<>(Arrays.asList(
            Component.text("Hold in hand to be notified about a selected wolf's").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text("performance. Right click a wolf to bind/unbind it to").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text("this Signet. Reports damage in/out, as well as critical").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text("hits, evades, effects, etc.").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text(""),
            Component.text("Currently Selected Wolf:").color(TextColor.color(0xffffff)).decorate(TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC,false)
    ));

    public static Random random = new Random();

    public static boolean isGlassPane(Material material){
        // Ordinals:
        // Glass pane 312
        // White: 439, Orange: 440, Magenta: 441, Light Blue: 442 ... Red: 453, Black: 454
        return (material.ordinal() == Material.GLASS_PANE.ordinal()) || (material.ordinal() >= Material.WHITE_STAINED_GLASS_PANE.ordinal() && material.ordinal() <= Material.BLACK_STAINED_GLASS_PANE.ordinal());
    }

    public static boolean isEvalSignet(ItemStack itemStack){
        if (itemStack == null || !isGlassPane(itemStack.getType())){
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        return itemPDC.has(EvalSignetWolfID);
    }

    public static ItemStack generateNewSignet(){
        ItemStack itemStack = new ItemStack(Material.GLASS_PANE);
        itemStack.addUnsafeEnchantment(Enchantment.DIG_SPEED,0);
        
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(nameText);

        ArrayList<Component> newLore = new ArrayList<>(loreText);
        newLore.add(Component.text(""));
        newLore.add(WolfColorHandler.noWolfText);
        itemMeta.lore(newLore);

        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        itemPDC.set(EvalSignetWolfID, PersistentDataType.INTEGER, 0);

        // Prevent Signets from stacking
        itemPDC.set(EvalSignetRandomID, PersistentDataType.INTEGER, random.nextInt());

        // Prevent players from placing evalSignets
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

        if (isGlassPane(first.getType()) && !isEvalSignet(first) && first.getAmount() == 1 && second.getType() == Material.BONE && second.getAmount() == 1){
            anvilInventory.setRepairCost(2);
            event.setResult(generateNewSignet());
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
            Main.logger.log(Level.WARNING, "Player right clicked a wolf with neither the main nor offhand? WolfEvalSignet Error");
        }
        if (!isEvalSignet(itemStack)){return;}

        Wolf wolf = (Wolf) event.getRightClicked();
        DyeColor dyeColor = wolf.getCollarColor();
        int wolfID = WolfGeneralHandler.getWolfID(wolf);

        ItemMeta itemMeta = itemStack.getItemMeta();

        ArrayList<Component> newLore = new ArrayList<>(loreText);

        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        if (itemPDC.get(EvalSignetWolfID, PersistentDataType.INTEGER) == wolfID){
            // Same wolf - Unselect
            itemPDC.set(EvalSignetWolfID, PersistentDataType.INTEGER, 0);

            newLore.add(Component.text(""));
            newLore.add(WolfColorHandler.noWolfText);
            itemMeta.displayName(nameText);
            itemStack.setType(Material.GLASS_PANE);

            player.sendMessage(Component.text("Unselected ").color(TextColor.color(0xffffff)).append(
                    Component.text(wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(dyeColor))).append(
                            Component.text(" with the Evaluation Signet").color(TextColor.color(0xffffff)))));
        } else {
            // Different Wolf - Select
            itemPDC.set(EvalSignetWolfID, PersistentDataType.INTEGER, wolfID);

            newLore.addAll(WolfColorHandler.generateSelectionLoreText(wolf));
            itemMeta.displayName(Component.text("Wolf Evaluation Signet - " + wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(dyeColor))).decoration(TextDecoration.ITALIC,false));
            itemStack.setType(Material.valueOf(dyeColor.name() + "_STAINED_GLASS_PANE"));

            player.sendMessage(Component.text("Selected ").color(TextColor.color(0xffffff)).append(
                    Component.text(wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(dyeColor))).append(
                            Component.text(" with the Evaluation Signet").color(TextColor.color(0xffffff)))));
        }

        itemMeta.lore(newLore);
        itemStack.setItemMeta(itemMeta);

    }

    public static void broadcastSignetMessage(Wolf wolf, Component message){

        // Message every player with a Signet in hand targeting this wolf.

        World world = wolf.getWorld();
        int wolfID = WolfGeneralHandler.getWolfID(wolf);
        List<Player> players = world.getPlayers();
        for (Player player : players){

            ItemStack mainhand = player.getInventory().getItemInMainHand();
            ItemStack offhand = player.getInventory().getItemInOffHand();

            int mainStickID = 0;
            int offStickID = 0;

            if (isEvalSignet(mainhand)){
                ItemMeta mainMeta = mainhand.getItemMeta();
                PersistentDataContainer mainPDC = mainMeta.getPersistentDataContainer();
                mainStickID = mainPDC.get(EvalSignetWolfID, PersistentDataType.INTEGER);
            }

            if (isEvalSignet(offhand)){
                ItemMeta offMeta = offhand.getItemMeta();
                PersistentDataContainer offPDC = offMeta.getPersistentDataContainer();
                offStickID = offPDC.get(EvalSignetWolfID, PersistentDataType.INTEGER);
            }

            if (mainStickID == wolfID || offStickID == wolfID){
                player.sendMessage(message);
            }


        }
    }

    @EventHandler
    void onWolfDealsDamage(EntityDamageByEntityEvent event){
        // When a wolf deals damage
        if (!(event.getDamager() instanceof Wolf) || !((Wolf) event.getDamager()).isTamed()){
            return;
        }
        Entity victim = event.getEntity();

        Wolf wolf = (Wolf) event.getDamager();
        DyeColor dyeColor = wolf.getCollarColor();

        Bukkit.getScheduler().runTaskLater(Main.thisPlugin,() -> {
            double eventDamage = Math.round(event.getDamage() * 100.0)/100.0;
            if (eventDamage > 0.0){
                broadcastSignetMessage(wolf,Component.text(wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(dyeColor))).append(
                        Component.text(" dealt " + eventDamage + " damage to " + victim.getName() + ".").color(TextColor.color(0xffffff))));

            }

        }, 2L);

    }

    @EventHandler
    void onWolfReceivesDamage(EntityDamageEvent event){
        if (!(event.getEntity() instanceof Wolf) || !((Wolf) event.getEntity()).isTamed()){
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        DyeColor dyeColor = wolf.getCollarColor();

        Bukkit.getScheduler().runTaskLater(Main.thisPlugin, () -> {
            double eventDamage = Math.round(event.getDamage() * 100.0)/100.0;
            if (eventDamage > 0.0){
                broadcastSignetMessage(wolf,Component.text(wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(dyeColor))).append(
                        Component.text(" received " + eventDamage + " damage.").color(TextColor.color(0xffffff))));
            }

        },2L);

    }

}
