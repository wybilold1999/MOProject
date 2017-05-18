package com.cyanbirds.momo.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.config.AppConstants;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.utils.PushMsgUtil;
import com.meizu.cloud.pushinternal.DebugLogger;
import com.meizu.cloud.pushsdk.MzPushMessageReceiver;
import com.meizu.cloud.pushsdk.notification.PushNotificationBuilder;
import com.meizu.cloud.pushsdk.platform.message.PushSwitchStatus;
import com.meizu.cloud.pushsdk.platform.message.RegisterStatus;
import com.meizu.cloud.pushsdk.platform.message.SubAliasStatus;
import com.meizu.cloud.pushsdk.platform.message.SubTagsStatus;
import com.meizu.cloud.pushsdk.platform.message.UnRegisterStatus;

import static android.os.Looper.getMainLooper;

/**
 * 作者：wangyb
 * 时间：2017/3/31 22:56
 * 描述：
 */
public class MeizuPushMessageReceiver extends MzPushMessageReceiver {

	private Handler mHandler = new Handler(getMainLooper());

	@Override
	@Deprecated
	public void onRegister(Context context, String pushid) {
		//应用在接受返回的 pushid
	}
	@Override
	public void onMessage(Context context, String s) {
		//接收服务器推送的消息
	}

	@Override
	public void onMessage(Context context, Intent intent) {
		String content = intent.getExtras().toString();
	}

	@Override
	@Deprecated
	public void onUnRegister(Context context, boolean b) {
		//调用 PushManager.unRegister(context）方法后，会在此回调反注册状态
	}
	//设置通知栏小图标
	@Override
	public void onUpdateNotificationBuilder(PushNotificationBuilder pushNotificationBuilder) {
		pushNotificationBuilder.setmLargIcon(R.mipmap.ic_launcher);
	}
	@Override
	public void onPushStatus(Context context,PushSwitchStatus pushSwitchStatus) {
		//检查通知栏和透传消息开关状态回调
	}
	@Override
	public void onRegisterStatus(Context context,RegisterStatus registerStatus) {
		com.meizu.cloud.pushsdk.PushManager.subScribeAlias(
				context, AppConstants.MZ_APP_ID,
				AppConstants.MZ_APP_KEY, registerStatus.getPushId(),
				AppManager.getClientUser().userId);
		if ("男".equals(AppManager.getClientUser().sex)) {
			com.meizu.cloud.pushsdk.PushManager.subScribeTags(
					context, AppConstants.MZ_APP_ID,
					AppConstants.MZ_APP_KEY, registerStatus.getPushId(), "female");
		} else {
			com.meizu.cloud.pushsdk.PushManager.subScribeTags(
					context, AppConstants.MZ_APP_ID,
					AppConstants.MZ_APP_KEY, registerStatus.getPushId(), "male");
		}
		//新版订阅回调
		Log.d("test", registerStatus.getPushId());
	}
	@Override
	public void onUnRegisterStatus(Context context,UnRegisterStatus unRegisterStatus) {
		//新版反订阅回调
	}
	@Override
	public void onSubTagsStatus(Context context,SubTagsStatus subTagsStatus) {
		//标签回调
	}
	@Override
	public void onSubAliasStatus(Context context,SubAliasStatus subAliasStatus) {
		//别名回调
	}
	@Override
	public void onNotificationArrived(Context context, String title, final String content,
									  final String selfDefineContentString) {
		//通知栏消息到达回调
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				PushMsgUtil.getInstance().handlePushMsg(true, selfDefineContentString);
			}
		});
	}
	@Override
	public void onNotificationClicked(Context context, String title, String content,
									  String selfDefineContentString) {
		//通知栏消息点击回调
	}
	@Override
	public void onNotificationDeleted(Context context, String title, String content,
									  String selfDefineContentString) {
		//通知栏消息删除回调；flyme6 以上不再回调
	}

	@Override
	public void onNotifyMessageArrived(Context context, String message) {
	}
}
