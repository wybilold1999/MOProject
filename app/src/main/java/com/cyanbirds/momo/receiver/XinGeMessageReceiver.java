package com.cyanbirds.momo.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.cyanbirds.momo.activity.LauncherActivity;
import com.cyanbirds.momo.activity.MainActivity;
import com.cyanbirds.momo.helper.AppActivityLifecycleCallbacks;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.utils.PushMsgUtil;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

public class XinGeMessageReceiver extends XGPushBaseReceiver {
	private Handler mHandler = new Handler();

	// 通知展示
	@Override
	public void onNotifactionShowedResult(Context context, XGPushShowedResult notifiShowedRlt) {
		if (context == null || notifiShowedRlt == null) {
			return;
		}
	}

	@Override
	public void onUnregisterResult(Context context, int errorCode) {
		if (errorCode == XGPushBaseReceiver.SUCCESS) {
		} else {
		}

	}

	@Override
	public void onSetTagResult(Context context, int errorCode, String tagName) {
		if (errorCode == XGPushBaseReceiver.SUCCESS) {
		} else {
		}

	}

	@Override
	public void onDeleteTagResult(Context context, int errorCode, String tagName) {
		if (errorCode == XGPushBaseReceiver.SUCCESS) {
		} else {
		}

	}

	// 通知点击回调 actionType=1为该消息被清除，actionType=0为该消息被点击
	@Override
	public void onNotifactionClickedResult(Context context, XGPushClickedResult message) {
		if (context == null || message == null) {
			return;
		}
		//当前app未运行则启动，否则直接进入主界面
		//打开自定义的Activity
		if (AppManager.isAppAlive(context, AppManager.pkgName)) {
			if (!AppActivityLifecycleCallbacks.getInstance().getIsForeground()) {
				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
				intent.setClass(context, MainActivity.class);
				context.startActivity(intent);
			}
		} else {
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
			intent.setClass(context, LauncherActivity.class);
			context.startActivity(intent);
		}
	}

	@Override
	public void onRegisterResult(Context context, int errorCode, XGPushRegisterResult message) {
		// TODO Auto-generated method stub
		if (context == null || message == null) {
			return;
		}
		if (errorCode == XGPushBaseReceiver.SUCCESS) {
		}
	}

	// 消息透传
	@Override
	public void onTextMessage(Context context, XGPushTextMessage message) {
		// 获取自定义key-value
		final String content = message.getContent();
		if (!TextUtils.isEmpty(content)) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					PushMsgUtil.getInstance().handlePushMsg(true, content);
				}
			});
		}
	}

}
