package com.cyanbirds.momo.adapter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cyanbirds.momo.CSApplication;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.db.ExpressionGroupSqlManager;
import com.cyanbirds.momo.entity.ExpressionGroup;
import com.cyanbirds.momo.eventtype.StickerEvent;
import com.cyanbirds.momo.listener.DownloadProgressExpressionListener;
import com.cyanbirds.momo.net.request.DownloadExpressionRequest;
import com.cyanbirds.momo.utils.FileAccessorUtils;
import com.cyanbirds.momo.utils.FileUtils;
import com.cyanbirds.momo.utils.ToastUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * 
 * @ClassName:DownloadExpressionAdapter.java
 * @Description:下载表情Adapter
 * @author zxj
 * @Date:2015年6月12日上午11:28:32
 */
public class DownloadExpressionAdapter extends
		RecyclerView.Adapter<DownloadExpressionAdapter.ViewHolder> {

	private List<ExpressionGroup> mExpressionGroups;

	/** 正在下载的数量 */
	private int mInDownloadCount;
	/** 最大同时下载数量 */
	private int mMaxDownloadCount = 3;
	/** 等待下载的表情信息 */
	private List<ExpressionGroup> mWaitDownload;

	private boolean mIsMySticker;//是否是‘我的贴图’tab

	public DownloadExpressionAdapter(List<ExpressionGroup> expressionGroups, boolean isMySticker) {
		this.mExpressionGroups = expressionGroups;
		mIsMySticker = isMySticker;
		mWaitDownload = new ArrayList<ExpressionGroup>();
	}

	@Override
	public int getItemCount() {
		return mExpressionGroups == null ? 0 : mExpressionGroups.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		ExpressionGroup group = mExpressionGroups.get(position);
		holder.expression_title.setText(group.name);
		DraweeController controller = Fresco.newDraweeControllerBuilder()
				.setUri(Uri.parse(group.cover))
				.setAutoPlayAnimations(true)
				.build();
		holder.expression_icon.setController(controller);
		holder.download_expression.setVisibility(View.GONE);
		holder.already_download.setVisibility(View.GONE);
		holder.download_progress.setVisibility(View.GONE);

		if (group.status == ExpressionGroup.ExpressionGroupStatus.ALREADY_DOWNLOAD) {
			holder.already_download.setVisibility(View.VISIBLE);
		} else if (group.status == ExpressionGroup.ExpressionGroupStatus.DOWNLOAD) {
			holder.download_progress.setVisibility(View.VISIBLE);
			holder.download_progress.setProgress(group.progress);
		} else if (group.status == ExpressionGroup.ExpressionGroupStatus.NO_DOWNLOAD) {
			holder.download_expression.setVisibility(View.VISIBLE);
		}

		if (mIsMySticker) {
			holder.download_expression.setVisibility(View.GONE);
			holder.already_download.setVisibility(View.GONE);
			holder.download_progress.setVisibility(View.GONE);
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.item_download_expression, parent, false);
		return new ViewHolder(view);
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements
			OnClickListener {

		SimpleDraweeView expression_icon;
		TextView expression_title;
		FancyButton download_expression;
		TextView already_download;
		ProgressBar download_progress;

		public ViewHolder(View itemView) {
			super(itemView);
			expression_icon = (SimpleDraweeView) itemView
					.findViewById(R.id.expression_icon);
			expression_title = (TextView) itemView
					.findViewById(R.id.expression_title);
			download_expression = (FancyButton) itemView
					.findViewById(R.id.download_expression);
			already_download = (TextView) itemView
					.findViewById(R.id.already_download);
			download_expression.setOnClickListener(this);
			download_progress = (ProgressBar) itemView
					.findViewById(R.id.download_progress);
		}

		@Override
		public void onClick(View v) {
			int position = getAdapterPosition();
			ExpressionGroup expression = mExpressionGroups.get(position);
			String savePath = FileAccessorUtils.getExpressionPathName()
					.getAbsolutePath();
			download_progress.setProgress(0);
			expression.status = ExpressionGroup.ExpressionGroupStatus.DOWNLOAD;
			download_progress.setVisibility(View.VISIBLE);
			already_download.setVisibility(View.GONE);
			download_expression.setVisibility(View.GONE);

			if (mInDownloadCount < mMaxDownloadCount) {
				new DownloadExpressionTask(expression).request(
						itemView.getContext(), expression.zip, savePath,
						String.valueOf(expression.id_pic_themes) + ".zip",
						expression);
			} else {
				mWaitDownload.add(expression);
			}
		}

		/**
		 * 下载表情
		 */
		class DownloadExpressionTask extends DownloadExpressionRequest {

			public ExpressionGroup expression;

			public DownloadExpressionTask(ExpressionGroup expression) {
				this.expression = expression;
				mInDownloadCount++;
			}

			@Override
			public void onPostExecute(String result) {
				try {
					expression.status = ExpressionGroup.ExpressionGroupStatus.ALREADY_DOWNLOAD;
					ExpressionGroupSqlManager.getInstance(CSApplication.getInstance()).insertExpressionGroup(expression);
//					ExpressionSqlManager.getInstance(CSApplication.getInstance()).insertExpressions(expression.expressions);
					String filePath = FileAccessorUtils.getExpressionPathName().getAbsolutePath();
					File zipFile = new File(filePath + "/" + expression.id_pic_themes + ".zip");
					if (zipFile.exists()) {
						zipFile.delete();
					}
					// 需添加到子线程解压解决卡顿
//					new Thread(new UnzipThread(new File(filePath + "/"
//							+ expression.id_pic_themes + ".zip"), filePath)).start();

					DownloadProgressExpressionListener.getInstance()
							.notifyExpressionProgressChanged(expression, 100,
									true);
					EventBus.getDefault().post(new StickerEvent());
				} catch (Exception e) {
					e.printStackTrace();
				}
				mInDownloadCount--;
				downloadWaitSticker();
			}

			@Override
			public void onErrorExecute(String error) {
				ToastUtil.showMessage(itemView.getContext().getResources()
						.getString(R.string.download_failure));
				mInDownloadCount--;
				downloadWaitSticker();
			}
		}

		/**
		 * 下载贴图
		 */
		public synchronized void downloadWaitSticker() {
			try {
				if (mWaitDownload != null && mWaitDownload.size() > 0) {
					String savePath = FileAccessorUtils.getExpressionPathName()
							.getAbsolutePath();
					ExpressionGroup eg = mWaitDownload.get(0);
					new DownloadExpressionTask(eg).request(
							itemView.getContext(),
							eg.zip, savePath,
							String.valueOf(eg.id_pic_themes) + ".zip", eg);
					mWaitDownload.remove(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class UnzipThread implements Runnable {

		File zipFile;
		String folderPath;

		public UnzipThread(File zipFile, String folderPath) {
			this.zipFile = zipFile;
			this.folderPath = folderPath;
		}

		@Override
		public void run() {
			try {
				FileUtils.unzipFile(zipFile, folderPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
