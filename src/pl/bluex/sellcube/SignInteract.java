package pl.bluex.sellcube;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

public class SignInteract extends BlockListener {
	private SellCube plugin;
	
	public SignInteract(SellCube instance) {
		this.plugin = instance;
	}
	
    @Override
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.isCancelled()) return;
		Block block  = event.getBlock();
		Player player = event.getPlayer();
        if (!(block.getState() instanceof Sign)) return;
        AdSign ad = AdSign.get(block);
        if(ad != null) {
            if(SellCube.lwc.findProtection(block) == null || SellCube.lwc.canAdminProtection(player, block)) {
                ad.remove();
                player.sendMessage(ChatColor.BLUE + "Ogloszenie usuniete");

            } else {
                event.setCancelled(true);
            }
        }
	}

    @Override
    public void onSignChange(SignChangeEvent event) {
        if(event.isCancelled()) return;
		Block block  = event.getBlock();
        if (!(block.getState() instanceof Sign)) return;
        AdSign ad = AdSign.get(block);
        if(ad != null) {
            event.setCancelled(true);
        }
    }
}
