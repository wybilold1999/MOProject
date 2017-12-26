package com.cyanbirds.momo.db;


import android.content.Context;

import com.cyanbirds.momo.db.base.DBManager;
import com.cyanbirds.momo.entity.ExpressionGroup;
import com.cyanbirds.momo.greendao.ExpressionGroupDao;

import java.util.List;

/**
 * 
 * @ClassName:ExpressionGroupSqlManager
 * @Description:表情分组数据库管理
 * @author zxj
 * @Date:2015年6月15日上午9:58:29
 */
public class ExpressionGroupSqlManager extends DBManager {

	private static ExpressionGroupSqlManager mInstance;
	private Context mContext;
	private ExpressionGroupDao mExpressionGroupDao;

	public ExpressionGroupSqlManager(Context context) {
		super(context);
		mContext = context;
		mExpressionGroupDao = getDaoSession().getExpressionGroupDao();
	}

	public static ExpressionGroupSqlManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (ExpressionGroupSqlManager.class) {
				if (mInstance == null) {
					mInstance = new ExpressionGroupSqlManager(context);
				}
			}
		}
		return mInstance;
	}

	/**
	 * 查询我添加的在线表情组
	 * 
	 * @return
	 */
	public List<ExpressionGroup> getExpressionGroup() {
		return mExpressionGroupDao.loadAll();
	}

	/**
	 * 插入在线表情组
	 * 
	 * @return
	 */
	public long insertExpressionGroup(ExpressionGroup group) {
		if (group == null) {
			return -1;
		}
		return mExpressionGroupDao.insertOrReplace(group);
	}

	public void insertExpressionGroupList(List<ExpressionGroup> groups) {
		if (groups == null) {
			return;
		}
		mExpressionGroupDao.insertOrReplaceInTx(groups);
	}

	/**
	 * 删除已添加的表情
	 * 
	 * @param id
	 * @return
	 */
	public static long deleteExpressionGroupById(int id) {
		long row = -1;
		return row;
	}

	public static void reset() {
		release();
		mInstance = null;
	}
}
