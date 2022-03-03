package rezcom.mobbalance.moblevels;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rezcom.mobbalance.Main;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class CreeperHandler implements Listener {

	public static boolean creeperDebug = false;

	public static PotionEffect weakSpeed = new PotionEffect(PotionEffectType.SPEED,240,0);
	public static PotionEffect normalSpeed = new PotionEffect(PotionEffectType.SPEED,240,1);
	public static PotionEffect strongSpeed = new PotionEffect(PotionEffectType.SPEED,240,2);

	public static PotionEffect weakResist = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,240,0);
	public static PotionEffect normalResist = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 240,1);
	public static PotionEffect strongResist = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 240, 2);

	public static PotionEffect fireResist = new PotionEffect(PotionEffectType.FIRE_RESISTANCE,240,0);

	public static PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY,240,0);
	@EventHandler
	void onCreeperSpawn(CreatureSpawnEvent event){
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM || !(MobLevelHandler.spawnReasons.contains(event.getSpawnReason()))){
			return;
		}
		if (!(event.getEntity() instanceof Creeper)){
			Main.sendDebugMessage("Not a creeper",creeperDebug);
			return;
		}

		Creeper creeper = (Creeper) event.getEntity();
		Integer level;
		if (MobLevelHandler.crimsonNight){
			// BLOOD MOON!
			level = MobLevelHandler.rollProbability(MobLevelHandler.bloodProbs);
		} else {
			level = MobLevelHandler.rollProbability(MobLevelHandler.defaultProbs);
		}

		if (level == null){
			Main.logger.log(Level.WARNING,"Creeper level was null!");
			return;
		}

		creeper.setMetadata("Level", new FixedMetadataValue(Main.thisPlugin,level));

		Main.sendDebugMessage("Spawning a level " + level + " creeper.",creeperDebug);

		Random random = new Random();

		if (level == 2){
			creeper.addPotionEffect(weakResist);
			creeper.setPowered(false);
		} else if (level == 3){
			creeper.addPotionEffect(weakResist);
			creeper.addPotionEffect(weakSpeed);
			creeper.setPowered(false);
		} else if (level == 4){
			creeper.addPotionEffect(normalResist);
			creeper.addPotionEffect(weakSpeed);
			creeper.setPowered(false);
		} else if (level == 5){
			creeper.addPotionEffect(normalSpeed);
			creeper.addPotionEffect(normalResist);
			creeper.setPowered(false);
			creeper.setExplosionRadius(4);
		} else if (level == 6){
			creeper.addPotionEffect(normalSpeed);
			creeper.addPotionEffect(normalResist);
			creeper.setPowered(random.nextDouble() <= 0.10);
			creeper.setExplosionRadius(4);
			creeper.setMaxFuseTicks(25);
		} else if (level == 7){
			creeper.addPotionEffect(normalSpeed);
			creeper.addPotionEffect(normalResist);
			creeper.setPowered(random.nextDouble() <= 0.25);
			creeper.setExplosionRadius(5);
			creeper.setMaxFuseTicks(25);
		} else if (level == 8){
			creeper.addPotionEffect(normalSpeed);
			creeper.addPotionEffect(normalResist);
			creeper.addPotionEffect(fireResist);
			creeper.setPowered(random.nextDouble() <= 0.33);
			creeper.setExplosionRadius(5);
			creeper.setMaxFuseTicks(20);
		} else if (level == 9){
			creeper.addPotionEffect(normalSpeed);
			creeper.addPotionEffect(strongResist);
			creeper.addPotionEffect(fireResist);
			creeper.setPowered(random.nextDouble() <= 0.50);
			creeper.setExplosionRadius(6);
			creeper.setMaxFuseTicks(20);
		} else if (level == 10){
			creeper.addPotionEffect(normalSpeed);
			creeper.addPotionEffect(strongResist);
			creeper.addPotionEffect(fireResist);
			creeper.setPowered(random.nextDouble() <= 0.66);
			creeper.setExplosionRadius(6);
			creeper.setMaxFuseTicks(20);
		} else if (level == 11){
			creeper.addPotionEffect(strongSpeed);
			creeper.addPotionEffect(strongResist);
			creeper.addPotionEffect(fireResist);
			creeper.setPowered(random.nextDouble() <= 0.75);
			creeper.setExplosionRadius(7);
			creeper.setMaxFuseTicks(15);
		} else if (level == 12){
			creeper.addPotionEffect(strongSpeed);
			creeper.addPotionEffect(strongResist);
			creeper.addPotionEffect(fireResist);
			creeper.addPotionEffect(invisibility);
			creeper.setPowered(true);
			creeper.setExplosionRadius(7);
			creeper.setMaxFuseTicks(15);
		}

		if (level > 6){
			AttributeInstance attributeInstance = creeper.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
			if (attributeInstance != null){
				attributeInstance.setBaseValue(60);
			} else {
				Main.logger.log(Level.WARNING, "Creeper GENERIC_FOLLOW_RANGE attribute was null!");
			}

		}
	}

	@EventHandler
	void onCreeperHit(EntityDamageByEntityEvent event){
		// Player has to be the damager,
		// person getting hit needs to be creeper.
		if (!(event.getEntity() instanceof Creeper) || event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE){
			Main.sendDebugMessage("Creeper wasn't shot",creeperDebug);
			Main.sendDebugMessage("Entity was Creeper: " + (event.getEntity() instanceof Creeper),creeperDebug);
			Main.sendDebugMessage("Damager: " + (event.getDamager()) + ", therefore " + (event.getDamager() instanceof Player),creeperDebug);
			Main.sendDebugMessage("Cause: " + (event.getCause()) + ", therefore " + (event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE),creeperDebug);
			return;
		}
		Main.sendDebugMessage("A creeper was shot",creeperDebug);

		Creeper creeper = (Creeper) event.getEntity();
		if (!(creeper.hasMetadata("Level"))){return;}

		List<MetadataValue> metadataValueList = creeper.getMetadata("Level");
		int level = metadataValueList.get(metadataValueList.size() - 1).asInt();

		double curDamage = event.getDamage();

		if (level == 2 || level == 3){
			Main.sendDebugMessage("Creeper 0.75 damage",creeperDebug);
			event.setDamage(curDamage * 0.75);
		} else if ((level >= 4) && (level < 6)){
			Main.sendDebugMessage("Creeper 0.50 damage",creeperDebug);
			event.setDamage(level * 0.50);
		} else if ((level >= 6) && (level < 9)){
			Main.sendDebugMessage("Creeper 0.25 damage",creeperDebug);
			event.setDamage(level * 0.25);
		} else if (level >= 9){
			Main.sendDebugMessage("Creeper 0.10 damage",creeperDebug);
			event.setDamage(level * 0.10);
		}
	}
}
