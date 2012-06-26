package pl.bluex.sellcube;

import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pl.bluex.sellcube.entities.AdSignManager;

public enum Permissions {
    sell,
    sell_all,
    rent,
    buy,
    lwc_pass,
    tp,
    find;

    private static final String prefix = "sellcube";
    private static String defaultColor;
    private static ConfigurationSection locGroups;
    private static HashMap<String, String> groupsColors = new HashMap<String, String>();
    protected static int maxRentDays;

    public static void init(FileConfiguration config) {
        ConfigurationSection gc = config.getConfigurationSection("colors");
        if(gc != null) {
            String s;
            for (String key : gc.getKeys(true)) {
                s = gc.get(key).toString();
                if(s.matches("[0-9A-Fa-f]")) {
                    groupsColors.put(key, "§" + s.charAt(0));
                }
                else {
                    Utils.log(Level.WARNING, String.format("Wrong color setting (%s: %s)", key, s));
                }
            }
        }
        defaultColor = config.getString("colors.default", "§f");
        locGroups = config.getConfigurationSection("location_groups");
        maxRentDays = config.getInt("max_rent_days");
    }

    public static boolean has(Player player, Permissions node) {
        return has(player, node, true);
    }

    public static boolean has(Player player, Permissions node, String locationType) {
        return has(player, node, locationType, true);
    }

	public static boolean has(Player player, Permissions node, boolean msg) {
        String sNode = String.format("%s.%s", prefix, node.name());
		return has(player, sNode, msg);
	}
    
    public static boolean has(Player player, Permissions node, String locationGroup, boolean msg) {
        if(locationGroup == null || locationGroup.isEmpty())
            return has(player, node, msg);
        String sNode = String.format("%s.%s.%s", prefix, locationGroup, node.name());
		return has(player, sNode, msg);
	}

    private static boolean has(Player player, String node, boolean msg) {
		boolean permission = SellCube.permission.has(player, node);
		if(permission == false && msg == true) {
			player.sendMessage(ChatColor.RED + "Nie masz wystarczajacych uprawnien.");
		}
		return permission;
	}

    public static boolean has(String player, Permissions node, String world) {
		return SellCube.permission.has(world, player, String.format("%s.%s", prefix, node.name()));
	}

    public static boolean has(String player, Permissions node, String locationGroup, String world) {
		return SellCube.permission.has(world, player, String.format("%s.%s.%s", prefix, locationGroup, node.name()));
	}

    public static String getPlayerColor(String player, String world) {
        String c = groupsColors.get(SellCube.permission.getPrimaryGroup(world, player));
        if(c != null) return c;
        else return defaultColor;
    }

    public static boolean can(Player player, String location, Permissions node) {
        return can(player, location, node, true);
    }

    public static boolean can(Player player, String location, Permissions node, boolean msg) {
        int val;
        switch(node) {
            case buy: 
                val = AdSignManager.getAdCountBuy(player.getName());
                break;
            case rent:
                val = AdSignManager.getAdCountRent(player.getName());
                break;
            default: return has(player, node, location);
        }
        if(!has(player, node, location)) return false;
        int limit = getLimit(player.getName(), player.getWorld().getName(), location, node);
        if(limit == Integer.MIN_VALUE) {
            if(msg) player.sendMessage(ChatColor.RED + "Nie mozesz "+(node.equals(Permissions.rent)?"wynajac":"kupic")+" tego terenu");
            return false;
        }
        if(val >= limit) {
            if(msg) player.sendMessage(ChatColor.RED + "Limit wykorzystany.");
            return false;
        }
        return true;
    }

    private static Integer getLimit(String player, String world, String location, Permissions node) {
        int limit = Integer.MIN_VALUE;
        for(String locGroup : locGroups.getKeys(false)) {
            if(has(player, node, locGroup, world)) {
                int x = locGroups.getInt(String.format("%s.%s.%s", locGroup, location, node.name()), Integer.MIN_VALUE);
                if(x != -1)
                    limit = Math.max(limit, x);
                else
                    return Integer.MAX_VALUE;
            }
        }
        return limit;
    }
}
