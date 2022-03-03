package rezcom.mobbalance.XPView;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class XPGainHandler implements Listener {

	@EventHandler
	void onXPGain(PlayerPickupExperienceEvent event){
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		if (XPViewCommand.XPViewPlayers.containsKey(uuid) && XPViewCommand.XPViewPlayers.get(uuid)) {
			player.sendMessage("Gained " + event.getExperienceOrb().getExperience() + " Experience Points");
		}
	}
}
