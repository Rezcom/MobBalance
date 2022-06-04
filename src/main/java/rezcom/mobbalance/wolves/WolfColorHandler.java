package rezcom.mobbalance.wolves;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import rezcom.mobbalance.Main;

import java.util.*;
import java.util.logging.Level;

public class WolfColorHandler implements Listener {

    public static final Map<DyeColor, Double> wildProbs = new HashMap<DyeColor, Double>(){{
        // 84% of the time, it'll be one of these four.
        put(DyeColor.RED,0.21);
        put(DyeColor.BLUE,0.21);
        put(DyeColor.GREEN,0.21);
        put(DyeColor.BROWN,0.21);
        // 12% of the time, it'll be one of these three.
        put(DyeColor.YELLOW,0.04);
        put(DyeColor.BLACK,0.04);
        put(DyeColor.WHITE,0.04);
        // 4% of the time, it'll be one of these four.
        put(DyeColor.LIGHT_BLUE,0.01);
        put(DyeColor.GRAY,0.01);
        put(DyeColor.PURPLE,0.01);
        put(DyeColor.ORANGE,0.01);
    }};



    @EventHandler
    void onWolfTamed(EntityTameEvent event){
        if (!(event.getEntity() instanceof Wolf)){
            return;
        }

        Wolf wolf = (Wolf) event.getEntity();
        wolf.setCollarColor(rollColor(wildProbs));
        wolf.setMetadata("EXP", new FixedMetadataValue(Main.thisPlugin, 0));
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
            WolfHandler.wolfDebugMessage(wolf, "Attempt to change the color of " + wolf.getName() + " was cancelled.");
        }


    }

    static Material getFavoriteItem(DyeColor dyeColor){
        switch (dyeColor){
            case RED:
                return Material.PORKCHOP;
            case BLUE:
                return Material.BEEF;
            case GREEN:
                return Material.CHICKEN;
            case BROWN:
                return Material.MUTTON;
            case YELLOW:
                return Material.PHANTOM_MEMBRANE;
            case BLACK:
                return Material.ROTTEN_FLESH;
            case WHITE:
                return Material.APPLE;
            case LIGHT_BLUE:
                return Material.SWEET_BERRIES;
            case GRAY:
                return Material.SALMON;
            case PURPLE:
                return Material.CARROT;
            case ORANGE:
                return Material.MELON;
            case PINK:
                return Material.AMETHYST_SHARD;
            case CYAN:
                return Material.GOLDEN_CARROT;
            case LIGHT_GRAY:
                return Material.RABBIT;
            case LIME:
                return Material.CHORUS_FRUIT;
            case MAGENTA:
                return Material.GLISTERING_MELON_SLICE;
        }
        Main.logger.log(Level.WARNING, "Attempted to get the favorite item for a DyeColor that wasn't accounted for! Check your code!!");
        return Material.DIRT;
    }
}
