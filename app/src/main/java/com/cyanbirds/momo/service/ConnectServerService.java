package com.cyanbirds.momo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.cyanbirds.momo.manager.AppManager;
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
		final Handler handler = new Handler();
		// 每30分钟请求一次鉴权
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				saveUserOnOffLine();
				handler.postDelayed(this, 3 * 5000);
			}
		};
        handler.postDelayed(runnable, 0);
		return super.onStartCommand(intent, flags, startId);
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
