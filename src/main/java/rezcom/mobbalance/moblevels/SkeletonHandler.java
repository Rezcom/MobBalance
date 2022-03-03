package rezcom.mobbalance.moblevels;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rezcom.mobbalance.Main;

import java.util.Collections;
import java.util.Random;

public class SkeletonHandler implements Listener {

	public static boolean skeletonDebug = false;
	public static final Component decayIdentifier =
			Component.text("Decay Bow");

	public static final Component strongDecayIdentifier =
			Component.text("Strong Decay Bow");
	@EventHandler
	void onSkeletonSpawn(CreatureSpawnEvent event){
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM || !(MobLevelHandler.spawnReasons.contains(event.getSpawnReason()))){
			// If a plugin spawned it, don't bother.
			Main.sendDebugMessage("Wasn't a valid reason, it was: " + event.getSpawnReason(),skeletonDebug);
			return;
		}
		if (!(event.getEntity() instanceof Skeleton)){
			Main.sendDebugMessage("Not a skeleton",skeletonDebug);
			return;
		}

		Skeleton skeleton = (Skeleton) event.getEntity();
		Integer level;
		if (MobLevelHandler.crimsonNight){
			level = MobLevelHandler.rollProbability(MobLevelHandler.bloodProbs);
		} else {
			level = MobLevelHandler.rollProbability(MobLevelHandler.defaultProbs);
		}
		if (level == null){
			Main.sendDebugMessage("Skeleton level null oh no",skeletonDebug);
			return;
		}

		skeleton.setMetadata("Level",new FixedMetadataValue(Main.thisPlugin, level));
		EntityEquipment entityEquipment = skeleton.getEquipment();
		Random random = new Random();
		Main.sendDebugMessage("Spawning a Level " + level + " skeleton.",skeletonDebug);

		PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1);
		PotionEffect speedStrong = new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,2);
		PotionEffect fireResist = new PotionEffect(PotionEffectType.FIRE_RESISTANCE,Integer.MAX_VALUE,1);
		PotionEffect resistanceEffect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,Integer.MAX_VALUE,1);

		if (level == 1){
			ZombieHandler.setLevel1Armor(entityEquipment, random);
		} else if (level == 2){
			ZombieHandler.setLevel2Armor(entityEquipment, random);
		} else if (level == 3){
			ZombieHandler.setLevel3Armor(entityEquipment, random);
			setLevel3Weapons(entityEquipment, random);
		} else if (level == 4){
			ZombieHandler.setLevel4Armor(entityEquipment, random);
			setLevel3Weapons(entityEquipment, random);
		} else if (level == 5){
			ZombieHandler.setLevel4Armor(entityEquipment, random);
			setLevel5Weapons(entityEquipment, random);
		} else if (level == 6){
			setLevel6Weapons(entityEquipment, random);
			ZombieHandler.setLevel6Armor(entityEquipment, random);
			skeleton.addPotionEffect(speedEffect);
		} else if (level == 7){
			ZombieHandler.setLevel7Armor(entityEquipment, random);
			setLevel6Weapons(entityEquipment, random);
			skeleton.addPotionEffect(speedEffect);
		} else if (level == 8){
			ZombieHandler.setLevel7Armor(entityEquipment, random);
			skeleton.addPotionEffect(speedEffect);
			setLevel8Weapons(entityEquipment,random);
		} else if (level == 9){
			ZombieHandler.setLevel9Armor(entityEquipment,random);
			setLevel9Weapons(entityEquipment, random);
			skeleton.addPotionEffect(speedStrong);
		} else if (level == 10){
			ZombieHandler.setLevel10Armor(entityEquipment,random);
			setLevel10Weapon(entityEquipment, random);
			skeleton.addPotionEffect(speedStrong);
			skeleton.addPotionEffect(fireResist);
			skeleton.addPotionEffect(resistanceEffect);
		} else if (level == 11){
			ZombieHandler.setLevel11Armor(entityEquipment, random);
			setLevel10Weapon(entityEquipment, random);
			skeleton.addPotionEffect(speedStrong);
			skeleton.addPotionEffect(fireResist);
			skeleton.addPotionEffect(resistanceEffect);
		} else if (level == 12){
			ZombieHandler.setLevel11Armor(entityEquipment, random);
			setLevel12Weapon(entityEquipment, random);
			skeleton.addPotionEffect(speedStrong);
			skeleton.addPotionEffect(fireResist);
			skeleton.addPotionEffect(resistanceEffect);
		}
	}

	@EventHandler
	void onSkeletonShoot(EntityShootBowEvent event){
		ItemStack bow = event.getBow();
		Main.sendDebugMessage("Someone shot",skeletonDebug);
		if (bow == null || !bow.hasItemMeta()){return;}
		Main.sendDebugMessage("Bow is correct",skeletonDebug);
		ItemMeta bowMeta = bow.getItemMeta();
		if (!bowMeta.hasLore()){return;}
		Main.sendDebugMessage("Bow has lore",skeletonDebug);
		if (bow.lore().contains(decayIdentifier)){
			Main.sendDebugMessage("Setting projectile",skeletonDebug);
			Arrow arrow = (Arrow) event.getProjectile();
			PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS,200,1);
			arrow.addCustomEffect(weakness,true);
			event.setProjectile(arrow);
		} else if (bow.lore().contains(strongDecayIdentifier)){
			Main.sendDebugMessage("Setting stronger",skeletonDebug);
			Arrow arrow = (Arrow) event.getProjectile();
			PotionEffect strongWeakness = new PotionEffect(PotionEffectType.WEAKNESS,200,2);
			arrow.addCustomEffect(strongWeakness,true);
			event.setProjectile(arrow);
		}
	}

	public static void setLevel3Weapons(EntityEquipment entityEquipment, Random random){
		entityEquipment.setItemInMainHandDropChance(0.05f);
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta bowMeta = bow.getItemMeta();
		bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK,1,true);
		bow.setItemMeta(bowMeta);
		entityEquipment.setItemInMainHand(bow);
	}

	public static void setLevel5Weapons(EntityEquipment entityEquipment, Random random){
		entityEquipment.setItemInMainHandDropChance(0.0f);
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta bowMeta = bow.getItemMeta();

		bowMeta.lore(Collections.singletonList(decayIdentifier));

		bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK,random.nextDouble() <= 0.80 ? 1 : 2,true);
		bowMeta.addEnchant(random.nextDouble() <= 0.5 ? Enchantment.ARROW_DAMAGE : Enchantment.ARROW_FIRE, 1, true);
		bow.setItemMeta(bowMeta);
		entityEquipment.setItemInMainHand(bow);
	}

	public static void setLevel6Weapons(EntityEquipment entityEquipment, Random random){
		entityEquipment.setItemInMainHandDropChance(0.0f);
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta bowMeta = bow.getItemMeta();

		bowMeta.lore(Collections.singletonList(decayIdentifier));

		bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, random.nextDouble() <= 0.66 ? 1 : 2, true);
		bowMeta.addEnchant(Enchantment.ARROW_DAMAGE, random.nextDouble() <= 0.66 ? 1 : 2, true);
		if (random.nextDouble() <= 0.25){
			bowMeta.addEnchant(Enchantment.ARROW_FIRE, random.nextDouble() <= 0.33 ? 1 : 2,true);
		}
		bow.setItemMeta(bowMeta);
		entityEquipment.setItemInMainHand(bow);
	}

	public static void setLevel8Weapons(EntityEquipment entityEquipment, Random random){
		entityEquipment.setItemInMainHandDropChance(0.0f);
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta bowMeta = bow.getItemMeta();

		bowMeta.lore(Collections.singletonList(strongDecayIdentifier));

		bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, random.nextDouble() <= 0.66 ? 1 : 2,true);
		bowMeta.addEnchant(Enchantment.ARROW_DAMAGE, random.nextDouble() <= 0.66 ? 1 : 2,true);
		bowMeta.addEnchant(Enchantment.ARROW_FIRE, 1,true);
		bow.setItemMeta(bowMeta);
		entityEquipment.setItemInMainHand(bow);
	}

	public static void setLevel9Weapons(EntityEquipment entityEquipment, Random random){
		entityEquipment.setItemInMainHandDropChance(0.0f);
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta bowMeta = bow.getItemMeta();

		bowMeta.lore(Collections.singletonList(strongDecayIdentifier));

		bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, random.nextDouble() <= 0.66 ? 2 : 3,true);
		bowMeta.addEnchant(Enchantment.ARROW_DAMAGE, random.nextDouble() <= 0.66 ? 2 : 3,true);
		bowMeta.addEnchant(Enchantment.ARROW_FIRE, 1,true);
		bow.setItemMeta(bowMeta);
		entityEquipment.setItemInMainHand(bow);
	}

	public static void setLevel10Weapon(EntityEquipment entityEquipment, Random random){
		entityEquipment.setItemInMainHandDropChance(0.0f);
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta bowMeta = bow.getItemMeta();

		bowMeta.lore(Collections.singletonList(strongDecayIdentifier));

		bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 3,true);
		bowMeta.addEnchant(Enchantment.ARROW_DAMAGE, random.nextDouble() <= 0.66 ? 3 : 4,true);
		bowMeta.addEnchant(Enchantment.ARROW_FIRE, 1,true);
		bow.setItemMeta(bowMeta);
		entityEquipment.setItemInMainHand(bow);
	}

	public static void setLevel12Weapon(EntityEquipment entityEquipment, Random random){
		entityEquipment.setItemInMainHandDropChance(0.0f);
		ItemStack bow = new ItemStack(Material.BOW);
		ItemMeta bowMeta = bow.getItemMeta();

		bowMeta.lore(Collections.singletonList(strongDecayIdentifier));

		bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 3,true);
		bowMeta.addEnchant(Enchantment.ARROW_DAMAGE, random.nextDouble() <= 0.66 ? 4 : 5,true);
		bowMeta.addEnchant(Enchantment.ARROW_FIRE, 1,true);
		bow.setItemMeta(bowMeta);
		entityEquipment.setItemInMainHand(bow);

	}

}
