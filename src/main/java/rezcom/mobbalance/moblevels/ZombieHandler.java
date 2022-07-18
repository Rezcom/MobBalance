package rezcom.mobbalance.moblevels;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rezcom.mobbalance.Main;

import java.util.Random;

public class ZombieHandler implements Listener {

	public static boolean zombieDebug = false;
	public static boolean zombieFirstDebug = false;

	public static final PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1);
	public static final PotionEffect speedStrong = new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,2);
	public static final PotionEffect fireResist = new PotionEffect(PotionEffectType.FIRE_RESISTANCE,Integer.MAX_VALUE,1);
	public static final PotionEffect resistanceEffect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,Integer.MAX_VALUE,1);
	public static final PotionEffect strengthBuff = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE,1);
	public static final PotionEffect strengthStrong = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE,3);

	public static final PotionEffect weakHunger = new PotionEffect(PotionEffectType.HUNGER,200,30);
	public static final PotionEffect normalHunger = new PotionEffect(PotionEffectType.HUNGER,200,60);
	public static final PotionEffect strongHunger = new PotionEffect(PotionEffectType.HUNGER,200,255);
	@EventHandler
	void onZombieSpawn(CreatureSpawnEvent event){
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM || !(MobLevelHandler.spawnReasons.contains(event.getSpawnReason()))){
			// If a plugin spawned it, don't bother.
			Main.sendDebugMessage("Wasn't a valid reason, it was: " + event.getSpawnReason(),zombieDebug);
			return;
		}
		if (!(event.getEntity() instanceof Zombie)){
			Main.sendDebugMessage("Not a zombie",zombieDebug);
			return; // Not a zombie, don't bother.
		}

		Zombie zombie = (Zombie) event.getEntity();
		Integer level;
		if (zombieFirstDebug){
			level = MobLevelHandler.rollProbability(MobLevelHandler.firstDebugProbs);
		} else if (MobLevelHandler.crimsonNight){
			// BLOOD MOON!
			level = MobLevelHandler.rollProbability(MobLevelHandler.bloodProbs);
		} else {
			level = MobLevelHandler.rollProbability(MobLevelHandler.defaultProbs);
		}
		if (level == null){
			Main.sendDebugMessage("ZOMBIE LEVEL NULL OH SHIT",zombieDebug);
			return;
		}



		PersistentDataContainer zombiePDC = zombie.getPersistentDataContainer();
		zombiePDC.set(MobLevelHandler.MobLevel, PersistentDataType.INTEGER, level);
		MobLevelHandler.checkElite(zombie);

		EntityEquipment entityEquipment = zombie.getEquipment();
		Random random = new Random();
		Main.sendDebugMessage("Spawning a Level " + level + " zombie.",zombieDebug);


		applyEffects(zombie, level);

		if (random.nextDouble() <= 0.20){
			equipWeapons(entityEquipment, level, random);
		}

		if (random.nextDouble() <= 0.25){
			equipArmor(entityEquipment,level,random);
		}
	}
	@EventHandler
	void onZombieHit(EntityDamageByEntityEvent event){
		// Whenever a player gets hit by a zombie, they should have a chance to receive hunger.

		if (!(event.getEntity() instanceof LivingEntity) || !(event.getDamager() instanceof Zombie)){
			// Player wasn't was who was hit, or attacker isn't a zombie.
			return;
		}
		LivingEntity livingEntity = (LivingEntity) event.getEntity();
		Zombie zombie = (Zombie) event.getDamager();

		int level = MobLevelHandler.getMobLevel(zombie);

		Random random = new Random();
		double eventDamage = event.getDamage();

		boolean weaponEquipped = zombie.getEquipment().getItemInMainHand().getType() != Material.AIR || zombie.getEquipment().getItemInOffHand().getType() != Material.AIR;


		// Apply hunger effects and damage multiplayer
		if (level >= 4 && level <= 6){
			event.setDamage(eventDamage * (weaponEquipped ? 1.5 : 1.75));
			if (random.nextDouble() <= 0.33){
				livingEntity.addPotionEffect(weakHunger);
			}
		} else if (level <= 9){
			event.setDamage(eventDamage * (weaponEquipped ? 1.75 : 2.0));
			if (random.nextDouble() <= 0.66){
				livingEntity.addPotionEffect(normalHunger);
			}
		} else if (level <= 12){
			event.setDamage(eventDamage * (weaponEquipped ? 2.0 : 2.25));
			livingEntity.addPotionEffect(strongHunger);
		}

	}

	@EventHandler
	void onPlayerDamageZombie(EntityDamageByEntityEvent event){
		// Whenever a player attacks a zombie, the zombie takes reduced damage dependent on level.

		if (!(event.getEntity() instanceof Zombie) || !(event.getDamager() instanceof Player || (event.getDamager() instanceof Wolf))){
			return;
		}

		Zombie zombie = (Zombie) event.getEntity();

		int level = MobLevelHandler.getMobLevel(zombie);

		double eventDamage = event.getDamage();

		if (level < 4){
			event.setDamage(eventDamage * 0.70);
		} else if (level <= 6){
			event.setDamage(eventDamage * 0.55);
		} else if (level <= 8){
			event.setDamage(eventDamage * 0.40);
		} else if (level <= 10){
			event.setDamage(eventDamage * 0.25);
		} else if (level <= 12){
			event.setDamage(eventDamage * 0.10);
		}
	}

	private static void applyEffects(Zombie zombie, int level){
		if (level >= 6 && level <= 8){
			// BLOOD MOON TIME!!
			zombie.addPotionEffect(speedEffect);
		} else if (level == 9){
			zombie.addPotionEffect(speedStrong);
		} else if (level == 10){
			zombie.addPotionEffect(fireResist);
			zombie.addPotionEffect(resistanceEffect);
			zombie.addPotionEffect(speedStrong);
		} else if (level == 11){
			zombie.addPotionEffect(fireResist);
			zombie.addPotionEffect(resistanceEffect);
			zombie.addPotionEffect(strengthBuff);
			zombie.addPotionEffect(speedStrong);
		} else if (level == 12) {
			zombie.addPotionEffect(fireResist);
			zombie.addPotionEffect(resistanceEffect);
			zombie.addPotionEffect(strengthStrong);
			zombie.addPotionEffect(speedStrong);
		}
	}

	private static void equipWeapons(EntityEquipment entityEquipment, int level, Random random){
		if (level == 3 || level == 4){
			setLevel3Weapon(entityEquipment, random);
		} else if (level == 5 || level == 6){
			setLevel5Weapon(entityEquipment, random);
		} else if (level == 7){
			setLevel7Weapons(entityEquipment, random);
		} else if (level == 8 || level == 9){
			setLevel8Weapons(entityEquipment, random);
		} else if (level >= 10){
			setLevel10Weapons(entityEquipment, random);
		}
	}

	private static void equipArmor(EntityEquipment entityEquipment, int level, Random random){
		if (level == 1){
			setLevel1Armor(entityEquipment, random);
		} else if (level == 2){
			setLevel2Armor(entityEquipment, random);
		} else if (level == 3){
			setLevel3Armor(entityEquipment, random);
		} else if (level == 4 || level == 5){
			setLevel4Armor(entityEquipment, random);
		} else if (level == 6){
			// BLOOD MOON TIME!!
			setLevel6Armor(entityEquipment, random);
		} else if (level == 7 || level == 8){
			setLevel7Armor(entityEquipment, random);
		} else if (level == 9){
			setLevel9Armor(entityEquipment, random);
		} else if (level == 10){
			setLevel10Armor(entityEquipment, random);
		} else if (level == 11 || level == 12){
			setLevel11Armor(entityEquipment, random);
		}
	}

	public static void setLevel1Armor(EntityEquipment entityEquipment, Random random){
		// Helmet?
		if (random.nextDouble() <= 0.2){
			entityEquipment.setHelmetDropChance(0.05f);
			ItemStack helmet = new ItemStack(random.nextDouble() <= 0.5 ? Material.LEATHER_HELMET : Material.CHAINMAIL_HELMET);
			entityEquipment.setHelmet(helmet);
		}
		// Chestplate?
		if (random.nextDouble() <= 0.8){
			entityEquipment.setChestplateDropChance(0.02f);
			ItemStack chestplate = new ItemStack(random.nextDouble() <= 0.75 ? Material.LEATHER_CHESTPLATE : Material.CHAINMAIL_CHESTPLATE);
			// Percent chance for enchant
			if (random.nextDouble() <= 0.11){
				ItemMeta chestplateMeta = chestplate.getItemMeta();
				chestplateMeta.addEnchant(random.nextDouble() <= 0.5 ? Enchantment.PROTECTION_PROJECTILE : Enchantment.PROTECTION_ENVIRONMENTAL,1,true);
				chestplate.setItemMeta(chestplateMeta);
			}
			entityEquipment.setChestplate(chestplate);
		}
		// Leggings?
		if (random.nextDouble() <= 0.75){
			entityEquipment.setLeggingsDropChance(0.05f);
			ItemStack leggings = new ItemStack(random.nextDouble() <= 0.5 ? Material.LEATHER_LEGGINGS : Material.CHAINMAIL_LEGGINGS);
			if (random.nextDouble() <= 0.25){
				ItemMeta leggingsMeta = leggings.getItemMeta();
				leggingsMeta.addEnchant(random.nextDouble() <= 0.80 ? Enchantment.PROTECTION_ENVIRONMENTAL : Enchantment.PROTECTION_PROJECTILE,1,true);
				leggings.setItemMeta(leggingsMeta);
			}
			entityEquipment.setLeggings(leggings);
		}
		// Boots?
		if (random.nextDouble() <= 0.6){
			entityEquipment.setBootsDropChance(0.05f);
			ItemStack boots = new ItemStack(random.nextDouble() <= 0.75 ? Material.LEATHER_BOOTS : Material.CHAINMAIL_BOOTS);
			if (random.nextDouble() <= 0.33){
				ItemMeta bootsMeta = boots.getItemMeta();
				bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1,true);
				boots.setItemMeta(bootsMeta);
			}
			entityEquipment.setBoots(boots);
		}
	}

	public static void setLevel2Armor(EntityEquipment entityEquipment, Random random){
		// Helmet
		entityEquipment.setHelmetDropChance(0.05f);
		ItemStack helmet = new ItemStack(random.nextDouble() <= 0.65 ? Material.IRON_HELMET : Material.CHAINMAIL_HELMET);
		if (random.nextDouble() <= 0.35){
			ItemMeta helmetMeta = helmet.getItemMeta();
			helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1,true);
			helmet.setItemMeta(helmetMeta);
		}
		entityEquipment.setHelmet(helmet);

		// Chestplate
		entityEquipment.setChestplateDropChance(0.01f);
		ItemStack chestplate = new ItemStack(random.nextDouble() <= 0.75 ? Material.IRON_CHESTPLATE : Material.GOLDEN_CHESTPLATE);
		ItemMeta chestplateMeta = chestplate.getItemMeta();
		chestplateMeta.addEnchant(random.nextDouble() <= 0.85 ? Enchantment.PROTECTION_ENVIRONMENTAL : Enchantment.PROTECTION_PROJECTILE, 1,true);
		chestplate.setItemMeta(chestplateMeta);
		entityEquipment.setChestplate(chestplate);

		// Leggings
		entityEquipment.setLeggingsDropChance(0.05f);
		ItemStack leggings = new ItemStack(random.nextDouble() <= 0.75 ? Material.IRON_LEGGINGS : Material.GOLDEN_LEGGINGS);
		ItemMeta leggingsMeta = leggings.getItemMeta();
		leggingsMeta.addEnchant(random.nextDouble() <= 0.85 ? Enchantment.PROTECTION_ENVIRONMENTAL : Enchantment.PROTECTION_PROJECTILE,1,true);
		leggings.setItemMeta(leggingsMeta);
		entityEquipment.setLeggings(leggings);

		// Boots
		entityEquipment.setBootsDropChance(0.05f);
		ItemStack boots = new ItemStack(random.nextDouble() <= 0.50 ? Material.IRON_BOOTS : Material.GOLDEN_BOOTS);
		if (random.nextDouble() <= 0.35){
			ItemMeta bootsMeta = boots.getItemMeta();
			bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1,true);
			boots.setItemMeta(bootsMeta);
		}
		entityEquipment.setBoots(boots);
	}

	public static void setLevel3Armor(EntityEquipment entityEquipment, Random random){
		// Helmet
		entityEquipment.setHelmetDropChance(0.05f);
		ItemStack helmet = new ItemStack(random.nextDouble() <= 0.75 ? Material.IRON_HELMET : Material.CHAINMAIL_HELMET);
		ItemMeta helmetMeta = helmet.getItemMeta();
		if (random.nextDouble() <= 0.80){
			helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1,true);
		}
		helmetMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE,1,true);
		helmet.setItemMeta(helmetMeta);
		entityEquipment.setHelmet(helmet);

		// Chestplate
		entityEquipment.setChestplateDropChance(0.01f);
		ItemStack chestplate = new ItemStack(random.nextDouble() <= 0.75 ? Material.IRON_CHESTPLATE : Material.CHAINMAIL_CHESTPLATE);
		ItemMeta chestplateMeta = chestplate.getItemMeta();
		if (random.nextDouble() <= 0.80){
			chestplateMeta.addEnchant(random.nextDouble() <= 0.90 ? Enchantment.PROTECTION_ENVIRONMENTAL : Enchantment.PROTECTION_EXPLOSIONS,2,true);
		}
		chestplateMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE,1,true);
		chestplate.setItemMeta(chestplateMeta);
		entityEquipment.setChestplate(chestplate);

		// Leggings
		entityEquipment.setLeggingsDropChance(0.05f);
		ItemStack leggings = new ItemStack(random.nextDouble() <= 0.75 ? Material.IRON_LEGGINGS : Material.CHAINMAIL_LEGGINGS);
		ItemMeta leggingsMeta = leggings.getItemMeta();
		if (random.nextDouble() <= 0.80){
			chestplateMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1,true);
		}
		leggings.setItemMeta(leggingsMeta);
		entityEquipment.setLeggings(leggings);

		// Boots
		entityEquipment.setBootsDropChance(0.05f);
		ItemStack boots = new ItemStack(random.nextDouble() <= 0.75 ? Material.IRON_BOOTS : Material.CHAINMAIL_BOOTS);
		ItemMeta bootsMeta = boots.getItemMeta();
		if (random.nextDouble() <= 0.80){
			chestplateMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1,true);
		}
		boots.setItemMeta(bootsMeta);
		entityEquipment.setBoots(boots);
	}

	public static void setLevel3Weapon(EntityEquipment entityEquipment, Random random){
		ItemStack ironSword = new ItemStack(random.nextDouble() <= 0.80 ? Material.IRON_SWORD : Material.STONE_SWORD);
		ItemStack ironAxe = new ItemStack(random.nextDouble() <= 0.50 ? Material.IRON_AXE : Material.STONE_AXE);
		entityEquipment.setItemInMainHand(random.nextDouble() <= 0.5 ? ironSword : ironAxe);
		entityEquipment.setItemInMainHandDropChance(0.1f);
	}

	public static void setLevel4Armor(EntityEquipment entityEquipment, Random random){
		// Helmet
		entityEquipment.setHelmetDropChance(0.05f);
		ItemStack helmet = new ItemStack(random.nextDouble() <= 0.90 ? Material.IRON_HELMET : Material.CHAINMAIL_HELMET);
		ItemMeta helmetMeta = helmet.getItemMeta();
		helmetMeta.addEnchant(random.nextDouble() <= 0.75 ? Enchantment.PROTECTION_ENVIRONMENTAL : Enchantment.PROTECTION_EXPLOSIONS,1,true);
		helmetMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE,1,true);
		helmet.setItemMeta(helmetMeta);
		entityEquipment.setHelmet(helmet);

		// Chestplate
		entityEquipment.setChestplateDropChance(0.01f);
		ItemStack chestplate = new ItemStack(random.nextDouble() <= 0.90 ? Material.IRON_CHESTPLATE : Material.CHAINMAIL_CHESTPLATE);
		ItemMeta chestplateMeta = chestplate.getItemMeta();
		chestplateMeta.addEnchant(random.nextDouble() <= 0.90 ? Enchantment.PROTECTION_ENVIRONMENTAL : Enchantment.PROTECTION_EXPLOSIONS,2,true);
		chestplateMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE,1,true);
		chestplate.setItemMeta(chestplateMeta);
		entityEquipment.setChestplate(chestplate);

		// Leggings
		entityEquipment.setLeggingsDropChance(0.05f);
		ItemStack leggings = new ItemStack(random.nextDouble() <= 0.90 ? Material.IRON_LEGGINGS : Material.CHAINMAIL_LEGGINGS);
		ItemMeta leggingsMeta = leggings.getItemMeta();
		leggingsMeta.addEnchant(random.nextDouble() <= 0.90 ? Enchantment.PROTECTION_ENVIRONMENTAL : Enchantment.PROTECTION_EXPLOSIONS,1,true);
		leggingsMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE,1,true);
		leggings.setItemMeta(leggingsMeta);
		entityEquipment.setLeggings(leggings);

		// Boots
		entityEquipment.setBootsDropChance(0.05f);
		ItemStack boots = new ItemStack(random.nextDouble() <= 0.90 ? Material.IRON_BOOTS : Material.CHAINMAIL_BOOTS);
		ItemMeta bootsMeta = boots.getItemMeta();
		bootsMeta.addEnchant(random.nextDouble() <= 0.90 ? Enchantment.PROTECTION_ENVIRONMENTAL : Enchantment.PROTECTION_EXPLOSIONS,1,true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_FALL,1,true);
		boots.setItemMeta(bootsMeta);
		entityEquipment.setBoots(boots);
	}

	public static void setLevel5Weapon(EntityEquipment entityEquipment, Random random){

		if (random.nextDouble() <= 0.50){
			ItemStack swordStack = new ItemStack(random.nextDouble() <= 0.90 ? Material.IRON_SWORD : Material.DIAMOND_SWORD);
			// Sword
			ItemMeta swordMeta = swordStack.getItemMeta();
			swordMeta.addEnchant(Enchantment.DAMAGE_ALL, 1,true);
			double extraChance = random.nextDouble();
			if (extraChance <= 0.33){
				swordMeta.addEnchant(Enchantment.KNOCKBACK,1,true);
			} else if (extraChance <= 0.66){
				swordMeta.addEnchant(Enchantment.FIRE_ASPECT,1,true);
			}
			swordStack.setItemMeta(swordMeta);
			entityEquipment.setItemInMainHand(swordStack);
		} else {
			ItemStack axeStack = new ItemStack(random.nextDouble() <= 0.95 ? Material.IRON_AXE : Material.DIAMOND_AXE);
			// Axe
			ItemMeta axeMeta = axeStack.getItemMeta();
			axeMeta.addEnchant(Enchantment.DAMAGE_ALL, random.nextDouble() <= 0.5 ? 1 : 2,true);
			axeStack.setItemMeta(axeMeta);
			entityEquipment.setItemInMainHand(axeStack);
		}
		entityEquipment.setItemInMainHandDropChance(entityEquipment.getItemInMainHand().getType() == Material.IRON_SWORD || entityEquipment.getItemInMainHand().getType() == Material.IRON_AXE ? 0.01f : 0.0f);
	}

	public static void setLevel6Armor(EntityEquipment entityEquipment, Random random){
		// Helmet
		ItemStack helmet = new ItemStack(random.nextDouble() <= 0.66 ? Material.IRON_HELMET : Material.DIAMOND_HELMET);
		entityEquipment.setHelmetDropChance(helmet.getType() == Material.IRON_HELMET ? 0.02f : 0.0f);
		ItemMeta helmetMeta = helmet.getItemMeta();
		double helmetChance = random.nextDouble();
		if (helmetChance <= 0.33){
			helmetMeta.addEnchant(random.nextDouble() <= 0.5 ? Enchantment.PROTECTION_EXPLOSIONS : Enchantment.PROTECTION_PROJECTILE,random.nextDouble() <= 0.5 ? 1 : 2,true);
		} else if (helmetChance <= 0.66){
			helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.66 ? 2 : 3,true);
		}
		helmet.setItemMeta(helmetMeta);
		entityEquipment.setHelmet(helmet);

		// Chestplate
		ItemStack chestplate = new ItemStack(random.nextDouble() <= 0.80 ? Material.IRON_CHESTPLATE : Material.DIAMOND_CHESTPLATE);
		entityEquipment.setChestplateDropChance(chestplate.getType() == Material.IRON_CHESTPLATE ? 0.005f : 0.0f);
		ItemMeta chestplateMeta = chestplate.getItemMeta();
		chestplateMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.66 ? 2 : 3,true);
		double extraChestplateChance = random.nextDouble();
		if (extraChestplateChance <= 0.33){
			chestplateMeta.addEnchant(random.nextDouble() <= 0.5 ? Enchantment.PROTECTION_EXPLOSIONS : Enchantment.THORNS, random.nextDouble() <= 0.66 ? 1 : 2,true);
		} else if (extraChestplateChance <= 0.66){
			chestplateMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.80 ? 2 : 3,true);
		}
		chestplate.setItemMeta(chestplateMeta);
		entityEquipment.setChestplate(chestplate);

		// Leggings
		ItemStack leggings = new ItemStack(random.nextDouble() <= 0.66 ? Material.IRON_LEGGINGS : Material.DIAMOND_LEGGINGS);
		entityEquipment.setLeggingsDropChance(leggings.getType() == Material.IRON_LEGGINGS ? 0.02f : 0.0f);
		ItemMeta leggingsMeta = leggings.getItemMeta();
		leggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.5 ? 2 : 3, true);
		if (random.nextDouble() <= 0.5){
			leggingsMeta.addEnchant(random.nextDouble() <= 0.5 ? Enchantment.PROTECTION_FIRE : Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.66 ? 2 : 3,true);
		}
		leggings.setItemMeta(leggingsMeta);
		entityEquipment.setLeggings(leggings);

		// Boots
		ItemStack boots = new ItemStack(random.nextDouble() <= 0.66 ? Material.IRON_BOOTS : Material.DIAMOND_BOOTS);
		entityEquipment.setBootsDropChance(boots.getType() == Material.IRON_BOOTS ? 0.02f : 0.0f);
		ItemMeta bootsMeta = boots.getItemMeta();
		double extraBootsChance = random.nextDouble();
		if (extraBootsChance <= 0.33){
			bootsMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.66 ? 2 : 3,true);
		} else if (extraBootsChance <= 0.66){
			bootsMeta.addEnchant(random.nextDouble() <= 0.5 ? Enchantment.PROTECTION_PROJECTILE : Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.66 ? 1 : 2,true);
		} else if (extraBootsChance <= 0.91){
			bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.66 ? 2 : 3,true);
		}
		bootsMeta.addEnchant(Enchantment.PROTECTION_FALL,1,true);
		boots.setItemMeta(bootsMeta);
		entityEquipment.setBoots(boots);
	}

	public static void setLevel7Armor(EntityEquipment entityEquipment, Random random){
		// Helmet
		entityEquipment.setHelmetDropChance(0.0f);
		ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
		ItemMeta helmetMeta = helmet.getItemMeta();

		if (random.nextDouble() <= 0.33){helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.80 ? 2 : 3,true);}
		if (random.nextDouble() <= 0.33){helmetMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.80 ? 2 : 3,true);}
		if (random.nextDouble() <= 0.33){helmetMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.80 ? 2 : 3,true);}
		if (random.nextDouble() <= 0.33){helmetMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.80 ? 2 : 3,true);}

		helmet.setItemMeta(helmetMeta);
		entityEquipment.setHelmet(helmet);

		// Chestplate
		entityEquipment.setChestplateDropChance(0.0f);
		ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
		ItemMeta chestplateMeta = chestplate.getItemMeta();
		if (random.nextDouble() <= 0.33){chestplateMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.80 ? 3 : 4,true);}
		if (random.nextDouble() <= 0.33){chestplateMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.80 ? 3 : 4, true);}
		if (random.nextDouble() <= 0.33){chestplateMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.80 ? 3 : 4, true);}
		if (random.nextDouble() <= 0.33){chestplateMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.80 ? 3 : 4, true);}
		if (random.nextDouble() <= 0.5){
			chestplateMeta.addEnchant(Enchantment.THORNS, random.nextDouble() <= 0.66 ? 1 : 2, true);
		}
		chestplate.setItemMeta(chestplateMeta);
		entityEquipment.setChestplate(chestplate);

		// Leggings
		entityEquipment.setLeggingsDropChance(0.0f);
		ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
		ItemMeta leggingsMeta = leggings.getItemMeta();
		if (random.nextDouble() <= 0.33){leggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,random.nextDouble() <= 0.80 ? 2 : 3, true);}
		if (random.nextDouble() <= 0.33){leggingsMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.80 ? 2 : 3, true);}
		if (random.nextDouble() <= 0.33){leggingsMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.80 ? 2 : 3, true);}
		if (random.nextDouble() <= 0.33){leggingsMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.80 ? 2 : 3, true);}
		leggings.setItemMeta(leggingsMeta);
		entityEquipment.setLeggings(leggings);

		// Boots
		entityEquipment.setBootsDropChance(0.0f);
		ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
		ItemMeta bootsMeta = boots.getItemMeta();
		if (random.nextDouble() <= 0.33){bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.80 ? 1 : 2,true);}
		if (random.nextDouble() <= 0.33){bootsMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.80 ? 1 : 2, true);}
		if (random.nextDouble() <= 0.33){bootsMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.80 ? 1 : 2, true);}
		if (random.nextDouble() <= 0.33){bootsMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.80 ? 1 : 2,true);}
		bootsMeta.addEnchant(Enchantment.PROTECTION_FALL, random.nextDouble() <= 0.80 ? 1 : 2,true);
		boots.setItemMeta(bootsMeta);
		entityEquipment.setBoots(boots);
	}

	public static void setLevel7Weapons(EntityEquipment entityEquipment, Random random){

		if (random.nextDouble() <= 0.50){
			ItemStack swordStack = new ItemStack(random.nextDouble() <= 0.90 ? Material.IRON_SWORD : Material.DIAMOND_SWORD);
			// Sword
			ItemMeta swordMeta = swordStack.getItemMeta();
			swordMeta.addEnchant(Enchantment.DAMAGE_ALL, random.nextDouble() <= 0.66 ? 2 : 3,true);
			if (random.nextDouble() <= 0.33){
				swordMeta.addEnchant(Enchantment.KNOCKBACK,random.nextDouble() <= 0.66 ? 1 : 2,true);
			}
			if (random.nextDouble() <= 0.33){
				swordMeta.addEnchant(Enchantment.FIRE_ASPECT,random.nextDouble() <= 0.66 ? 1 : 2,true);
			}
			if (random.nextDouble() <= 0.33){
				swordMeta.addEnchant(Enchantment.SWEEPING_EDGE, random.nextDouble() <= 0.66 ? 1 : 2, true);
			}
			swordStack.setItemMeta(swordMeta);
			entityEquipment.setItemInMainHand(swordStack);
		} else {
			// Axe
			ItemStack axeStack = new ItemStack(random.nextDouble() <= 0.95 ? Material.IRON_AXE : Material.DIAMOND_AXE);
			ItemMeta axeMeta = axeStack.getItemMeta();
			axeMeta.addEnchant(Enchantment.DAMAGE_ALL, random.nextDouble() <= 0.66 ? 2 : 3,true);
			axeStack.setItemMeta(axeMeta);
			entityEquipment.setItemInMainHand(axeStack);
		}
		entityEquipment.setItemInMainHandDropChance(entityEquipment.getItemInMainHand().getType() == Material.IRON_SWORD || entityEquipment.getItemInMainHand().getType() == Material.IRON_AXE ? 0.001f : 0.0f);
	}

	public static void setLevel8Weapons(EntityEquipment entityEquipment, Random random){
		entityEquipment.setItemInMainHandDropChance(0.0f);

		if (random.nextDouble() <= 0.50){
			ItemStack swordStack = new ItemStack(Material.DIAMOND_SWORD);
			ItemMeta swordMeta = swordStack.getItemMeta();
			swordMeta.addEnchant(Enchantment.DAMAGE_ALL, random.nextDouble() <= 0.66 ? 3 : 4,true);
			if (random.nextDouble() <= 0.33){
				swordMeta.addEnchant(Enchantment.KNOCKBACK,random.nextDouble() <= 0.66 ? 2 : 3,true);
			}
			if (random.nextDouble() <= 0.33){
				swordMeta.addEnchant(Enchantment.FIRE_ASPECT,random.nextDouble() <= 0.66 ? 2 : 3,true);
			}
			if (random.nextDouble() <= 0.33){
				swordMeta.addEnchant(Enchantment.SWEEPING_EDGE, random.nextDouble() <= 0.66 ? 2 : 3, true);
			}
			swordStack.setItemMeta(swordMeta);
			entityEquipment.setItemInMainHand(swordStack);
		} else {
			ItemStack axeStack = new ItemStack(Material.DIAMOND_AXE);
			ItemMeta axeMeta = axeStack.getItemMeta();
			axeMeta.addEnchant(Enchantment.DAMAGE_ALL, random.nextDouble() <= 0.66 ? 3 : 4,true);
			axeStack.setItemMeta(axeMeta);
			entityEquipment.setItemInMainHand(axeStack);
		}

	}

	public static void setLevel9Armor(EntityEquipment entityEquipment, Random random){
		// Helmet
		entityEquipment.setHelmetDropChance(0.0f);
		ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
		ItemMeta helmetMeta = helmet.getItemMeta();

		helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.80 ? 2 : 3,true);
		helmetMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.80 ? 2 : 3,true);
		helmetMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.80 ? 2 : 3,true);
		helmetMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.80 ? 2 : 3,true);
		helmet.setItemMeta(helmetMeta);
		entityEquipment.setHelmet(helmet);

		// Chestplate
		entityEquipment.setChestplateDropChance(0.0f);
		ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
		ItemMeta chestplateMeta = chestplate.getItemMeta();
		chestplateMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.80 ? 3 : 4,true);
		chestplateMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.80 ? 3 : 4, true);
		chestplateMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.80 ? 3 : 4, true);
		chestplateMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.80 ? 3 : 4, true);
		if (random.nextDouble() <= 0.5){
			chestplateMeta.addEnchant(Enchantment.THORNS, random.nextDouble() <= 0.66 ? 1 : 2, true);
		}
		chestplate.setItemMeta(chestplateMeta);
		entityEquipment.setChestplate(chestplate);

		// Leggings
		entityEquipment.setLeggingsDropChance(0.0f);
		ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
		ItemMeta leggingsMeta = leggings.getItemMeta();
		leggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,random.nextDouble() <= 0.80 ? 2 : 3, true);
		leggingsMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.80 ? 2 : 3, true);
		leggingsMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.80 ? 2 : 3, true);
		leggingsMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.80 ? 2 : 3, true);
		leggings.setItemMeta(leggingsMeta);
		entityEquipment.setLeggings(leggings);

		// Boots
		entityEquipment.setBootsDropChance(0.0f);
		ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
		ItemMeta bootsMeta = boots.getItemMeta();
		bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.80 ? 1 : 2,true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.80 ? 1 : 2, true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.80 ? 1 : 2, true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.80 ? 1 : 2,true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_FALL, random.nextDouble() <= 0.80 ? 1 : 2,true);
		boots.setItemMeta(bootsMeta);
		entityEquipment.setBoots(boots);
	}

	public static void setLevel10Armor(EntityEquipment entityEquipment, Random random){
		// Helmet
		entityEquipment.setHelmetDropChance(0.0f);
		ItemStack helmet = new ItemStack(random.nextDouble() <= 0.5 ? Material.DIAMOND_HELMET : Material.NETHERITE_HELMET);
		ItemMeta helmetMeta = helmet.getItemMeta();

		helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.20 ? 2 : 3,true);
		helmetMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.20 ? 2 : 3,true);
		helmetMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.20 ? 2 : 3,true);
		helmetMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.20 ? 2 : 3,true);
		helmet.setItemMeta(helmetMeta);
		entityEquipment.setHelmet(helmet);

		// Chestplate
		entityEquipment.setChestplateDropChance(0.0f);
		ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
		ItemMeta chestplateMeta = chestplate.getItemMeta();
		chestplateMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.20 ? 3 : 4,true);
		chestplateMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.20 ? 3 : 4, true);
		chestplateMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.20 ? 3 : 4, true);
		chestplateMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.20 ? 3 : 4, true);
		if (random.nextDouble() <= 0.5){
			chestplateMeta.addEnchant(Enchantment.THORNS, random.nextDouble() <= 0.33 ? 1 : 2, true);
		}
		chestplate.setItemMeta(chestplateMeta);
		entityEquipment.setChestplate(chestplate);

		// Leggings
		entityEquipment.setLeggingsDropChance(0.0f);
		ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
		ItemMeta leggingsMeta = leggings.getItemMeta();
		leggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,random.nextDouble() <= 0.20 ? 2 : 3, true);
		leggingsMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.20 ? 2 : 3, true);
		leggingsMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.20 ? 2 : 3, true);
		leggingsMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.20 ? 2 : 3, true);
		leggings.setItemMeta(leggingsMeta);
		entityEquipment.setLeggings(leggings);

		// Boots
		entityEquipment.setBootsDropChance(0.0f);
		ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
		ItemMeta bootsMeta = boots.getItemMeta();
		bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.20 ? 1 : 2,true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.20 ? 1 : 2, true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.20 ? 1 : 2, true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.20 ? 1 : 2,true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_FALL, random.nextDouble() <= 0.20 ? 1 : 2,true);
		boots.setItemMeta(bootsMeta);
		entityEquipment.setBoots(boots);
	}

	public static void setLevel10Weapons(EntityEquipment entityEquipment, Random random){
		entityEquipment.setItemInMainHandDropChance(0.0f);

		if (random.nextDouble() <= 0.50){
			ItemStack swordStack = new ItemStack(random.nextDouble() <= 0.66 ? Material.DIAMOND_SWORD : Material.NETHERITE_SWORD);
			ItemMeta swordMeta = swordStack.getItemMeta();
			swordMeta.addEnchant(Enchantment.DAMAGE_ALL, random.nextDouble() <= 0.66 ? 3 : 4,true);
			if (random.nextDouble() <= 0.40){
				swordMeta.addEnchant(Enchantment.KNOCKBACK,random.nextDouble() <= 0.66 ? 2 : 3,true);
			}
			if (random.nextDouble() <= 0.40){
				swordMeta.addEnchant(Enchantment.FIRE_ASPECT,random.nextDouble() <= 0.66 ? 2 : 3,true);
			}
			if (random.nextDouble() <= 0.40){
				swordMeta.addEnchant(Enchantment.SWEEPING_EDGE, random.nextDouble() <= 0.66 ? 2 : 3, true);
			}
			swordStack.setItemMeta(swordMeta);
			entityEquipment.setItemInMainHand(swordStack);
		} else {
			ItemStack axeStack = new ItemStack(random.nextDouble() <= 0.66 ? Material.DIAMOND_AXE : Material.NETHERITE_AXE);
			ItemMeta axeMeta = axeStack.getItemMeta();
			axeMeta.addEnchant(Enchantment.DAMAGE_ALL, random.nextDouble() <= 0.66 ? 3 : 4,true);
			axeStack.setItemMeta(axeMeta);
			entityEquipment.setItemInMainHand(axeStack);
		}
	}

	public static void setLevel11Armor(EntityEquipment entityEquipment, Random random){
		// Helmet
		entityEquipment.setHelmetDropChance(0.0f);
		ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
		ItemMeta helmetMeta = helmet.getItemMeta();

		helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.66 ? 3 : 4,true);
		helmetMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.66 ? 3 : 4,true);
		helmetMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.66 ? 3 : 4,true);
		helmetMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.66 ? 3 : 4,true);
		helmet.setItemMeta(helmetMeta);
		entityEquipment.setHelmet(helmet);

		// Chestplate
		entityEquipment.setChestplateDropChance(0.0f);
		ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
		ItemMeta chestplateMeta = chestplate.getItemMeta();
		chestplateMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.66 ? 4 : 5,true);
		chestplateMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.66 ? 4 : 5, true);
		chestplateMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.66 ? 4 : 5, true);
		chestplateMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.66 ? 4 : 5, true);
		if (random.nextDouble() <= 0.5){
			chestplateMeta.addEnchant(Enchantment.THORNS, random.nextDouble() <= 0.66 ? 2 : 3, true);
		}
		chestplate.setItemMeta(chestplateMeta);
		entityEquipment.setChestplate(chestplate);

		// Leggings
		entityEquipment.setLeggingsDropChance(0.0f);
		ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
		ItemMeta leggingsMeta = leggings.getItemMeta();
		leggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,random.nextDouble() <= 0.66 ? 3 : 4, true);
		leggingsMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.66 ? 3 : 4, true);
		leggingsMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.66 ? 3 : 4, true);
		leggingsMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.66 ? 3 : 4, true);
		leggings.setItemMeta(leggingsMeta);
		entityEquipment.setLeggings(leggings);

		// Boots
		entityEquipment.setBootsDropChance(0.0f);
		ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
		ItemMeta bootsMeta = boots.getItemMeta();
		bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, random.nextDouble() <= 0.80 ? 2 : 3,true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, random.nextDouble() <= 0.80 ? 2 : 3, true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, random.nextDouble() <= 0.80 ? 2 : 3, true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_FIRE, random.nextDouble() <= 0.80 ? 2 : 3,true);
		bootsMeta.addEnchant(Enchantment.PROTECTION_FALL, 4,true);
		boots.setItemMeta(bootsMeta);
		entityEquipment.setBoots(boots);
	}
}
