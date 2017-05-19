package com.cyanbirds.momo.db;

import android.content.Context;

import com.cyanbirds.momo.db.base.DBManager;
import com.cyanbirds.momo.entity.IMessage;
import com.cyanbirds.momo.entity.LocationModel;
import com.cyanbirds.momo.greendao.IMessageDao;
import com.cyanbirds.momo.greendao.LocationModelDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * 作者：wangyb
 * 时间：2017/3/26 17:03
 * 描述：
 */
public class LocationManager extends DBManager {
	private static LocationManager mInstance;
	private LocationModelDao mLocationModelDao;
	private Context mContext;

	private LocationManager(Context context) {
		super(context);
		mContext = context;
		mLocationModelDao = getDaoSession().getLocationModelDao();
	}

	public static LocationManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (ConversationSqlManager.class) {
				if (mInstance == null) {
					mInstance = new LocationManager(context);
				}
			}
		}
		return mInstance;
	}

	/**
	 * 查询所有
	 * @return
	 */
	public List<LocationModel> queryAllLocation() {
		return mLocationModelDao.loadAll();
	}

	public LocationModel queryLocationByUserId(String userId) {
		QueryBuilder<LocationModel> qb = mLocationModelDao.queryBuilder();
		qb.where(LocationModelDao.Properties.UserId.eq(userId));
		return qb.unique();
	}

	public void inserLocationModels(List<LocationModel> locationModels) {
		mLocationModelDao.insertOrReplaceInTx(locationModels);
	}
}
