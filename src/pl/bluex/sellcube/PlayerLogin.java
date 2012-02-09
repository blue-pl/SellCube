package pl.bluex.sellcube;

import java.util.logging.Level;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;


public class PlayerLogin extends PlayerListener {
	private SellCube plugin;

	public PlayerLogin(SellCube instance){
		plugin = instance;
	}

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        onPlayerAction(event, true);
    }

    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {
        onPlayerAction(event, false);
    }
    
    private void onPlayerAction(PlayerEvent event, boolean online) {
        String player_name = event.getPlayer().getName();
        SellCube.log(Level.INFO, "Updating " + player_name + " signs");
        AdSignManager.updateSigns(player_name, online);
    }

    private void onPlayerActionDelayed(final PlayerEvent event, final boolean online) {
        class R implements Runnable {public void run() {onPlayerAction(event, online);}}
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new R(), 20);
    }
}