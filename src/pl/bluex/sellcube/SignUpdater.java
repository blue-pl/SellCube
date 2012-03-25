
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
