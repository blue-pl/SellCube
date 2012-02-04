package pl.bluex.sellcube;

import com.griefcraft.model.Protection;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;
import com.nijikokun.register.payment.Methods;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class PlayerInteract extends PlayerListener {
	private SellCube plugin;
	
	public PlayerInteract(SellCube instance){
		plugin = instance;
	}
	
    @Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.isCancelled()) return;
		Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (!(block.getState() instanceof Sign)) return;
        

    	/*Matcher ma = Pattern.compile("[0-9]*\\.?[0-9]+").matcher(sign.getLine(3));
    	if(!ma.find()) return;
		float price = Float.parseFloat(ma.group());*/

        AdSign ad = AdSign.get(block);
        if (action == Action.LEFT_CLICK_BLOCK) {
            if(ad != null) { // if(znak_w_bazie)
                infoAction(player, ad);
            }
            else if(SellCube.newAds.containsKey(player)) {
                ad = SellCube.newAds.get(player);
                if(ad.getSignWorld() != null)
                    copyAction(player, block, ad);
                else if(ad.getRegion() != null)
                    addAction(player, block, ad);
                else if(ad.getActive() == false)
                    statusAction(player, block, ad);
            }
        }
        else if (action == Action.RIGHT_CLICK_BLOCK) {
            if(ad != null) {
                buyAction(player, ad);
            }
            else {
                player.sendMessage(ChatColor.RED + "Ten znak jest juz ogloszeniem");
            }
        }
    }

    protected void addAction(Player player, Block block, AdSign ad) {
        ad.setSignBlock(block);
        ad.add();
        SellCube.newAds.remove(player);
        player.sendMessage(ChatColor.BLUE + "Ogloszenie utworzone");
    }

    protected void infoAction(Player player, AdSign ad) {
        if(ad.getActive()) {
            if(SellCube.checkPermission(player, "sellcube.sell", false)) {
                player.sendMessage(ChatColor.BLUE + "ID: " + ChatColor.DARK_AQUA + ad.getId().toString());
                player.sendMessage(ChatColor.BLUE + "Region: " + ChatColor.DARK_AQUA + ad.getRegion());
            }
            player.sendMessage(ChatColor.BLUE + "Sprzedajacy: " + SellCube.getPlayerGroupColor(ad.getOwner(), ad.getSignWorld()) + ad.getOwner());
            player.sendMessage(ChatColor.BLUE + "Cena: " + ChatColor.DARK_AQUA + ad.getPrice());
        }
    }

    protected void copyAction(Player player, Block block, AdSign ad) {
        ad.setSignBlock(block);
        ad.add();
        SellCube.newAds.remove(player);
        player.sendMessage(ChatColor.BLUE + "Ogloszenie skopiowane");
    }
    
    protected void statusAction(Player player, Block block, AdSign ad) {
        ad.setSignBlock(block);
        ad.add();
        ad.updateOwnerInfo();
        SellCube.newAds.remove(player);
        player.sendMessage(ChatColor.BLUE + "Informacja utworzona");
    }

    protected void buyAction(Player player, AdSign ad) {
        if(ad.getActive() && SellCube.checkPermission(player, "sellcube.buy")) {
            String buyerName = player.getName();
            String sellerName = ad.getOwner();
            String regionName = ad.getRegion();
            double price = ad.getPrice().doubleValue();
            Block block = ad.getSignBlock();

            // Check region
            RegionManager manager = SellCube.wg.getGlobalRegionManager().get(player.getWorld());
            ProtectedRegion region = manager.getRegion(regionName);
            if(region == null ||
                    !(region.getOwners().getPlayers().contains(sellerName) ||
                      SellCube.checkPermission(sellerName, "sellcube.sell_all", ad.getSignWorld()))) {
                if(block != null) {
                    ad.remove();
                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation(),
                            new ItemStack(Material.SIGN, 1));
                }
                player.sendMessage(ChatColor.RED + "Ogloszenie nieaktualne");
                return;
            }

            // Check accounts
            Method m = Methods.getMethod();
            if(!m.hasAccount(buyerName) || !m.hasAccount(sellerName)) {
                player.sendMessage(ChatColor.RED + "Blad konta");
                return;
            }
            MethodAccount buyerMA = m.getAccount(buyerName);
            MethodAccount sellerMA = m.getAccount(sellerName);
            if(!buyerMA.hasEnough(price)) {
                player.sendMessage(ChatColor.RED + "Nie masz wystarczajacej liczby coinow");
                return;
            }

            // Transfer money
            buyerMA.subtract(price);
            sellerMA.add(price);
            player.sendMessage(ChatColor.GREEN + "Pobrano " +
                    ChatColor.DARK_AQUA + price +
                    ChatColor.GREEN + "c z twojego konta (stan " +
                    ChatColor.DARK_AQUA + buyerMA.balance() +
                    ChatColor.GREEN + "c)");
            Player seller = Bukkit.getPlayer(sellerName);
            if(seller != null) {
                seller.sendMessage(ChatColor.GREEN + "Przelano " +
                        ChatColor.DARK_AQUA + price +
                        ChatColor.GREEN + "c na twoje konto (stan " +
                        ChatColor.DARK_AQUA + sellerMA.balance() +
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
            ad.changeOwner(buyerName);
            
            // Deactivate ad
            ad.setActive(false);

            // Update sign
            ad.updateOwnerInfo();

            ad.save();
        }
    }
}
