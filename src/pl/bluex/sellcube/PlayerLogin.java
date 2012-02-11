package pl.bluex.sellcube;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class PlayerLogin implements Listener {

	public PlayerLogin(SellCube plugin){
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        onPlayerAction(event, true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        onPlayerAction(event, false);
    }
    
    private void onPlayerAction(PlayerEvent event, boolean online) {
        String player_name = event.getPlayer().getName();
        SellCube.log(Level.INFO, "Updating " + player_name + " signs");
        AdSignManager.updateSigns(player_name, online);
    }

    /*private void onPlayerActionDelayed(final PlayerEvent event, final boolean online) {
        class R implements Runnable {public void run() {onPlayerAction(event, online);}}
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new R(), 20);
    }*/
}