/* This file is part of SellCube.

    SellCube is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SellCube is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with SellCube.  If not, see <http://www.gnu.org/licenses/>.
*/

package pl.bluex.sellcube;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.bluex.sellcube.entities.AdSign;
import pl.bluex.sellcube.entities.AdSignManager;

class SellCubeCommand implements CommandExecutor {
	private SellCube plugin;
//    private final HashMap<String, Method> commands = new HashMap<String, Method>();
	
	public SellCubeCommand(SellCube instance) {
		this.plugin = instance;
        instance.getCommand("sellcube").setExecutor(this);
//        for(Method m: SellCubeCommand.class.getMethods()) {
//            String[] c = m.getName().split("Command");
//            if(c.length == 2 && "".equals(c[1])) {
//                commands.put("sc" + c[0], m);
//                instance.getCommand(c[0]).setExecutor(this);
//            }
//        }
        instance.getCommand("scadd").setExecutor(this);
        instance.getCommand("sccancel").setExecutor(this);
        instance.getCommand("scstatus").setExecutor(this);
        instance.getCommand("sctp").setExecutor(this);
        instance.getCommand("scfind").setExecutor(this);
//        instance.getCommand("sccopy").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        LinkedList<String> argsl = new LinkedList<String>(Arrays.asList(args));
        // Console command
        if(!(sender instanceof Player)) {
            if("sellcube".equalsIgnoreCase(cmd.getName()) && argsl.size() == 1 && "update".equalsIgnoreCase(argsl.get(0))) {
                new SignUpdater(plugin).run();
            }
            return true;
        }
        // Player command
        Player player = (Player)sender;
        String command;
        if("sellcube".equalsIgnoreCase(cmd.getName())) {
            if(argsl.size() < 1) return false;
            command = argsl.get(0);
            argsl.remove(0);
        }
        else {
            command = cmd.getName().substring(2);
        }

//        Method m = commands.get(command);
//        if(m != null) {
//            try {
//                return m.invoke(this, player).equals(true);
//            }
//            catch (IllegalAccessException ex) {}
//            catch (IllegalArgumentException ex) {}
//            catch (InvocationTargetException ex) {}
//        }

        if("cancel".equalsIgnoreCase(command))
            return cancelCommand(player);
        else if("status".equalsIgnoreCase(command))
            return statusCommand(player);
        else if("tp".equalsIgnoreCase(command))
            return teleportCommand(player);
        else if("find".equalsIgnoreCase(command))
            return findCommand(player);
        else if("add".equalsIgnoreCase(command))
            return addCommand(player, argsl);
        return false;
	}

    protected boolean cancelCommand(Player player) {
        SellCube.newAds.remove(player);
        player.sendMessage(ChatColor.BLUE + "Tworzenie ogłoszenia anulowane");
        return true;
    }

    protected boolean addCommand(Player player, LinkedList<String> argsl) {
        if(!Permissions.has(player, Permissions.sell)) return true;
        if(argsl.size() < 3) return false;
        boolean lwcPass = false, rental = false;
        for(int i = 0; i < argsl.size() - 3; i++) {
            if("lp".equalsIgnoreCase(argsl.get(i))) {
                if(!Permissions.has(player, Permissions.lwc_pass)) return true;
                lwcPass = true;
            }
            if("r".equalsIgnoreCase(argsl.get(i))) {
                rental = true;
            }
            else
                return false;
        }
        BigDecimal price;
        try {
            price = new BigDecimal(argsl.get(argsl.size() - 3));
            if(price.intValue() > 9999) {
                player.sendMessage(ChatColor.RED + "Zbyt wysoka cena");
                return true;
            }
        }
        catch (NumberFormatException nfe) {
            player.sendMessage(ChatColor.RED + "Nieprawidlowa cena");
                return true;
        }
        String regName = argsl.get(argsl.size() - 2);
        if(regName.equalsIgnoreCase("__global__")) {
            player.sendMessage(ChatColor.RED + "Niedostepna nazwa regionu");
            return true;
        }
        ProtectedRegion region = SellCube.wg.getGlobalRegionManager().get(player.getWorld()).getRegion(regName);
        if(region == null) {
            player.sendMessage(ChatColor.RED + "Nie ma takiego regionu");
            return true;
        }
        if(!region.getOwners().getPlayers().contains(player.getName())
                && !Permissions.has(player, Permissions.sell_all, false)) {
            player.sendMessage(ChatColor.RED + "Nie jestes wlascicielem regionu");
            return true;
        }
        String location = argsl.get(argsl.size() - 2);
        if("-".equals(location))
            location = "default";
        
        AdSign ad = new AdSign();
        ad.setRegion(regName);
        ad.setPrice(price);
        ad.setLwcPass(lwcPass);
        ad.setSeller(player.getName());
        ad.setName(location);
        ad.setRental(rental);
        SellCube.newAds.put(player, ad);
        player.sendMessage(ChatColor.BLUE + "Kliknij znak z ogloszeniem" + ((!lwcPass)?" [LWC Pass]":""));
        return true;
    }

    /*protected boolean copyCommand(Player player, LinkedList<String> argsl) {
        if(!SellCube.checkPermission(player, "sellcube.sell_all")) return true;
        if(argsl.size() != 1) return false;
        try {
            AdSign ad = AdSignManager.get(Integer.valueOf(argsl.get(0)).intValue());
            if(ad == null) {
                player.sendMessage(ChatColor.RED + "Brak ID w bazie danych");
                return true;
            }
            else if(!ad.getActive()) {
                player.sendMessage(ChatColor.RED + "Wybrane ogloszenie jest nieaktywne");
                return true;
            }
            SellCube.newAds.put(player, ad.copy());
            player.sendMessage(ChatColor.BLUE + "Kliknij znak");
        }
        catch (NumberFormatException nfe) {
            player.sendMessage(ChatColor.RED + "Nieprawidlowe ID");
            return false;
        }
        return true;
    }*/

    protected boolean statusCommand(Player player) {
        AdSign ad = new AdSign();
        ad.setOwner(player.getName());
        ad.setActive(false);
        SellCube.newAds.put(player, ad);
        player.sendMessage(ChatColor.BLUE + "Kliknij znak");
        return true;
    }

    protected boolean teleportCommand(Player player) {
        if(!Permissions.has(player, Permissions.tp)
                || SellCube.es == null) return true;
        for(AdSign ad : AdSignManager.get(player.getName(), false).where().isNotNull("region").order().desc("id").findList()) {
            Block block = ad.getSignBlock();
            if(block == null) continue;
            Utils.teleport(player, block);
            return true;
        }
        player.sendMessage(ChatColor.RED + "Nie kupiles zadnego regionu");
        return true;
    }

    protected boolean findCommand(Player player) {
        if(!Permissions.has(player, Permissions.find)
                || SellCube.es == null) return true;
        for(AdSign ad : AdSignManager.get(true).order().asc("id").findList()) {
            Block block = ad.getSignBlock();
            if(block == null) continue;
            Utils.teleport(player, block);
            return true;
        }
        player.sendMessage(ChatColor.RED + "Nie znaleziono zadnego ogloszenia");
        return true;
    }
}
