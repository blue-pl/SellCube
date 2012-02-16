package pl.bluex.sellcube;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.griefcraft.model.Protection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import pl.bluex.firstlastseendb.PlayerTimeStamp;
import pl.bluex.firstlastseendb.PlayerTimeStampManager;

public class AdSignManager {
    protected static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
    protected static long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;
    protected static int offlineDays = 21;

    public static void changeOwner(AdSign ad, String owner) {
        ad.setOwner(owner);
        if(ad.getLwcPass()) {
            Protection protection = SellCube.lwc.findProtection(
                    Bukkit.getWorld(ad.getSignWorld()),
                    ad.getSignX(), ad.getSignY(), ad.getSignZ());
            if(protection != null) {
                protection.setOwner(owner);
                protection.save();
            }
        }
    }

    public static void add(AdSign ad) {
        add(ad, true);
    }

    public static void add(AdSign ad, boolean addProtection) {
        Block block = ad.getSignBlock();
        if(block == null) return;
        //SellCube.database.insert(this);
        save(ad);
        if(addProtection) {
            SellCube.lwc.getPhysicalDatabase().registerProtection(
                block.getTypeId(), Protection.Type.PUBLIC,
                ad.getSignWorld(), ad.getOwner(), "",
                ad.getSignX(), ad.getSignY(), ad.getSignZ());
        }
    }

    public static void remove(AdSign ad) {
        remove(ad, true);
    }

    public static void remove(AdSign ad, boolean removeProtection) {
        if(removeProtection) {
            Protection protection = SellCube.lwc.findProtection(
                    Bukkit.getWorld(ad.getSignWorld()),
                    ad.getSignX(), ad.getSignY(), ad.getSignZ());
            if(protection != null) {
                protection.remove();
                protection.save();
            }
        }
        SellCube.database.delete(ad);
    }

    public static void save(AdSign ad) {
        SellCube.database.save(ad);
    }

    public static AdSign copy(AdSign ad) {
        AdSign newAd = new AdSign(
                null, ad.getOwner(), ad.getSignWorld(),
                ad.getSignX(), ad.getSignY(), ad.getSignZ());
        return newAd;
    }

    public static AdSign get(int id) {
        return SellCube.database.find(AdSign.class)
                .where()
                .eq("id", id)
                .findUnique();
    }

    public static ExpressionList<AdSign> get(boolean active) {
        return SellCube.database.find(AdSign.class)
                .where()
                .eq("active", active);
    }

    public static ExpressionList<AdSign> get(String playerName) {
        return SellCube.database.find(AdSign.class)
                .where()
                .ieq("owner", playerName);
    }

    public static ExpressionList<AdSign> get(String playerName, boolean active) {
        return SellCube.database.find(AdSign.class)
                .where()
                .ieq("owner", playerName)
                .eq("active", active);
    }

    public static AdSign get(Block block) {
        return SellCube.database.find(AdSign.class)
                .where()
                .ieq("sign_world", block.getWorld().getName())
                .eq("sign_x", block.getX())
                .eq("sign_y", block.getY())
                .eq("sign_z", block.getZ())
                .findUnique();
    }

    public static void updateSign(AdSign ad) {
        updateSign(ad, Bukkit.getOfflinePlayer(ad.getOwner()).isOnline());
    }

    public static void updateSign(AdSign ad, boolean online) {
        updateSign(ad, online, PlayerTimeStampManager.get(ad.getOwner()));
    }

    public static void updateSign(AdSign ad, boolean online, PlayerTimeStamp pts) {
        updateSign(ad, online, pts, SellCube.getPlayerGroupColor(ad.getOwner()));
    }

    public static void updateSign(AdSign ad, boolean online, PlayerTimeStamp pts, String ownerColor) {
        if(ad.getActive() == true) return;
        Block block = ad.getSignBlock();
        if(block == null) return;
        Sign sign = (Sign)block.getState();
        long now = new Date().getTime();
        if(pts != null) {
            String color = "§0"; // black
            if(online) {
                color = "§a"; //green
            }
            else {
                if(pts.getHoliday() != null && pts.getHoliday().getTime() >= now) {
                    color = "§5"; // putple
                }
                else if((now - pts.getLastSeen().getTime()) / MILLSECS_PER_DAY > offlineDays) {
                    color = "§4"; // red
                }
            }
            sign.setLine(3, color + dateFormat.format(pts.getLastSeen()));
        }
        else
            sign.setLine(3, "---");
        sign.setLine(0, "Gracz:");
        sign.setLine(1, ownerColor + ad.getOwner());
        sign.setLine(2, "Ostatnio byl:");
        sign.update(true);
    }

    public static void updateSigns() {
        SqlQuery query = SellCube.database.createSqlQuery("select owner from sellcube group by owner");
        String owner;
        for(SqlRow row : query.findList()) {
            owner = row.getString("owner");
            updateSigns(owner);
        }
    }

    public static void updateSigns(String owner) {
        updateSigns(owner, Bukkit.getOfflinePlayer(owner).isOnline());
    }

    public static void updateSigns(String owner, boolean online) {
        PlayerTimeStamp pts = PlayerTimeStampManager.get(owner);
        String ownerColor = SellCube.getPlayerGroupColor(owner);
        for(AdSign ad : get(owner, false)/*.select("owner, signX, signY, signZ, signWorld")*/.findList()) {
            updateSign(ad, online, pts, ownerColor);
        }
    }
}
