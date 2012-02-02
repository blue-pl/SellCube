package pl.bluex.sellcube;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerListener;


public class PlayerLogin extends PlayerListener {
	private SellCube plugin;

	public PlayerLogin(SellCube instance){
		plugin = instance;
	}

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        onPlayerActionDelayed(event);
    }

    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {
        onPlayerActionDelayed(event);
    }

    @Override
    public void onPlayerKick(final PlayerKickEvent event) {
        onPlayerActionDelayed(event);
    }



    private void onPlayerAction(PlayerEvent event) {
        try {
            String player_name = event.getPlayer().getName();
            plugin.info("Updating " + player_name + " signs");
            ResultSet rs = plugin.getPlayerAd(player_name, false);
            while(rs.next()) {
                Block block = Bukkit.getWorld(rs.getString("sign_world")).getBlockAt(rs.getInt("sign_x"), rs.getInt("sign_y"), rs.getInt("sign_z"));
                if (!(block.getState() instanceof Sign)) {
                    plugin.removeAd(block);
                    continue;
                }
                plugin.updateSign((Sign)block.getState(), rs.getString("owner"));
            }
        } catch (SQLException e) {
            plugin.severe("SQL exception: " + e.getMessage());
        }
    }

    private void onPlayerActionDelayed(final PlayerEvent event) {
        class R implements Runnable {public void run() {onPlayerAction(event);}}
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new R(), 20);
    }
}