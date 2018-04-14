package com.cyanbirds.momo.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.adapter.ViewPagerAdapter;
import com.cyanbirds.momo.config.AppConstants;
import com.cyanbirds.momo.db.ConversationSqlManager;
import com.cyanbirds.momo.entity.CityInfo;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.fragment.ContactsFragment;
import com.cyanbirds.momo.fragment.FoundGridFragment;
import com.cyanbirds.momo.fragment.MessageFragment;
import com.cyanbirds.momo.fragment.MyPersonalFragment;
import com.cyanbirds.momo.helper.BottomNavigationViewHelper;
import com.cyanbirds.momo.helper.SDKCoreHelper;
import com.cyanbirds.momo.listener.MessageUnReadListener;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.request.GetCityInfoRequest;
import com.cyanbirds.momo.net.request.UploadCityInfoRequest;
import com.cyanbirds.momo.ui.widget.CustomViewPager;
import com.cyanbirds.momo.utils.DensityUtil;
import com.cyanbirds.momo.utils.PreferencesUtils;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.yuntongxun.ecsdk.ECInitParams;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class MainNewActivity extends BaseActivity implements MessageUnReadListener.OnMessageUnReadListener, AMapLocationListener {

	private CustomViewPager viewPager;
	private BottomNavigationView bottomNavigationView;

	private static final int REQUEST_PERMISSION = 0;
	private final int REQUEST_LOCATION_PERMISSION = 1000;
	private final int REQUEST_PERMISSION_SETTING = 10001;

	private AMapLocationClientOption mLocationOption;
	private AMapLocationClient mlocationClient;
	private boolean isSecondAccess = false;
	private boolean isSecondRead = false;

	private String curLat;
	private String curLon;

	private Badge mBadgeView;
	private QBadgeView mQBadgeView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		new GetCityInfoTask().request();
		setupViews();
		setupEvent();
		SDKCoreHelper.init(this, ECInitParams.LoginMode.FORCE_LOGIN);
		updateConversationUnRead();

		initLocationClient();

		registerWeiXin();
	}

	private void registerWeiXin() {
		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		AppManager.setIWX_PAY_API(WXAPIFactory.createWXAPI(this, AppConstants.WEIXIN_PAY_ID, true));
		AppManager.getIWX_PAY_API().registerApp(AppConstants.WEIXIN_PAY_ID);
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

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);// 必须要调用这句(信鸽推送)
	}

	@Override
	public void onLocationChanged(AMapLocation aMapLocation) {
		if (aMapLocation != null && !TextUtils.isEmpty(aMapLocation.getCity())) {
			PreferencesUtils.setCurrentCity(this, aMapLocation.getCity());
			ClientUser clientUser = AppManager.getClientUser();
			clientUser.latitude = String.valueOf(aMapLocation.getLatitude());
			clientUser.longitude = String.valueOf(aMapLocation.getLongitude());
			AppManager.setClientUser(clientUser);
			curLat = clientUser.latitude;
			curLon = clientUser.longitude;
			if (TextUtils.isEmpty(PreferencesUtils.getCurrentProvince(this))) {
				PreferencesUtils.setCurrentProvince(this, aMapLocation.getProvince());
			}
			new UploadCityInfoRequest().request(aMapLocation.getCity(), String.valueOf(aMapLocation.getLatitude()),
					String.valueOf(aMapLocation.getLongitude()));
		}
		PreferencesUtils.setLatitude(this, curLat);
		PreferencesUtils.setLongitude(this, curLon);
	}

	/**
	 * 获取用户所在城市
	 */
	class GetCityInfoTask extends GetCityInfoRequest {

		@Override
		public void onPostExecute(CityInfo cityInfo) {
			if (cityInfo != null) {
				try {
					String[] rectangle = cityInfo.rectangle.split(";");
					String[] leftBottom = rectangle[0].split(",");
					String[] rightTop = rectangle[1].split(",");

					double lat = Double.parseDouble(leftBottom[1]) + (Double.parseDouble(rightTop[1]) - Double.parseDouble(leftBottom[1])) / 5;
					curLat = String.valueOf(lat);

					double lon = Double.parseDouble(leftBottom[0]) + (Double.parseDouble(rightTop[0]) - Double.parseDouble(leftBottom[0])) / 5;
					curLon = String.valueOf(lon);

					AppManager.getClientUser().latitude = curLat;
					AppManager.getClientUser().longitude = curLon;
				} catch (Exception e) {

				}
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
		viewPager = findViewById(R.id.viewpager);
		viewPager.setNoScroll(true);
		bottomNavigationView = findViewById(R.id.bottom_navigation);
		//默认 >3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
		BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
		bottomNavigationView.setOnNavigationItemSelectedListener(
				new BottomNavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(@NonNull MenuItem item) {
						switch (item.getItemId()) {
							case R.id.item_news:
								viewPager.setCurrentItem(0);
								break;
							case R.id.item_lib:
								viewPager.setCurrentItem(1);
								break;
							case R.id.item_find:
								viewPager.setCurrentItem(2);
								break;
							case R.id.item_more:
								viewPager.setCurrentItem(3);
								break;
						}
						return false;
					}
				});

		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if(bottomNavigationView.getMenu().getItem(position).isChecked()){
					bottomNavigationView.getMenu().getItem(position).setChecked(false);
				}
			}

			@Override
			public void onPageSelected(int position) {
				bottomNavigationView.getMenu().getItem(position).setChecked(true);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		setupViewPager(viewPager);

		BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
		if (menuView != null) {
			mQBadgeView = new QBadgeView(this);
			mBadgeView = mQBadgeView.setGravityOffset((float) (DensityUtil.getWidthInPx(this) / 1.25), 2, false)
					.bindTarget(menuView);
		}

	}

	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

		adapter.addFragment(new MessageFragment());
		adapter.addFragment(new ContactsFragment());
		adapter.addFragment(new FoundGridFragment());
		adapter.addFragment(new MyPersonalFragment());
		viewPager.setAdapter(adapter);
	}


	private void setupEvent(){
		MessageUnReadListener.getInstance().setMessageUnReadListener(this);
	}


	@Override
	public void notifyUnReadChanged(int type) {
		updateConversationUnRead();
	}

	/**
	 * 更新会话未读消息总数
	 */
	private void updateConversationUnRead() {
		if (mBadgeView != null) {
			int total = ConversationSqlManager.getInstance(this)
					.getAnalyticsUnReadConversation();
			if (total > 0) {
				mQBadgeView.setVisibility(View.VISIBLE);
				if (total >= 100) {
					mBadgeView.setBadgeText("99+");
				} else {
					mBadgeView.setBadgeText(String.valueOf(total));
				}
			} else {
				mQBadgeView.setVisibility(View.GONE);
			}
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
					ActivityCompat.requestPermissions(MainNewActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
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
					ActivityCompat.requestPermissions(MainNewActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
							REQUEST_LOCATION_PERMISSION);
				}

			}
		});
		builder.show();
	}


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
		/*if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - clickTime) > 2000) {
				ToastUtil.showMessage(R.string.exit_tips);
				clickTime = System.currentTimeMillis();
			} else {
				exitApp();
			}
			return true;
		}*/
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			moveTaskToBack(false);
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
