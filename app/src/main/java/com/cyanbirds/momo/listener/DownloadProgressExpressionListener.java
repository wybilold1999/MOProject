package com.cyanbirds.momo.listener;


import com.cyanbirds.momo.entity.ExpressionGroup;

import java.util.ArrayList;

/**
 * 
 * @Description:下载表情监听进度
 * @author zxj
 * @Date:2015年7月22日下午5:43:07
 */
public class DownloadProgressExpressionListener {

	private static DownloadProgressExpressionListener mNoticeCenter;
	private ArrayList<OnExpressionProgressChangedListener> mOnExpressionProgressChangedListener;

	private DownloadProgressExpressionListener() {
	}

	public interface OnExpressionProgressChangedListener {
		public void onDownloadExpressionProgressChanged(
				ExpressionGroup expressionGroup, int progress,
				boolean is_changed_down_view);
	}

	public static DownloadProgressExpressionListener getInstance() {
		if (null == mNoticeCenter) {
			mNoticeCenter = new DownloadProgressExpressionListener();
			mNoticeCenter.init();
		}
		return mNoticeCenter;
	}

	private void init() {
		mOnExpressionProgressChangedListener = new ArrayList<OnExpressionProgressChangedListener>();
	}

	public void addOnExpressionProgressChangedListener(
			OnExpressionProgressChangedListener listener) {
		mOnExpressionProgressChangedListener.add(listener);
	}

	/*
	 * public void removeOnFileProgressChangedListener(
	 * OnFileProgressChangedListener listener) {
	 * mOnExpressionProgressChangedListener.remove(listener); }
	 */

	public void removeOnExpressionProgressChangedListener(
			OnExpressionProgressChangedListener listener) {
		mOnExpressionProgressChangedListener.remove(listener);
	}

	public void notifyExpressionProgressChanged(
			ExpressionGroup expressionGroup, int progress,
			boolean is_changed_down_view) {
		for (OnExpressionProgressChangedListener listener : mOnExpressionProgressChangedListener) {
			if (listener != null) {
				listener.onDownloadExpressionProgressChanged(expressionGroup,
						progress, is_changed_down_view);
			}
		}
	}
}
