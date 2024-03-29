package rezcom.mobbalance.wolves;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import rezcom.mobbalance.Main;
import rezcom.mobbalance.wolves.commands.WolfDebugCommand;

import java.util.*;
import java.util.logging.Level;

public class WolfColorHandler implements Listener {

    public static final Map<DyeColor, Double> wildProbs = new HashMap<DyeColor, Double>(){{
        // 81% of the time, it'll be one of these four.
        put(DyeColor.RED,0.2025);
        put(DyeColor.BLUE,0.2025);
        put(DyeColor.GREEN,0.2025);
        put(DyeColor.BROWN,0.2025);
        // 15% of the time, it'll be one of these three.
        put(DyeColor.YELLOW,0.05);
        put(DyeColor.BLACK,0.05);
        put(DyeColor.WHITE,0.05);
        // 4% of the time, it'll be one of these four.
        put(DyeColor.LIGHT_BLUE,0.01);
        put(DyeColor.GRAY,0.01);
        put(DyeColor.PURPLE,0.01);
        put(DyeColor.ORANGE,0.01);
    }};

    public static final Map<DyeColor, Material> favoriteItems = new HashMap<DyeColor, Material>(){{
        put(DyeColor.RED,Material.PORKCHOP);
        put(DyeColor.BLUE,Material.BEEF);
        put(DyeColor.GREEN,Material.CHICKEN);
        put(DyeColor.BROWN,Material.MUTTON);
        put(DyeColor.YELLOW,Material.PHANTOM_MEMBRANE);
        put(DyeColor.BLACK,Material.ROTTEN_FLESH);
        put(DyeColor.WHITE,Material.APPLE);
        put(DyeColor.LIGHT_BLUE,Material.SWEET_BERRIES);
        put(DyeColor.GRAY,Material.SALMON);
        put(DyeColor.PURPLE,Material.CARROT);
        put(DyeColor.ORANGE,Material.MELON);
        put(DyeColor.PINK,Material.AMETHYST_SHARD);
        put(DyeColor.CYAN,Material.GOLDEN_CARROT);
        put(DyeColor.LIGHT_GRAY,Material.RABBIT);
        put(DyeColor.LIME,Material.CHORUS_FRUIT);
        put(DyeColor.MAGENTA,Material.GLISTERING_MELON_SLICE);
    }};


    @EventHandler
    void onWolfTamed(EntityTameEvent event){
        if (!(event.getEntity() instanceof Wolf)){
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        wolf.setCollarColor(rollColor(wildProbs));

        PersistentDataContainer wolfPDC = wolf.getPersistentDataContainer();

        wolfPDC.set(WolfGeneralHandler.WolfLevel, PersistentDataType.INTEGER, 0);
        wolfPDC.set(WolfGeneralHandler.WolfEXP, PersistentDataType.INTEGER, 0);
    }

    private static boolean isValid(Map<DyeColor,Double> probMap){
        Double sum = 0.0;
        for (Map.Entry<DyeColor,Double> entry : probMap.entrySet()){
            sum += entry.getValue();
        }
        String resultSum = String.format("%.3f",sum);
        // Needs to be accurate within 3 decimal places.
        String correct = "1.000";
        return correct.equals(resultSum);
    }

    public static DyeColor rollColor(Map<DyeColor,Double> probMap){
        if (!(isValid(probMap))){
            Main.logger.log(Level.SEVERE, "Wolf spawn probability map that was passed was not valid! Sum of probabilities was not 1.0; check your code!!");
            return DyeColor.RED;
        }

        ArrayList<DyeColor> dyeColors = new ArrayList<>();
        ArrayList<Double> probabilities = new ArrayList<>();
        for (Map.Entry<DyeColor,Double> entry : probMap.entrySet()){
            dyeColors.add(entry.getKey());
            probabilities.add(entry.getValue());
        }
        Double base = 0.0;
        Random random = new Random();
        double result = random.nextDouble();
        Main.sendDebugMessage("WOLF DYE RESULT: " + result, WolfDebugCommand.wolfDebug);
        for (int i = 0; i < dyeColors.size(); i++){
            Main.sendDebugMessage("RESULT TO GO UNDER: " + (probabilities.get(i) + base), WolfDebugCommand.wolfDebug);
            if (result <= probabilities.get(i) + base){
                Main.sendDebugMessage("SUCCESS, returning " + dyeColors.get(i), WolfDebugCommand.wolfDebug);
                return dyeColors.get(i);
            }
            base += probabilities.get(i);
        }
        // All rolls failed. This should never happen.
        Main.logger.log(Level.SEVERE, "All probability rolls failed during DyeColor(). This should never happen!");
        return DyeColor.RED;
    }

    static List<Material> dyeMaterials = Arrays.asList(
            Material.RED_DYE,Material.BLUE_DYE,Material.GREEN_DYE,Material.BROWN_DYE,
            Material.YELLOW_DYE,Material.BLACK_DYE,Material.WHITE_DYE,
            Material.LIGHT_BLUE_DYE,Material.GRAY_DYE,Material.PURPLE_DYE,Material.ORANGE_DYE,
            Material.PINK_DYE,Material.CYAN_DYE,Material.LIGHT_GRAY_DYE,Material.LIME_DYE,Material.MAGENTA_DYE);

    @EventHandler
    void preventColorCollar(PlayerInteractEntityEvent event){
        if (!(event.getRightClicked() instanceof Wolf) || !(((Wolf) event.getRightClicked()).isTamed())){
            return;
        }

        Player player = event.getPlayer();

        Material itemInHand;
        if (event.getHand() == EquipmentSlot.HAND){
            itemInHand = player.getInventory().getItemInMainHand().getType();
        } else {
            itemInHand = player.getInventory().getItemInOffHand().getType();
        }
        if (dyeMaterials.contains(itemInHand)){
            event.setCancelled(true);
            Wolf wolf = (Wolf) event.getRightClicked();
            WolfDebugCommand.wolfDebugMessage(wolf, "Attempt to change the color of " + wolf.getName() + " was cancelled.");
        }
    }

    boolean hasParentsOfTheseColors(DyeColor first, DyeColor second, DyeColor color1, DyeColor color2){
        return (first == color1 && second == color2) || (first == color2 && second == color1);
    }

    // Returns the hybrid color between 2 colors if it exists. Returns null if it doesn't.
    DyeColor getHybridColor(DyeColor first, DyeColor second){
        if (hasParentsOfTheseColors(first, second, DyeColor.BLUE, DyeColor.WHITE)){
            return DyeColor.LIGHT_BLUE;
        } else if (hasParentsOfTheseColors(first, second, DyeColor.BLACK, DyeColor.WHITE)){
            return DyeColor.GRAY;
        } else if (hasParentsOfTheseColors(first, second, DyeColor.BLUE, DyeColor.RED)){
            return DyeColor.PURPLE;
        } else if (hasParentsOfTheseColors(first, second, DyeColor.RED, DyeColor.YELLOW)){
            return DyeColor.ORANGE;
        } else if (hasParentsOfTheseColors(first, second, DyeColor.RED, DyeColor.WHITE)){
            return DyeColor.PINK;
        } else if (hasParentsOfTheseColors(first, second, DyeColor.GREEN, DyeColor.BLUE)){
            return DyeColor.CYAN;
        } else if (hasParentsOfTheseColors(first, second, DyeColor.WHITE, DyeColor.GRAY)){
            return DyeColor.LIGHT_GRAY;
        } else if (hasParentsOfTheseColors(first, second, DyeColor.PURPLE, DyeColor.PINK)){
            return DyeColor.MAGENTA;
        } else if (hasParentsOfTheseColors(first, second, DyeColor.GREEN, DyeColor.WHITE)){
            return DyeColor.LIME;
        } else {
            return null;
        }
    }

    @EventHandler
    void onWolfBreed(EntityBreedEvent event){

        // When wolves breed, if there exists no hybrid between the parents' colors, then it's a 50/50
        // Otherwise, it's a 33/33/33 between the hybrid, and each of the parents' colors.

        if (!(event.getEntity() instanceof Wolf)){
            return;
        }
        Random random = new Random();

        Wolf father = (Wolf) event.getFather();
        Wolf mother = (Wolf) event.getMother();


        int fatherLevel = WolfGeneralHandler.getWolfLevel(father);
        int motherLevel = WolfGeneralHandler.getWolfLevel(mother);
        if (fatherLevel < 5 || motherLevel < 5){
            event.setCancelled(true);
            return;
        }

        Wolf child = (Wolf) event.getEntity();

        DyeColor hybrid = getHybridColor(father.getCollarColor(),mother.getCollarColor());
        double result = random.nextDouble();
        WolfDebugCommand.wolfDebugMessage(child, "RESULT: " + result);
        if (hybrid == null){
            // There's no hybrid. It's a 50/50.
            WolfDebugCommand.wolfDebugMessage(child, "There is no hybrid available for those 2 colors.");
            if (result <= 0.5){
                child.setCollarColor(father.getCollarColor());
            } else {
                child.setCollarColor(mother.getCollarColor());
            }
        } else {
            // There is a hybrid. It's a 33/33/33
            WolfDebugCommand.wolfDebugMessage(child, "There is a hybrid available.");
            if (result <= 0.33){
                child.setCollarColor(hybrid);
            } else if (result <= 0.66){
                child.setCollarColor(father.getCollarColor());
            } else {
                child.setCollarColor(mother.getCollarColor());
            }
        }

        PersistentDataContainer childPDC = child.getPersistentDataContainer();
        childPDC.set(WolfGeneralHandler.WolfLevel,PersistentDataType.INTEGER,0);
        childPDC.set(WolfGeneralHandler.WolfEXP,PersistentDataType.INTEGER,0);

    }


    // TEXT-RELEVANT METHODS - FOR WHEN COLOR MATTERS IN LORE TEXT AND SUCH //

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
        put(DyeColor.PURPLE, "Conscientious");
        put(DyeColor.ORANGE, "Brazen");
        put(DyeColor.PINK, "Reserved");
        put(DyeColor.CYAN, "Acquisitive");
        put(DyeColor.LIGHT_GRAY, "Anxious");
        put(DyeColor.LIME, "Mischievous");
        put(DyeColor.MAGENTA, "Gracious");
    }};
    public static final TextComponent noWolfText = Component.text("None. Right click a tamed wolf to select.").color(TextColor.color(0xffffff));

    public static ArrayList<Component> generateSelectionLoreText(Wolf wolf){
        int wolfID = WolfGeneralHandler.getWolfID(wolf);
        DyeColor dyeColor = wolf.getCollarColor();
        return new ArrayList<>(Arrays.asList(
                Component.text(wolf.getName()).color(TextColor.color(WolfColorHandler.dyeColorLightTextMap.get(dyeColor))).decoration(TextDecoration.ITALIC,false),
                Component.text(WolfColorHandler.dyeColorToStringMap.get(dyeColor)).color(TextColor.color(WolfColorHandler.dyeColorTextColorMap.get(dyeColor))).decoration(TextDecoration.ITALIC,false),
                Component.text(""),
                Component.text("ID: " + wolfID).color(TextColor.color(0xffffff)).decoration(TextDecoration.ITALIC,true)
        ));
    }



}
