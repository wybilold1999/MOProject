package com.cyanbirds.momo.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.cyanbirds.momo.entity.LocationModel;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "LOCATION_MODEL".
*/
public class LocationModelDao extends AbstractDao<LocationModel, Long> {

    public static final String TABLENAME = "LOCATION_MODEL";

    /**
     * Properties of entity LocationModel.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property UserId = new Property(1, String.class, "userId", false, "USER_ID");
        public final static Property Latitude = new Property(2, String.class, "latitude", false, "LATITUDE");
        public final static Property Longitude = new Property(3, String.class, "longitude", false, "LONGITUDE");
    }


    public LocationModelDao(DaoConfig config) {
        super(config);
    }
    
    public LocationModelDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"LOCATION_MODEL\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"USER_ID\" TEXT NOT NULL UNIQUE ," + // 1: userId
                "\"LATITUDE\" TEXT NOT NULL ," + // 2: latitude
                "\"LONGITUDE\" TEXT NOT NULL );"); // 3: longitude
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"LOCATION_MODEL\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, LocationModel entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getUserId());
        stmt.bindString(3, entity.getLatitude());
        stmt.bindString(4, entity.getLongitude());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, LocationModel entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getUserId());
        stmt.bindString(3, entity.getLatitude());
        stmt.bindString(4, entity.getLongitude());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public LocationModel readEntity(Cursor cursor, int offset) {
        LocationModel entity = new LocationModel( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // userId
            cursor.getString(offset + 2), // latitude
            cursor.getString(offset + 3) // longitude
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, LocationModel entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUserId(cursor.getString(offset + 1));
        entity.setLatitude(cursor.getString(offset + 2));
        entity.setLongitude(cursor.getString(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(LocationModel entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(LocationModel entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(LocationModel entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
