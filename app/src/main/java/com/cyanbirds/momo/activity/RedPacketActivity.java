package com.cyanbirds.momo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.cyanbirds.momo.CSApplication;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.config.AppConstants;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.MemberBuy;
import com.cyanbirds.momo.entity.PayResult;
import com.cyanbirds.momo.entity.WeChatPay;
import com.cyanbirds.momo.eventtype.PayEvent;
import com.cyanbirds.momo.net.request.GetMemberBuyListRequest;
import com.cyanbirds.momo.net.request.RPAliPayOrderInfoRequest;
import com.cyanbirds.momo.net.request.RPCreateOrderRequest;
import com.cyanbirds.momo.utils.ToastUtil;
import com.jaeger.library.StatusBarUtil;
import com.tencent.mm.sdk.modelpay.PayReq;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * 作者：wangyb
 * 时间：2017/6/17 11:17
 * 描述：红包
 */
public class RedPacketActivity extends BaseActivity {

	@BindView(R.id.read_packet_amount)
	EditText mReadPacketAmount;
	@BindView(R.id.blessings)
	EditText mBlessings;
	@BindView(R.id.money)
	TextView mMoney;
	@BindView(R.id.single_more)
	TextView mSingleMore;
	@BindView(R.id.limit)
	TextView mMoneyLimit;
	@BindView(R.id.select_alipay)
	CheckBox mSelectAlipay;
	@BindView(R.id.alipay_lay)
	RelativeLayout mAlipayLay;
	@BindView(R.id.select_wechatpay)
	CheckBox mSelectWechatpay;
	@BindView(R.id.wechat_lay)
	RelativeLayout mWechatLay;
	@BindView(R.id.pay_lay)
	LinearLayout mPayLay;
	private FancyButton mBtnSendMoney;

	private static final int SDK_PAY_FLAG = 1;
	private int MEMBER_BUY_TYPE_RED_PACKET = 5;//红包
	private String mPayType;//支付方式
	private MemberBuy mMemberBuy;//选中的商品
	private DecimalFormat mFormat;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@SuppressWarnings("unused")
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SDK_PAY_FLAG: {
					@SuppressWarnings("unchecked")
					PayResult payResult = new PayResult((Map<String, String>) msg.obj);
					/**
					 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
					 */
					String resultInfo = payResult.getResult();// 同步返回需要验证的信息
					String resultStatus = payResult.getResultStatus();
					// 判断resultStatus 为9000则代表支付成功
					if (TextUtils.equals(resultStatus, "9000")) {
						// 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
						finishActivity();
					} else {
						// 该笔订单真实的支付结果，需要依赖服务端的异步通知。
						ToastUtil.showMessage("支付失败");
					}
					break;
				}
				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_red_packet);
		ButterKnife.bind(this);
		StatusBarUtil.setTransparent(this);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
		setupView();
		setupEvent();
		setupData();
	}

	private void setupView() {
		mBtnSendMoney = (FancyButton) findViewById(R.id.btn_send_money);
	}

	private void setupEvent() {
		mReadPacketAmount.setFocusable(true);
		mReadPacketAmount.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!TextUtils.isEmpty(s.toString())) {
					mReadPacketAmount.setSelection(s.toString().length());
					int index = s.toString().indexOf(".");
					if (index != -1) {
						if (s.toString().length() - index > 3) {
							String money = s.toString().substring(0, index + 3);
							mReadPacketAmount.setText(money);
							mReadPacketAmount.setSelection(money.length());
							s = mReadPacketAmount.getText();
						}
					}
					if (Double.parseDouble(s.toString()) > Double.parseDouble(mMemberBuy.months)) {
						mMoneyLimit.setVisibility(View.VISIBLE);
						mBtnSendMoney.setClickable(false);
						mBtnSendMoney.setEnabled(false);
						mBtnSendMoney.setTextColor(getResources().getColor(R.color.btn_send_money_text));
						mBtnSendMoney.setBackgroundColor(getResources().getColor(R.color.btn_send_money_unenable));
						mBtnSendMoney.setOnClickListener(null);
					} else if (Double.parseDouble(s.toString()) > mMemberBuy.price) {
						mMoneyLimit.setVisibility(View.INVISIBLE);
						mBtnSendMoney.setEnabled(true);
						mBtnSendMoney.setClickable(true);
						mBtnSendMoney.setTextColor(getResources().getColor(R.color.item_find_love_bg));
						mBtnSendMoney.setBackgroundColor(getResources().getColor(R.color.btn_send_money_normal));
						mBtnSendMoney.setFocusBackgroundColor(getResources().getColor(R.color.btn_send_money_press));
						mBtnSendMoney.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								/*if (mMemberBuy != null) {
									if (mPayType.equals(AppConstants.ALI_PAY_PLATFORM)) {
										new GetAliPayOrderInfoTask().request(mMemberBuy.id, AppConstants.ALI_PAY_PLATFORM, mReadPacketAmount.getText().toString());
									} else {
										new CreateOrderTask().request(mMemberBuy.id, AppConstants.WX_PAY_PLATFORM, mReadPacketAmount.getText().toString());
									}
								}*/
								finishActivity();
							}
						});
					} else {
						mMoneyLimit.setVisibility(View.INVISIBLE);
						mBtnSendMoney.setClickable(false);
						mBtnSendMoney.setEnabled(false);
						mBtnSendMoney.setTextColor(getResources().getColor(R.color.btn_send_money_text));
						mBtnSendMoney.setBackgroundColor(getResources().getColor(R.color.btn_send_money_unenable));
						mBtnSendMoney.setOnClickListener(null);
					}
					String finalMoney = mFormat.format(Double.parseDouble(s.toString()));
					mMoney.setText(String.format(getResources().getString(R.string.money), finalMoney));
				} else {
					mMoney.setText("￥0.00");
				}
			}
		});
	}

	private void setupData() {
		mFormat = new DecimalFormat("#.00");
		/**
		 * 默认支付宝支付
		 */
		mPayType = AppConstants.ALI_PAY_PLATFORM;
		mSelectAlipay.setChecked(true);
		mSelectWechatpay.setChecked(false);
		new GetGoldListTask().request(MEMBER_BUY_TYPE_RED_PACKET);
	}

	/**
	 * 请求金币商品列表
	 */
	class GetGoldListTask extends GetMemberBuyListRequest {
		@Override
		public void onPostExecute(List<MemberBuy> memberBuys) {
			mMemberBuy = memberBuys.get(0);
			mMoneyLimit.setText(String.format(getResources().getString(
					R.string.single_red_packet_limit), mMemberBuy.months));
			mSingleMore.setText(String.format(getResources().getString(
					R.string.single_red_packet_more), String.valueOf(mMemberBuy.price)));
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
		}
	}


	@OnClick({R.id.select_alipay, R.id.alipay_lay, R.id.select_wechatpay, R.id.wechat_lay})
	public void onViewClicked(View view) {
		switch (view.getId()) {
			case R.id.select_alipay:
				mPayType = AppConstants.ALI_PAY_PLATFORM;
				mSelectAlipay.setChecked(true);
				mSelectWechatpay.setChecked(false);
				break;
			case R.id.alipay_lay:
				mPayType = AppConstants.ALI_PAY_PLATFORM;
				mSelectAlipay.setChecked(true);
				mSelectWechatpay.setChecked(false);
				break;
			case R.id.select_wechatpay:
				mPayType = AppConstants.WX_PAY_PLATFORM;
				mSelectAlipay.setChecked(false);
				mSelectWechatpay.setChecked(true);
				break;
			case R.id.wechat_lay:
				mPayType = AppConstants.WX_PAY_PLATFORM;
				mSelectAlipay.setChecked(false);
				mSelectWechatpay.setChecked(true);
				break;
		}
	}

	class CreateOrderTask extends RPCreateOrderRequest {
		@Override
		public void onPostExecute(WeChatPay weChatPay) {
			PayReq payReq = new PayReq();
			payReq.appId = AppConstants.WEIXIN_ID;
			payReq.partnerId = weChatPay.mch_id;
			payReq.prepayId = weChatPay.prepay_id;
			payReq.packageValue = "Sign=WXPay";
			payReq.nonceStr = weChatPay.nonce_str;
			payReq.timeStamp = weChatPay.timeStamp;
			payReq.sign = weChatPay.appSign;
			CSApplication.api.sendReq(payReq);
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
		}
	}

	/**
	 * 调用支付宝支付
	 */
	class GetAliPayOrderInfoTask extends RPAliPayOrderInfoRequest {
		@Override
		public void onPostExecute(String s) {
			payV2(s);
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
		}
	}

	/**
	 * 支付宝支付业务
	 *
	 * @param orderInfo
	 */
	public void payV2(final String orderInfo) {
		/**
		 * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
		 * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
		 * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
		 * orderInfo的获取必须来自服务端；
		 */
		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				PayTask alipay = new PayTask(RedPacketActivity.this);
				Map<String, String> result = alipay.payV2(orderInfo, true);

				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void paySuccess(PayEvent event) {
		finishActivity();
	}

	private void finishActivity() {
		Intent intent = new Intent();
		if (!TextUtils.isEmpty(mBlessings.getText().toString())) {
			intent.putExtra(ValueKey.DATA, mBlessings.getText().toString());
		} else {
			intent.putExtra(ValueKey.DATA, getResources().getString(R.string.feedback_info));
		}
		setResult(RESULT_OK, intent);
		finish();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 0 && requestCode == SDK_PAY_FLAG) {
		}
	}
}
