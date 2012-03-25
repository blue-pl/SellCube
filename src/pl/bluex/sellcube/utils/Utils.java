package pl.bluex.sellcube.utils;

import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.Directional;
import org.bukkit.util.Vector;
import pl.bluex.sellcube.SellCube;

public class Utils {
	public static final Logger logger = Logger.getLogger("Minecraft");
    public static String pluginName = "SellCube";
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
}
