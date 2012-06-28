/* This file is part of SellCube.

    SellCube is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SellCube is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with SellCube.  If not, see <http://www.gnu.org/licenses/>.
*/

package pl.bluex.sellcube;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.bluex.sellcube.entities.AdSignManager;


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
        Utils.log(Level.INFO, "Updating " + player_name + " signs");
        AdSignManager.updateSigns(player_name, online);
    }

    /*private void onPlayerActionDelayed(final PlayerEvent event, final boolean online) {
        class R implements Runnable {public void run() {onPlayerAction(event, online);}}
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new R(), 20);
    }*/
}