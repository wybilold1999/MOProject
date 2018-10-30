package com.cyanbirds.momo.activity;

import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyanbirds.momo.CSApplication;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.adapter.MemberBuyAdapter;
import com.cyanbirds.momo.config.AppConstants;
import com.cyanbirds.momo.entity.MemberBuy;
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
import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.pay.PaySignUtil;
import com.huawei.hms.support.api.entity.pay.PayReq;
import com.huawei.hms.support.api.entity.pay.PayStatusCodes;
import com.sunfusheng.marqueeview.MarqueeView;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import com.umeng.analytics.MobclickAgent;
import com.yuntongxun.ecsdk.ECInitParams;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class VipHWCenterActivity extends BaseActivity {

	@BindView(R.id.toolbar)
	Toolbar mToolbar;
	@BindView(R.id.marqueeView)
	MarqueeView mMarqueeView;
	@BindView(R.id.recyclerview)
	RecyclerView mRecyclerView;
	@BindView(R.id.scrollView)
	NestedScrollView mScrollView;
	@BindView(R.id.cum_qq)
	TextView mCumQQ;
	@BindView(R.id.speaker_lay)
	RelativeLayout mSpeakerLay;

	private MemberBuyAdapter mAdapter;

	/**
	 * 没有网络时显示开通会员的名单
	 */
	private List<String> turnOnVipNameList;

	/**
	 * 普通会员商品
	 */
	private final int NORMAL_VIP = 0;

	private Observable<?> observable;

	private String hwPayPrivateKey = "";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hw_vipcenter);
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
		getHWPayKey();
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
		if (AppManager.getClientUser().isShowVip) {
			mSpeakerLay.setVisibility(View.VISIBLE);
		} else {
			mSpeakerLay.setVisibility(View.GONE);
		}
		if (AppManager.getClientUser().isShowGiveVip || AppManager.getClientUser().isShowDownloadVip) {
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
					mAdapter = new MemberBuyAdapter(VipHWCenterActivity.this, memberBuys);
					mAdapter.setOnItemClickListener(mOnItemClickListener);
					mRecyclerView.setAdapter(mAdapter);
					getUserName(1, 100);
				}, throwable -> {});
	}

	/**
	 * 请求华为支付所需要的key
	 */
	private void getHWPayKey() {
		RetrofitFactory.getRetrofit().create(IUserBuyApi.class)
				.getHWPayPrivateKey(AppManager.getClientUser().sessionId)
				.subscribeOn(Schedulers.io())
				.map(responseBody -> AESOperator.getInstance().decrypt(responseBody.string()))
				.observeOn(AndroidSchedulers.mainThread())
				.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
				.subscribe(payKey -> {
					if (!TextUtils.isEmpty(payKey)) {
						hwPayPrivateKey = payKey;
					}
				}, throwable -> {});
	}


	private MemberBuyAdapter.OnItemClickListener mOnItemClickListener = new MemberBuyAdapter.OnItemClickListener() {
		@Override
		public void onItemClick(View view, int position) {
			MemberBuy memberBuy = mAdapter.getItem(position);
			requesetPay(memberBuy);
		}
	};

	private void requesetPay(MemberBuy memberBuy) {
		PayReq payReq = createPayReq(memberBuy);
		HMSAgent.Pay.pay(payReq, (retCode, payInfo) -> {
			if (retCode == HMSAgent.AgentResultCode.HMSAGENT_SUCCESS && payInfo != null) {
				modifyUserToVIP();
			} else if (retCode == HMSAgent.AgentResultCode.ON_ACTIVITY_RESULT_ERROR
					|| retCode == PayStatusCodes.PAY_STATE_TIME_OUT
					|| retCode == PayStatusCodes.PAY_STATE_NET_ERROR) {
				// 需要查询订单状态：对于没有服务器的单机应用，调用查询订单接口查询；其他应用到开发者服务器查询订单状态。
				getPayResult();
			} else {
				ToastUtil.showMessage(R.string.pay_failure);
			}
		});
	}

	/**
	 * 创建普通支付请求对象 | Create an ordinary Payment request object
	 * @return 普通支付请求对象 | Ordinary Payment Request Object
	 */
	private PayReq createPayReq(MemberBuy memberBuy) {
		PayReq payReq = new PayReq();

		/**
		 * 生成requestId | Generate RequestID
		 */
		DateFormat format = new java.text.SimpleDateFormat("yyyyMMddhhmmssSSS");
		int random= new SecureRandom().nextInt() % 100000;
		random = random < 0 ? -random : random;
		String requestId = format.format(new Date());
		requestId = String.format("%s%05d", requestId, random);

		/**
		 * 生成总金额 | Generate Total Amount
		 */
		String amount = String.format("%.2f", memberBuy.price);

		//商品名称 | Product Name
		payReq.productName = memberBuy.months + "会员";
		//商品描述 | Product Description
		payReq.productDesc = memberBuy.descreption;
		// 商户ID，来源于开发者联盟，也叫“支付id” | Merchant ID, from the Developer Alliance, also known as "Payment ID"
		payReq.merchantId = AppConstants.HW_MERCHANT_ID;
		// 应用ID，来源于开发者联盟 | Application ID, from the Developer Alliance
		payReq.applicationID = AppConstants.HW_APP_ID;
		// 支付金额 | Amount paid
		payReq.amount = amount;
		// 支付订单号 | Payment order Number
		payReq.requestId = requestId;
		// 国家码 | Country code
		payReq.country = "CN";
		//币种 | Currency
		payReq.currency = "CNY";
		// 渠道号 | Channel number
		payReq.sdkChannel = 1;
		// 回调接口版本号 | Callback Interface Version number
		payReq.urlVer = "2";

		// 商户名称，必填，不参与签名。会显示在支付结果页面 | Merchant name, must be filled out, do not participate in the signature. will appear on the Pay results page
		payReq.merchantName = "青鸟网络";
		//分类，必填，不参与签名。该字段会影响风控策略 | Categories, required, do not participate in the signature. This field affects wind control policy
		// X4：主题,X5：应用商店,	X6：游戏,X7：天际通,X8：云空间,X9：电子书,X10：华为学习,X11：音乐,X12 视频, | X4: Theme, X5: App Store, X6: Games, X7: Sky Pass, X8: Cloud Space, X9: ebook, X10: Huawei Learning, X11: Music, X12 video,
		// X31 话费充值,X32 机票/酒店,X33 电影票,X34 团购,X35 手机预购,X36 公共缴费,X39 流量充值 | X31, X32 air tickets/hotels, X33 movie tickets, X34 Group purchase, X35 mobile phone advance, X36 public fees, X39 flow Recharge
		payReq.serviceCatalog = "X6";
		//商户保留信息，选填不参与签名，支付成功后会华为支付平台会原样 回调CP服务端 | The merchant retains the information, chooses not to participate in the signature, the payment will be successful, the Huawei payment platform will be back to the CP service
		payReq.extReserved = "Here to fill in the Merchant reservation information";

		//对单机应用可以直接调用此方法对请求信息签名，非单机应用一定要在服务器端储存签名私钥，并在服务器端进行签名操作。| For stand-alone applications, this method can be called directly to the request information signature, not stand-alone application must store the signature private key on the server side, and sign operation on the server side.
		// 在服务端进行签名的cp可以将getStringForSign返回的待签名字符串传给服务端进行签名 | The CP, signed on the server side, can pass the pending signature string returned by Getstringforsign to the service side for signature
		payReq.sign = PaySignUtil.rsaSign(PaySignUtil.getStringForSign(payReq), hwPayPrivateKey);

		return payReq;
	}


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
					if (userVipModel != null && userVipModel.isVip) {
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

	/**
	 * 修改user为vip
	 */
	private void modifyUserToVIP() {
		RetrofitFactory.getRetrofit().create(IUserApi.class)
				.modifyUserVip(AppManager.getClientUser().sessionId)
				.subscribeOn(Schedulers.io())
				.map(responseBody -> {
					JsonObject obj = new JsonParser().parse(responseBody.string()).getAsJsonObject();
					int code = obj.get("code").getAsInt();
					return code;
				})
				.observeOn(AndroidSchedulers.mainThread())
				.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
				.subscribe(status -> {
					if (status == 0) {//成功
						SDKCoreHelper.init(CSApplication.getInstance(), ECInitParams.LoginMode.FORCE_LOGIN);
						AppManager.getClientUser().is_vip = true;
						Snackbar.make(findViewById(R.id.vip_layout),
								"您已经是会员了，赶快去聊天吧", Snackbar.LENGTH_SHORT)
								.show();
					}
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
