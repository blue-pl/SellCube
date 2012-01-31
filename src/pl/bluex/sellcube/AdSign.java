package pl.bluex.sellcube;

import java.sql.ResultSet;

public class AdSign {
    private static AdSignManager manager = AdSignManager.INSTANCE;
    private ResultSet rs = null;
    private int _id = -1;
    private String _owner;
    private String _region;
    private float _price;
    private boolean _active;
    private boolean _lwcPass;

    public AdSign(ResultSet rs) {
        this.rs = rs;
        //_owner = rs.getString()
    }

}
