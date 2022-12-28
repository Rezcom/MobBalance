package rezcom.mobbalance.wolves;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.units.qual.N;
import rezcom.mobbalance.Main;

import java.util.*;
import java.util.logging.Level;

public class WolfEvalStickHandler implements Listener {

    public static final NamespacedKey EvalStickWolfID = new NamespacedKey(Main.thisPlugin,"EvalStickWolfID");

    // This is to prevent sticks targeted on the same wolf from stacking.
    public static final NamespacedKey EvalStickRandomID = new NamespacedKey(Main.thisPlugin, "EvalStickRandomID");

    public static final TextComponent nameText = Component.text("Wolf Evaluation Stick").color(TextColor.color(0xb0f7dd)).decoration(TextDecoration.ITALIC,false);
    public static final ArrayList<Component> loreText = new ArrayList<>(Arrays.asList(
            Component.text("For use in evaluating wolves. Hold in hand to be notified").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text("about selected wolf's performance. Right click a wolf to").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text("to set the currently selected wolf of this stick.").color(TextColor.color(0xf7dbb0)).decoration(TextDecoration.ITALIC,false),
            Component.text(""),
            Component.text("Currently Selected Wolf:").color(TextColor.color(0xffffff)).decorate(TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC,false)
    ));
    public static final TextComponent noWolfText = Component.text("None. Right click a tamed wolf to select.").color(TextColor.color(0xffffff));

    public static Random random = new Random();

    public static ItemStack generateNewStick(){
        ItemStack itemStack = new ItemStack(Material.STICK);
        itemStack.addUnsafeEnchantment(Enchantment.DIG_SPEED,0);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(nameText);

        ArrayList<Component> newLore = new ArrayList<>(loreText);
        newLore.add(Component.text(""));
        newLore.add(noWolfText);
        itemMeta.lore(newLore);

        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        itemPDC.set(EvalStickWolfID, PersistentDataType.INTEGER, 0);

        // Prevent sticks from stacking
        itemPDC.set(EvalStickRandomID, PersistentDataType.INTEGER, random.nextInt());

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static boolean isEvalStick(ItemStack itemStack){
        if (itemStack == null || itemStack.getType() != Material.STICK){
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        return itemPDC.has(EvalStickWolfID);
    }

    @EventHandler
    void onAnvilEvent(PrepareAnvilEvent event){
        AnvilInventory anvilInventory = event.getInventory();
        ItemStack first = anvilInventory.getFirstItem();
        ItemStack second = anvilInventory.getSecondItem();
        if (first == null || second == null){
            return;
        }

        if (first.getType() == Material.STICK && !isEvalStick(first) && first.getAmount() == 1 && second.getType() == Material.IRON_INGOT && second.getAmount() == 1){
            anvilInventory.setRepairCost(2);
            event.setResult(generateNewStick());
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
       put(DyeColor.LIGHT_BLUE, "Arrogant");
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
            Main.logger.log(Level.WARNING, "Player right clicked a wolf with neither the main nor offhand? WolfEvalStick Error");
        }
        if (!isEvalStick(itemStack)){return;}

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
        itemMeta.displayName(Component.text("Wolf Evaluation Stick - " + wolf.getName()).color(TextColor.color(dyeColorLightTextMap.get(dyeColor))).decoration(TextDecoration.ITALIC,false));

        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();
        itemPDC.set(EvalStickWolfID, PersistentDataType.INTEGER, wolfID);

        itemStack.setItemMeta(itemMeta);

        player.sendMessage("Selected " + wolf.getName() + " with Evaluation Stick.");
    }
}
