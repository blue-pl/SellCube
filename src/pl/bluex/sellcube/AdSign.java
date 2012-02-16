package pl.bluex.sellcube;

import java.math.BigDecimal;
import java.util.logging.Level;
import javax.persistence.*;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

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
    @Version
    private int version;

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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Transient
    public Block getSignBlock() {
        Block block = Bukkit.getWorld(getSignWorld()).getBlockAt(getSignX(), getSignY(), getSignZ());
        if (block.getState() instanceof Sign) {
            return block;
        }
        if(getId() != null) {
            SellCube.log(Level.INFO, String.format(
                    "Block (%d,%d,%d,%s) is not sign. Ad removed",
                    getSignX(), getSignY(), getSignZ(), getSignWorld()));
            AdSignManager.remove(this);
        }
        return null;
    }

    @Transient
    public void setSignBlock(Block block) {
        setSignWorld(block.getWorld().getName());
        setSignX(block.getX());
        setSignY(block.getY());
        setSignZ(block.getZ());
    }
    // </editor-fold>
}