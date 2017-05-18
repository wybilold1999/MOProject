package com.cyanbirds.momo.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.adapter.SuccessCaseAdapter;
import com.cyanbirds.momo.entity.SuccessCase;
import com.cyanbirds.momo.net.request.GetSuccessCaseListRequest;
import com.cyanbirds.momo.ui.widget.WrapperLinearLayoutManager;
import com.cyanbirds.momo.utils.ToastUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者：wangyb
 * 时间：2016/11/2 15:56
 * 描述：成功案例
 */
public class SuccessCaseActivity extends BaseActivity {

	@BindView(R.id.image)
	SimpleDraweeView mImage;
	@BindView(R.id.recyclerview)
	RecyclerView mRecyclerView;
	@BindView(R.id.scrollView)
	NestedScrollView mScrollView;

	private SuccessCaseAdapter mAdapter;
	private LinearLayoutManager layoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_success_case);
		ButterKnife.bind(this);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
		setupView();
		setupData();
	}

	private void setupView() {
		mImage.setImageURI(Uri.parse("http://cdn.wmlover.cn/style/assets/wap/ID11/banner.jpg"));
		mScrollView.smoothScrollTo(0, 0);

		layoutManager = new WrapperLinearLayoutManager(
				this, LinearLayoutManager.VERTICAL, false);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setNestedScrollingEnabled(false);
	}

	private void setupData() {
		new GetSuccessCaseListTask().request();
	}

	class GetSuccessCaseListTask extends GetSuccessCaseListRequest {
		@Override
		public void onPostExecute(List<SuccessCase> successCases) {
			mAdapter = new SuccessCaseAdapter(successCases, SuccessCaseActivity.this);
			mRecyclerView.setAdapter(mAdapter);
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
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
}
