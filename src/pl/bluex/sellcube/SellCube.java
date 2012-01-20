package pl.bluex.sellcube;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.block.Sign;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import lib.PatPeter.SQLibrary.MySQL;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import sl.nuclearw.firstlastseen.firstlastseen;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import java.text.SimpleDateFormat;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class SellCube extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	protected String logPrefix;
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
	protected final HashMap<Player, String> newAdsRN = new HashMap<Player, String>();
    protected final HashMap<Player, Float> newAdsP = new HashMap<Player, Float>();
    protected final HashMap<Player, Boolean> newAdsLWC = new HashMap<Player, Boolean>();
    protected final HashMap<Player, Integer> newAdsID = new HashMap<Player, Integer>();
	protected PluginManager pm;
	protected static WorldGuardPlugin wg;
	protected static PermissionManager pex;
    protected static LWC lwc;
	protected static MySQL db;
	protected String pluginName = "SellCube";
    protected String holidaysTab;
	protected FileConfiguration config;
    protected ConfigurationSection groupsColors;
	
	private String dbHost;
	private String dbPort;
	private String dbUser;
	private String dbPass;
	private String dbName;
    private int offlineDays;
    private boolean updater;

    private final long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;

	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		pluginName = pdfFile.getName();
        holidaysTab = pluginName + "_holidays";
		logPrefix = "[" + pluginName + "] ";
		pm = getServer().getPluginManager();
		config = getConfig();
		dbHost = config.getString("database.hostname", "localhost");
		dbPort = config.getString("database.port", "3306");
		dbUser = config.getString("database.user", "minecraft");
		dbPass = config.getString("database.password", "");
		dbName = config.getString("database.name", "minecraft");
        offlineDays = config.getInt("misc.offline_days", 21);
        updater = config.getBoolean("misc.sign_updater", false);
        groupsColors = config.getConfigurationSection("colors");
        if(config.getConfigurationSection("database") == null) {
            config.set("database.hostname", dbHost);
            config.set("database.port", dbPort);
            config.set("database.user", dbUser);
            config.set("database.password", dbPass);
            config.set("database.dbname", dbName);
            config.set("misc.offline_days", offlineDays);
            config.set("misc.sign_updater", updater);
            saveConfig();
            info("Config saved");
            return;
        }
		if (setupWorldGuard() && setupPermissions() && setupLWC() && setupDB()) {
			pm.registerEvent(Event.Type.PLAYER_INTERACT, 
					new PlayerInteract(this), Event.Priority.Highest, this);
            pm.registerEvent(Event.Type.PLAYER_JOIN,
					new PlayerLogin(this), Event.Priority.Normal, this);
            pm.registerEvent(Event.Type.PLAYER_QUIT,
					new PlayerLogin(this), Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.BLOCK_BREAK, 
					new SignInteract(this), Event.Priority.Normal, this);
			getCommand("sellcube").setExecutor(new SellCubeCommand(this));
            if(updater) {
                getServer().getScheduler().scheduleAsyncRepeatingTask(this,
                        new SignUpdater(this), 100L, 864000L);
            }
		}

		info(pluginName + " v" + pdfFile.getVersion() + " is enabled.");
	}

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		info(pluginName + " v" + pdfFile.getVersion() + " is disabled.");
	}

    protected static boolean checkPermission(Player player, String node) {
        return checkPermission(player, node, true);
    }

	protected static boolean checkPermission(Player player, String node, boolean msg) {
		boolean permission = (pex != null) ? pex.has(player, node) : player.hasPermission(node);
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
				info("PermissionsEx plugin detection failed");
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
				info("WorldGuard detection failed.");
				wg = null;
				return false;
			} else {
				wg = (WorldGuardPlugin) worldGuard;
			}
		}
		return true;
	}

    protected boolean setupLWC() {
		if (lwc == null) {
			Plugin lwc_plug = pm.getPlugin("LWC");

			if (lwc_plug == null || !(lwc_plug instanceof LWCPlugin)) {
				info("LWC detection failed.");
				lwc = null;
				return false;
			} else {
				lwc = ((LWCPlugin)lwc_plug).getLWC();
			}
		}
		return true;
	}
	
	protected boolean setupDB() {
		db = new MySQL(log, logPrefix, dbHost, dbPort, dbName, dbUser, dbPass);
		if(db.open() == null) {
			info("MySQL connection failed.");
			db = null;
			return false;
		}
		if(!db.checkTable(pluginName)) {
			info("Creating table: " + pluginName);
			db.createTable("CREATE TABLE " + pluginName + " (" +
					"id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
					"owner VARCHAR(255), " +
					"region VARCHAR(255), " +
                    "price DECIMAL(6,2), " +
                    "active BOOL, " +
                    "lwc_pass BOOL, " +
                    "sign_world VARCHAR(255), " +
					"sign_x INT, sign_y INT, sign_z INT, " +
					"tp_x INT, tp_y INT, tp_z INT);");
		}
        if(!db.checkTable(holidaysTab)) {
			info("Creating table: " + holidaysTab);
			db.createTable("CREATE TABLE " + holidaysTab + " (" +
					"id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
					"user VARCHAR(255), " +
					"end DATE);");
		}
		return true;
	}

    protected void info(String msg) {
        log.info(logPrefix + msg);
    }

    protected void warning(String msg) {
        log.warning(logPrefix + msg);
    }

    protected void severe(String msg) {
        log.severe(logPrefix + msg);
    }

    protected void addAd(String player, String reg, float price, Block block, boolean lwc_pass) {
        addAd(player, reg, price, block, lwc_pass, true);
    }

	protected void addAd(String player, String reg, float price, Block block, boolean lwc_pass, boolean active) {
		db.query(String.format("INSERT INTO %s " +
				"(owner, region, price, active, lwc_pass, sign_world, sign_x, sign_y, sign_z) " +
				"VALUES ('%s', %s, %.2f, %d, %d, '%s', %d, %d, %d)",
			pluginName, player, (reg!=null)?"'"+reg+"'":"NULL", price, active?1:0, lwc_pass?1:0,
            block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
        SellCube.lwc.getPhysicalDatabase().registerProtection(
            block.getTypeId(), Protection.Type.PRIVATE,
            block.getWorld().getName(), player, "",
            block.getX(), block.getY(), block.getZ());
	}
	
	protected ResultSet getAd(String region_name) {
		return db.query(String.format("SELECT * FROM %s "
                + "WHERE region='%s'", pluginName, region_name));
	}

    protected ResultSet getAd(Block block) {
		return db.query(String.format("SELECT * FROM %s "
                + "WHERE sign_world='%s' AND sign_x=%d AND sign_y=%d AND sign_z=%d",
            pluginName, block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
	}

    protected ResultSet getAd(int id) {
		return db.query(String.format("SELECT * FROM %s "
                + "WHERE id=%d", pluginName, id));
	}

    protected void deactivateAd(Block block, String player) {
        db.query(String.format("UPDATE %s SET active=%d, owner='%s'"
                + "WHERE sign_world='%s' AND sign_x=%d AND sign_y=%d AND sign_z=%d",
            pluginName, 0, player, block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
    }

    protected ResultSet getDeactivatedAd() {
		return db.query(String.format("SELECT * FROM %s WHERE active=0", pluginName));
	}

    protected ResultSet getPlayerAd(String player_name) {
		return db.query(String.format("SELECT * FROM %s WHERE owner='%s'", pluginName, player_name));
	}
	
	protected void removeAd(Block block) {
		db.query(String.format("DELETE FROM %s "
                + "WHERE sign_world='%s' AND sign_x=%d AND sign_y=%d AND sign_z=%d",
            pluginName, block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
        Protection protection = SellCube.lwc.findProtection(block);
        if(protection != null) {
            protection.remove();
            protection.save();
        }
	}
	
	protected void addAdTP(int tx, int ty, int tz) {
		//TODO: db.query("UPDATE %s SET tp_x=%d, tp_y=%d, tp_z=%d WHERE id=%d");
	}

    protected void addHoliday(String player, Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");
		db.query(String.format("INSERT INTO %s (user, end) VALUES ('%s', '%s')",
			holidaysTab, player, formatter.format(date)));
	}

	protected ResultSet getHoliday(String player) {
		return db.query(String.format("SELECT * FROM %s "
                + "WHERE user='%s'", holidaysTab, player));
	}

    protected void updateSign(Sign sign, String player_name) {
        long time = firstlastseen.getLastSeenLong(player_name);
        long now = new Date().getTime();
        OfflinePlayer player = getServer().getOfflinePlayer(player_name);
        if(time != -1 && player != null) {
            Date date = new Date(time);
            String color = "§0"; // black
            if(player.isOnline())
                color = "§a"; //green
            else {
                try {
                    ResultSet rs = getHoliday(player_name);
                    rs = (rs.next()) ? rs : null;
                    if(rs != null && rs.getDate("end").getTime() >= now) {
                        color = "§5"; // putple
                    }
                    else if((now - time) / MILLSECS_PER_DAY > offlineDays) {
                        color = "§4"; // red
                    }
                } catch (SQLException ex) {
                    info("SQL exception: " + ex.getMessage());
                }
            }
            sign.setLine(3, color + dateFormat.format(date));
        }
        else
            sign.setLine(3, "---");
        sign.update(true);
    }

    protected String getPlayerGroupColor(Player player) {
        String color = "§f";
        if(groupsColors != null) {
            String s;
            String world_name = player.getWorld().getName();
            for (String g : SellCube.pex.getUser(player).getGroupsNames(world_name)) {
                s = groupsColors.get(g).toString();
                if(s != null && s.matches("[0-9A-Fa-f]")) {
                    color = "§" + s.charAt(0);
                    break;
                }
            }
        }
        return color;
    }
}
