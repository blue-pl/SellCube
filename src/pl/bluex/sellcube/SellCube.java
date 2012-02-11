package pl.bluex.sellcube;

import com.avaje.ebean.EbeanServer;
import com.earth2me.essentials.Essentials;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.PersistenceException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.Directional;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class SellCube extends JavaPlugin {
	protected static final Logger logger = Logger.getLogger("Minecraft");
    protected static final HashMap<Player, AdSign> newAds = new HashMap<Player, AdSign>();
    protected static final HashMap<String, String> groupsColors = new HashMap<String, String>();
    protected static EbeanServer database;
    protected static String pluginName = "SellCube";

    protected static PluginManager pm;
    protected static WorldGuardPlugin wg;
	protected static PermissionManager pex;
    protected static Essentials es;
    protected static LWC lwc;

    private PlayerInteract playerInteract = null;
    private PlayerLogin playerLogin = null;
    private SignInteract signInteract = null;
    private SellCubeCommand sellCubeCommand = null;

	@Override
	public void onEnable() {
		pm = getServer().getPluginManager();
        pluginName = getDescription().getName();
		FileConfiguration config = getConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        AdSignManager.offlineDays = config.getInt("offline_days");
        boolean updater = config.getBoolean("sign_updater");
        setupDatabase();
        setupEssentials();
		if (setupWorldGuard() && setupPermissions() && setupLWC()) {
			playerInteract = new PlayerInteract(this);
            playerLogin = new PlayerLogin(this);
            signInteract = new SignInteract(this);
            sellCubeCommand = new SellCubeCommand(this);
			getCommand("sellcube").setExecutor(sellCubeCommand);
            getCommand("scadd").setExecutor(sellCubeCommand);
            getCommand("sccancel").setExecutor(sellCubeCommand);
            getCommand("scstatus").setExecutor(sellCubeCommand);
            getCommand("sctp").setExecutor(sellCubeCommand);
            getCommand("scfind").setExecutor(sellCubeCommand);
            getCommand("sccopy").setExecutor(sellCubeCommand);
            if(updater) {
                getServer().getScheduler().scheduleAsyncRepeatingTask(this,
                        new SignUpdater(this), 100L, 864000L);
            }
		}
        ConfigurationSection gc = config.getConfigurationSection("colors");
        if(gc != null) {
            String s;
            for (String key : gc.getKeys(true)) {
                s = gc.get(key).toString();
                if(s.matches("[0-9A-Fa-f]")) {
                    groupsColors.put(key, "§" + s.charAt(0));
                }
            }
        }
		log(Level.INFO, String.format("%s is enabled.", getDescription().getFullName()));
	}

	@Override
	public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
		log(Level.INFO, String.format("%s is disabled.", getDescription().getFullName()));
	}

    protected static void log(Level level, String msg) {
        logger.log(level, String.format("[%s] %s", pluginName, msg));
    }

    protected static boolean checkPermission(Player player, String node) {
        return checkPermission(player, node, true);
    }

    protected static boolean checkPermission(String player, String node, String world) {
		return pex.has(player, node, world);
	}

	protected static boolean checkPermission(Player player, String node, boolean msg) {
		//boolean permission = (pex != null) ? pex.has(player, node) : player.hasPermission(node);
        boolean permission = pex.has(player, node);
		if(permission == false && msg == true) {
			player.sendMessage(ChatColor.RED + "Nie masz wystarczajacych uprawnien.");
		}
		return permission;
	}

	protected boolean setupPermissions() {
		if (pex == null) {
            if(getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
                pex = PermissionsEx.getPermissionManager();
            }
            else {
				log(Level.SEVERE, "PermissionsEx plugin detection failed");
				pex = null;
				return false;
			}
		}
		return true;
	}
	
	protected boolean setupWorldGuard() {
		if (wg == null) {
			Plugin worldGuard = pm.getPlugin("WorldGuard");

			if (worldGuard == null || !(worldGuard instanceof WorldGuardPlugin)) {
				log(Level.SEVERE, "WorldGuard detection failed.");
				wg = null;
				return false;
			} else {
				wg = (WorldGuardPlugin) worldGuard;
			}
		}
		return true;
	}

    protected boolean setupEssentials() {
		if (es == null) {
			Plugin essentials = pm.getPlugin("Essentials");

			if (essentials == null || !(essentials instanceof Essentials)) {
				log(Level.WARNING, "Essentials detection failed.");
				es = null;
				return false;
			} else {
				es = (Essentials) essentials;
			}
		}
		return true;
	}

    protected boolean setupLWC() {
		if (lwc == null) {
			Plugin lwc_plug = pm.getPlugin("LWC");

			if (lwc_plug == null || !(lwc_plug instanceof LWCPlugin)) {
				log(Level.SEVERE, "LWC detection failed.");
				lwc = null;
				return false;
			} else {
				lwc = ((LWCPlugin)lwc_plug).getLWC();
			}
		}
		return true;
	}
	
	private void setupDatabase() {
        try {
            getDatabase().find(AdSign.class).findRowCount();
        } catch (PersistenceException ex) {
            log(Level.INFO, "Setting up database");
            installDDL();
        }
        database = getDatabase();
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(AdSign.class);
        return list;
    }

    protected static String getPlayerGroupColor(String player) {
        for (String g : SellCube.pex.getUser(player).getGroupsNames()) {
            if(groupsColors.containsKey(g)) {
                return groupsColors.get(g);
            }
        }
        return "§f";
    }

    protected static boolean teleport(Player player, Block block) {
        BlockFace dir = ((Directional) block.getType().getNewData(block.getData())).getFacing();
        Vector v = new Vector(dir.getModX(), dir.getModY(), dir.getModZ());
        Location l = block.getLocation().clone();
        l.setPitch(0);
        l.setYaw((float)(Math.atan2(dir.getModX(), -dir.getModZ()) * 180f / (float) Math.PI));
        l.add(v.multiply(2));
        l.add(0.5, 0, 0.5);
        l.getChunk(); // force load chunk
        return es.getUser(player).teleport(l, PlayerTeleportEvent.TeleportCause.COMMAND);
    }
}
