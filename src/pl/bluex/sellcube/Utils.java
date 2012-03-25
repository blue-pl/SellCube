package pl.bluex.sellcube;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.Directional;
import org.bukkit.util.Vector;

public class Utils {
	public static final Logger logger = Logger.getLogger("Minecraft");
    public static final String pluginName = "SellCube";
    public static final long DAY = 24 * 60 * 60 * 1000;
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");

    public static void log(Level level, String msg) {
        logger.log(level, String.format("[%s] %s", pluginName, msg));
    }

    public static boolean teleport(Player player, Block block) {
        BlockFace dir = ((Directional) block.getType().getNewData(block.getData())).getFacing();
        Vector v = new Vector(dir.getModX(), dir.getModY(), dir.getModZ());
        Location l = block.getLocation().clone();
        l.setPitch(0);
        l.setYaw((float)(Math.atan2(dir.getModX(), -dir.getModZ()) * 180f / (float) Math.PI));
        l.add(v.multiply(2));
        l.add(0.5, 0, 0.5);
        l.getChunk(); // force load chunk
        return SellCube.es.getUser(player).teleport(l, PlayerTeleportEvent.TeleportCause.COMMAND);
    }

    public static boolean setRegionOwner(ProtectedRegion region, String playerName, RegionManager manager) {
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

    public static boolean transferMoney(String fromPlayer, String toPlayer, Double ammount) {
        Player fromPO = Bukkit.getPlayer(fromPlayer);
        if(!SellCube.economy.hasAccount(fromPlayer) || !SellCube.economy.hasAccount(toPlayer)) {
            fromPO.sendMessage(ChatColor.RED + "Blad konta");
            return false;
        }
        if(!SellCube.economy.has(fromPlayer, ammount)) {
            fromPO.sendMessage(ChatColor.RED + "Nie masz wystarczajacej liczby coinow");
            return false;
        }

        SellCube.economy.withdrawPlayer(fromPlayer, ammount);
        SellCube.economy.depositPlayer(toPlayer, ammount);

        fromPO.sendMessage(
                ChatColor.GREEN + "Pobrano " +
                ChatColor.DARK_AQUA + ammount +
                ChatColor.GREEN + "c z twojego konta (stan " +
                ChatColor.DARK_AQUA + SellCube.economy.getBalance(fromPlayer) +
                ChatColor.GREEN + "c)");
        OfflinePlayer toPO = Bukkit.getOfflinePlayer(toPlayer);
        if(toPO.isOnline()) {
            toPO.getPlayer().sendMessage(
                    ChatColor.GREEN + "Przelano " +
                    ChatColor.DARK_AQUA + ammount +
                    ChatColor.GREEN + "c na twoje konto (stan " +
                    ChatColor.DARK_AQUA + SellCube.economy.getBalance(toPlayer) +
                    ChatColor.GREEN + "c)");
        }
        return true;
    }
}
