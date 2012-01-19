package pl.bluex.sellcube;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.BlockListener;

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
        try {
            ResultSet rs = plugin.getAd(block);
            rs = (rs.next()) ? rs : null;
            if(rs != null) {
                /*if(SellCube.checkPermission(player, "sellcube.destroy")
                        && (player.getName().equals(rs.getString("owner"))
                        || SellCube.checkPermission(player, "sellcube.destroy_all"))) {*/
                    plugin.removeAd(block);
                    player.sendMessage(ChatColor.BLUE + "Ogloszenie usuniete");

                /*} else {
                    event.setCancelled(true);
                }*/
            }
        } catch (SQLException e) {
            plugin.severe("SQL exception: " + e.getMessage());
        }
	}

    @Override
    public void onSignChange(SignChangeEvent event) {
        if(event.isCancelled()) return;
		Block block  = event.getBlock();
        if (!(block.getState() instanceof Sign)) return;
        try {
            ResultSet rs = plugin.getAd(block);
            rs = (rs.next()) ? rs : null;
            if(rs != null) {
                event.setCancelled(true);
            }
        } catch (SQLException e) {
            plugin.severe("SQL exception: " + e.getMessage());
        }
    }
}
