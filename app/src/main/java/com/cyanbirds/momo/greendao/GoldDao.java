package com.cyanbirds.momo.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.cyanbirds.momo.entity.Gold;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "GOLD".
*/
public class GoldDao extends AbstractDao<Gold, Long> {

    public static final String TABLENAME = "GOLD";

    /**
     * Properties of entity Gold.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property DownloadTime = new Property(1, long.class, "downloadTime", false, "DOWNLOAD_TIME");
        public final static Property Banlance = new Property(2, double.class, "banlance", false, "BANLANCE");
        public final static Property ClickCount = new Property(3, int.class, "clickCount", false, "CLICK_COUNT");
        public final static Property VipFlag = new Property(4, int.class, "vipFlag", false, "VIP_FLAG");
    }


    public GoldDao(DaoConfig config) {
        super(config);
    }
    
    public GoldDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"GOLD\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"DOWNLOAD_TIME\" INTEGER NOT NULL ," + // 1: downloadTime
                "\"BANLANCE\" REAL NOT NULL ," + // 2: banlance
                "\"CLICK_COUNT\" INTEGER NOT NULL ," + // 3: clickCount
                "\"VIP_FLAG\" INTEGER NOT NULL );"); // 4: vipFlag
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"GOLD\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Gold entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getDownloadTime());
        stmt.bindDouble(3, entity.getBanlance());
        stmt.bindLong(4, entity.getClickCount());
        stmt.bindLong(5, entity.getVipFlag());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Gold entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getDownloadTime());
        stmt.bindDouble(3, entity.getBanlance());
        stmt.bindLong(4, entity.getClickCount());
        stmt.bindLong(5, entity.getVipFlag());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Gold readEntity(Cursor cursor, int offset) {
        Gold entity = new Gold( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // downloadTime
            cursor.getDouble(offset + 2), // banlance
            cursor.getInt(offset + 3), // clickCount
            cursor.getInt(offset + 4) // vipFlag
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Gold entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDownloadTime(cursor.getLong(offset + 1));
        entity.setBanlance(cursor.getDouble(offset + 2));
        entity.setClickCount(cursor.getInt(offset + 3));
        entity.setVipFlag(cursor.getInt(offset + 4));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Gold entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Gold entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Gold entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
