package rezcom.mobbalance.moblevels;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
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


public class SpiderHandler implements Listener {

	public static boolean spiderDebug = false;

	public static PotionEffect weakNausea = new PotionEffect(PotionEffectType.CONFUSION,300,0);
	public static PotionEffect normalNausea = new PotionEffect(PotionEffectType.CONFUSION,300,1);
	public static PotionEffect strongNausea = new PotionEffect(PotionEffectType.CONFUSION,300,2);
	public static PotionEffect severeNausea = new PotionEffect(PotionEffectType.CONFUSION,300,3);

	public static PotionEffect weakPoison = new PotionEffect(PotionEffectType.POISON,300,0);
	public static PotionEffect normalPoison = new PotionEffect(PotionEffectType.POISON,300,1);
	public static PotionEffect strongPoison = new PotionEffect(PotionEffectType.POISON,300,2);
	public static PotionEffect severePoison = new PotionEffect(PotionEffectType.POISON,300,3);

	public static PotionEffect weakSlow = new PotionEffect(PotionEffectType.SLOW,300,0);
	public static PotionEffect normalSlow = new PotionEffect(PotionEffectType.SLOW,300,1);
	public static PotionEffect strongSlow = new PotionEffect(PotionEffectType.SLOW,300,2);
	public static PotionEffect severeSlow = new PotionEffect(PotionEffectType.SLOW,300,3);

	public static PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS,300,0);

	public static PotionEffect weakFatigue = new PotionEffect(PotionEffectType.SLOW_DIGGING,300,1);
	public static PotionEffect normalFatigue = new PotionEffect(PotionEffectType.SLOW_DIGGING,300,2);
	public static PotionEffect strongFatigue = new PotionEffect(PotionEffectType.SLOW_DIGGING,300,3);
	public static PotionEffect severeFatigue = new PotionEffect(PotionEffectType.SLOW_DIGGING,300,4);

	public static PotionEffect weakWeakness = new PotionEffect(PotionEffectType.WEAKNESS,60,0);
	public static PotionEffect normalWeakness = new PotionEffect(PotionEffectType.WEAKNESS,60,1);
	public static PotionEffect strongWeakness = new PotionEffect(PotionEffectType.WEAKNESS,60,2);
	public static PotionEffect severeWeakness = new PotionEffect(PotionEffectType.WEAKNESS,60,3);

	public static PotionEffect severeWither = new PotionEffect(PotionEffectType.WITHER,300,3);
	@EventHandler
	void onSpiderSpawn(CreatureSpawnEvent event){
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM || !(MobLevelHandler.spawnReasons.contains(event.getSpawnReason()))){
			// If a plugin spawned it, don't bother.
			return;
		}
		if (!(event.getEntity() instanceof Spider)){
			return;
		}
		Spider spider = (Spider) event.getEntity();
		Integer level;
		if (MobLevelHandler.crimsonNight){
			level = MobLevelHandler.rollProbability(MobLevelHandler.bloodProbs);
		} else {
			level = MobLevelHandler.rollProbability(MobLevelHandler.defaultProbs);
		}
		if (level == null){
			return;
		}

		spider.setMetadata("Level",new FixedMetadataValue(Main.thisPlugin, level));
		Main.sendDebugMessage("Spawning a level " + level + " spider",spiderDebug);

		PotionEffect weakResist = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,Integer.MAX_VALUE,0);
		PotionEffect normalResist = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,Integer.MAX_VALUE,1);
		PotionEffect strongResist = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE,2);

		PotionEffect weakSpeed = new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,0);
		PotionEffect normalSpeed = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE,1);

		PotionEffect weakStrength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,0);
		PotionEffect normalStrength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,1);
		PotionEffect strongStrength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,2);

		PotionEffect fireResist = new PotionEffect(PotionEffectType.FIRE_RESISTANCE,Integer.MAX_VALUE,0);

		PotionEffect invisible = new PotionEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,0);
		if (level == 1 || level == 2){
			spider.addPotionEffect(weakResist);
			spider.addPotionEffect(weakStrength);
		} else if (level == 3 || level == 4){
			spider.addPotionEffect(normalResist);
			spider.addPotionEffect(normalStrength);
		} else if (level == 5){
			spider.addPotionEffect(normalResist);
			spider.addPotionEffect(weakSpeed);
			spider.addPotionEffect(normalStrength);
		} else if (level == 6){
			spider.addPotionEffect(strongResist);
			spider.addPotionEffect(weakSpeed);
			spider.addPotionEffect(strongStrength);
		} else if (level == 7 || level == 8){
			spider.addPotionEffect(strongResist);
			spider.addPotionEffect(weakSpeed);
			spider.addPotionEffect(strongStrength);
		} else if (level == 9){
			spider.addPotionEffect(strongResist);
			spider.addPotionEffect(weakSpeed);
			spider.addPotionEffect(strongStrength);
		} else if (level == 10 || level == 11){
			spider.addPotionEffect(strongResist);
			spider.addPotionEffect(weakSpeed);
			spider.addPotionEffect(strongStrength);
			spider.addPotionEffect(fireResist);
		} else if (level == 12){
			spider.addPotionEffect(strongResist);
			spider.addPotionEffect(normalSpeed);
			spider.addPotionEffect(strongStrength);
			spider.addPotionEffect(fireResist);
			spider.addPotionEffect(invisible);
		}

		if (level > 6){
			AttributeInstance attributeInstance = spider.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
			if (attributeInstance != null){
				attributeInstance.setBaseValue(60);
			} else {
				Main.logger.log(Level.WARNING, "Spider GENERIC_FOLLOW_RANGE attribute was null!");
			}
		}
	}

	@EventHandler
	void onSpiderHit(EntityDamageByEntityEvent event){
		if (!(event.getEntity() instanceof Spider) || event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE){
			return;
		}
		Main.sendDebugMessage("A spider was shot",spiderDebug);

		Spider spider = (Spider) event.getEntity();
		if (!(spider.hasMetadata("Level"))){return;}

		List<MetadataValue> metadataValueList = spider.getMetadata("Level");
		int level = metadataValueList.get(metadataValueList.size() - 1).asInt();

		double curDamage = event.getDamage();

		if (level <= 4){
			Main.sendDebugMessage("Spider 2/3 damage",spiderDebug);
			event.setDamage(curDamage * 0.66);
		} else if (level <= 9){
			Main.sendDebugMessage("Spider 1/2 damage",spiderDebug);
			event.setDamage(curDamage * 0.50);
		} else {
			Main.sendDebugMessage("Spider 1/3 damage",spiderDebug);
			event.setDamage(curDamage * 0.33);
		}
	}

	@EventHandler
	void applySpiderHit(EntityDamageByEntityEvent event){
		if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Spider)){
			// Player wasn't was who was hit, or damager isn't a spider.
			return;
		}
		Player player = (Player) event.getEntity();
		Spider spider = (Spider) event.getDamager();

		if (!(spider.hasMetadata("Level"))){return;}

		List<MetadataValue> metadataValueList = spider.getMetadata("Level");
		int level = metadataValueList.get(metadataValueList.size() - 1).asInt();

		Random random = new Random();
		if (level == 1){
			if (random.nextDouble() <= 0.15){player.addPotionEffect(weakNausea);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(weakPoison);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(weakFatigue);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(weakSlow);}
		} else if (level == 2){
			if (random.nextDouble() <= 0.20){player.addPotionEffect(weakNausea);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(weakPoison);}
			if (random.nextDouble() <= 0.20){player.addPotionEffect(weakFatigue);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(weakSlow);}
		} else if (level == 3){
			if (random.nextDouble() <= 0.20){player.addPotionEffect(normalNausea);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(normalPoison);}
			if (random.nextDouble() <= 0.20){player.addPotionEffect(weakFatigue);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(weakSlow);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(weakWeakness);}
		} else if (level == 4 || level == 5){
			if (random.nextDouble() <= 0.20){player.addPotionEffect(normalNausea);}
			if (random.nextDouble() <= 0.10){player.addPotionEffect(normalPoison);}
			if (random.nextDouble() <= 0.20){player.addPotionEffect(weakFatigue);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(weakSlow);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(normalWeakness);}
		} else if (level == 6 || level == 7){
			if (random.nextDouble() <= 0.20){player.addPotionEffect(strongNausea);}
			if (random.nextDouble() <= 0.10){player.addPotionEffect(normalPoison);}
			if (random.nextDouble() <= 0.20){player.addPotionEffect(normalFatigue);}
			if (random.nextDouble() <= 0.16){player.addPotionEffect(normalSlow);}
			if (random.nextDouble() <= 0.16){player.addPotionEffect(normalWeakness);}
			if (random.nextDouble() <= 0.16){player.addPotionEffect(blindness);}
		} else if (level == 8 || level == 9){
			if (random.nextDouble() <= 0.20){player.addPotionEffect(strongNausea);}
			if (random.nextDouble() <= 0.10){player.addPotionEffect(normalPoison);}
			if (random.nextDouble() <= 0.20){player.addPotionEffect(strongFatigue);}
			if (random.nextDouble() <= 0.16){player.addPotionEffect(strongSlow);}
			if (random.nextDouble() <= 0.16){player.addPotionEffect(strongWeakness);}
			if (random.nextDouble() <= 0.20){player.addPotionEffect(blindness);}
		} else if (level == 10){
			if (random.nextDouble() <= 0.25){player.addPotionEffect(severeNausea);}
			if (random.nextDouble() <= 0.10){player.addPotionEffect(strongPoison);}
			if (random.nextDouble() <= 0.20){player.addPotionEffect(severeFatigue);}
			if (random.nextDouble() <= 0.20){player.addPotionEffect(strongSlow);}
			if (random.nextDouble() <= 0.20){player.addPotionEffect(strongWeakness);}
			if (random.nextDouble() <= 0.33){player.addPotionEffect(blindness);}
		} else if (level == 11){
			if (random.nextDouble() <= 0.33){player.addPotionEffect(severeNausea);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(severePoison);}
			if (random.nextDouble() <= 0.33){player.addPotionEffect(severeFatigue);}
			if (random.nextDouble() <= 0.20){player.addPotionEffect(severeSlow);}
			if (random.nextDouble() <= 0.33){player.addPotionEffect(severeWeakness);}
			if (random.nextDouble() <= 0.66){player.addPotionEffect(blindness);}
		} else if (level == 12){
			if (random.nextDouble() <= 0.66){player.addPotionEffect(severeNausea);}
			if (random.nextDouble() <= 0.15){player.addPotionEffect(severeWither);}
			if (random.nextDouble() <= 0.33){player.addPotionEffect(severeFatigue);}
			if (random.nextDouble() <= 0.33){player.addPotionEffect(severeSlow);}
			if (random.nextDouble() <= 0.66){player.addPotionEffect(severeWeakness);}
			if (random.nextDouble() <= 0.66){player.addPotionEffect(blindness);}
		}
	}
}
