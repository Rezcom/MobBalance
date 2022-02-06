package rezcom.mobbalance.moblevels;

import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import rezcom.mobbalance.Main;

import java.util.*;
import java.util.logging.Level;

public class MobLevelHandler {

	public static boolean crimsonNight = false;

	// Spawn Reasons that would trigger levels
	public static final ArrayList<CreatureSpawnEvent.SpawnReason> spawnReasons = new ArrayList<>(Arrays.asList(
			CreatureSpawnEvent.SpawnReason.NATURAL,
			CreatureSpawnEvent.SpawnReason.LIGHTNING,
			CreatureSpawnEvent.SpawnReason.REINFORCEMENTS,
			CreatureSpawnEvent.SpawnReason.SPAWNER,
			CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
			CreatureSpawnEvent.SpawnReason.COMMAND,
			CreatureSpawnEvent.SpawnReason.DEFAULT
	));

	// Probability Maps are in the form of the following:
	// <Level of Mob, Probability%>
	public static Map<Integer, Double> defaultProbs = new HashMap<Integer, Double>(){{
		put(0, 0.21);
		put(1, 0.17);
		put(2, 0.15);
		put(3, 0.13);
		put(4, 0.10);
		put(5, 0.07);
		put(6, 0.05);
		put(7, 0.04);
		put(8, 0.03);
		put(9, 0.02);
		put(10,0.01);
		put(11,0.01);
		put(12,0.01);
	}};

	public static Map<Integer, Double> bloodProbs = new HashMap<Integer, Double>(){{
		put(6, 0.20);
		put(7, 0.20);
		put(8, 0.18);
		put(9, 0.15);
		put(10,0.14);
		put(11,0.08);
		put(12,0.05);
	}};

	public static void determineEXP(EntityDeathEvent event){
		// Please only call this if a player killed
		// with 6 or less kills in the chunk counter.
		// This method does NOT check that those conditions are fulfilled.

	}

	private static boolean isValid(Map<Integer,Double> probMap){
		Double sum = 0.0;
		for (Map.Entry<Integer,Double> entry : probMap.entrySet()){
			sum += entry.getValue();
		}
		return sum == 1.0;
	}
	public static Integer rollProbability(Map<Integer,Double> probMap){
		if (!(isValid(probMap))){
			Main.logger.log(Level.SEVERE, "Probability map that was passed was not valid! Sum of probabilities was not 1.0; check your code!!");
			return null;
		}
		ArrayList<Integer> levels = new ArrayList<>();
		ArrayList<Double> probabilities = new ArrayList<>();
		for (Map.Entry<Integer,Double> entry : probMap.entrySet()){
			levels.add(entry.getKey());
			probabilities.add(entry.getValue());
		}
		Double base = 0.0;
		Random random = new Random();
		double result = random.nextDouble();
		for (int i = 0; i < levels.size(); i++){
			if (result <= probabilities.get(i)){
				// Roll Success
				return levels.get(i);
			}
			// Roll Failure
			base += probabilities.get(i);
		}
		// All rolls failed, just return last one.
		// This should never happen...
		return levels.get(levels.size() - 1);
	}
}
