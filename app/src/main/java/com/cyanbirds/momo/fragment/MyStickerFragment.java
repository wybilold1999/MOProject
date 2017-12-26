package com.cyanbirds.momo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.adapter.DownloadExpressionAdapter;
import com.cyanbirds.momo.db.ExpressionGroupSqlManager;
import com.cyanbirds.momo.entity.ExpressionGroup;
import com.cyanbirds.momo.eventtype.StickerEvent;
import com.cyanbirds.momo.ui.widget.CircularProgress;
import com.cyanbirds.momo.ui.widget.WrapperLinearLayoutManager;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author: wangyb
 * @datetime: 2015-12-20 11:33 GMT+8
 * @email: 395044952@qq.com
 * @description:
 */
public class MyStickerFragment extends Fragment {

	@BindView(R.id.recyclerview)
	RecyclerView mRecyclerView;
	@BindView(R.id.progress_bar)
	CircularProgress mProgressBar;

	private View rootView;
	private DownloadExpressionAdapter mAdapter;
	private List<ExpressionGroup> mExpressionGroups;

	private boolean isMySticker = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_sticker_market, null);
			ButterKnife.bind(this, rootView);
			EventBus.getDefault().register(this);
			setupViews();
			setupEvent();
			setupData();
			setHasOptionsMenu(true);
		}
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		return rootView;
	}

	private void setupViews() {
		LinearLayoutManager manager = new WrapperLinearLayoutManager(getActivity());
		manager.setOrientation(LinearLayout.VERTICAL);
		mRecyclerView.setLayoutManager(manager);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
	}

	private void setupEvent() {
	}

	private void setupData() {
		mExpressionGroups = new ArrayList<>();
		List<ExpressionGroup> list = ExpressionGroupSqlManager.getInstance(getActivity()).getExpressionGroup();
		if (list != null) {
			mExpressionGroups.addAll(list);
			mAdapter = new DownloadExpressionAdapter(mExpressionGroups, isMySticker);
			mRecyclerView.setAdapter(mAdapter);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void updateList(StickerEvent event) {
		updateMyExpressionGroups();
	}

	private void updateMyExpressionGroups() {
		List<ExpressionGroup> list = ExpressionGroupSqlManager.getInstance(getActivity()).getExpressionGroup();
		if (list != null) {
			mExpressionGroups.clear();
			mExpressionGroups.addAll(list);
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			} else {
				mAdapter = new DownloadExpressionAdapter(mExpressionGroups, isMySticker);
				mRecyclerView.setAdapter(mAdapter);
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
		EventBus.getDefault().unregister(this);
	}
}
