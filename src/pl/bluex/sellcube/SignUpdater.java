
package pl.bluex.sellcube;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;


public class SignUpdater implements Runnable {
    private SellCube plugin;
	
	public SignUpdater(SellCube instance) {
		this.plugin = instance;
	}

    @Override
    public void run() {
        try {
            plugin.info("Updating signs");
            ResultSet rs = plugin.getDeactivatedAd();
            while(rs.next()) {
                Block block = new Location(Bukkit.getWorld(rs.getString("sign_world")), rs.getInt("sign_x"), rs.getInt("sign_y"), rs.getInt("sign_z")).getBlock();
                if (!(block.getState() instanceof Sign)) {
                    plugin.removeAd(block);
                    continue;
                }
                if(!rs.getBoolean("active")) {
                    plugin.updateSign((Sign)block.getState(), rs.getString("owner"));
                }
            }
        } catch (SQLException e) {
            plugin.severe("SQL exception: " + e.getMessage());
        }
    }

}
