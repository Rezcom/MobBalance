package rezcom.mobbalance.moblevels;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class ZombieHandler implements Listener {

	@EventHandler
	void onZombieSpawn(CreatureSpawnEvent event){
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM || !(MobLevelHandler.spawnReasons.contains(event.getSpawnReason()))){
			// If a plugin spawned it, don't bother.
			return;
		}
		if (event.getEntity().getType() != EntityType.ZOMBIE){
			return; // Not a zombie, don't bother.
		}

		Zombie zombie = (Zombie) event.getEntity();
		Integer level;
		if (MobLevelHandler.crimsonNight){
			// BLOOD MOON!
			level = MobLevelHandler.rollProbability(MobLevelHandler.bloodProbs);
		} else {
			level = MobLevelHandler.rollProbability(MobLevelHandler.defaultProbs);
		}
		if (level == null){return;}

		if (level == 1){

		} else if (level == 2){

		} else if (level == 3){

		}

	}
}
