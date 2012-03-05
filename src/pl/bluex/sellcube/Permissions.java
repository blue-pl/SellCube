package pl.bluex.sellcube;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public enum Permissions {
    sell,
    sell_all,
    rent,
    buy,
    lwc_pass,
    tp,
    find;

    private static final String prefix = "sellcube";
    private static final String defaults = "default";
    private static ConfigurationSection cs;

    public void init(SellCube plugin) {
        // TODO: init code
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
    
    public static boolean has(Player player, Permissions node, String locationType, boolean msg) {
        if(locationType == null || locationType.isEmpty())
            return has(player, node, msg);
        String sNode = String.format("%s.%s.%s", prefix, locationType, node.name());
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

    public static boolean has(String player, Permissions node, String locationType, String world) {
		return SellCube.permission.has(world, player, String.format("%s.%s.%s", prefix, locationType, node.name()));
	}

    public static boolean can(Player player, Permissions node, String locationType) {
        if(locationType == null)
            locationType = "default";
        //SellCube.pex.getUser(player).getPermissions(prefix); // TODO: get player permissions
        //SellCube.permission.
        String group;
        if(node == sell || node == buy || node == rent) {
            Integer limit = getLimit(group, node.name(), locationType);
        }
        //cs.contains(prefix);
        return false;
    }

    private static Integer getLimit(String group, String node, String location) {
        return getLimit(group, node, location, false);
    }

    private static Integer getLimit(String group, String node, String location, Boolean recurrent) {
        if(group == null)
            group = defaults;
        if(node == null)
            node = defaults;
        if(location == null)
            location = defaults;
        String paths[];
        if(recurrent == false) {
            String _paths[] = {
                String.format("%s.limits.%s.%s", group, node, location),
                String.format("%s.limits.%s.%s", group, node, defaults),
                String.format("%s.limits.%s.%s", defaults, node, location),
                String.format("%s.limits.%s.%s", defaults, node, defaults)
            };
            paths = _paths;
        }
        else {
            String _paths[] = {
                String.format("%s.limits.%s.%s", group, node, location)
            };
            paths = _paths;
        }
        Integer limit;
        for(String path : paths) {
            limit = cs.getInt(path);
            if(limit != null)
                return limit;
            List<String> parents = cs.getStringList(String.format("%s.inherit", group));
            if(parents != null) {
                for(String parent_group : parents) {
                    limit = getLimit(parent_group, node, location, true);
                    if(limit != null)
                        return limit;
                }
            }
        }
        return null;
    }
}
