package com.cyanbirds.momo.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.manager.NotificationManagerUtils;
import com.cyanbirds.momo.net.IUserApi;
import com.cyanbirds.momo.net.base.RetrofitFactory;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 作者：wangyb
 * 时间：2016/9/24 17:42
 * 描述：
 */
public class ConnectServerService extends Service {

	private NotificationManager mNotificationManager;


	public ConnectServerService() {
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForeground(1, NotificationManagerUtils.getInstance().getNotification());
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
	}

	/**
	 * 更新客户端和服务端最新的连接时间
	 */
	private void saveUserOnOffLine() {
		RetrofitFactory.getRetrofit().create(IUserApi.class)
				.saveUserOnOffLine(Integer.parseInt(AppManager.getClientUser().userId))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(responseBody -> {}, throwable -> {});
	}

}
