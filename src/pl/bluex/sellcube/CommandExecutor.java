package pl.bluex.sellcube;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.material.Directional;
import org.bukkit.util.Vector;

class SellCubeCommand implements CommandExecutor {
	private SellCube plugin;
	
	public SellCubeCommand(SellCube instance) {
		this.plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(!cmd.getName().equalsIgnoreCase("sellcube")) return true;
        List<String> args_l = new LinkedList<String>(Arrays.asList(args));
        if(!(sender instanceof Player)) { // console command
            if(args_l.size() == 1 && args_l.get(0).equals("update")) {
                new SignUpdater(plugin).run();
            }
            return true;
        }
        Player player = (Player)sender;
        boolean lwc_pass = true;
        
        if(!plugin.setupWorldGuard()) {
            player.sendMessage(ChatColor.RED + "Nie mozna uzyskac dostepu do listy regionow");
            return true;
        }
        if(args_l.size() >= 1) {
            if("cancel".equalsIgnoreCase(args_l.get(0))) {
                cancelCommand(player);
                return true;
            }
            else if("status".equalsIgnoreCase(args_l.get(0))) {
                statusCommand(player);
                return true;
            }
            else if("tp".equalsIgnoreCase(args_l.get(0))) {
                teleportCommand(player);
                return true;
            }
            else if("find".equalsIgnoreCase(args_l.get(0))) {
                findCommand(player);
                return true;
            }
            else if(args_l.size() >= 2) {
                if("copy".equalsIgnoreCase(args_l.get(0))) {
                    copyCommand(player, args_l.get(1));
                    return true;
                }
                if("lp".equalsIgnoreCase(args_l.get(0))) {
                    if(!SellCube.checkPermission(player, "sellcube.lwc_pass"))
                        return true;
                    lwc_pass = false;
                    args_l.remove(0);
                }
                addCommand(player, args_l.get(0), args_l.get(1), lwc_pass);
                return true;
            }
        }
        return false;
	}

    protected void cancelCommand(Player player) {
        plugin.newAdsRN.remove(player);
        plugin.newAdsP.remove(player);
        plugin.newAdsLWC.remove(player);
        plugin.newAdsID.remove(player);
        player.sendMessage(ChatColor.BLUE + "Tworzenie ogłoszenia anulowane");
    }

    protected void addCommand(Player player, String price, String reg_name, boolean lwc_pass) {
        if(!SellCube.checkPermission(player, "sellcube.sell")) return;
        float _price;
        try {
            _price = Float.valueOf(price).floatValue();
            if(_price > 9999) {
                player.sendMessage(ChatColor.RED + "Zbyt wysoka cena");
                return;
            }
        }
        catch (NumberFormatException nfe) {
            player.sendMessage(ChatColor.RED + "Nieprawidlowa cena");
                return;
        }
        if(reg_name.equalsIgnoreCase("__global__")) {
            player.sendMessage(ChatColor.RED + "Niedostepna nazwa regionu");
            return;
        }
        ProtectedRegion region = SellCube.wg.getGlobalRegionManager().get(player.getWorld()).getRegion(reg_name);
        if(region == null) {
            player.sendMessage(ChatColor.RED + "Nie ma takiego regionu");
            return;
        }
        if(!region.getOwners().getPlayers().contains(player.getName())
                && !SellCube.checkPermission(player, "sellcube.sell_all", false)) {
            player.sendMessage(ChatColor.RED + "Nie jestes wlascicielem regionu");
            return;
        }
        plugin.newAdsRN.put(player, reg_name);
        plugin.newAdsP.put(player, _price);
        plugin.newAdsLWC.put(player, lwc_pass);
        player.sendMessage(ChatColor.BLUE + "Kliknij znak z ogloszeniem" + ((!lwc_pass)?" [LWC Pass]":""));
    }

    protected void copyCommand(Player player, String id) {
        if(!SellCube.checkPermission(player, "sellcube.sell_all")) return;
        int _id;
        try {
            _id = Integer.valueOf(id).intValue();
            ResultSet rs = plugin.getAd(_id);
            if(!rs.next()) {
                player.sendMessage(ChatColor.RED + "Brak ID w bazie danych");
                return;
            }
            else if(!rs.getBoolean("active")) {
                player.sendMessage(ChatColor.RED + "Wybrane ogloszenie jest nieaktywne");
                return;
            }
            plugin.newAdsID.put(player, _id);
            player.sendMessage(ChatColor.BLUE + "Kliknij znak");
        }
        catch (NumberFormatException nfe) {
            player.sendMessage(ChatColor.RED + "Nieprawidlowe ID");
        }
        catch (SQLException e) {
			plugin.severe("SQL exception: " + e.getMessage());
		}
    }

    protected void statusCommand(Player player) {
        plugin.newAdsRN.put(player, null);
        player.sendMessage(ChatColor.BLUE + "Kliknij znak");
    }

    protected void teleportCommand(Player player) {
        if(!SellCube.checkPermission(player, "sellcube.buy")) return;
        if(SellCube.es == null) return;
        try {
            ResultSet rs = plugin.getPlayerAd(player.getName());
            if(rs.last()) {
                Block block = Bukkit.getWorld(rs.getString("sign_world")).getBlockAt(rs.getInt("sign_x"), rs.getInt("sign_y"), rs.getInt("sign_z"));
                if (!(block.getState() instanceof Sign)) return;
                BlockFace dir = ((Directional) block.getType().getNewData(block.getData())).getFacing();
                Vector v = new Vector(dir.getModX(), dir.getModY(), dir.getModZ());
                Location l = block.getLocation().clone();
                l.setPitch(0);
                l.setYaw((float)(Math.atan2(dir.getModX(), -dir.getModZ()) * 180f / (float) Math.PI));
                l.add(v.multiply(2));
                l.add(0.5, 0, 0.5);
                l.getChunk(); // force load chunk
                plugin.info(l.toString());
                SellCube.es.getUser(player).teleport(l, TeleportCause.COMMAND);
            }
        } catch (SQLException e) {
            plugin.severe("SQL exception: " + e.getMessage());
        }
    }

    protected void findCommand(Player player) {
        if(!SellCube.checkPermission(player, "sellcube.buy")) return;
        if(SellCube.es == null) return;
        try {
            ResultSet rs = plugin.getActivedAd();
            if(rs.first()) {
                Block block = Bukkit.getWorld(rs.getString("sign_world")).getBlockAt(rs.getInt("sign_x"), rs.getInt("sign_y"), rs.getInt("sign_z"));
                if (!(block.getState() instanceof Sign)) return;
                BlockFace dir = ((Directional) block.getType().getNewData(block.getData())).getFacing();
                Vector v = new Vector(dir.getModX(), dir.getModY(), dir.getModZ());
                Location l = block.getLocation().clone();
                l.setPitch(0);
                l.setYaw((float)(Math.atan2(dir.getModX(), -dir.getModZ()) * 180f / (float) Math.PI));
                l.add(v.multiply(2));
                l.add(0.5, 0, 0.5);
                l.getChunk(); // force load chunk
                plugin.info(l.toString());
                SellCube.es.getUser(player).teleport(l, TeleportCause.COMMAND);
                //player.teleport(l, TeleportCause.COMMAND);
            }
        } catch (SQLException e) {
            plugin.severe("SQL exception: " + e.getMessage());
        }
    }
}
