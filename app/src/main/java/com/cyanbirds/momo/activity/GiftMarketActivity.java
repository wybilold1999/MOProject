package com.cyanbirds.momo.activity;

import android.arch.lifecycle.Lifecycle;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.adapter.GiftMarketAdapter;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.entity.Gift;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.IUserApi;
import com.cyanbirds.momo.net.base.RetrofitFactory;
import com.cyanbirds.momo.utils.JsonUtils;
import com.cyanbirds.momo.utils.ToastUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 作者：wangyb
 * 时间：2016/11/25 19:27
 * 描述：
 */
public class GiftMarketActivity extends BaseActivity implements View.OnClickListener{
	@BindView(R.id.recyclerview)
	RecyclerView mRecyclerview;

	TextView mGiftName;
	SimpleDraweeView mGiftUrl;
	TextView mAmount;
	TextView mVipAmount;
	SimpleDraweeView mMyPortrait;
	SimpleDraweeView mOtherPortrait;
	TextView mSendGift;
	ImageView mVip;
	LinearLayout mVipLay;

	private View mGiftDialogView;
	private AlertDialog mGiftDialog;
	private Gift gift;

	private GridLayoutManager mGridLayoutManager;
	private GiftMarketAdapter mAdapter;
	private ClientUser giftUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gift_market);
		ButterKnife.bind(this);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
		initView();
		getGiftList();
	}

	private void initView() {
		mGridLayoutManager = new GridLayoutManager(this, 3);
		mRecyclerview.setLayoutManager(mGridLayoutManager);

		giftUser = (ClientUser) getIntent().getSerializableExtra(ValueKey.USER);
	}

	private void getGiftList() {
		RetrofitFactory.getRetrofit().create(IUserApi.class)
				.getGift(AppManager.getClientUser().sessionId)
				.subscribeOn(Schedulers.io())
				.map(responseBody -> JsonUtils.parseGiftList(responseBody.string()))
				.observeOn(AndroidSchedulers.mainThread())
				.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
				.subscribe(gifts -> {
					mAdapter = new GiftMarketAdapter(GiftMarketActivity.this, gifts);
					mAdapter.setOnItemClickListener(mOnItemClickListener);
					mRecyclerview.setAdapter(mAdapter);
				}, throwable -> ToastUtil.showMessage(R.string.network_requests_error));
	}

	private GiftMarketAdapter.OnItemClickListener mOnItemClickListener = new GiftMarketAdapter.OnItemClickListener() {
		@Override
		public void onItemClick(View view, int position) {
			gift = mAdapter.getItem(position);
			initSendGiftDialogView();
			AlertDialog.Builder builder = new AlertDialog.Builder(GiftMarketActivity.this);
			builder.setView(mGiftDialogView);
			mGiftDialog = builder.show();

			mGiftName.setText(gift.name);
			mGiftUrl.setImageURI(Uri.parse(gift.dynamic_image_url));
			if (gift.vip_amount == 0) {
				mVipAmount.setText("免费");
			} else {
				mVipAmount.setText(gift.vip_amount + "金币");
			}
			mAmount.setText(String.format(getResources().getString(R.string.org_price), gift.amount));
			if (!TextUtils.isEmpty(AppManager.getClientUser().face_url)) {
				mMyPortrait.setImageURI(Uri.parse(AppManager.getClientUser().face_url));
			}
			if (giftUser != null) {
				mOtherPortrait.setImageURI(Uri.parse(giftUser.face_url));
			}
		}
	};

	private void initSendGiftDialogView() {
		mGiftDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_send_gift, null);
		mGiftName = (TextView) mGiftDialogView.findViewById(R.id.gift_name);
		mGiftUrl = (SimpleDraweeView) mGiftDialogView.findViewById(R.id.gift_url);
		mAmount = (TextView) mGiftDialogView.findViewById(R.id.amount);
		mVip = (ImageView) mGiftDialogView.findViewById(R.id.iv_vip);
		mVipLay = (LinearLayout)  mGiftDialogView.findViewById(R.id.vip_lay);
		mVipAmount = (TextView) mGiftDialogView.findViewById(R.id.vip_amount);
		mMyPortrait = (SimpleDraweeView) mGiftDialogView.findViewById(R.id.my_portrait);
		mOtherPortrait = (SimpleDraweeView) mGiftDialogView.findViewById(R.id.other_portrait);
		mSendGift = (TextView) mGiftDialogView.findViewById(R.id.send_gift);
		mSendGift.setOnClickListener(this);
		if (AppManager.getClientUser().isShowGold) {
			mVipLay.setVisibility(View.VISIBLE);
			mAmount.setVisibility(View.VISIBLE);
		} else {
			mVipLay.setVisibility(View.GONE);
			mAmount.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		mGiftDialog.dismiss();
		Snackbar.make(findViewById(R.id.recyclerview),
				getResources().getString(R.string.send_gift_success),
				Snackbar.LENGTH_LONG).show();
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
}
