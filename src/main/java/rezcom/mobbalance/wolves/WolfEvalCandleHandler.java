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
import org.bukkit.event.block.BlockPlaceEvent;
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

public class WolfEvalCandleHandler implements Listener {

    public static final NamespacedKey EvalCandleWolfID = new NamespacedKey(Main.thisPlugin,"EvalCandleWolfID");

    // This is to prevent candles targeted on the same wolf from stacking.
    public static final NamespacedKey EvalCandleRandomID = new NamespacedKey(Main.thisPlugin, "EvalCandleRandomID");

    public static final TextComponent nameText = Component.text("Wolf Evaluation Candle").color(TextColor.color(0xb0f7dd)).decoration(TextDecoration.ITALIC,false);
    public static final ArrayList<Component> loreText = new ArrayList<>(Arrays.asList(
            Component.text("Hold in hand to be notified about a selected wolf's").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text("performance. Right click a wolf to set this candle").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text("to it. Reports damage in/out, as well as critical hits,").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text("evades, effects, etc.").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text(""),
            Component.text("Currently Selected Wolf:").color(TextColor.color(0xffffff)).decorate(TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC,false)
    ));
    public static final TextComponent noWolfText = Component.text("None. Right click a tamed wolf to select.").color(TextColor.color(0xffffff));

    public static Random random = new Random();

    public static boolean isACandle(Material material){
        // In between 1125 and 1141 (inclusive)
        return Material.CANDLE.ordinal() <= material.ordinal() && material.ordinal() <= Material.BLACK_CANDLE.ordinal();
    }

    public static ItemStack generateNewCandle(){
        ItemStack itemStack = new ItemStack(Material.CANDLE);
        itemStack.addUnsafeEnchantment(Enchantment.DIG_SPEED,0);
        
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(nameText);

        ArrayList<Component> newLore = new ArrayList<>(loreText);
        newLore.add(Component.text(""));
        newLore.add(noWolfText);
        itemMeta.lore(newLore);

        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        itemPDC.set(EvalCandleWolfID, PersistentDataType.INTEGER, 0);

        // Prevent candles from stacking
        itemPDC.set(EvalCandleRandomID, PersistentDataType.INTEGER, random.nextInt());

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static boolean isEvalCandle(ItemStack itemStack){
        if (itemStack == null || !isACandle(itemStack.getType())){
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        return itemPDC.has(EvalCandleWolfID);
    }

    @EventHandler
    void onAnvilEvent(PrepareAnvilEvent event){
        AnvilInventory anvilInventory = event.getInventory();
        ItemStack first = anvilInventory.getFirstItem();
        ItemStack second = anvilInventory.getSecondItem();
        if (first == null || second == null){
            return;
        }

        if (isACandle(first.getType()) && !isEvalCandle(first) && first.getAmount() == 1 && second.getType() == Material.IRON_INGOT && second.getAmount() == 1){
            anvilInventory.setRepairCost(2);
            event.setResult(generateNewCandle());
        }
    }

    public static final Map<DyeColor,Integer> dyeColorTextColorMap = new HashMap<DyeColor, Integer>(){{
        put(DyeColor.BLACK, 0x454545);
        put(DyeColor.RED, 0xed3f34);
        put(DyeColor.GREEN, 0x83ad1c);
        put(DyeColor.BROWN, 0xa6693d);
        put(DyeColor.BLUE, 0x4953d1);
        put(DyeColor.PURPLE, 0x9f3ad6);
        put(DyeColor.CYAN, 0x169C9C);
        put(DyeColor.LIGHT_GRAY, 0xbababa);
        put(DyeColor.GRAY,0x747474);
        put(DyeColor.PINK, 0xF38BAA);
        put(DyeColor.LIME, 0x80C71F);
        put(DyeColor.YELLOW, 0xFED83D);
        put(DyeColor.LIGHT_BLUE, 0x40c9f5);
        put(DyeColor.MAGENTA, 0xeb5bdf);
        put(DyeColor.ORANGE, 0xF9801D);
        put(DyeColor.WHITE, 0xF9FFFE);
    }};

    public static final Map<DyeColor,Integer> dyeColorLightTextMap = new HashMap<DyeColor, Integer>(){{
        put(DyeColor.BLACK, 0xc7c7c7);
        put(DyeColor.RED, 0xffa49e);
        put(DyeColor.GREEN, 0xb2f590);
        put(DyeColor.BROWN, 0xb88763);
        put(DyeColor.BLUE, 0xbcbfeb);
        put(DyeColor.PURPLE, 0xeed2fc);
        put(DyeColor.CYAN, 0xbae8e8);
        put(DyeColor.LIGHT_GRAY, 0xe0e0e0);
        put(DyeColor.GRAY, 0xdbdbdb);
        put(DyeColor.PINK, 0xffd1df);
        put(DyeColor.LIME, 0xd7f2b1);
        put(DyeColor.YELLOW, 0xfff4c7);
        put(DyeColor.LIGHT_BLUE, 0xbfefff);
        put(DyeColor.MAGENTA, 0xf7cbf7);
        put(DyeColor.ORANGE, 0xfccca4);
        put(DyeColor.WHITE, 0xffffff);
    }};

    public static final Map<DyeColor,String> dyeColorToStringMap = new HashMap<DyeColor, String>(){{
       put(DyeColor.RED, "Courageous");
       put(DyeColor.BLUE, "Honorable");
       put(DyeColor.GREEN, "Clever");
       put(DyeColor.BROWN, "Determined");
       put(DyeColor.YELLOW, "Timid");
       put(DyeColor.BLACK, "Vindictive");
       put(DyeColor.WHITE, "Insightful");
       put(DyeColor.LIGHT_BLUE, "Cunning");
       put(DyeColor.GRAY, "Skeptical");
       put(DyeColor.PURPLE, "Idealistic");
       put(DyeColor.ORANGE, "Brazen");
       put(DyeColor.PINK, "Reserved");
       put(DyeColor.CYAN, "Acquisitive");
       put(DyeColor.LIGHT_GRAY, "Anxious");
       put(DyeColor.LIME, "Mischievous");
       put(DyeColor.MAGENTA, "Gracious");
    }};

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
            Main.logger.log(Level.WARNING, "Player right clicked a wolf with neither the main nor offhand? WolfEvalCandle Error");
        }
        if (!isEvalCandle(itemStack)){return;}

        Wolf wolf = (Wolf) event.getRightClicked();
        DyeColor dyeColor = wolf.getCollarColor();
        int wolfID = WolfGeneralHandler.getWolfID(wolf);

        ItemMeta itemMeta = itemStack.getItemMeta();

        ArrayList<Component> newLore = new ArrayList<>(loreText);
        newLore.addAll(new ArrayList<>(Arrays.asList(
            Component.text(wolf.getName()).color(TextColor.color(dyeColorLightTextMap.get(dyeColor))).decoration(TextDecoration.ITALIC,false),
            Component.text(dyeColorToStringMap.get(dyeColor)).color(TextColor.color(dyeColorTextColorMap.get(dyeColor))).decoration(TextDecoration.ITALIC,false),
            Component.text(""),
            Component.text("ID: " + wolfID).color(TextColor.color(0xffffff)).decoration(TextDecoration.ITALIC,true)
        )));

        itemMeta.lore(newLore);
        itemMeta.displayName(Component.text("Wolf Evaluation Candle - " + wolf.getName()).color(TextColor.color(dyeColorLightTextMap.get(dyeColor))).decoration(TextDecoration.ITALIC,false));

        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        itemPDC.set(EvalCandleWolfID, PersistentDataType.INTEGER, wolfID);

        itemStack.setItemMeta(itemMeta);
        itemStack.setType(Material.valueOf(dyeColor.name() + "_CANDLE"));

        player.sendMessage("Selected " + wolf.getName() + " with Evaluation Candle.");
    }

    public static void broadcastCandleMessage(Wolf wolf, Component message){

        // Message every player with a candle in hand targeting this wolf.

        World world = wolf.getWorld();
        int wolfID = WolfGeneralHandler.getWolfID(wolf);
        List<Player> players = world.getPlayers();
        for (Player player : players){

            ItemStack mainhand = player.getInventory().getItemInMainHand();
            ItemStack offhand = player.getInventory().getItemInOffHand();

            int mainStickID = 0;
            int offStickID = 0;

            if (isEvalCandle(mainhand)){
                ItemMeta mainMeta = mainhand.getItemMeta();
                PersistentDataContainer mainPDC = mainMeta.getPersistentDataContainer();
                mainStickID = mainPDC.get(EvalCandleWolfID, PersistentDataType.INTEGER);
            }

            if (isEvalCandle(offhand)){
                ItemMeta offMeta = offhand.getItemMeta();
                PersistentDataContainer offPDC = offMeta.getPersistentDataContainer();
                offStickID = offPDC.get(EvalCandleWolfID, PersistentDataType.INTEGER);
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
                broadcastCandleMessage(wolf,Component.text(wolf.getName()).color(TextColor.color(dyeColorLightTextMap.get(dyeColor))).append(
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
                broadcastCandleMessage(wolf,Component.text(wolf.getName()).color(TextColor.color(dyeColorLightTextMap.get(dyeColor))).append(
                        Component.text(" received " + eventDamage + " damage.").color(TextColor.color(0xffffff))));
            }

        },2L);

    }

    @EventHandler
    void onPlayerPlaceCandle(BlockPlaceEvent event){
        // Prevent a player from actually placing the eval candles
        ItemStack item = event.getItemInHand();
        if (isEvalCandle(item)){
            event.setCancelled(true);
        }
    }

}
