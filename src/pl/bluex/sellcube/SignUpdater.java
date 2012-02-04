
package pl.bluex.sellcube;

import com.avaje.ebean.QueryIterator;
import java.util.logging.Level;

public class SignUpdater implements Runnable {
    private SellCube plugin;
	
	public SignUpdater(SellCube instance) {
		this.plugin = instance;
	}

    @Override
    public void run() {
        SellCube.log(Level.INFO, "Updating signs");
        QueryIterator<AdSign> query = AdSign.get(false).order().asc("owner").findIterate();
        while(query.hasNext()) {
            AdSign ad = query.next();
            if(ad.getSignBlock() != null)
                ad.updateOwnerInfo();
        }
    }
}
