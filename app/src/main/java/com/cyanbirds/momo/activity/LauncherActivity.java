package com.cyanbirds.momo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.helper.IMChattingHelper;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.request.DownloadFileRequest;
import com.cyanbirds.momo.net.request.GetIDKeyRequest;
import com.cyanbirds.momo.net.request.UserLoginRequest;
import com.cyanbirds.momo.utils.FileAccessorUtils;
import com.cyanbirds.momo.utils.Md5Util;
import com.cyanbirds.momo.utils.PreferencesUtils;
import com.cyanbirds.momo.utils.PushMsgUtil;
import com.cyanbirds.momo.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

/**
 * @ClassName:LauncherActivity
 * @Description:启动界面
 * @Author:wangyb
 * @Date:2015年5月4日下午5:18:59
 */
public class LauncherActivity extends Activity {

    private long mStartTime;// 开始时间
    private final int SHOW_TIME_MIN = 1500;// 最小显示时间
    private final int LONG_SCUESS = 0;
    private final int LONG_FAIURE = 1;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case LONG_SCUESS:
                    long loadingTime = System.currentTimeMillis() - mStartTime;// 计算一下总共花费的时间
                    if (loadingTime < SHOW_TIME_MIN) {// 如果比最小显示时间还短，就延时进入MainActivity，否则直接进入
                        mHandler.postDelayed(mainActivity, SHOW_TIME_MIN
                                - loadingTime);
                    } else {
                        mHandler.postDelayed(mainActivity, 0);
                    }
                    break;
                case LONG_FAIURE:
                    mHandler.postDelayed(noLogin, 0);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStartTime = System.currentTimeMillis();// 记录开始时间
        init();
        loadData();
    }

    Runnable mainActivity = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(LauncherActivity.this,
                    MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    };

    private void init() {
        new GetIDKeyRequest().request();
        if (AppManager.isLogin()) {//是否已经登录
            login();
        } else {
            if (AppManager.getClientUser() != null
					&& !TextUtils.isEmpty(AppManager.getClientUser().userId)){// && Integer.parseInt(AppManager.getClientUser().userId) > 0) {
				mHandler.postDelayed(firstLauncher, SHOW_TIME_MIN);
			} else {
				mHandler.postDelayed(firstLauncher, SHOW_TIME_MIN);
			}

        }
    }

	/**
     * 点击通知栏的消息，将消息入库
     */
    private void loadData() {
        String msg = getIntent().getStringExtra(ValueKey.DATA);
        if (!TextUtils.isEmpty(msg)) {
            PushMsgUtil.getInstance().handlePushMsg(false, msg);
        }
    }

    /**
     * 没有登录
     */
    Runnable noLogin = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(LauncherActivity.this,
                    LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    };

    /**
     * 第一次进入
     */
    Runnable firstLauncher = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(LauncherActivity.this,
                    EntranceActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    };

    /**
     * 登录
     */
    private void login() {
        try {
            String userId = AppManager.getClientUser().userId;
            String userPwd = AppManager.getClientUser().userPwd;
            if(!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(userPwd)){
                new UserLoginTask().request(
                        userId, userPwd, PreferencesUtils.getCurrentCity(this));
            }
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(LONG_FAIURE);
        }
    }

    class UserLoginTask extends UserLoginRequest {
        @Override
        public void onPostExecute(ClientUser clientUser) {
            if (clientUser != null) {
                mHandler.sendEmptyMessage(LONG_SCUESS);
                if(!new File(FileAccessorUtils.FACE_IMAGE,
                        Md5Util.md5(clientUser.face_url) + ".jpg").exists()
                        && !TextUtils.isEmpty(clientUser.face_url)){
                    new DownloadPortraitTask().request(clientUser.face_url,
                            FileAccessorUtils.FACE_IMAGE,
                            Md5Util.md5(clientUser.face_url) + ".jpg");
                }
                AppManager.setClientUser(clientUser);
                AppManager.saveUserInfo();
                AppManager.getClientUser().loginTime = System.currentTimeMillis();
                PreferencesUtils.setLoginTime(LauncherActivity.this, System.currentTimeMillis());
                IMChattingHelper.getInstance().sendInitLoginMsg();
            }
        }

        @Override
        public void onErrorExecute(String error) {
            ToastUtil.showMessage(error);
            mHandler.sendEmptyMessage(LONG_FAIURE);
        }
    }

    class DownloadPortraitTask extends DownloadFileRequest {
        @Override
        public void onPostExecute(String s) {
            AppManager.getClientUser().face_local = s;
            PreferencesUtils.setFaceLocal(LauncherActivity.this, s);
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {// 禁用返回键
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mainActivity);
        mHandler.removeCallbacks(firstLauncher);
        mHandler.removeCallbacks(noLogin);
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
