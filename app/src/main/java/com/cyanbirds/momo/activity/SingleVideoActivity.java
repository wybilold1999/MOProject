package com.cyanbirds.momo.activity;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.VideoModel;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.request.UpdateGoldRequest;
import com.cyanbirds.momo.utils.ToastUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/6/19.
 */
public class SingleVideoActivity extends BaseActivity {
	@BindView(R.id.img_queue)
	SimpleDraweeView imgQueue;
	@BindView(R.id.rose_num)
	TextView roseNum;
	@BindView(R.id.video_info)
	TextView mVideoInfo;
	@BindView(R.id.tv_video_show)
	TextView mTvVideoShow;

	private VideoModel mVideoModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_video);
		ButterKnife.bind(this);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
		setupView();
		setupData();
	}

	private void setupView() {
	}

	private void setupData() {
		mVideoModel = (VideoModel) getIntent().getSerializableExtra(ValueKey.VIDEO);
		if (mVideoModel != null) {
			imgQueue.setImageURI(Uri.parse(mVideoModel.curImgPath));
			roseNum.setText(mVideoModel.gold + "枚金币");
			mVideoInfo.setText(mVideoModel.description);
		}
	}

	@OnClick({R.id.img_queue, R.id.tv_video_show})
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.img_queue:
				if (AppManager.getClientUser().is_vip && AppManager.getClientUser().gold_num == 0) {
					showBuyRoseDialog();
				} else if (!AppManager.getClientUser().is_vip){
					showTurnOnVipDialog();
				} else if (AppManager.getClientUser().gold_num < Integer.parseInt(mVideoModel.gold)) {
					showBuyRoseDialog();
				} else {
					/**
					 * 更新服务器上的金币数量
					 */
					AppManager.getClientUser().gold_num -=  Integer.parseInt(mVideoModel.gold);
					new UpdateGoldTask().request(AppManager.getClientUser().gold_num, mVideoModel.curVideoPath);
				}
				break;
			case R.id.tv_video_show:
				Intent intent = new Intent();
				intent.setClass(this, VideoShowActivity.class);
				intent.putExtra(ValueKey.USER_ID, mVideoModel.usersByUserId);
				startActivity(intent);
				break;
		}
	}

	class UpdateGoldTask extends UpdateGoldRequest {
		@Override
		public void onPostExecute(Integer integer) {
			Intent intent = new Intent();
			intent.setClass(SingleVideoActivity.this, VideoPlayActivity.class);
			intent.putExtra(ValueKey.VIDEO, mVideoModel.curVideoPath);
			startActivity(intent);
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
		}
	}

	private void showTurnOnVipDialog() {
		Builder builder = new Builder(this);
		builder.setMessage(R.string.un_see_video);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(SingleVideoActivity.this, VipCenterActivity.class);
				startActivity(intent);
			}
		});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}

	private void showBuyRoseDialog() {
		Builder builder = new Builder(this);
		builder.setMessage(String.format(getResources().getString(R.string.buy_gold), Integer.parseInt(mVideoModel.gold)));
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(SingleVideoActivity.this, MyGoldActivity.class);
				startActivity(intent);
			}
		});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
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
