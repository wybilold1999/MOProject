package com.cyanbirds.momo.db;


import android.content.Context;

import com.cyanbirds.momo.db.base.DBManager;
import com.cyanbirds.momo.entity.Expression;
import com.cyanbirds.momo.greendao.ExpressionDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;


/**
 * 
 * @ClassName:ExpressionSqlManager
 * @Description:在线表情数据库管理
 * @author zxj
 * @Date:2015年6月15日上午10:20:23
 */
public class ExpressionSqlManager extends DBManager {

	private static ExpressionSqlManager mInstance;
	private Context mContext;
	private ExpressionDao mExpressionDao;

	public ExpressionSqlManager(Context context) {
		super(context);
		mContext = context;
		mExpressionDao = getDaoSession().getExpressionDao();
	}

	public static ExpressionSqlManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (ExpressionSqlManager.class) {
				if (mInstance == null) {
					mInstance = new ExpressionSqlManager(context);
				}
			}
		}
		return mInstance;
	}

	/**
	 * 根据主题id查询表情
	 * 
	 * @return
	 */
	public List<Expression> getExpressions(String themesId) {
		QueryBuilder<Expression> qb = mExpressionDao.queryBuilder();
		qb.where(ExpressionDao.Properties.Theme_id.eq(themesId));
		return qb.list();
	}

	/**
	 * 根据表情id查询表情信息
	 * @param picId
	 * @return
	 */
	public static Expression getExpressionsByPicId(String picId) {
		return null;
	}

	/**
	 * 批量插入表情数据
	 * @param expressions
	 * @return
	 */
	public void insertExpressions(List<Expression> expressions) {
		mExpressionDao.insertOrReplaceInTx(expressions);
	}

	/**
	 * 插入在线表情组
	 * 
	 * @return
	 */
	public static long insertExpression(Expression expression) {
		if (expression == null) {
			return -1;
		}
		return -1;
	}

	public static void reset() {
		release();
		mInstance = null;
	}
}
