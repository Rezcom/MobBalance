package rezcom.mobbalance.moblevels;

import org.bukkit.entity.Phantom;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class PhantomHandler implements Listener {

    @EventHandler
    void onPhantomSpawn(CreatureSpawnEvent event){
        if (event.getEntity() instanceof Phantom){
            event.setCancelled(true);
        }
    }
}
