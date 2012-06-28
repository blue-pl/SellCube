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

package pl.bluex.sellcube;

import java.util.logging.Level;
import pl.bluex.sellcube.entities.AdSignManager;

public class SignUpdater implements Runnable {
    private SellCube plugin;
	
	public SignUpdater(SellCube instance) {
		this.plugin = instance;
	}

    @Override
    public void run() {
        Utils.log(Level.INFO, "Updating signs");
        AdSignManager.updateSigns();
        Utils.log(Level.INFO, "Update complete");
    }
}
