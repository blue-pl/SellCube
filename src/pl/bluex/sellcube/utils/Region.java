package pl.bluex.sellcube.utils;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

public class Region {
    public static boolean changeOwner(ProtectedRegion region, String playerName, RegionManager manager) {
        try {
            //region.getOwners().removePlayer(sellerName);
            Set<String> owners = region.getOwners().getPlayers();
            for(String s : owners)
                region.getOwners().removePlayer(s);
            region.getOwners().addPlayer(playerName);
            manager.save();
        } catch (IOException e) {
            Utils.log(Level.SEVERE, "Region save error: " +  e.getMessage());
            return false;
        }
        return true;
    }
}
