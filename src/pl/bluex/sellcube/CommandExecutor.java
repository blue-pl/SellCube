package pl.bluex.sellcube;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.LinkedList;

class SellCubeCommand implements CommandExecutor {
	private SellCube plugin;
	
	public SellCubeCommand(SellCube instance) {
		this.plugin = instance;
	}

    static int PRICE = 0, REGION_NAME = 1;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("sellcube")) {
            List<String> args_l = new LinkedList<String>(Arrays.asList(args));
            if(!(sender instanceof Player)) { // console command
                if(args_l.size() == 1 && args_l.get(0).equals("update")) {
                    new SignUpdater(plugin).run();
                }
                return true;
            }
			Player player = (Player)sender;
            float price;
            boolean lwc_pass = true;
			if(SellCube.checkPermission(player, "sellcube.sell")) {
				if(!plugin.setupWorldGuard()) {
					player.sendMessage(ChatColor.RED + "Nie mozna uzyskac dostepu do listy regionow");
					return true;
				}
                if("cancel".equals(args_l.get(0))) {
                    plugin.newAdsRN.remove(player);
                    plugin.newAdsP.remove(player);
                    plugin.newAdsLWC.remove(player);
                    player.sendMessage(ChatColor.BLUE + "Tworzenie ogłoszenia anulowane");
                    return true;
                }
                if("lp".equals(args_l.get(0))) {
                    if(!SellCube.checkPermission(player, "sellcube.lwc_pass"))
                        return true;
                    lwc_pass = false;
                    args_l.remove(0);
                }
                if(args_l.size() < 2) {
                    return false;
                }
                try {
                    price = Float.valueOf(args_l.get(PRICE)).floatValue();
                    if(price > 9999) {
                        player.sendMessage(ChatColor.RED + "Zbyt wysoka cena");
                        return true;
                    }
                }
                catch (NumberFormatException nfe) {
                    player.sendMessage(ChatColor.RED + "Nieprawidlowa cena");
                        return true;
                }
				if(args_l.get(REGION_NAME).equalsIgnoreCase("__global__")) {
					player.sendMessage(ChatColor.RED + "Niedostepna nazwa regionu");
					return true;
				}
				ProtectedRegion region = SellCube.wg.getGlobalRegionManager().get(player.getWorld()).getRegion(args_l.get(REGION_NAME));
				if(region == null) {
					player.sendMessage(ChatColor.RED + "Nie ma takiego regionu");
					return true;
				}
				if(!region.getOwners().getPlayers().contains(player.getName()) 
						&& !SellCube.checkPermission(player, "sellcube.sell_all", false)) {
                    player.sendMessage(ChatColor.RED + "Nie jestes wlascicielem regionu");
					return true;
                }
				plugin.newAdsRN.put(player, args_l.get(REGION_NAME));
                plugin.newAdsP.put(player, price);
                plugin.newAdsLWC.put(player, lwc_pass);
				player.sendMessage(ChatColor.BLUE + "Kliknij znak z ogloszeniem" + ((!lwc_pass)?" [LWC Pass]":""));
				return true;
			}
		}
		return true;
	}

}
