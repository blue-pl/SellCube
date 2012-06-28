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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import pl.bluex.sellcube.entities.AdSign;
import pl.bluex.sellcube.entities.AdSignManager;

public class SignInteract implements Listener {
	
	public SignInteract(SellCube plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
    @EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.isCancelled()) return;
		Block block  = event.getBlock();
		Player player = event.getPlayer();
        if (!(block.getState() instanceof Sign)) return;
        AdSign ad = AdSignManager.get(block);
        if(ad != null) {
            if(SellCube.lwc.findProtection(block) == null || SellCube.lwc.canAdminProtection(player, block)) {
                AdSignManager.remove(ad);
                player.sendMessage(ChatColor.BLUE + "Ogloszenie usuniete");

            } else {
                event.setCancelled(true);
            }
        }
	}

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignChange(SignChangeEvent event) {
        if(event.isCancelled()) return;
		Block block  = event.getBlock();
        if (!(block.getState() instanceof Sign)) return;
        AdSign ad = AdSignManager.get(block);
        if(ad != null) {
            event.setCancelled(true);
        }
    }
}
