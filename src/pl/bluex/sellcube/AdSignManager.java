package pl.bluex.sellcube;

import java.sql.ResultSet;
import java.util.Iterator;
import lib.PatPeter.SQLibrary.MySQL;

public enum AdSignManager {
    INSTANCE;
    private static MySQL db;
    static enum SCTable {
        id("INT", true, false);

        private SCTable(String type, boolean key, boolean nullable) {
            this.type = type;
            this.key = key;
            this.nullable = nullable;
        }

        public String getType() {
            return type;
        }

        public boolean isKey() {
            return key;
        }

        public boolean isNullable() {
            return nullable;
        }
        private final String type;
        private final boolean key;
        private final boolean nullable;
    }

    public void init(MySQL db) {
        this.db = db;
    }

    private interface Table {

        public String getType();

        public boolean isKey();

        public boolean isNullable();
    }

    /*private abstract class TableHelper<Table> {
        public String dbTableCreate(String tableName, Table[] values) {
            StringBuilder builder = new StringBuilder("Create table ");
            builder.append(tableName);
            builder.append("(");
            for (Table column : values) {
                builder.append(column.name());
                builder.append(" ");
                builder.append(column.getType());
                builder.append(column.isKey() ? " primary key" : "");
                builder.append(", ");
            }
            builder = new StringBuilder(builder.substring(0, builder.length() - 2));
            builder.append(");");
            return builder.toString();
        }
    }*/

    class AdIterator implements Iterator<AdSign> {
        ResultSet rs;

        public AdIterator(ResultSet rs) {
            this.rs = rs;
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public AdSign next() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
