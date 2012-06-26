package pl.bluex.sellcube.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.*;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import pl.bluex.sellcube.SellCube;
import pl.bluex.sellcube.Utils;

@Entity
@Table(name = "sellcube_adsign", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sign_world", "sign_x", "sign_y", "sign_z"})})
public class AdSign {
    @Id
    private Integer id; // Identifier
    @Column(name = "seller", nullable = false, length = 255)
    private String seller; // Player creating ad
    @Column(name = "owner", nullable = false, length = 255)
    private String owner; // Player owning ad
    @Column(name = "region", length = 255)
    private String region; // Region name
    //@Max(value=9999)  @Min(value=0) //if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "price", precision = 6, scale = 2)
    private BigDecimal price = BigDecimal.ZERO; // Price
    @Column(name = "active")
    private Boolean active = true; // true - advert, false - owner info
    @Column(name = "lwc_pass")
    private Boolean lwcPass = false; // sign lwc owner pass
    @Column(name = "rental")
    private Boolean rental = false; // true - rent ad, false - sell ad
    @Column(name = "rented_to", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date rentedTo; // rented to date
    @Column(name = "sign_world", nullable = false, length = 255)
    private String signWorld; // sign world
    @Column(name = "sign_x", nullable = false)
    private int signX; // sign location
    @Column(name = "sign_y", nullable = false)
    private int signY; // sign location
    @Column(name = "sign_z", nullable = false)
    private int signZ; // sign location
    @Column(name = "invited")
    @OneToMany(cascade=CascadeType.ALL, mappedBy="adSign")
    private List<InvitedPlayer>invited; // list of invited players
    @Column(name = "location")
    private String location; // name of group of locations
    @Column(name = "name")
    private String name; // name of location given by player (used by tp)

    public AdSign() {
    }
    
    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
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

    public Boolean getRental() {
        return rental;
    }

    public void setRental(Boolean rental) {
        this.rental = rental;
    }

    public Date getRentedTo() {
        return rentedTo;
    }

    public void setRentedTo(Date rentedTo) {
        this.rentedTo = rentedTo;
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

    public List<InvitedPlayer> getInvited() {
        return invited;
    }

    public void setInvited(List<InvitedPlayer> invited) {
        this.invited = invited;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Block getSignBlock() {
        Block block = Bukkit.getWorld(getSignWorld()).getBlockAt(getSignX(), getSignY(), getSignZ());
        if (block.getState() instanceof Sign) {
            return block;
        }
        else if(getId() != null) {
            Utils.log(Level.INFO, String.format(
                    "Block %s @ (%d,%d,%d,%s) is not sign (%s). Ad removed",
                    block.getState().getType(), getSignX(), getSignY(), getSignZ(), getSignWorld()));
            AdSignManager.remove(this);
        }
        return null;
    }

    public void setSignBlock(Block block) {
        setSignWorld(block.getWorld().getName());
        setSignX(block.getX());
        setSignY(block.getY());
        setSignZ(block.getZ());
    }
    // </editor-fold>

    public void save() {
        SellCube.database.save(this);
    }
}