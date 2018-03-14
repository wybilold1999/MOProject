package com.cyanbirds.momo.db;

import android.content.Context;
import android.text.TextUtils;

import com.cyanbirds.momo.CSApplication;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.db.base.DBManager;
import com.cyanbirds.momo.entity.FConversation;
import com.cyanbirds.momo.eventtype.DataEvent;
import com.cyanbirds.momo.greendao.FConversationDao;
import com.cyanbirds.momo.listener.MessageChangedListener;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.request.DownloadFileRequest;
import com.cyanbirds.momo.utils.FileAccessorUtils;
import com.cyanbirds.momo.utils.Md5Util;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.List;


/**
 *
 * @ClassName:FConversationSqlManager
 * @Description:会话数据库管理
 * @author wangyb
 * @Date:2015年5月24日下午8:03:20
 *
 */
public class FConversationSqlManager extends DBManager {

	private static FConversationSqlManager mInstance;
	private FConversationDao conversationDao;
	private Context mContext;

	private FConversationSqlManager(Context context) {
		super(context);
		mContext = context;
		conversationDao = getDaoSession().getFConversationDao();
	}

	public static FConversationSqlManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (FConversationSqlManager.class) {
				if (mInstance == null) {
					mInstance = new FConversationSqlManager(context);
				}
			}
		}
		return mInstance;
	}


	/**
	 * 获取所有会话统计未读消息数
	 * @return
	 */
	public int getAnalyticsUnReadConversation() {
		List<FConversation> conversations = conversationDao.loadAll();
		if (conversations == null || conversations.size() == 0) {
			return 0 ;
		}
		int total = 0;
		for (int i = 0; i < conversations.size(); i++) {
			FConversation c = conversations.get(i);
			total += c.unreadCount;
		}
		return total;
	}

	/**
	 * 获取未读会话个数
	 * @return
	 */
	public int getConversationUnReadNum() {
		List<FConversation> conversations = conversationDao.loadAll();
		if (conversations == null || conversations.size() == 0) {
			return 0 ;
		}
		int total = 0;
		for (int i = 0; i < conversations.size(); i++) {
			if (conversations.get(i).unreadCount > 0) {
				total++;
			}
		}
		return total;
	}


	/**
	 * 根据聊天对象的userid来查询会话
	 * @return
	 */
	public FConversation queryConversationForByTalkerId(String talkerId) {
		QueryBuilder<FConversation> qb = conversationDao.queryBuilder();
		qb.where(FConversationDao.Properties.Talker.eq(talkerId));
		return qb.unique();
	}

	/**
	 * 根据真实用户的会话id查询所有的会话
	 * @return
     */
	public List<FConversation> queryConversations(long realId) {
		QueryBuilder<FConversation> qb = conversationDao.queryBuilder();
		qb.where(FConversationDao.Properties.Rid.eq(realId)).orderDesc(FConversationDao.Properties.CreateTime);
		return qb.list();
	}

	/**
	 * 修改会话
	 * @return
	 */
	public void updateConversation(FConversation conversation) {
		if (conversation == null
				|| TextUtils.isEmpty(String.valueOf(conversation.id))) {
			return;
		}
		conversationDao.update(conversation);
	}

	/**
	 * 删除所有会话
	 *
	 * @return
	 */
	public void deleteAllConversation() {
		conversationDao.deleteAll();
	}

	/**
	 * 删除真实用户对应的假用户id
	 */
	public void deleteAllConversationByRid(long rid) {
		List<FConversation> list = queryConversations(rid);
		if (list != null && !list.isEmpty()) {
			conversationDao.deleteInTx(list);
		}
	}

	public void deleteConversationById(FConversation conversation) {
		conversationDao.delete(conversation);
	}


	/**
	 * 官方消息发送时插入的会话
	 * @param conversation
	 * @return
	 */
	public long inserConversation(FConversation conversation) {
		return conversationDao.insertOrReplace(conversation);
	}

	/**
	 *
	 * @param rId 真实用户的会话id
	 * @param userData
	 * @param ecMessage
     * @return
     */
	public long insertConversation(long rId, String userData, ECMessage ecMessage) {
		String[] userInfos = userData.split(";");

		String talker = "";
		String talkerName = "";
		String portraitUrl = "";
		boolean isSend = false;
		if (ecMessage.getDirection() == ECMessage.Direction.SEND) {
			isSend = true;
		}
		if (isSend) {
			talker = userInfos[0];
			talkerName = userInfos[1];
			portraitUrl = userInfos[2];
		} else {
			talker = userInfos[3];
			talkerName = userInfos[4];
			portraitUrl = userInfos[5];
		}
		//根据talker去查询有没有对应的会话
		FConversation conversation = queryConversationForByTalkerId(talker);
		if (null == conversation) {//没有会话
			conversation = new FConversation();
		}
		conversation.Rid = rId;
		conversation.talker = talker;
		conversation.talkerName = talkerName;
		conversation.createTime = ecMessage.getMsgTime();
		conversation.faceUrl = portraitUrl;
		if (!isSend && !"com.cyanbirds.momo.activity.ChatActivity".equals(AppManager.getTopActivity(mContext))) {
			conversation.unreadCount++;
		}
		if (ecMessage.getType() == ECMessage.Type.TXT) {
			ECTextMessageBody body = (ECTextMessageBody)ecMessage.getBody();
			if (body != null) {
				conversation.content = body.getMessage();
			}
			conversation.type = ECMessage.Type.TXT.ordinal();
		} else if (ecMessage.getType() == ECMessage.Type.IMAGE) {
			conversation.content = CSApplication.getInstance().getResources().getString(R.string.image_symbol);
			conversation.type = ECMessage.Type.IMAGE.ordinal();
		} else if (ecMessage.getType() == ECMessage.Type.LOCATION) {
			conversation.content = CSApplication.getInstance().getResources().getString(R.string.location_symbol);
			conversation.type = ECMessage.Type.LOCATION.ordinal();
		} else if (ecMessage.getType() == ECMessage.Type.FILE) {

		} else if (ecMessage.getType() == ECMessage.Type.STATE) {
			conversation.content = CSApplication.getInstance().getResources().getString(R.string.rpt_symbol);
			conversation.type = ECMessage.Type.STATE.ordinal();
		}
		long id = conversationDao.insertOrReplace(conversation);
		conversation.id = id;
		//插入会话之后就下载头像 String faceUrl = ecMessage.getUserData(); 并更新会话
		if (TextUtils.isEmpty(conversation.localPortrait) ||
				!new File(conversation.localPortrait).exists()) {
			new DownloadPortraitTask(conversation).request(
					portraitUrl, FileAccessorUtils.FACE_IMAGE, Md5Util.md5(portraitUrl) + ".jpg");
		}
		/**
		 * 通知会话内容的改变
		 */
		MessageChangedListener.getInstance().notifyMessageChanged(String.valueOf(id));
		EventBus.getDefault().post(new DataEvent());
		return id;
	}

	/**
	 * 下载头像
	 */
	class DownloadPortraitTask extends DownloadFileRequest {
		private FConversation mConversation;

		public DownloadPortraitTask(FConversation conversation) {
			mConversation = conversation;
		}

		@Override
		public void onPostExecute(String s) {
			mConversation.localPortrait = s;
			updateConversation(mConversation);
			MessageChangedListener.getInstance().notifyMessageChanged(String.valueOf(mConversation.id));
			EventBus.getDefault().post(new DataEvent());
		}

		@Override
		public void onErrorExecute(String error) {
		}
	}

	public static void reset() {
		release();
		mInstance = null;
	}
}