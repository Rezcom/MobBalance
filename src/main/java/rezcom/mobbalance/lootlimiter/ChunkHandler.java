package rezcom.mobbalance.lootlimiter;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import rezcom.mobbalance.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ChunkHandler implements Listener {

	// Counters of how many hostile mobs gave loot in each chunk.
	// Form is this: Map<World Key, Map<ChunkKey, Counter>>
	public static Map<NamespacedKey,Map<Long,Integer>> chunkCounters = new HashMap<>();
	public static boolean lootDebug = false;
	public static boolean unloadDebug = true;

	@EventHandler
	void onChunkLoad(ChunkLoadEvent event){

		// When a chunk loads, ensure it's in the map.

		NamespacedKey worldKey = event.getWorld().getKey();
		Chunk chunk = event.getChunk();
		long chunkKey = chunk.getChunkKey();

		if (chunkCounters.containsKey(worldKey)){
			// CONTAINS WORLD KEY
			Map<Long,Integer> chunksMap = chunkCounters.get(worldKey);
			if (chunksMap != null && !chunksMap.containsKey(chunkKey)) {
				// Chunk isn't in map yet.
				chunksMap.put(chunkKey,0);
			}
			Main.sendDebugMessage("World existed, ensuring chunk exists",lootDebug);
		} else {
			// DOESN'T CONTAIN WORLD KEY
			chunkCounters.put(worldKey, new HashMap<>());
			chunkCounters.get(worldKey).put(chunkKey, 0);

			Main.sendDebugMessage("World didn't exist, added.",lootDebug);
		}

		Main.sendDebugMessage("Load Event finished. Number of Chunks in this world: " + chunkCounters.get(worldKey).size(),lootDebug);
	}

	public static void purgeChunks(Server server){
		// Remove from the hashmap all chunks that aren't currently loaded
		for (World world : server.getWorlds()){
			NamespacedKey worldKey = world.getKey();
			Chunk[] loadedChunks = world.getLoadedChunks();

			removeChunks(loadedChunks,chunkCounters.get(worldKey));
		}
	}

	private static void removeChunks(Chunk[] loadedChunks, Map<Long,Integer> chunksMap){
		ArrayList<Long> chunkKeys = new ArrayList<Long>();
		for (Chunk chunk : loadedChunks){
			// Figure out all the chunk keys in the loaded chunks
			chunkKeys.add(chunk.getChunkKey());
		}

		if (chunksMap == null){
			return;
		}
		int original = chunksMap.size();

		// If the key isn't it chunkKeys...
		chunksMap.entrySet().removeIf(entry -> !chunkKeys.contains(entry.getKey()));

		int after_removal = chunksMap.size();
		Main.logger.log(Level.INFO,"Chunks removed from map: " + (original - after_removal));
	}

	public static void decreaseAllOtherCounters(NamespacedKey worldKey, long chunkKey){
		Map<Long,Integer> worldChunks = chunkCounters.get(worldKey);
		for (Map.Entry<Long,Integer> entry : worldChunks.entrySet()){
			if (entry.getValue() == null){
				entry.setValue(0);
			}
			if (entry.getKey() != chunkKey && entry.getValue() > 0){
				entry.setValue(entry.getValue() - 1);
			}
		}
	}

	public static void updateChunkValue(Map<Long,Integer> chunkMap, NamespacedKey worldKey, long chunkKey,Integer amount,Integer max){

		// Call this early to ensure that the chunk key isn't null!!
		// Updates a chunk value by the given amount, but not over a given maximum.
		// Decreases all other chunks by 1
		decreaseAllOtherCounters(worldKey,chunkKey);
		if (chunkMap.containsKey(chunkKey)){
			if (chunkMap.get(chunkKey) == null){
				chunkMap.replace(chunkKey,1);
			} else {
				chunkMap.replace(chunkKey, Integer.min(max,chunkMap.get(chunkKey) + amount));
			}
		} else {
			chunkMap.put(chunkKey,Integer.min(amount,max));
		}
	}

	public static void refreshAllChunks(){
		for (Map.Entry<NamespacedKey,Map<Long,Integer>> entryWorld : chunkCounters.entrySet()){
			refreshChunk(entryWorld.getValue());
		}
	}

	private static void refreshChunk(Map<Long,Integer> chunkMap){
		// Ensures that all counters are NOT null
		for (Map.Entry<Long,Integer> entryChunk : chunkMap.entrySet()){
			if (entryChunk.getValue() == null){
				entryChunk.setValue(0);
			}
		}
	}



}
