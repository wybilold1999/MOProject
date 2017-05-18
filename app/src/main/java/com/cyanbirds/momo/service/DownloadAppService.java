package com.cyanbirds.momo.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.ApkInfo;
import com.cyanbirds.momo.entity.IMessage;
import com.cyanbirds.momo.listener.FileProgressListener;
import com.cyanbirds.momo.net.request.DownloadApkFileRequest;
import com.cyanbirds.momo.utils.FileAccessorUtils;
import com.cyanbirds.momo.utils.ToastUtil;

import java.io.File;

/**
 * 作者：wangyb
 * 时间：2016/9/24 17:42
 * 描述：
 */
public class DownloadAppService extends Service implements FileProgressListener.OnFileProgressChangedListener {

	private NotificationManager manager = null;
	private NotificationCompat.Builder builder = null;
	private Notification notification = null;

	private ApkInfo mApkInfo = null;

	public DownloadAppService() {
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		FileProgressListener.getInstance().addOnFileProgressChangedListener(this);
		manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		builder = new NotificationCompat.Builder(DownloadAppService.this);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setContentTitle(getString(R.string.app_name));
		builder.setTicker(getString(R.string.downloading));
		builder.setContentText(String.format(getString(R.string.download_progress), 0f));

		notification = builder.build();
		notification.defaults = Notification.DEFAULT_SOUND;
		manager.notify(0, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			mApkInfo = (ApkInfo) intent.getSerializableExtra(ValueKey.DATA);
			File apkFile = new File(FileAccessorUtils.APK_PATH, mApkInfo.apkName + ".apk");
			if (apkFile.exists()) {
				apkFile.delete();
			}
			new DownloadApkFileTask().request(mApkInfo.apkUrl, FileAccessorUtils.APK_PATH, mApkInfo.apkName + ".apk");
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onFileProgressChanged(IMessage message, int progress) {
		float fprogress = progress;
		builder.setContentText(String.format(getString(R.string.download_progress), fprogress));
		builder.setProgress(100, progress, false);
		manager.notify(0, builder.build());
	}

	class DownloadApkFileTask extends DownloadApkFileRequest {
		@Override
		public void onPostExecute(String s) {
			FileProgressListener.getInstance().removeOnFileProgressChangedListener(DownloadAppService.this);
			File apkFile = new File(s);

			/**
			 * 创建通知栏的intent
			 */
			Intent notifyIntent = new Intent(Intent.ACTION_VIEW);
			Uri uri = Uri.fromFile(apkFile);
			//设置intent的类型
			notifyIntent.setDataAndType(uri,
					"application/vnd.android.package-archive");
			PendingIntent pendingIntent = PendingIntent.getActivity(DownloadAppService.this, 0, notifyIntent, 0);
			builder.setContentIntent(pendingIntent);
			builder.setContentText(getString(R.string.download_finish));
			builder.setProgress(100, 100, false);
			manager.notify(0, builder.build());

			ToastUtil.showMessage(R.string.download_finished);

			stopSelf();
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
			builder.setContentText(getString(R.string.download_fail));
			manager.notify(0, builder.build());
//			showVersionInfo();
		}
	}

	/*private void showVersionInfo() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
		builder.setMessage(R.string.download_fail_retry);
		builder.setPositiveButton(getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						*//**
						 * 开始下载apk文件
						 *//*
						new DownloadApkFileTask().request(mApkInfo.apkUrl, FileAccessorUtils.APK_PATH, mApkInfo.apkName + ".apk");
					}
				});
		builder.show();
	}*/

}
