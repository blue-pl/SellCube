package pl.bluex.sellcube;

import com.avaje.ebean.ExpressionList;
import com.griefcraft.model.Protection;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.persistence.*;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import pl.bluex.firstlastseendb.PlayerTimeStamp;

@Entity
@Table(name = "sellcube", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sign_world", "sign_x", "sign_y", "sign_z"})})
public class AdSign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "owner", nullable = false, length = 255)
    private String owner;
    @Column(name = "region", length = 255)
    private String region;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "price", precision = 6, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;
    @Column(name = "active")
    private Boolean active = true;
    @Column(name = "lwc_pass")
    private Boolean lwcPass = true;
    @Column(name = "sign_world", nullable = false, length = 255)
    private String signWorld;
    @Column(name = "sign_x", nullable = false)
    private int signX;
    @Column(name = "sign_y", nullable = false)
    private int signY;
    @Column(name = "sign_z", nullable = false)
    private int signZ;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
    private static long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;
    private static int offlineDays = 21;

    public AdSign() {
    }

    public AdSign(Integer id) {
        this.id = id;
    }

    public AdSign(Integer id, String owner, String signWorld, int signX, int signY, int signZ) {
        this.id = id;
        this.owner = owner;
        this.signWorld = signWorld;
        this.signX = signX;
        this.signY = signY;
        this.signZ = signZ;
    }

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getLwcPass() {
        return lwcPass;
    }

    public void setLwcPass(Boolean lwcPass) {
        this.lwcPass = lwcPass;
    }

    public String getSignWorld() {
        return signWorld;
    }

    public void setSignWorld(String signWorld) {
        this.signWorld = signWorld;
    }

    public int getSignX() {
        return signX;
    }

    public void setSignX(int signX) {
        this.signX = signX;
    }

    public int getSignY() {
        return signY;
    }

    public void setSignY(int signY) {
        this.signY = signY;
    }

    public int getSignZ() {
        return signZ;
    }

    public void setSignZ(int signZ) {
        this.signZ = signZ;
    }

    public Block getSignBlock() {
        Block block = Bukkit.getWorld(this.signWorld).getBlockAt(signX, signY, signZ);
        if (block.getState() instanceof Sign) {
            return block;
        }
        if(id != null) {
            remove();
            SellCube.log(Level.INFO, "Block is not sign. Ad removed " + toString());
        }
        return null;
    }

    public void setSignBlock(Block block) {
        /*setSignWorld(block.getWorld().getName());
        setSignX(block.getX());
        setSignY(block.getY());
        setSignZ(block.getZ());*/
        signWorld = block.getWorld().getName();
        signX = block.getX();
        signY = block.getY();
        signZ = block.getZ();
    }
    // </editor-fold>

    public void changeOwner(String owner) {
        this.owner = owner;
        if(lwcPass) {
            Protection protection = SellCube.lwc.findProtection(Bukkit.getWorld(signWorld), signX, signY, signZ);
            if(protection != null) {
                protection.setOwner(owner);
                protection.save();
            }
        }
    }

    public void add() {
        add(true);
    }

    public void add(boolean addProtection) {
        Block block = getSignBlock();
        if(block == null) return;
        SellCube.database.insert(this);
        if(addProtection) {
            SellCube.lwc.getPhysicalDatabase().registerProtection(
                block.getTypeId(), Protection.Type.PUBLIC,
                signWorld, owner, "",
                signX, signY, signZ);
        }
    }

    public void remove() {
        remove(true);
    }

    public void remove(boolean removeProtection) {
        SellCube.database.delete(this);
        if(removeProtection) {
            Protection protection = SellCube.lwc.findProtection(Bukkit.getWorld(signWorld), signX, signY, signZ);
            if(protection != null) {
                protection.remove();
                protection.save();
            }
        }
    }

    public void save() {
        SellCube.log(Level.INFO, "Saving ad");
        SellCube.database.save(this);
    }

    public AdSign copy() {
        AdSign ad = this.copy();
        ad.id = null;
        return ad;
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

    protected void updateOwnerInfo() {
        updateOwnerInfo(Bukkit.getOfflinePlayer(owner).isOnline());
    }

    protected void updateOwnerInfo(boolean online) {
        if(active == true) return;
        Sign sign = (Sign)getSignBlock().getState();
        PlayerTimeStamp pts = PlayerTimeStamp.get(owner);
        long now = new Date().getTime();
        if(pts != null) {
            String color = "§0"; // black
            if(online)
                color = "§a"; //green
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
        sign.setLine(1, SellCube.getPlayerGroupColor(owner, signWorld) + owner);
        sign.setLine(2, "Ostatnio byl:");
        sign.update(true);
    }
}