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
