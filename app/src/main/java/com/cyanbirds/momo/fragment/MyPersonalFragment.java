package com.cyanbirds.momo.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.AboutActivity;
import com.cyanbirds.momo.activity.ExpressionActivity;
import com.cyanbirds.momo.activity.FeedBackActivity;
import com.cyanbirds.momo.activity.ModifyUserInfoNewActivity;
import com.cyanbirds.momo.activity.SettingActivity;
import com.cyanbirds.momo.db.ContactSqlManager;
import com.cyanbirds.momo.db.ConversationSqlManager;
import com.cyanbirds.momo.db.ExpressionGroupSqlManager;
import com.cyanbirds.momo.db.ExpressionSqlManager;
import com.cyanbirds.momo.db.IMessageDaoManager;
import com.cyanbirds.momo.db.MyGoldDaoManager;
import com.cyanbirds.momo.db.NameListDaoManager;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.eventtype.UserEvent;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.manager.NotificationManager;
import com.cyanbirds.momo.net.request.DownloadFileRequest;
import com.cyanbirds.momo.net.request.LogoutRequest;
import com.cyanbirds.momo.utils.FileAccessorUtils;
import com.cyanbirds.momo.utils.Md5Util;
import com.cyanbirds.momo.utils.PreferencesUtils;
import com.cyanbirds.momo.utils.ProgressDialogUtils;
import com.cyanbirds.momo.utils.ToastUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cyanbirds.momo.activity.base.BaseActivity.finishAll;

/**
 * @author: wangyb
 * @datetime: 2015-12-20 11:34 GMT+8
 * @email: 395044952@qq.com
 * @description:
 */
public class MyPersonalFragment extends Fragment {

	@BindView(R.id.user_name)
	TextView userName;
	@BindView(R.id.signature)
	TextView signature;
	@BindView(R.id.user_info)
	LinearLayout userInfo;
	@BindView(R.id.head_portrait_lay)
	RelativeLayout headPortraitLay;
	@BindView(R.id.portrait)
	SimpleDraweeView mPortrait;
	@BindView(R.id.quit)
	RelativeLayout mQuit;
	@BindView(R.id.setting)
	RelativeLayout setting;
	@BindView(R.id.about)
	RelativeLayout about;
	@BindView(R.id.sticker_market)
	RelativeLayout mSticker;
	@BindView(R.id.feed_back)
	RelativeLayout mFeedBack;

	private View rootView;

	private ClientUser clientUser;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_my_personal, null);
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
		((AppCompatActivity) getActivity()).getSupportActionBar().show();
		((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
				R.string.tab_personal);
		ButterKnife.bind(this, rootView);
		return rootView;
	}

	private void setupViews() {
	}

	private void setupEvent() {
	}

	private void setupData() {
		setUserInfo();
	}


	/**
	 * 设置用户信息
	 */
	private void setUserInfo() {
		clientUser = AppManager.getClientUser();
		if (clientUser != null) {
			if (!TextUtils.isEmpty(clientUser.face_local) && new File(clientUser.face_local).exists()) {
				mPortrait.setImageURI(Uri.parse("file://" + clientUser.face_local));
			} else if (!TextUtils.isEmpty(clientUser.face_url)) {
				mPortrait.setImageURI(Uri.parse(clientUser.face_url));
				new DownloadPortraitTask().request(clientUser.face_url,
						FileAccessorUtils.FACE_IMAGE,
						Md5Util.md5(clientUser.face_url) + ".jpg");
			}
			if (!TextUtils.isEmpty(clientUser.signature)) {
				signature.setText(clientUser.signature);
			}
			if (!TextUtils.isEmpty(clientUser.user_name)) {
				userName.setText(clientUser.user_name);
			}
		}
	}

	/**
	 * 下载头像
	 */
	class DownloadPortraitTask extends DownloadFileRequest {
		@Override
		public void onPostExecute(String s) {
			AppManager.getClientUser().face_local = s;
			PreferencesUtils.setFaceLocal(getActivity(), s);
		}

		@Override
		public void onErrorExecute(String error) {
		}
	}

	@OnClick({
			R.id.head_portrait_lay, R.id.quit, R.id.setting, R.id.about,
			R.id.sticker_market, R.id.feed_back})
	public void onClick(View view) {
		Intent intent = new Intent();
		switch (view.getId()) {
			case R.id.head_portrait_lay:
				intent.setClass(getActivity(), ModifyUserInfoNewActivity.class);
				startActivity(intent);
				break;
			case R.id.quit:
				showQuitDialog();
				break;
			case R.id.setting:
				intent.setClass(getActivity(), SettingActivity.class);
				startActivity(intent);
				break;
			case R.id.about:
				intent.setClass(getActivity(), AboutActivity.class);
				startActivity(intent);
				break;
			case R.id.sticker_market:
				intent.setClass(getActivity(), ExpressionActivity.class);
				startActivity(intent);
				break;
			case R.id.feed_back:
				intent.setClass(getActivity(), FeedBackActivity.class);
				startActivity(intent);
				break;
		}
	}

	class LogoutTask extends LogoutRequest {
		@Override
		public void onPostExecute(String s) {
			ProgressDialogUtils.getInstance(getActivity()).dismiss();
			MobclickAgent.onProfileSignOff();
			release();
			NotificationManager.getInstance().cancelNotification();
			finishAll();
			PreferencesUtils.setIsLogin(getActivity(), false);
			Intent intent = getActivity().getPackageManager()
					.getLaunchIntentForPackage(
							getActivity().getPackageName());
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}

		@Override
		public void onErrorExecute(String error) {
			ProgressDialogUtils.getInstance(getActivity()).dismiss();
			ToastUtil.showMessage(error);
		}
	}

	/**
	 * 显示退出dialog
	 */
	private void showQuitDialog() {
		new AlertDialog.Builder(getActivity())
				.setItems(R.array.quit_items,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								switch (which) {
									case 0:
										ProgressDialogUtils.getInstance(getActivity()).show(R.string.dialog_logout_tips);
										new LogoutTask().request();
										break;
									case 1:
										finishAll();
										break;
								}
							}
						}).setTitle(R.string.quit).show();
	}

	/**
	 * 释放数据库
	 */
	private static void release() {
		IMessageDaoManager.reset();
		ConversationSqlManager.reset();
		MyGoldDaoManager.reset();
		ContactSqlManager.reset();
		ExpressionGroupSqlManager.reset();
		ExpressionSqlManager.reset();
		NameListDaoManager.reset();
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void changeUserInfo(UserEvent event) {
		ClientUser clientUser = AppManager.getClientUser();
		if (clientUser != null) {
			if (!TextUtils.isEmpty(clientUser.face_url)) {
				mPortrait.setImageURI(Uri.parse(clientUser.face_url));
			}
			if (!TextUtils.isEmpty(clientUser.signature)) {
				signature.setText(clientUser.signature);
			}
			if (!TextUtils.isEmpty(clientUser.user_name)) {
				userName.setText(clientUser.user_name);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
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
}
