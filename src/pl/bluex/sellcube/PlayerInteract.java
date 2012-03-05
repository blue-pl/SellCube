package pl.bluex.sellcube;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import pl.bluex.sellcube.entities.AdSign;
import pl.bluex.sellcube.entities.AdSignManager;

public class PlayerInteract implements Listener {
	
	public PlayerInteract(SellCube plugin){
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
    @EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.isCancelled()) return;
		Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (!(block.getState() instanceof Sign)) return;
        
        AdSign ad = AdSignManager.get(block);

        if (action == Action.LEFT_CLICK_BLOCK) {
            if(ad != null) {
                infoAction(player, ad);
            }
            if(SellCube.newAds.containsKey(player)) {
                if(ad != null) {
                    player.sendMessage(ChatColor.RED + "Ten znak jest juz ogloszeniem");
                }
                else {
                    ad = SellCube.newAds.get(player);
                    //if(ad.getSignWorld() != null)
                        //copyAction(player, block, ad);
                    if(ad.getRegion() != null)
                        addAction(player, block, ad);
                    else if(ad.getActive() == false)
                        statusAction(player, block, ad);
                }
            }
        }
        else if (action == Action.RIGHT_CLICK_BLOCK) {
            if(ad != null) {
                buyAction(player, ad);
            }
        }
    }

    protected void addAction(Player player, Block block, AdSign ad) {
        ad.setSignBlock(block);
        AdSignManager.add(ad);
        SellCube.newAds.remove(player);
        player.sendMessage(ChatColor.BLUE + "Ogloszenie utworzone");
    }

    protected void infoAction(Player player, AdSign ad) {
        if(ad.getActive()) {
            if(Permissions.has(player, Permissions.sell, false)) {
                player.sendMessage(ChatColor.BLUE + "ID: " + ChatColor.DARK_AQUA + ad.getId().toString());
                player.sendMessage(ChatColor.BLUE + "Region: " + ChatColor.DARK_AQUA + ad.getRegion());
            }
            player.sendMessage(ChatColor.BLUE + "Sprzedajacy: " + SellCube.getPlayerGroupColor(ad.getSeller()) + ad.getSeller());
            player.sendMessage(ChatColor.BLUE + "Cena: " + ChatColor.DARK_AQUA + ad.getPrice());
        }
    }

    /*protected void copyAction(Player player, Block block, AdSign ad) {
        ad.setSignBlock(block);
        AdSignManager.add(ad);
        SellCube.newAds.remove(player);
        player.sendMessage(ChatColor.BLUE + "Ogloszenie skopiowane");
    }*/
    
    protected void statusAction(Player player, Block block, AdSign ad) {
        ad.setSignBlock(block);
        AdSignManager.add(ad);
        AdSignManager.updateSign(ad);
        SellCube.newAds.remove(player);
        player.sendMessage(ChatColor.BLUE + "Informacja utworzona");
    }

    protected void buyAction(Player player, AdSign ad) {
        if(ad.getActive() && Permissions.has(player, Permissions.buy, ad.getLocationType())) {
            String buyerName = player.getName();
            String sellerName = ad.getOwner();
            String regionName = ad.getRegion();
            double price = ad.getPrice().doubleValue();
            Block block = ad.getSignBlock();
            String locationType = ad.getLocationType();

            if(block == null) return;
            
            // Check region
            RegionManager manager = SellCube.wg.getGlobalRegionManager().get(player.getWorld());
            ProtectedRegion region = manager.getRegion(regionName);
            if(region == null ||
                    !(region.getOwners().getPlayers().contains(sellerName) ||
                      Permissions.has(sellerName, Permissions.sell_all, locationType, ad.getSignWorld()))) {
                if(block != null) {
                    AdSignManager.remove(ad);
                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation(),
                            new ItemStack(Material.SIGN, 1));
                }
                player.sendMessage(ChatColor.RED + "Ogloszenie nieaktualne");
                return;
            }

            // Check accounts
            if(!SellCube.economy.hasAccount(buyerName) ||
                    !SellCube.economy.hasAccount(sellerName)) {
                player.sendMessage(ChatColor.RED + "Blad konta");
                return;
            }
            //MethodAccount buyerMA = SellCube.economy.getAccount(buyerName);
            //MethodAccount sellerMA = m.getAccount(sellerName);
            if(!SellCube.economy.has(buyerName, price)) {
                player.sendMessage(ChatColor.RED + "Nie masz wystarczajacej liczby coinow");
                return;
            }

            // Transfer money
            SellCube.economy.withdrawPlayer(buyerName, price);
            SellCube.economy.depositPlayer(sellerName, price);
            player.sendMessage(ChatColor.GREEN + "Pobrano " +
                    ChatColor.DARK_AQUA + price +
                    ChatColor.GREEN + "c z twojego konta (stan " +
                    ChatColor.DARK_AQUA + SellCube.economy.getBalance(buyerName) +
                    ChatColor.GREEN + "c)");
            Player seller = Bukkit.getPlayer(sellerName);
            if(Bukkit.getOfflinePlayer(sellerName).isOnline()) {
                seller.sendMessage(ChatColor.GREEN + "Przelano " +
                        ChatColor.DARK_AQUA + price +
                        ChatColor.GREEN + "c na twoje konto (stan " +
                        ChatColor.DARK_AQUA + SellCube.economy.getBalance(sellerName) +
                        ChatColor.GREEN + "c)");
            }

            // Change region owner
            try {
                //region.getOwners().removePlayer(sellerName);
                Set<String> owners = region.getOwners().getPlayers();
                for(String s : owners)
                    region.getOwners().removePlayer(s);
                region.getOwners().addPlayer(buyerName);
                manager.save();
            } catch (IOException e) {
                SellCube.log(Level.WARNING, "Region save error: " +  e.getMessage());
                return;
            }

            // Change sign owner
            AdSignManager.changeOwner(ad, buyerName);
            // Deactivate ad
            ad.setActive(false);
            ad.save();

            // Update sign
            AdSignManager.updateSign(ad);
        }
    }
}
