package pl.bluex.sellcube.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.bluex.sellcube.SellCube;

public class Economy {
    public static boolean transfer(String fromPlayer, String toPlayer, Double ammount) {
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
