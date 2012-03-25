package pl.bluex.sellcube;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Date;
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
                    else
                        statusAction(player, block, ad);
                }
            }
        }
        else if (action == Action.RIGHT_CLICK_BLOCK) {
            if(ad != null) {
                if(!ad.getRental())
                    buyAction(player, ad);
                else
                    rentAction(player, ad);
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
            player.sendMessage(
                    ChatColor.BLUE + ((!ad.getRental())?"Sprzedajacy: ":"Wynajmujacy: ") +
                    Permissions.getPlayerColor(ad.getSeller(), ad.getSignWorld()) + ad.getSeller());
            player.sendMessage(ChatColor.BLUE + "Cena: " + ChatColor.DARK_AQUA + ad.getPrice());
        }
        else if(ad.getRental() && ad.getOwner() != null && ad.getOwner().equals(player.getName())) {
            player.sendMessage(ChatColor.BLUE + ("Wynajmujacy: ") +
                    Permissions.getPlayerColor(ad.getSeller(), ad.getSignWorld()) + ad.getSeller());
            player.sendMessage(ChatColor.BLUE + "Cena: " + ChatColor.DARK_AQUA + ad.getPrice());
            player.sendMessage(ChatColor.BLUE + "Wynajete do: " + ChatColor.DARK_AQUA + Utils.dateFormat.format(ad.getRentedTo()));
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
        if(!ad.getActive()) return;
        String locationType = ad.getLocationType();
        if(!Permissions.can(player, locationType, Permissions.buy)) return;
        String buyerName = player.getName();
        String sellerName = ad.getOwner();
        String regionName = ad.getRegion();
        double price = ad.getPrice().doubleValue();

        // Check region
        RegionManager manager = SellCube.wg.getGlobalRegionManager().get(player.getWorld());
        ProtectedRegion region = manager.getRegion(regionName);
        if(region == null ||
                !(region.getOwners().getPlayers().contains(sellerName) ||
                    Permissions.has(sellerName, Permissions.sell_all, locationType, ad.getSignWorld()))) {
            Block block = ad.getSignBlock();
            if(block != null) {
                AdSignManager.remove(ad);
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(),
                        new ItemStack(Material.SIGN, 1));
            }
            player.sendMessage(ChatColor.RED + "Ogloszenie nieaktualne");
            return;
        }

        if(!Utils.transferMoney(buyerName, sellerName, price)) return;
        if(!Utils.setRegionOwner(region, buyerName, manager)) return;

        // Change sign owner
        AdSignManager.changeOwner(ad, buyerName);
        // Deactivate ad
        ad.setActive(false);
        ad.save();

        // Update sign
        AdSignManager.updateSign(ad);

        player.sendMessage(ChatColor.BLUE + "Teren nalezy teraz do ciebie");
    }

    private void rentAction(Player player, AdSign ad) {
        String locationType = ad.getLocationType();
        if(!Permissions.can(player, locationType, Permissions.rent)) return;

        String buyerName = player.getName();
        String sellerName = ad.getOwner();
        String regionName = ad.getRegion();
        double price = ad.getPrice().doubleValue();

        if(ad.getActive()) { // Rent region
            // Check region
            RegionManager manager = SellCube.wg.getGlobalRegionManager().get(player.getWorld());
            ProtectedRegion region = manager.getRegion(regionName);
            if(region == null ||
                    !(region.getOwners().getPlayers().contains(sellerName) ||
                        Permissions.has(sellerName, Permissions.sell_all, locationType, ad.getSignWorld()))) {
                Block block = ad.getSignBlock();
                if(block != null) {
                    AdSignManager.remove(ad);
                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation(),
                            new ItemStack(Material.SIGN, 1));
                }
                player.sendMessage(ChatColor.RED + "Ogloszenie nieaktualne");
                return;
            }

            if(!Utils.transferMoney(buyerName, sellerName, price)) return;
            if(!Utils.setRegionOwner(region, buyerName, manager)) return;

            // Change sign owner
            AdSignManager.changeOwner(ad, buyerName);
            // Deactivate ad
            ad.setActive(false);
            ad.save();

            // Update sign
            AdSignManager.updateSign(ad);

            player.sendMessage(ChatColor.BLUE + "Teren nalezy do ciebie do " + Utils.dateFormat.format(ad.getRentedTo()));
        }
        else { // Extend rental period
            long rentedTo = ad.getRentedTo().getTime();
            if((rentedTo - new Date().getTime()) / Utils.DAY > Permissions.maxRentDays - 1) {
                player.sendMessage(ChatColor.RED + "Nie mozna bardziej przedluzyc wynajmu");
                return;
            }
            if(!Utils.transferMoney(buyerName, sellerName, price)) return;
            ad.setRentedTo(new Date(rentedTo + Utils.DAY));
            ad.save();

            player.sendMessage(ChatColor.BLUE + "Wynajem przedluzono do " + Utils.dateFormat.format(ad.getRentedTo()));
        }
    }
}
