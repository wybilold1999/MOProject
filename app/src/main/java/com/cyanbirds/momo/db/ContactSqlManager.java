package com.cyanbirds.momo.db;

import android.content.Context;

import com.cyanbirds.momo.db.base.DBManager;
import com.cyanbirds.momo.entity.Contact;
import com.cyanbirds.momo.greendao.ContactDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;


/**
 * 
 * @ClassName:ConversationSqlManager
 * @Description:会话数据库管理
 * @author wangyb
 * @Date:2015年5月24日下午8:03:20
 *
 */
public class ContactSqlManager extends DBManager {

	private static ContactSqlManager mInstance;
	private ContactDao mContactDao;
	private Context mContext;

	private ContactSqlManager(Context context) {
		super(context);
		mContext = context;
		mContactDao = getDaoSession().getContactDao();
	}

	public static ContactSqlManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (ContactSqlManager.class) {
				if (mInstance == null) {
					mInstance = new ContactSqlManager(context);
				}
			}
		}
		return mInstance;
	}

	/**
	 * 查询所有的联系人
	 * @return
     */
	public List<Contact> queryAllContacts() {
		return mContactDao.loadAll();
	}

	/**
	 * 根据来源查询联系人
	 * @return
     */
	public List<Contact> queryAllContactsByFrom(boolean isFromAdd) {
		QueryBuilder<Contact> qb = mContactDao.queryBuilder();
		return qb.where(ContactDao.Properties.IsFromAdd.eq(isFromAdd)).list();
	}



	/**
	 * 插入联系人
	 * @param contacts
	 * @return
	 */
	public void inserContacts(List<Contact> contacts) {
		mContactDao.insertOrReplaceInTx(contacts);
	}

	public void inserContact(Contact contacts) {
		mContactDao.insertOrReplace(contacts);
	}

	/**
	 * 根据用户id删除用户
	 * @param userId
     */
	public void deleteContactById(String userId) {
		QueryBuilder<Contact> qb = mContactDao.queryBuilder();
		Contact contact = qb.where(ContactDao.Properties.UserId.eq(userId)).unique();
		if (contact != null) {
			mContactDao.delete(contact);
		}
	}

	public static void reset() {
		release();
		mInstance = null;
	}
}
