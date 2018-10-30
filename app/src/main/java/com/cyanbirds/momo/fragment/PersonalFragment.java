package com.cyanbirds.momo.fragment;

import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.AboutActivity;
import com.cyanbirds.momo.activity.AttentionMeActivity;
import com.cyanbirds.momo.activity.CustomServiceActivity;
import com.cyanbirds.momo.activity.FeedBackActivity;
import com.cyanbirds.momo.activity.GiveVipActivity;
import com.cyanbirds.momo.activity.LoveFormeActivity;
import com.cyanbirds.momo.activity.MyAttentionActivity;
import com.cyanbirds.momo.activity.MyGiftsActivity;
import com.cyanbirds.momo.activity.PersonalInfoActivity;
import com.cyanbirds.momo.activity.SettingActivity;
import com.cyanbirds.momo.activity.VipCenterActivity;
import com.cyanbirds.momo.activity.VipHWCenterActivity;
import com.cyanbirds.momo.config.AppConstants;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.eventtype.UserEvent;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.IUserFollowApi;
import com.cyanbirds.momo.net.base.RetrofitFactory;
import com.cyanbirds.momo.net.request.DownloadFileRequest;
import com.cyanbirds.momo.utils.FileAccessorUtils;
import com.cyanbirds.momo.utils.JsonUtils;
import com.cyanbirds.momo.utils.Md5Util;
import com.cyanbirds.momo.utils.PreferencesUtils;
import com.cyanbirds.momo.utils.RxBus;
import com.facebook.drawee.view.SimpleDraweeView;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author: wangyb
 * @datetime: 2015-12-20 11:34 GMT+8
 * @email: 395044952@qq.com
 * @description:
 */
public class PersonalFragment extends Fragment {

	@BindView(R.id.user_name)
	TextView userName;
	@BindView(R.id.signature)
	TextView signature;
	@BindView(R.id.user_info)
	LinearLayout userInfo;
	@BindView(R.id.is_vip)
	ImageView isVip;
	@BindView(R.id.head_portrait_lay)
	RelativeLayout headPortraitLay;
	@BindView(R.id.vip_img)
	ImageView vipImg;
	@BindView(R.id.vip_lay)
	RelativeLayout vipLay;
	@BindView(R.id.my_attention_img)
	ImageView myAttentionImg;
	@BindView(R.id.my_attention)
	RelativeLayout myAttention;
	@BindView(R.id.attentioned_user_img)
	ImageView attentionedUserImg;
	@BindView(R.id.attention_count)
	TextView attentionCount;
	@BindView(R.id.attentioned_user)
	RelativeLayout attentionedUser;
	@BindView(R.id.good_user_img)
	ImageView goodUserImg;
	@BindView(R.id.love_count)
	TextView loveCount;
	@BindView(R.id.good_user)
	RelativeLayout goodUser;
	@BindView(R.id.setting_img)
	ImageView settingImg;
	@BindView(R.id.setting)
	RelativeLayout setting;
	@BindView(R.id.about)
	RelativeLayout about;
	@BindView(R.id.portrait)
    SimpleDraweeView mPortrait;
	@BindView(R.id.my_gifts)
	RelativeLayout mMyGifts;
	@BindView(R.id.gifts_count)
	TextView giftsCount;
	@BindView(R.id.vip_card)
    CardView mVipCard;
	@BindView(R.id.gift_red_point)
	ImageView mGiftRedPoint;
	@BindView(R.id.attention_red_point)
	ImageView mAttentionRedPoint;
	@BindView(R.id.love_red_point)
	ImageView mLoveRedPoint;
	@BindView(R.id.card_feedback)
    CardView mFeedBackCard;
	@BindView(R.id.feedback)
	RelativeLayout mFeedBack;
	@BindView(R.id.custom_service)
	RelativeLayout mCustomService;
	@BindView(R.id.my_appointment_lay)
	RelativeLayout mAppointmentLay;
	@BindView(R.id.give_vip)
	RelativeLayout mGiveVipLay;

	private View rootView;

	private ClientUser clientUser;

	private Observable<UserEvent> observable;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_personal, null);
			ButterKnife.bind(this, rootView);
			rxBusSub();
			setupData();
			setHasOptionsMenu(true);
		}
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		return rootView;
	}

	/**
	 * rx订阅
	 */
	private void rxBusSub() {
		observable = RxBus.getInstance().register(AppConstants.UPDATE_USER_INFO);
		observable.subscribe(this::changeUserInfo);
	}

	private void setupData() {
		mLoveRedPoint.setVisibility(View.VISIBLE);
		mAttentionRedPoint.setVisibility(View.VISIBLE);
		mGiftRedPoint.setVisibility(View.VISIBLE);
		setUserInfo();
		getFollowLove();
	}


	private void getFollowLove() {
		RetrofitFactory.getRetrofit().create(IUserFollowApi.class)
				.getFollowAndLoveInfo(AppManager.getClientUser().sessionId, AppManager.getClientUser().userId)
				.subscribeOn(Schedulers.io())
				.map(responseBody -> JsonUtils.parseFollowLove(responseBody.string()))
				.observeOn(AndroidSchedulers.mainThread())
				.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
				.subscribe(followLoveModel -> {
					if (null != followLoveModel) {
						if (followLoveModel.followCount > 0) {
							attentionCount.setVisibility(View.VISIBLE);
							attentionCount.setText(String.valueOf(followLoveModel.followCount));
						}
						if (followLoveModel.loveCount > 0) {
							loveCount.setVisibility(View.VISIBLE);
							loveCount.setText(String.valueOf(followLoveModel.loveCount));
						}
						if (followLoveModel.giftsCount > 0) {
							giftsCount.setVisibility(View.VISIBLE);
							giftsCount.setText(String.valueOf(followLoveModel.giftsCount));
						}
					}
				}, throwable -> {});

	}

	/**
	 * 设置用户信息
	 */
	private void setUserInfo() {
		clientUser = AppManager.getClientUser();
		if (clientUser != null) {
			if (!TextUtils.isEmpty(clientUser.face_local) && new File(clientUser.face_local).exists()) {
				mPortrait.setImageURI(Uri.parse("file://" + clientUser.face_local));
			} else if (!TextUtils.isEmpty(clientUser.face_url)) {
				mPortrait.setImageURI(Uri.parse(clientUser.face_url));
				new DownloadPortraitTask().request(clientUser.face_url,
						FileAccessorUtils.FACE_IMAGE,
						Md5Util.md5(clientUser.face_url) + ".jpg");
			}
			if (!TextUtils.isEmpty(clientUser.signature)) {
				signature.setText(clientUser.signature);
			}
			if (!TextUtils.isEmpty(clientUser.user_name)) {
				userName.setText(clientUser.user_name);
			}
			if (clientUser.isShowVip && clientUser.is_vip) {
				isVip.setVisibility(View.VISIBLE);
			} else {
				isVip.setVisibility(View.GONE);
			}
			if (clientUser.isShowVip) {
				mVipCard.setVisibility(View.VISIBLE);
				vipLay.setVisibility(View.VISIBLE);
			} else {
				mVipCard.setVisibility(View.GONE);
				vipLay.setVisibility(View.GONE);
			}
			if (clientUser.isShowAppointment) {
				mAppointmentLay.setVisibility(View.VISIBLE);
			} else {
				mAppointmentLay.setVisibility(View.GONE);
			}
			if (clientUser.isShowVip && clientUser.isShowGiveVip) {
				mGiveVipLay.setVisibility(View.VISIBLE);
			} else {
				mGiveVipLay.setVisibility(View.GONE);
			}
			if (!clientUser.isShowGiveVip || clientUser.isShowDownloadVip) {
				mCustomService.setVisibility(View.VISIBLE);
			} else {
				mCustomService.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 下载头像
	 */
	class DownloadPortraitTask extends DownloadFileRequest {
		@Override
		public void onPostExecute(String s) {
			if (!TextUtils.isEmpty(s)) {
				AppManager.getClientUser().face_local = s;
				PreferencesUtils.setFaceLocal(getActivity(), s);
			}
		}

		@Override
		public void onErrorExecute(String error) {
		}
	}

	@OnClick({
			R.id.head_portrait_lay, R.id.vip_lay, R.id.my_attention,
			R.id.attentioned_user, R.id.good_user, R.id.setting, R.id.about, R.id.my_gifts,
			R.id.feedback, R.id.custom_service, R.id.my_appointment_lay, R.id.give_vip})
	public void onClick(View view) {
		Intent intent = new Intent();
		switch (view.getId()) {
			case R.id.head_portrait_lay:
				intent.setClass(getActivity(), PersonalInfoActivity.class);
				intent.putExtra(ValueKey.USER_ID, AppManager.getClientUser().userId);
				startActivity(intent);
				break;
			case R.id.vip_lay:
				if (AppManager.getClientUser().isShowGold) {
					intent.setClass(getActivity(), VipHWCenterActivity.class);
				} else {
					intent.setClass(getActivity(), VipCenterActivity.class);
				}
				startActivity(intent);
				break;
			case R.id.my_attention:
				intent.setClass(getActivity(), MyAttentionActivity.class);
				startActivity(intent);
				break;
			case R.id.my_gifts:
				mGiftRedPoint.setVisibility(View.GONE);
				intent.setClass(getActivity(), MyGiftsActivity.class);
				startActivity(intent);
				break;
			case R.id.attentioned_user:
				mAttentionRedPoint.setVisibility(View.GONE);
				intent.setClass(getActivity(), AttentionMeActivity.class);
				startActivity(intent);
				break;
			case R.id.good_user:
				mLoveRedPoint.setVisibility(View.GONE);
				intent.setClass(getActivity(), LoveFormeActivity.class);
				startActivity(intent);
				break;
			case R.id.setting:
				intent.setClass(getActivity(), SettingActivity.class);
				startActivity(intent);
				break;
			case R.id.about:
				intent.setClass(getActivity(), AboutActivity.class);
				startActivity(intent);
				break;
			case R.id.feedback:
				intent.setClass(getActivity(), FeedBackActivity.class);
				startActivity(intent);
				break;
			case R.id.custom_service:
				intent.setClass(getActivity(), CustomServiceActivity.class);
				startActivity(intent);
				break;
			case R.id.my_appointment_lay:
//				intent.setClass(getActivity(), MyAppointmentActivity.class);
//				startActivity(intent);
				break;
			case R.id.give_vip:
				intent.setClass(getActivity(), GiveVipActivity.class);
				startActivity(intent);
				break;
		}
	}


	private void changeUserInfo(UserEvent event) {
		ClientUser clientUser = AppManager.getClientUser();
		if (clientUser != null) {
			if (!TextUtils.isEmpty(clientUser.face_url) && null != mPortrait) {
				mPortrait.setImageURI(Uri.parse(clientUser.face_url));
			}
			if (!TextUtils.isEmpty(clientUser.signature) && null != signature) {
				signature.setText(clientUser.signature);
			}
			if (!TextUtils.isEmpty(clientUser.user_name) && null != userName) {
				userName.setText(clientUser.user_name);
			}
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getName());
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getName());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		RxBus.getInstance().unregister(AppConstants.UPDATE_USER_INFO, observable);
	}
}
