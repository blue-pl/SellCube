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

package pl.bluex.sellcube.entities;

import javax.persistence.*;
import pl.bluex.sellcube.SellCube;

@Entity
@Table(name = "sellcube_invitedplayer")
public class InvitedPlayer {
    @Id
    private Integer id;
    @ManyToOne
    @JoinColumn(name="adsign_id")
    AdSign adSign;
    @Column(name = "player", nullable = false, length = 255)
    private String player;
    @Column(name = "name", length = 255)
    private String name;

    public InvitedPlayer() {
    }

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AdSign getAdSign() {
        return adSign;
    }

    public void setAdSign(AdSign adSign) {
        this.adSign = adSign;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    // </editor-fold>

    public void save() {
        SellCube.database.save(this);
    }
}