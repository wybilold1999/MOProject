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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.adapter.DownloadExpressionAdapter;
import com.cyanbirds.momo.db.ExpressionGroupSqlManager;
import com.cyanbirds.momo.entity.ExpressionGroup;
import com.cyanbirds.momo.listener.DownloadProgressExpressionListener;
import com.cyanbirds.momo.listener.DownloadProgressExpressionListener.OnExpressionProgressChangedListener;
import com.cyanbirds.momo.listener.UserStickerPackListener;
import com.cyanbirds.momo.listener.UserStickerPackListener.OnUserStickerPackListener;
import com.cyanbirds.momo.net.request.ExpressionRequest;
import com.cyanbirds.momo.ui.widget.CircularProgress;
import com.cyanbirds.momo.ui.widget.WrapperLinearLayoutManager;
import com.cyanbirds.momo.utils.ProgressDialogUtils;
import com.cyanbirds.momo.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * @author: wangyb
 * @datetime: 2015-12-20 11:33 GMT+8
 * @email: 395044952@qq.com
 * @description:
 */
public class StickerMarketFragment extends Fragment implements OnExpressionProgressChangedListener,
		OnUserStickerPackListener {
	@BindView(R.id.recyclerview)
	RecyclerView mRecyclerView;
	@BindView(R.id.progress_bar)
	CircularProgress mProgressBar;

	private View rootView;

	private DownloadExpressionAdapter mAdapter;

	private List<ExpressionGroup> mExpressionGroups;

	/** 已有表情id */
	private List<ExpressionGroup> mPresenceExpression;

	private boolean isMySticker = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_sticker_market, null);
			ButterKnife.bind(this, rootView);
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
		DownloadProgressExpressionListener.getInstance()
				.addOnExpressionProgressChangedListener(this);
		UserStickerPackListener.getInstance()
				.addOnUserStickerPackListener(this);
	}

	private void setupData() {
		mExpressionGroups = new ArrayList<ExpressionGroup>();
		mPresenceExpression = new ArrayList<ExpressionGroup>();
		List<ExpressionGroup> list = ExpressionGroupSqlManager.getInstance(getActivity()).getExpressionGroup();
		if (list != null) {
			mPresenceExpression = list;
		}

		mAdapter = new DownloadExpressionAdapter(mExpressionGroups, isMySticker);
		mRecyclerView.setAdapter(mAdapter);
		ProgressDialogUtils.getInstance(getActivity()).show(R.string.dialog_request_data);
		new ExpressionTask().request();
	}

	/**
	 * 在线表情数据请求
	 */
	class ExpressionTask extends ExpressionRequest {
		@Override
		public void onPostExecute(List<ExpressionGroup> result) {
			ProgressDialogUtils.getInstance(getActivity()).dismiss();
			if (result != null) {
				mExpressionGroups.clear();
				mExpressionGroups.addAll(result);
				// 遍历处理已经下载过的表情
				for (int i = 0; i < mExpressionGroups.size(); i++) {
					for (int j = 0; j < mPresenceExpression.size(); j++) {
						if (mExpressionGroups.get(i).id_pic_themes == mPresenceExpression
								.get(j).id_pic_themes) {
							mExpressionGroups.get(i).status = ExpressionGroup.ExpressionGroupStatus.ALREADY_DOWNLOAD;
						}
					}
				}
				mAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
			ProgressDialogUtils.getInstance(getActivity()).dismiss();
		}
	}

	@Override
	public void onDownloadExpressionProgressChanged(
			ExpressionGroup expressionGroup, int progress,
			boolean is_changed_down_view) {
		if (!is_changed_down_view) {
			return;
		}
		int chatMessageIndex = -1;
		if (mExpressionGroups != null) {
			for (int i = 0; i < mExpressionGroups.size(); i++) {
				if (mExpressionGroups.get(i).id_pic_themes == expressionGroup.id_pic_themes) {
					expressionGroup.progress = progress;
					if (progress >= 100) {
						expressionGroup.status = ExpressionGroup.ExpressionGroupStatus.ALREADY_DOWNLOAD;
						mPresenceExpression.add(expressionGroup);
					}
					mExpressionGroups.set(i, expressionGroup);
					chatMessageIndex = i;
					break;
				}
			}
		}
		LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView
				.getLayoutManager();
		int firstVisibleItemIndex = layoutManager
				.findFirstVisibleItemPosition();
		int lastVisibleItemIndex = layoutManager.findLastVisibleItemPosition();
		if (chatMessageIndex >= 0 && chatMessageIndex >= firstVisibleItemIndex
				&& chatMessageIndex <= lastVisibleItemIndex) {
			int offset = chatMessageIndex - firstVisibleItemIndex;
			View view = mRecyclerView.getChildAt(offset);
			if (null == view) {
				return;
			}
			ProgressBar pb = (ProgressBar) view
					.findViewById(R.id.download_progress);
			TextView already_download = (TextView) view
					.findViewById(R.id.already_download);
			FancyButton download_expression = (FancyButton) view
					.findViewById(R.id.download_expression);

			if (pb != null && already_download != null
					&& download_expression != null) {
				pb.setVisibility(View.GONE);
				already_download.setVisibility(View.GONE);
				download_expression.setVisibility(View.GONE);
				if (progress < 100) {
					pb.setVisibility(View.VISIBLE);
					pb.setProgress(progress);
				} else {
					already_download.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	@Override
	public void onDeleteStickerPack(int packId) {

		for (int i = 0; i < mPresenceExpression.size(); i++) {
			if (mPresenceExpression.get(i).id_pic_themes == packId) {
				mPresenceExpression.remove(i);
				break;
			}
		}

		// 遍历处理已经下载过的表情
		for (int i = 0; i < mExpressionGroups.size(); i++) {
			ExpressionGroup eg = mExpressionGroups.get(i);
			if (eg.id_pic_themes == packId) {
				eg.status = ExpressionGroup.ExpressionGroupStatus.NO_DOWNLOAD;
				mExpressionGroups.set(i, eg);
				mAdapter.notifyItemChanged(i);
				break;
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
		DownloadProgressExpressionListener.getInstance()
				.removeOnExpressionProgressChangedListener(this);
		UserStickerPackListener.getInstance().removeOnUserStickerPackListener(
				this);
	}
}
