package rezcom.mobbalance.lootlimiter;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import rezcom.mobbalance.Main;
import rezcom.mobbalance.moblevels.MobLevelHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LootLimiter implements Listener {

	static private final boolean limiterDebug = false;

	@EventHandler
	void onNonAnimalDeath(EntityDeathEvent event){
		LivingEntity livingEntity = event.getEntity();
		if (livingEntity.customName() != null || (livingEntity instanceof Animals) || (livingEntity instanceof Player)){
			// Named entity or Player, or an animal, which will be handled in another method.
			// Do nothing
			return;
		}
		Main.sendDebugMessage("Non-Animal death",limiterDebug);
		if (livingEntity.getKiller() != null){
			// A player killed it.
			Main.sendDebugMessage("Player Killed it",limiterDebug);
			NamespacedKey worldKey = event.getEntity().getWorld().getKey();
			long chunkKey = event.getEntity().getChunk().getChunkKey();

			Map<Long,Integer> curChunkMap = ChunkHandler.chunkCounters.get(worldKey);
			ChunkHandler.updateChunkValue(curChunkMap,worldKey,chunkKey,1,35);

			Integer numOfKills = curChunkMap.get(chunkKey);

			if (numOfKills <= 6){
				// 6 or less player kills, business as usual.
				MobLevelHandler.determineEXP(event);
			} else {
				// If 7 or more kills, no EXP
				event.setDroppedExp(0);
			}

			if (numOfKills > 8 && numOfKills <= 11){
				// If 9 or more kills, reduced drops
				reduceItemStackList(event.getDrops(),0.3);
			} else if (numOfKills > 11){
				// If 12 or more kills, no drops.
				event.getDrops().clear();
			}


		} else {
			// IT WASN'T A PLAYER WHO KILLED IT!
			// NO EXP OR DROPS
			event.getDrops().clear();
			event.setDroppedExp(0);
		}
	}

	// Animals give normal drops if killed by a player and
	// less than 7 kills in the current chunk. Most lenient!
	@EventHandler
	void onAnimalDeath(EntityDeathEvent event){
		LivingEntity livingEntity = event.getEntity();
		if (livingEntity.customName() != null || (livingEntity instanceof Player)){
			// Named Entity, or a Player, do nothing
			Main.sendDebugMessage("Named Entity, did nothing.",limiterDebug);
			return;
		}
		Main.sendDebugMessage("Not a named entity, limits apply.",limiterDebug);
		if (livingEntity instanceof Animals){
			// Entity is an animal
			NamespacedKey worldKey = event.getEntity().getWorld().getKey();
			Map<Long,Integer> curChunkMap = ChunkHandler.chunkCounters.get(worldKey);
			long chunkKey = event.getEntity().getChunk().getChunkKey();


			if (livingEntity.getKiller() != null){
				// Killer was a player and there's less than 7 kills in this chunk
				ChunkHandler.updateChunkValue(curChunkMap,worldKey,chunkKey,1,35);
				Integer numOfKills = curChunkMap.get(chunkKey);
				if (numOfKills <= 7){
					return;
				}

			}
			// Killer wasn't a player, or too many kills, give less rewards.
			// Less EXP
			event.setDroppedExp(event.getDroppedExp() / 2);

			// Less Drops, 40% of drops on average
			reduceItemStackList(event.getDrops(), 0.4);
		}
	}

	public static void reduceItemStackList(List<ItemStack> itemStacks, double prob){
		List<ItemStack> lessenedDrops = new ArrayList<>();
		Random random = new Random();
		for (ItemStack itemStack : itemStacks){
			if (random.nextDouble() <= prob){
				lessenedDrops.add(itemStack);
			}
		}
		itemStacks.clear();
		itemStacks.addAll(lessenedDrops);
	}
}
