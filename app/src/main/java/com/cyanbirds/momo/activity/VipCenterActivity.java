package com.cyanbirds.momo.activity;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.cyanbirds.momo.CSApplication;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.adapter.MemberBuyAdapter;
import com.cyanbirds.momo.config.AppConstants;
import com.cyanbirds.momo.entity.MemberBuy;
import com.cyanbirds.momo.entity.PayResult;
import com.cyanbirds.momo.entity.UserVipModel;
import com.cyanbirds.momo.helper.SDKCoreHelper;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.IUserApi;
import com.cyanbirds.momo.net.IUserBuyApi;
import com.cyanbirds.momo.net.base.RetrofitFactory;
import com.cyanbirds.momo.ui.widget.DividerItemDecoration;
import com.cyanbirds.momo.ui.widget.WrapperLinearLayoutManager;
import com.cyanbirds.momo.utils.AESOperator;
import com.cyanbirds.momo.utils.DensityUtil;
import com.cyanbirds.momo.utils.JsonUtils;
import com.cyanbirds.momo.utils.RxBus;
import com.cyanbirds.momo.utils.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sunfusheng.marqueeview.MarqueeView;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import com.umeng.analytics.MobclickAgent;
import com.yuntongxun.ecsdk.ECInitParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2016-01-13 21:34 GMT+8
 * @email 395044952@qq.com
 */
public class VipCenterActivity extends BaseActivity {

	@BindView(R.id.toolbar)
	Toolbar mToolbar;
	@BindView(R.id.marqueeView)
	MarqueeView mMarqueeView;
	@BindView(R.id.recyclerview)
	RecyclerView mRecyclerView;
	@BindView(R.id.preferential)
	TextView mPreferential;//优惠的说明文字，可以控制什么时候显示
	@BindView(R.id.vip_7_lay)
	RelativeLayout mVip7Lay;
	@BindView(R.id.scrollView)
	NestedScrollView mScrollView;
	@BindView(R.id.pref_tel_fare_lay)
	LinearLayout mPrefTelFareLay;
	@BindView(R.id.cum_qq)
	TextView mCumQQ;

	private MemberBuyAdapter mAdapter;

	/**
	 * 没有网络时显示开通会员的名单
	 */
	private List<String> turnOnVipNameList;

	private static final int SDK_PAY_FLAG = 1;

	/**
	 * 普通会员商品
	 */
	private final int NORMAL_VIP = 0;

	private List<Integer> array;

	private String mPref;//优惠信息

	private Observable<?> observable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vipcenter);
		ButterKnife.bind(this);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			setSupportActionBar(mToolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle(R.string.vip_center);
		}
		setupView();
		rxBusSub();
		setupData();
	}

	private void setupView() {
		LinearLayoutManager layoutManager = new WrapperLinearLayoutManager(
				this, LinearLayoutManager.VERTICAL, false);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.addItemDecoration(new DividerItemDecoration(
				this, LinearLayoutManager.VERTICAL, DensityUtil
				.dip2px(this, 12), DensityUtil.dip2px(
				this, 12)));
		mRecyclerView.setNestedScrollingEnabled(false);
	}

	/**
	 * rx订阅
	 */
	private void rxBusSub() {
		observable = RxBus.getInstance().register(AppConstants.CITY_WE_CHAT_RESP_CODE);
		observable.subscribe(o -> getPayResult());
	}

	private void setupData() {
		if (!AppManager.getClientUser().is_vip) {
			mVip7Lay.setVisibility(View.VISIBLE);
		} else {
			mVip7Lay.setVisibility(View.GONE);
		}
		if (!AppManager.getClientUser().isShowGiveVip || AppManager.getClientUser().isShowDownloadVip) {
			mCumQQ.setVisibility(View.VISIBLE);
		} else {
			mCumQQ.setVisibility(View.INVISIBLE);
		}
		getMemberBuy(NORMAL_VIP);
	}

	private void getUserName(int pageNo, int pageSize) {
		ArrayMap<String, String> params = new ArrayMap<>(2);
		params.put("pageNo", String.valueOf(pageNo));
		params.put("pageSize", String.valueOf(pageSize));
		RetrofitFactory.getRetrofit().create(IUserApi.class)
				.getUserName(AppManager.getClientUser().sessionId, params)
				.subscribeOn(Schedulers.io())
				.map(responseBody -> JsonUtils.parseUserName(responseBody.string()))
				.observeOn(AndroidSchedulers.mainThread())
				.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
				.subscribe(strings -> {
					if (strings != null && strings.size() > 0) {
						turnOnVipNameList = new ArrayList<>();
						for (String name : strings) {
							turnOnVipNameList.add(name + " 开通了会员，赶快去和TA聊天吧！");
						}
						mMarqueeView.startWithList(turnOnVipNameList);
						mPreferential.setText(mPref);
					} else {
						setTurnOnVipUserName();
					}
				}, throwable -> setTurnOnVipUserName());
	}

	private void getMemberBuy(int type) {
		RetrofitFactory.getRetrofit().create(IUserBuyApi.class)
				.getBuyList(AppManager.getClientUser().sessionId, type)
				.subscribeOn(Schedulers.io())
				.map(responseBody -> JsonUtils.parseMemberBuy(responseBody.string()))
				.observeOn(AndroidSchedulers.mainThread())
				.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
				.subscribe(memberBuys -> {
					mAdapter = new MemberBuyAdapter(VipCenterActivity.this, memberBuys);
					mAdapter.setOnItemClickListener(mOnItemClickListener);
					mRecyclerView.setAdapter(mAdapter);

					if (null != memberBuys && memberBuys.size() > 0) {
						array = new ArrayList<>(memberBuys.size());
						for (int i = 0; i < memberBuys.size(); i++) {
							if (!TextUtils.isEmpty(memberBuys.get(i).preferential) &&
									memberBuys.get(i).preferential.length() > 10) {
								mPreferential.setVisibility(View.VISIBLE);
								mPref = memberBuys.get(i).preferential;
								continue;
							}
							if (!TextUtils.isEmpty(memberBuys.get(i).preferential)) {
								array.add(Integer.parseInt(memberBuys.get(i).preferential));
							}
						}
						if (array.size() == 0) {
							mPrefTelFareLay.setVisibility(View.VISIBLE);
						} else {
							mPrefTelFareLay.setVisibility(View.GONE);
						}
					}
					getUserName(1, 100);
				}, throwable -> {});
	}

	private void setTurnOnVipUserName() {
		turnOnVipNameList = new ArrayList<>();
		turnOnVipNameList.add("雨天 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("秋叶 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("撕裂时光 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("真爱 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("许愿树 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("木瓜 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("山楂 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("夕阳 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("花依旧开 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("心在这里 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("无花果 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("萌兔 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("残缺布偶 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("潮汐 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("寂寞的心 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("丹樱。。。 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("如影随形 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("葛葛 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("薛金玲 开通了会员，赶快去和TA聊天吧！");
		turnOnVipNameList.add("花物语 开通了会员，赶快去和TA聊天吧！");
		if (null != mMarqueeView) {
			mMarqueeView.startWithList(turnOnVipNameList);
		}
	}

	private MemberBuyAdapter.OnItemClickListener mOnItemClickListener = new MemberBuyAdapter.OnItemClickListener() {
		@Override
		public void onItemClick(View view, int position) {
			MemberBuy memberBuy = mAdapter.getItem(position);
			createWeChatOrder(memberBuy.id, AppConstants.WX_PAY_PLATFORM);
		}
	};

	/**
	 * 获取支付成功之后用户开通了哪项服务
	 */
	private void getPayResult() {
		RetrofitFactory.getRetrofit().create(IUserBuyApi.class)
				.getPayResult(AppManager.getClientUser().sessionId)
				.subscribeOn(Schedulers.io())
				.map(responseBody -> {
					JsonObject obj = new JsonParser().parse(responseBody.string()).getAsJsonObject();
					int code = obj.get("code").getAsInt();
					if (code != 0) {
						return null;
					}
					JsonObject data = obj.get("data").getAsJsonObject();
					Gson gson = new Gson();
					UserVipModel model = gson.fromJson(data, UserVipModel.class);
					return model;
				})
				.observeOn(AndroidSchedulers.mainThread())
				.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
				.subscribe(userVipModel -> {
					if (userVipModel != null) {
						SDKCoreHelper.init(CSApplication.getInstance(), ECInitParams.LoginMode.FORCE_LOGIN);
						AppManager.getClientUser().is_vip = userVipModel.isVip;
						AppManager.getClientUser().is_download_vip = userVipModel.isDownloadVip;
						AppManager.getClientUser().gold_num = userVipModel.goldNum;
						Snackbar.make(findViewById(R.id.vip_layout),
								"您已经是会员了，赶快去聊天吧", Snackbar.LENGTH_SHORT)
								.show();
					}
				}, throwable -> {});
	}

	/*********************************************************************************************************************/

	/**
	 * 调用微信支付
	 */
	private void createWeChatOrder(int memberId, String payPlatform) {
		ArrayMap<String, String> params = new ArrayMap<>(2);
		params.put("memberId", String.valueOf(memberId));
		params.put("payPlatform", payPlatform);
		RetrofitFactory.getRetrofit().create(IUserBuyApi.class)
				.createOrder(AppManager.getClientUser().sessionId, params)
				.subscribeOn(Schedulers.io())
				.map(responseBody -> JsonUtils.parseWeChatPay(responseBody.string()))
				.observeOn(AndroidSchedulers.mainThread())
				.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
				.subscribe(weChatPay -> {
					if (weChatPay != null) {
						PayReq payReq = new PayReq();
						payReq.appId = AppConstants.WEIXIN_PAY_ID;
						payReq.partnerId = weChatPay.mch_id;
						payReq.prepayId = weChatPay.prepay_id;
						payReq.packageValue = "Sign=WXPay";
						payReq.nonceStr = weChatPay.nonce_str;
						payReq.timeStamp = weChatPay.timeStamp;
						payReq.sign = weChatPay.appSign;
						AppManager.getIWX_PAY_API().sendReq(payReq);
					}
				}, throwable -> ToastUtil.showMessage(R.string.network_requests_error));
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
	protected void onDestroy() {
		super.onDestroy();
		RxBus.getInstance().unregister(AppConstants.CITY_WE_CHAT_RESP_CODE, observable);
	}
}
