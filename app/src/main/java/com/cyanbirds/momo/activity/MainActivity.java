package com.cyanbirds.momo.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.config.AppConstants;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.db.ConversationSqlManager;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.entity.FederationToken;
import com.cyanbirds.momo.fragment.FoundFragment;
import com.cyanbirds.momo.fragment.HomeLoveFragment;
import com.cyanbirds.momo.fragment.MessageFragment;
import com.cyanbirds.momo.fragment.PersonalFragment;
import com.cyanbirds.momo.fragment.VideoShowFragment;
import com.cyanbirds.momo.helper.SDKCoreHelper;
import com.cyanbirds.momo.listener.MessageUnReadListener;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.manager.NotificationManager;
import com.cyanbirds.momo.net.request.GetOSSTokenRequest;
import com.cyanbirds.momo.net.request.UploadCityInfoRequest;
import com.cyanbirds.momo.service.MyIntentService;
import com.cyanbirds.momo.service.MyPushService;
import com.cyanbirds.momo.utils.PreferencesUtils;
import com.cyanbirds.momo.utils.PushMsgUtil;
import com.cyanbirds.momo.utils.ToastUtil;
import com.igexin.sdk.PushManager;
import com.tencent.android.tpush.XGPushManager;
import com.umeng.analytics.MobclickAgent;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.yuntongxun.ecsdk.ECInitParams;

import java.util.LinkedHashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class MainActivity extends BaseActivity implements MessageUnReadListener.OnMessageUnReadListener, AMapLocationListener {

	private FragmentTabHost mTabHost;
	private int mCurrentTab;
	private ClientConfiguration mOSSConf;

	private static final int REQUEST_PERMISSION = 0;
	private final int REQUEST_LOCATION_PERMISSION = 1000;
	private final int REQUEST_PERMISSION_SETTING = 10001;

	private static final int MSG_SET_ALIAS = 1001;//极光推送设置别名
	private static final int MSG_SET_TAGS = 1002;//极光推送设置tag

	private long clickTime = 0; //记录第一次点击的时间

	private AMapLocationClientOption mLocationOption;
	private AMapLocationClient mlocationClient;
	private boolean isSecondAccess = false;
	private boolean isSecondRead = false;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case MSG_SET_ALIAS:
					JPushInterface.setAliasAndTags(getApplicationContext(), null, null, mAliasCallback);
					JPushInterface.setAliasAndTags(getApplicationContext(), (String) msg.obj, null, mAliasCallback);
					break;
				case MSG_SET_TAGS:
					JPushInterface.setAliasAndTags(getApplicationContext(), null, null, mAliasCallback);
					JPushInterface.setAliasAndTags(getApplicationContext(), null, (Set<String>) msg.obj, mAliasCallback);
					break;
			}
		}
	};

	/**
	 * oss鉴权获取失败重试次数
	 */
	public int mOSSTokenRetryCount = 0;

	public final static String CURRENT_TAB = "current_tab";
	private static final TableConfig[] tableConfig = new TableConfig[] {
			new TableConfig(R.string.tab_find_love, HomeLoveFragment.class,
					R.drawable.tab_tao_love_selector),
			new TableConfig(R.string.tab_found, FoundFragment.class,
					R.drawable.tab_found_selector),
			new TableConfig(R.string.video_show, VideoShowFragment.class,
					R.drawable.tab_video_selector),
			new TableConfig(R.string.tab_message, MessageFragment.class,
					R.drawable.tab_my_message_selector),
			new TableConfig(R.string.tab_personal, PersonalFragment.class,
					R.drawable.tab_personal_selector) };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		setupViews();
		setupEvent();
		initOSS();
		SDKCoreHelper.init(this, ECInitParams.LoginMode.FORCE_LOGIN);
		updateConversationUnRead();


		AppManager.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				/**
				 * 注册小米推送
				 */
				MiPushClient.registerPush(MainActivity.this, AppConstants.MI_PUSH_APP_ID, AppConstants.MI_PUSH_APP_KEY);
				/**
				 * 注册信鸽推送
				 */
				XGPushManager.registerPush(getApplicationContext(), "userId=" + AppManager.getClientUser().userId);
				//个推
				initGeTuiPush();

				initJPush();

				initMeizuPush();

				loadData();

				initLocationClient();
			}
		});

		AppManager.requestLocationPermission(this);
		requestPermission();

	}

	/**
	 * 初始化定位
	 */
	private void initLocationClient() {
		mlocationClient = new AMapLocationClient(this);
		//初始化定位参数
		mLocationOption = new AMapLocationClientOption();
		//设置定位监听
		mlocationClient.setLocationListener(this);
		//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		//获取最近3s内精度最高的一次定位结果：
		mLocationOption.setOnceLocationLatest(true);
		//设置定位参数
		mlocationClient.setLocationOption(mLocationOption);
		//启动定位
		mlocationClient.startLocation();
	}

	/**
	 * 请求读写文件夹的权限
	 */
	private void requestPermission() {
		PackageManager pkgManager = getPackageManager();
		// 读写 sd card 权限非常重要, android6.0默认禁止的, 建议初始化之前就弹窗让用户赋予该权限
		boolean sdCardWritePermission =
				pkgManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName()) == PackageManager.PERMISSION_GRANTED;
		if (Build.VERSION.SDK_INT >= 23 && !sdCardWritePermission) {
			//请求权限
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
					REQUEST_PERMISSION);
		}

		boolean readPhoneState =
				pkgManager.checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName()) == PackageManager.PERMISSION_GRANTED;
		if (Build.VERSION.SDK_INT >= 23 && !sdCardWritePermission) {
			//请求权限
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_PHONE_STATE},
					REQUEST_PERMISSION);
		}

	}

	/**
	 * 点击通知栏的消息，将消息入库
	 */
	private void loadData() {
		String msg = getIntent().getStringExtra(ValueKey.DATA);
		if (!TextUtils.isEmpty(msg)) {
			PushMsgUtil.getInstance().handlePushMsg(false, msg);
			NotificationManager.getInstance().cancelNotification();
			AppManager.isMsgClick = true;
		}
	}

	/**
	 * 初始化oss
	 */
	private void initOSS() {
		mOSSConf = new ClientConfiguration();
		mOSSConf.setConnectionTimeout(30 * 1000); // 连接超时，默认15秒
		mOSSConf.setSocketTimeout(30 * 1000); // socket超时，默认15秒
		mOSSConf.setMaxConcurrentRequest(50); // 最大并发请求书，默认5个
		mOSSConf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
		OSSLog.enableLog();

		final Handler handler = new Handler();
		// 每30分钟请求一次鉴权
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				new GetFederationTokenTask().request();
				handler.postDelayed(this, 60 * 30 * 1000);
			}
		};

		handler.postDelayed(runnable, 0);
	}

	class GetFederationTokenTask extends GetOSSTokenRequest {

		@Override
		public void onPostExecute(FederationToken result) {
			try {
				if (result != null) {
					AppManager.setFederationToken(result);
					OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(result.accessKeyId, result.accessKeySecret, result.securityToken);
					OSS oss = new OSSClient(getApplicationContext(), result.endpoint, credentialProvider, mOSSConf);
					AppManager.setOSS(oss);
					mOSSTokenRetryCount = 0;
				} else {
					if (mOSSTokenRetryCount < 5) {
						new GetFederationTokenTask().request();
						mOSSTokenRetryCount++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onErrorExecute(String error) {
			if (mOSSTokenRetryCount < 5) {
				new GetFederationTokenTask().request();
				mOSSTokenRetryCount++;
			}
		}
	}

	/**
	 * 个推注册
	 */
	private void initGeTuiPush() {
		// SDK初始化，第三方程序启动时，都要进行SDK初始化工作
		PushManager.getInstance().initialize(this.getApplicationContext(), MyPushService.class);
		PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), MyIntentService.class);
	}

	private void initJPush() {
		// 初始化 JPush
		JPushInterface.init(this);
//		JPushInterface.setDebugMode(true);

		if (!PreferencesUtils.getJpushSetAliasState(this)) {
			//调用JPush API设置Alias
			mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, AppManager.getClientUser().userId));
			//调用JPush API设置Tag
			Set<String> tag = new LinkedHashSet<>(1);
			if ("男".equals(AppManager.getClientUser().sex)) {
				tag.add("female");
			} else {
				tag.add("male");
			}
			mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_TAGS, tag));
		}
	}

	private void initMeizuPush() {
		com.meizu.cloud.pushsdk.PushManager.register(this,
				AppConstants.MZ_APP_ID, AppConstants.MZ_APP_KEY);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);// 必须要调用这句(信鸽推送)
		mCurrentTab = getIntent().getIntExtra(CURRENT_TAB, 0);
		mTabHost.setCurrentTab(mCurrentTab);
	}

	@Override
	public void onLocationChanged(AMapLocation aMapLocation) {
		if (aMapLocation != null && !TextUtils.isEmpty(aMapLocation.getCity())) {
			AppManager.getClientUser().latitude = String.valueOf(aMapLocation.getLatitude());
			AppManager.getClientUser().longitude = String.valueOf(aMapLocation.getLongitude());
			new UploadCityInfoTask().request(aMapLocation.getCity());
		}
	}

	class UploadCityInfoTask extends UploadCityInfoRequest {

		@Override
		public void onPostExecute(String isShow) {
			if ("0".equals(isShow)) {
				AppManager.getClientUser().isShowDownloadVip = false;
				AppManager.getClientUser().isShowGold = false;
				AppManager.getClientUser().isShowLovers = false;
				AppManager.getClientUser().isShowMap = false;
				AppManager.getClientUser().isShowVideo = false;
				AppManager.getClientUser().isShowVip = false;
			}
		}

		@Override
		public void onErrorExecute(String error) {
		}
	}

	/**
	 * 设置视图
	 */
	private void setupViews() {
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		for (int i = 0; i < tableConfig.length; i++) {
			if (i == 2 && !AppManager.getClientUser().isShowVideo){
				continue;
			}
			mTabHost.addTab(
					mTabHost.newTabSpec(getString(tableConfig[i].titleId))
							.setIndicator(getIndicator(i)),
					tableConfig[i].targetClass, null);
		}
		if (Build.VERSION.SDK_INT >= 11) {
			mTabHost.getTabWidget().setShowDividers(
					LinearLayout.SHOW_DIVIDER_NONE);// 设置不显示分割线
		}
		mTabHost.setCurrentTab(mCurrentTab);

	}


	private void setupEvent(){
		MessageUnReadListener.getInstance().setMessageUnReadListener(this);
	}


	private View getIndicator(int index) {
		View view = View.inflate(this, R.layout.tab_indicator_view, null);
		TextView tv = (TextView) view.findViewById(R.id.tab_item);
		ImageView tab_icon = (ImageView) view.findViewById(R.id.tab_icon);
		tab_icon.setImageResource(tableConfig[index].tabImage);
		tv.setText(tableConfig[index].titleId);
		return view;

	}

	/**
	 * 底部导航配置
	 */
	private static class TableConfig {
		final int titleId;
		final Class<?> targetClass;
		final int tabImage;

		TableConfig(int titleId, Class<?> targetClass, int tabImage) {
			this.titleId = titleId;
			this.targetClass = targetClass;
			this.tabImage = tabImage;
		}
	}

	@Override
	public void notifyUnReadChanged(int type) {
		updateConversationUnRead();
	}

	/**
	 * 更新会话未读消息总数
	 */
	private void updateConversationUnRead() {
		View view;
		if (AppManager.getClientUser().isShowVideo) {
			view = mTabHost.getTabWidget().getChildTabViewAt(3);
		} else {
			view = mTabHost.getTabWidget().getChildTabViewAt(2);
		}
		TextView unread_message_num = (TextView) view
				.findViewById(R.id.unread_message_num);

		int total = ConversationSqlManager.getInstance(this)
				.getAnalyticsUnReadConversation();
		unread_message_num.setVisibility(View.GONE);
		if (total > 0) {
			if (total >= 100) {
				unread_message_num.setText(String.valueOf("99+"));
			} else {
				unread_message_num.setText(String.valueOf(total));
			}
			unread_message_num.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == REQUEST_PERMISSION) {
			// 拒绝授权
			if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
				// 勾选了不再提示
				if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
//					showOpenLocationDialog();
				} else {
					if (!isSecondRead) {
						showReadPhoneStateDialog();
					}
				}
			}
		} else if (requestCode == REQUEST_LOCATION_PERMISSION) {
			// 拒绝授权
			if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
				// 勾选了不再提示
				if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
						!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
					showOpenLocationDialog();
				} else {
					if (!isSecondAccess) {
						showAccessLocationDialog();
					}
				}
			} else {
				initLocationClient();
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private void showOpenLocationDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.open_location);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", getPackageName(), null);
				intent.setData(uri);
				startActivityForResult(intent, REQUEST_PERMISSION_SETTING);

			}
		});
		builder.show();
	}


	private void showAccessLocationDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.access_location);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				isSecondAccess = true;
				if (Build.VERSION.SDK_INT >= 23) {
					ActivityCompat.requestPermissions(MainActivity.this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
							REQUEST_LOCATION_PERMISSION);
				}

			}
		});
		builder.show();
	}

	private void showReadPhoneStateDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.get_read_phone_state);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				isSecondRead = true;
				if (Build.VERSION.SDK_INT >= 23) {
					ActivityCompat.requestPermissions(MainActivity.this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
							REQUEST_LOCATION_PERMISSION);
				}

			}
		});
		builder.show();
	}

	/**
	 * 极光推送设置别名后的回调
	 */
	private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

		@Override
		public void gotResult(int code, String alias, Set<String> tags) {
			switch (code) {
				case 0:
					//Set tag and alias success
					PreferencesUtils.setJpushSetAliasState(MainActivity.this, true);
					break;

				case 6002:
					//"Failed to set alias and tags due to timeout. Try again after 60s.";
					ConnectivityManager conn = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo info = conn.getActiveNetworkInfo();
					if (info != null && info.isConnected()) {
						mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
						mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_TAGS, tags), 1000 * 60);
					}
					break;
			}
		}
	};


	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getName());
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getName());
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - clickTime) > 2000) {
				ToastUtil.showMessage(R.string.exit_tips);
				clickTime = System.currentTimeMillis();
			} else {
				exitApp();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_PERMISSION_SETTING) {
			initLocationClient();
		}
	}
}
