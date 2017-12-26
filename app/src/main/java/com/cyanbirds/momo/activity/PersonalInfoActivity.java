package com.cyanbirds.momo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.adapter.PhotosAdapter;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.eventtype.UserEvent;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.request.AddFollowRequest;
import com.cyanbirds.momo.net.request.AddLoveRequest;
import com.cyanbirds.momo.net.request.GetUserInfoRequest;
import com.cyanbirds.momo.net.request.GetUserPictureRequest;
import com.cyanbirds.momo.net.request.SendGreetRequest;
import com.cyanbirds.momo.net.request.UpdateGoldRequest;
import com.cyanbirds.momo.ui.widget.NoScrollGridView;
import com.cyanbirds.momo.utils.ProgressDialogUtils;
import com.cyanbirds.momo.utils.StringUtil;
import com.cyanbirds.momo.utils.ToastUtil;
import com.dl7.tag.TagLayout;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author: wangyb
 * @datetime: 2016-01-02 16:26 GMT+8
 * @email: 395044952@qq.com
 * @description: 个人信息界面
 */
public class PersonalInfoActivity extends BaseActivity implements GeocodeSearch.OnGeocodeSearchListener,
		AMap.OnMapScreenShotListener{

	@BindView(R.id.portrait)
    SimpleDraweeView mPortrait;
	@BindView(R.id.identify_state)
	TextView mIdentifyState;
	@BindView(R.id.iv_isVip)
	ImageView mIvIsVip;
	@BindView(R.id.age)
	TextView mAge;
	@BindView(R.id.city_distance)
	TextView mCityDistance;
	@BindView(R.id.img_contents)
	NoScrollGridView mImgContents;
	@BindView(R.id.nick_name)
	TextView mNickName;
	@BindView(R.id.occupation)
	TextView mOccupation;
	@BindView(R.id.constellation)
	TextView mConstellation;
	@BindView(R.id.heigh)
	TextView mHeigh;
	@BindView(R.id.weight)
	TextView mWeight;
	@BindView(R.id.emotion)
	TextView mEmotion;
	@BindView(R.id.colleage)
	TextView mColleage;
	@BindView(R.id.signature)
	TextView mSignature;
	@BindView(R.id.purpose)
	TextView mPurpose;
	@BindView(R.id.conception)
	TextView mConception;
	@BindView(R.id.loveWhere)
	TextView mLoveWhere;
	@BindView(R.id.do_what_first)
	TextView mDoWhatFirst;
	@BindView(R.id.wechat_id)
	TextView mWechatId;
	@BindView(R.id.check_view_wechat)
	Button mCheckViewWechat;
	@BindView(R.id.qq_id)
	TextView mQqId;
	@BindView(R.id.check_view_qq)
	Button mCheckViewQq;
	@BindView(R.id.plable_flowlayout)
    TagLayout mPlableFlowlayout;
	@BindView(R.id.plable_lay)
	RelativeLayout mPlableLay;
	@BindView(R.id.part_flowlayout)
    TagLayout mPartFlowlayout;
	@BindView(R.id.part_lay)
	RelativeLayout mPartLay;
	@BindView(R.id.intrest_flowlayout)
    TagLayout mIntrestFlowlayout;
	@BindView(R.id.intrest_lay)
	RelativeLayout mIntrestLay;
	@BindView(R.id.fab)
    FloatingActionButton mFab;
	@BindView(R.id.gift)
	TextView mGift;
	@BindView(R.id.love)
	TextView mLove;
	@BindView(R.id.message)
	TextView mMessage;
	@BindView(R.id.attention)
	TextView mAttention;
	@BindView(R.id.bottom_layout)
	LinearLayout mBottomLayout;
	@BindView(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
	@BindView(R.id.tv_city)
	TextView mTvCity;
	@BindView(R.id.toolbar_actionbar)
    Toolbar mToolbar;
	@BindView(R.id.see_more)
	TextView mSeeMore;
	@BindView(R.id.city_lay)
	RelativeLayout mCityLay;
	@BindView(R.id.tv_social)
	TextView mTvSocial;
	@BindView(R.id.social_lay)
	LinearLayout mSocialLay;
	@BindView(R.id.photo_lay)
	RelativeLayout mPhotoLay;
	@BindView(R.id.map)
    MapView mapView;
	@BindView(R.id.address)
	TextView mAdress;
	@BindView(R.id.map_lay)
	LinearLayout mMapLay;
	@BindView(R.id.my_location)
	TextView mMyLocation;
	@BindView(R.id.tv_friend)
	TextView mTvFriend;
	@BindView(R.id.lay_friend)
	LinearLayout mCardFriend;

	private ClientUser mClientUser; //当前用户
	private String curUserId; //当前用户id
	private List<String> mVals = null;

	private AMap aMap;
	private UiSettings mUiSettings;
	private GeocodeSearch geocoderSearch;

	private LatLonPoint mLatLonPoint;
	private String mAddress;// 选中的地址
	private double latitude;
	private double longitude;

	private PhotosAdapter mAdapter;
	private List<String> mShowImgs;
	private ArrayList<String> imgList;//用户所有的相片

	private DecimalFormat mFormat;

	private static final int PHOTO_CHOISE = 100;

	private DPoint myPoint;
	private DPoint clienterPoint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_info);
		ButterKnife.bind(this);
		EventBus.getDefault().register(this);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		setupView();
		setupData();
	}

	private void setupView() {
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
	}

	private void setupData() {
		ProgressDialogUtils.getInstance(PersonalInfoActivity.this).show(R.string.dialog_request_data);
		mFormat = new DecimalFormat("#.00");
		mVals = new ArrayList<>();
		curUserId = getIntent().getStringExtra(ValueKey.USER_ID);
		if (!TextUtils.isEmpty(curUserId)) {
			initMap();
			if (AppManager.getClientUser().userId.equals(curUserId)) {
				mFab.setVisibility(View.VISIBLE);
				mBottomLayout.setVisibility(View.GONE);
			} else {
				mFab.setVisibility(View.GONE);
				mBottomLayout.setVisibility(View.VISIBLE);
			}
			new GetUserInfoTask().request(curUserId);
		}
	}

	/**
	 * 初始化AMap对象
	 */
	private void initMap() {
		if (aMap == null) {
			aMap = mapView.getMap();
			mUiSettings = aMap.getUiSettings();
			mUiSettings.setZoomControlsEnabled(false);// 不显示缩放按钮
			mUiSettings.setLogoPosition(-50);
			mUiSettings.setZoomGesturesEnabled(false);
			aMap.moveCamera(CameraUpdateFactory.zoomTo(16));// 设置缩放比例
		}


		// 地理编码
		geocoderSearch = new GeocodeSearch(this);
		geocoderSearch.setOnGeocodeSearchListener(this);

	}

	/**
	 * 获取用户信息
	 */
	class GetUserInfoTask extends GetUserInfoRequest {
		@Override
		public void onPostExecute(ClientUser clientUser) {
			ProgressDialogUtils.getInstance(PersonalInfoActivity.this).dismiss();
			mClientUser = clientUser;
			if (null != mClientUser) {
				if (!TextUtils.isEmpty(mClientUser.latitude) &&
						!TextUtils.isEmpty(mClientUser.longitude)) {
					latitude = Double.parseDouble(mClientUser.latitude);
					longitude = Double.parseDouble(mClientUser.longitude);
					getLocation();
				}
				setUserInfoAndValue(clientUser);
				if (!TextUtils.isEmpty(clientUser.imgUrls)) {
					mImgContents.setVisibility(View.VISIBLE);
					Type listType = new TypeToken<ArrayList<String>>() {
					}.getType();
					Gson gson = new Gson();
					imgList = gson.fromJson(clientUser.imgUrls, listType);
					if (imgList.size() > 8) {
						mShowImgs = new ArrayList<>(8);
						mSeeMore.setVisibility(View.VISIBLE);
						for (int i = 0; i < imgList.size(); i++) {
							mShowImgs.add(imgList.get(i));
							if (i == 7) {
								break;
							}
						}
						mAdapter = new PhotosAdapter(PersonalInfoActivity.this, mShowImgs, mImgContents);
					} else {
						mSeeMore.setVisibility(View.GONE);
						mAdapter = new PhotosAdapter(PersonalInfoActivity.this, imgList, mImgContents);
					}
					mImgContents.setAdapter(mAdapter);
					mSeeMore.setText(String.format(getResources().getString(R.string.see_more), imgList.size()));
				} else {
					mImgContents.setVisibility(View.GONE);
				}
			}
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
			ProgressDialogUtils.getInstance(PersonalInfoActivity.this).dismiss();
		}
	}

	/**
	 * 展示用户地图
	 */
	private void getLocation() {
		String myLatitude = AppManager.getClientUser().latitude;
		String myLongitude = AppManager.getClientUser().longitude;
		if (!TextUtils.isEmpty(myLatitude) &&
				!TextUtils.isEmpty(myLongitude)) {
			myPoint = new DPoint(Double.parseDouble(myLatitude), Double.parseDouble(myLongitude));
			LatLonPoint latLonPoint = null;
			if ("-1".equals(AppManager.getClientUser().userId) ||
					curUserId.equals(AppManager.getClientUser().userId)) {
				latLonPoint = new LatLonPoint(latitude, longitude);
			} else {
				latLonPoint = new LatLonPoint(Double.parseDouble(myLatitude) + latitude,
						Double.parseDouble(myLongitude) + longitude);
				clienterPoint = new DPoint(Double.parseDouble(myLatitude) + latitude,
						Double.parseDouble(myLongitude) + longitude);
			}
			mLatLonPoint = latLonPoint;
			LatLng latLng = null;
			if ("-1".equals(AppManager.getClientUser().userId) ||
					curUserId.equals(AppManager.getClientUser().userId)) {
				latLng = new LatLng(latitude, longitude);
			} else {
				latLng = new LatLng(Double.parseDouble(myLatitude) + latitude,
						Double.parseDouble(myLongitude) + longitude);
			}
			aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng));
			RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 1000,
					GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
			geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
		}
	}

	/**
	 * 将TabLayout和ViewPager关联起来。
	 *
	 * @param clientUser
	 */
	private void setUserInfoAndValue(ClientUser clientUser) {

		//如果是本人信息，先查找本地有没有头像，有就加载，没有就用face_url;如果是其他用户信息，直接使用face_url
		String imagePath = "";
		if (clientUser.userId.equals(AppManager.getClientUser().userId)) {
			if (!TextUtils.isEmpty(clientUser.face_url)) {
				imagePath = clientUser.face_url;
			} else if (!TextUtils.isEmpty(clientUser.face_local) && new File(clientUser.face_local).exists()) {
				imagePath = "file://" + clientUser.face_local;
			} else {
				imagePath = "res:///" + R.mipmap.default_head;
			}
		} else {
			imagePath = clientUser.face_url;
		}
		if (!TextUtils.isEmpty(imagePath)) {
			mPortrait.setImageURI(Uri.parse(imagePath));
		}
		if (AppManager.getClientUser().isShowVip && clientUser.is_vip) {
			mIvIsVip.setVisibility(View.VISIBLE);
			mIdentifyState.setVisibility(View.VISIBLE);
		} else {
			mIdentifyState.setVisibility(View.GONE);
			mIvIsVip.setVisibility(View.GONE);
		}
		if (curUserId.equals(AppManager.getClientUser().userId)) {
			mIdentifyState.setVisibility(View.GONE);
		}
		mAge.setText(String.valueOf(clientUser.age) + "岁");
		if (curUserId.equals(AppManager.getClientUser().userId)) {
			mCityLay.setVisibility(View.GONE);
			mCheckViewQq.setVisibility(View.GONE);
			mCheckViewWechat.setVisibility(View.GONE);
		} else {
			mCheckViewQq.setVisibility(View.VISIBLE);
			mCheckViewWechat.setVisibility(View.VISIBLE);
			mCityLay.setVisibility(View.VISIBLE);
			if (!TextUtils.isEmpty(clientUser.distance) && Double.parseDouble(clientUser.distance) != 0) {
				mTvCity.setText("距离");
				if (myPoint != null && clienterPoint != null) {
					mCityDistance.setText(mFormat.format(
							CoordinateConverter.calculateLineDistance(myPoint, clienterPoint) / 1000) + "km");
				} else {
					mCityDistance.setText(mFormat.format(Double.parseDouble(clientUser.distance)) + "km");
				}
			} else if (!TextUtils.isEmpty(clientUser.city)) {
				mTvCity.setText("城市");
				mCityDistance.setText(clientUser.city);
			}
		}
		if (mClientUser.isFollow) {
			mAttention.setText("已关注");
			mAttention.setTextColor(getResources().getColor(R.color.colorPrimary));
		}
		if (!TextUtils.isEmpty(clientUser.imgUrls)) {
			mPhotoLay.setVisibility(View.VISIBLE);
		} else {
			mPhotoLay.setVisibility(View.GONE);
		}
		if (AppManager.getClientUser().userId.equals(clientUser.userId)) {
			mAttention.setVisibility(View.GONE);
			mCityDistance.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(clientUser.user_name)) {
			mNickName.setText(clientUser.user_name);
			mCollapsingToolbarLayout.setTitle(clientUser.user_name);
		}
		if (!TextUtils.isEmpty(clientUser.occupation)) {
			mOccupation.setText(clientUser.occupation);
		}
		if (!TextUtils.isEmpty(clientUser.constellation)) {
			mConstellation.setText(clientUser.constellation);
		}
		if (!TextUtils.isEmpty(clientUser.tall)) {
			mHeigh.setText(clientUser.tall);
		}
		if (!TextUtils.isEmpty(clientUser.weight)) {
			mWeight.setText(clientUser.weight);
		}
		if (!TextUtils.isEmpty(clientUser.state_marry)) {
			mEmotion.setText(clientUser.state_marry);
		}
		if (!TextUtils.isEmpty(clientUser.education)) {
			mColleage.setText(clientUser.education);
		}
		if (!TextUtils.isEmpty(clientUser.signature)) {
			mSignature.setText(clientUser.signature);
		}
		if (!TextUtils.isEmpty(clientUser.purpose)) {
			mPurpose.setText(clientUser.purpose);
		}
		if (!TextUtils.isEmpty(clientUser.conception)) {
			mConception.setText(clientUser.conception);
		}
		if (!TextUtils.isEmpty(clientUser.love_where)) {
			mLoveWhere.setText(clientUser.love_where);
		}
		if (!TextUtils.isEmpty(clientUser.do_what_first)) {
			mDoWhatFirst.setText(clientUser.do_what_first);
		}
		if (AppManager.getClientUser().isShowVip) {
			mSocialLay.setVisibility(View.VISIBLE);
			mTvSocial.setVisibility(View.VISIBLE);
			if (curUserId.equals(AppManager.getClientUser().userId)) {
				if (!TextUtils.isEmpty(clientUser.weixin_no)) {
					mWechatId.setText(clientUser.weixin_no);
				}
				if (!TextUtils.isEmpty(clientUser.qq_no)) {
					mQqId.setText(clientUser.qq_no);
				}
			} else {
				if (!TextUtils.isEmpty(clientUser.weixin_no)) {
					String weChat = clientUser.weixin_no;
					String subUrl = clientUser.weixin_no.substring(2, clientUser.weixin_no.length() - 3);
					weChat = weChat.replaceAll(subUrl, "****");
					mWechatId.setText(weChat);
				}
				if (!TextUtils.isEmpty(clientUser.qq_no)) {
					String qq = clientUser.qq_no;
					String subUrl = clientUser.qq_no.substring(2, clientUser.qq_no.length() - 3);
					qq = qq.replaceAll(subUrl, "****");
					mQqId.setText(qq);
				}
			}
		} else {
			mSocialLay.setVisibility(View.GONE);
			mTvSocial.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(clientUser.part_tag)) {
			mPartFlowlayout.setVisibility(View.VISIBLE);
			mVals.clear();
			mVals = StringUtil.stringToIntList(clientUser.part_tag);
			for (int i = 0; i < mVals.size(); i++) {
				if ("".equals(mVals.get(i)) || " ".equals(mVals.get(i))) {
					mVals.remove(i);
				}
			}
			mPartFlowlayout.setTags(mVals);
		}
		if (!TextUtils.isEmpty(clientUser.personality_tag)) {
			mPlableFlowlayout.setVisibility(View.VISIBLE);
			mVals.clear();
			mVals = StringUtil.stringToIntList(clientUser.personality_tag);
			for (int i = 0; i < mVals.size(); i++) {
				if ("".equals(mVals.get(i)) || " ".equals(mVals.get(i))) {
					mVals.remove(i);
				}
			}
			mPlableFlowlayout.setTags(mVals);
		}
		if (!TextUtils.isEmpty(clientUser.intrest_tag)) {
			mIntrestFlowlayout.setVisibility(View.VISIBLE);
			mVals.clear();
			mVals = StringUtil.stringToIntList(clientUser.intrest_tag);
			for (int i = 0; i < mVals.size(); i++) {
				if ("".equals(mVals.get(i)) || " ".equals(mVals.get(i))) {
					mVals.remove(i);
				}
			}
			mIntrestFlowlayout.setTags(mVals);
		}
		/*if (AppManager.getClientUser().isShowLovers) {
			mCardFriend.setVisibility(View.VISIBLE);
			mTvFriend.setVisibility(View.VISIBLE);
			mGift.setVisibility(View.VISIBLE);
		} else {
			mCardFriend.setVisibility(View.GONE);
			mTvFriend.setVisibility(View.GONE);
			mGift.setVisibility(View.GONE);
		}*/
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void updateUserInfo(UserEvent event) {
		//如果是本人信息，先查找本地有没有头像，有就加载，没有就用face_url;如果是其他用户信息，直接使用face_url
		setUserInfoAndValue(AppManager.getClientUser());
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (AppManager.getClientUser().userId.equals(curUserId)) {
			getMenuInflater().inflate(R.menu.personal_menu, menu);
		} else {
			if (AppManager.getClientUser().isShowVip) {
				if (!AppManager.getClientUser().is_vip || AppManager.getClientUser().gold_num < 100) {
					getMenuInflater().inflate(R.menu.call_menu, menu);
				}
			}
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.modify_info) {
			Intent intent = new Intent(this, ModifyUserInfoActivity.class);
			startActivity(intent);
		} else if (item.getItemId() == R.id.call) {
			Intent intent = new Intent(this, VoipCallActivity.class);
			intent.putExtra(ValueKey.IMAGE_URL, mClientUser.face_url);
			intent.putExtra(ValueKey.USER_NAME, mClientUser.user_name);
			intent.putExtra(ValueKey.FROM_ACTIVITY, "PersonalInfoActivity");
			startActivity(intent);
		} else {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@OnClick({R.id.portrait, R.id.fab, R.id.gift, R.id.love, R.id.message,
			R.id.attention, R.id.see_more,
			R.id.check_view_wechat, R.id.check_view_qq})
	public void onClick(View view) {
		Intent intent = new Intent();
		switch (view.getId()) {
			case R.id.portrait:
				intent.setClass(this, PhotoViewActivity.class);
				if (mClientUser != null) {
					if (!TextUtils.isEmpty(mClientUser.face_local) && new File(mClientUser.face_local).exists()) {
						intent.putExtra(ValueKey.IMAGE_URL, "file://" + mClientUser.face_local);
					} else {
						intent.putExtra(ValueKey.IMAGE_URL, mClientUser.face_url);
					}
				}
				intent.putExtra(ValueKey.FROM_ACTIVITY, "PersonalInfoActivity");
				startActivity(intent);
				break;
			case R.id.fab:
				intent.setClass(this, PhotoChoserActivity.class);
				startActivityForResult(intent, PHOTO_CHOISE);
				break;
			case R.id.gift:
				intent.setClass(this, GiftMarketActivity.class);
				intent.putExtra(ValueKey.USER, mClientUser);
				startActivity(intent);
				break;
			case R.id.love:
				if (mClientUser != null) {
					new SenderGreetTask().request(mClientUser.userId);
					new AddLoveTask().request(mClientUser.userId);
				}
				break;
			case R.id.message:
				intent.setClass(this, ChatActivity.class);
				intent.putExtra(ValueKey.USER, mClientUser);
				startActivity(intent);
				break;
			case R.id.attention:
				new AddFollowTask().request(mClientUser.userId);
				break;
			case R.id.see_more:
				intent.setClass(this, PersonalPhotoActivity.class);
				intent.putExtra(ValueKey.IMAGE_URL, imgList);
				startActivity(intent);
				break;
			case R.id.check_view_wechat:
				if (AppManager.getClientUser().is_vip) {
					if (AppManager.getClientUser().gold_num < 1) {
						String tips = String.format(getResources().getString(R.string.social_id_need_gold), "微信");
						showBuyGoldDialog(tips);
					} else if (AppManager.getClientUser().gold_num < 101){
						String tips = String.format(getResources().getString(R.string.social_id_need_more_gold), "微信");
						showBuyGoldDialog(tips);
					} else {
						mWechatId.setText(mClientUser.weixin_no);
						if (!AppManager.getClientUser().is_download_vip) {
							//更新服务器上的金币数量
							AppManager.getClientUser().gold_num -= 101;
							new UpdateGoldTask().request(AppManager.getClientUser().gold_num, "");
						}
					}
				} else {
					showTurnOnVipDialog("微信");
				}
				break;
			case R.id.check_view_qq:
				if (AppManager.getClientUser().is_vip) {
					if (AppManager.getClientUser().gold_num < 1) {
						String tips = String.format(getResources().getString(R.string.social_id_need_gold), "QQ");
						showBuyGoldDialog(tips);
					} else if (AppManager.getClientUser().gold_num < 101){
						String tips = String.format(getResources().getString(R.string.social_id_need_more_gold), "QQ");
						showBuyGoldDialog(tips);
					} else {
						mQqId.setText(mClientUser.qq_no);
						if (!AppManager.getClientUser().is_download_vip) {
							//更新服务器上的金币数量
							AppManager.getClientUser().gold_num -= 101;
							new UpdateGoldTask().request(AppManager.getClientUser().gold_num, "");
						}
					}
				} else {
					showTurnOnVipDialog("QQ");
				}
				break;
		}
	}

	/**
	 * 不是下载赚钱会员，查看微信、QQ号时，减少金币数量
	 */
	class UpdateGoldTask extends UpdateGoldRequest {
		@Override
		public void onPostExecute(Integer integer) {
			if (AppManager.getClientUser().isShowDownloadVip) {
				Snackbar.make(PersonalInfoActivity.this.findViewById(R.id.content),
						"您还不是赚钱会员，查看该号码已消耗101枚金币", Snackbar.LENGTH_SHORT)
						.setActionTextColor(Color.RED)
						.setAction("立即开通", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(PersonalInfoActivity.this, MakeMoneyActivity.class);
								intent.putExtra(ValueKey.FROM_ACTIVITY, PersonalInfoActivity.this.getClass().getSimpleName());
								startActivity(intent);
							}
						}).show();
			} else {
				Snackbar.make(PersonalInfoActivity.this.findViewById(R.id.content), "查看该号码已消耗101枚金币",
						Snackbar.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onErrorExecute(String error) {
		}
	}

	/**
	 * 关注
	 */
	class AddFollowTask extends AddFollowRequest {
		@Override
		public void onPostExecute(String s) {
			if (s.equals("已关注")) {
				ToastUtil.showMessage(R.string.attention_success);
				mAttention.setText(s);
			} else {
				ToastUtil.showMessage(R.string.cancle_attention);
				mAttention.setText(R.string.attention);
			}
		}

		@Override
		public void onErrorExecute(String error) {
			super.onErrorExecute(error);
		}
	}

	class SenderGreetTask extends SendGreetRequest {
		@Override
		public void onPostExecute(String s) {
			ToastUtil.showMessage(s);
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
		}
	}

	class AddLoveTask extends AddLoveRequest {

		@Override
		public void onPostExecute(Boolean s) {
			if (s) {
				mLove.setText("已喜欢");
			} else {
				mLove.setText("喜欢");
			}
		}

		@Override
		public void onErrorExecute(String error) {
		}
	}
	
	
	private void showTurnOnVipDialog(String socialTpe) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(String.format(getResources().getString(R.string.social_id_need_vip), socialTpe));
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(PersonalInfoActivity.this, VipCenterActivity.class);
				startActivity(intent);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}

	private void showBuyGoldDialog(String tips) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(tips);
		builder.setPositiveButton(getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						Intent intent = new Intent(PersonalInfoActivity.this, MyGoldActivity.class);
						startActivity(intent);
					}
				});
		builder.setNegativeButton(getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PHOTO_CHOISE && resultCode == RESULT_OK) {
			ArrayList<String> pictures = data.getStringArrayListExtra(ValueKey.IMAGE_URL);
			imgList = pictures;
			if (pictures.size() > 8) {
				if (mShowImgs != null) {
					mShowImgs.clear();
				} else {
					mShowImgs = new ArrayList<>(8);
				}
				for (int i = pictures.size() - 1; i > 0; i--) {
					mShowImgs.add(pictures.get(i));
					if (mShowImgs.size() == 8) {
						break;
					}
				}
				mSeeMore.setVisibility(View.VISIBLE);
				mSeeMore.setText(String.format(getResources().getString(R.string.see_more), pictures.size()));
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				} else {
					mAdapter = new PhotosAdapter(PersonalInfoActivity.this, mShowImgs, mImgContents);
					mImgContents.setAdapter(mAdapter);
				}
			} else {
				mSeeMore.setVisibility(View.GONE);
				mAdapter = new PhotosAdapter(PersonalInfoActivity.this, pictures, mImgContents);
				mImgContents.setAdapter(mAdapter);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	public void onMapScreenShot(Bitmap bitmap) {

	}

	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		if (rCode == 1000) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {
				PoiItem poiItem = new PoiItem("", mLatLonPoint, "", result
						.getRegeocodeAddress().getFormatAddress());
				mAddress = poiItem.getSnippet();
				mAdress.setText(mAddress);
				if (AppManager.getClientUser().isShowMap &&
						!TextUtils.isEmpty(mClientUser.distance) &&
						!"0.0".equals(mClientUser.distance) &&
						!TextUtils.isEmpty(mAddress)) {
					mMyLocation.setVisibility(View.VISIBLE);
					mMapLay.setVisibility(View.VISIBLE);
				} else {
					mMapLay.setVisibility(View.GONE);
					mMyLocation.setVisibility(View.GONE);
				}
				if (mClientUser.userId.equals(AppManager.getClientUser().userId)) {
					mMyLocation.setVisibility(View.GONE);
					mMapLay.setVisibility(View.GONE);
				}
			} else {
				mMyLocation.setVisibility(View.GONE);
				mMapLay.setVisibility(View.GONE);
			}
		} else {
			mMyLocation.setVisibility(View.GONE);
			mMapLay.setVisibility(View.GONE);
		}
	}

	@Override
	public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
		MobclickAgent.onPageStart(this.getClass().getName());
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		MobclickAgent.onPageEnd(this.getClass().getName());
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		EventBus.getDefault().unregister(this);
	}

}
