package pl.bluex.sellcube;

import pl.bluex.sellcube.utils.Utils;
import com.avaje.ebean.EbeanServer;
import com.earth2me.essentials.Essentials;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.PersistenceException;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pl.bluex.sellcube.entities.AdSign;
import pl.bluex.sellcube.entities.AdSignManager;
import pl.bluex.sellcube.entities.InvitedPlayer;


public class SellCube extends JavaPlugin {
    protected static final HashMap<Player, AdSign> newAds = new HashMap<Player, AdSign>();
    
    public static EbeanServer database;
    public static PluginManager pm;
    public static WorldGuardPlugin wg;
    public static Permission permission = null;
    public static Economy economy = null;
    public static Chat chat = null;
    public static Essentials es;
    public static LWC lwc;

    private PlayerInteract playerInteract = null;
    private PlayerLogin playerLogin = null;
    private SignInteract signInteract = null;
    private SellCubeCommand sellCubeCommand = null;

	@Override
	public void onEnable() {
		pm = getServer().getPluginManager();
		FileConfiguration config = getConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        AdSignManager.offlineDays = config.getInt("offline_days");
        boolean updater = config.getBoolean("sign_updater");
        setupDatabase();
        setupEssentials();
		if(setupWorldGuard() && setupPermissions() && setupEconomy() && setupChat() && setupLWC()) {
			playerInteract = new PlayerInteract(this);
            playerLogin = new PlayerLogin(this);
            signInteract = new SignInteract(this);
            sellCubeCommand = new SellCubeCommand(this);
			
            if(updater) {
                getServer().getScheduler().scheduleAsyncRepeatingTask(this,
                        new SignUpdater(this), 100L, 864000L);
            }
            Permissions.init(config);
            Utils.log(Level.INFO, String.format("%s is enabled.", getDescription().getFullName()));
		}
	}

	@Override
	public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
		Utils.log(Level.INFO, String.format("%s is disabled.", getDescription().getFullName()));
	}

    private Boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private Boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    private Boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	protected boolean setupWorldGuard() {
		if (wg == null) {
			Plugin worldGuard = pm.getPlugin("WorldGuard");

			if (worldGuard == null || !(worldGuard instanceof WorldGuardPlugin)) {
				Utils.log(Level.SEVERE, "WorldGuard detection failed.");
				wg = null;
				return false;
			} else {
				wg = (WorldGuardPlugin) worldGuard;
			}
		}
		return true;
	}

    protected boolean setupEssentials() {
		if (es == null) {
			Plugin essentials = pm.getPlugin("Essentials");

			if (essentials == null || !(essentials instanceof Essentials)) {
				Utils.log(Level.WARNING, "Essentials detection failed.");
				es = null;
				return false;
			} else {
				es = (Essentials) essentials;
			}
		}
		return true;
	}

    protected boolean setupLWC() {
		if (lwc == null) {
			Plugin lwc_plug = pm.getPlugin("LWC");

			if (lwc_plug == null || !(lwc_plug instanceof LWCPlugin)) {
				Utils.log(Level.SEVERE, "LWC detection failed.");
				lwc = null;
				return false;
			} else {
				lwc = ((LWCPlugin)lwc_plug).getLWC();
			}
		}
		return true;
	}
	
	private void setupDatabase() {
        try {
            getDatabase().find(AdSign.class).findRowCount();
            getDatabase().find(InvitedPlayer.class).findRowCount();
        } catch (PersistenceException ex) {
            Utils.log(Level.INFO, "Setting up database");
            installDDL();
        }
        database = getDatabase();
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(AdSign.class);
        list.add(InvitedPlayer.class);
        return list;
    }
}
