package com.cyanbirds.momo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.db.ConversationSqlManager;
import com.cyanbirds.momo.db.IMessageDaoManager;
import com.cyanbirds.momo.db.MyGoldDaoManager;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.manager.NotificationManager;
import com.cyanbirds.momo.net.request.LogoutRequest;
import com.cyanbirds.momo.utils.PreferencesUtils;
import com.cyanbirds.momo.utils.ProgressDialogUtils;
import com.cyanbirds.momo.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * @author: wangyb
 * @datetime: 2016-01-23 16:58 GMT+8
 * @email: 395044952@qq.com
 * @description:
 */
public class SettingActivity extends BaseActivity {

    @BindView(R.id.banding_phone_lay)
    RelativeLayout bandingPhoneLay;
    @BindView(R.id.modify_pwd_lay)
    RelativeLayout modifyPwdLay;
    @BindView(R.id.quit)
    RelativeLayout quit;
    @BindView(R.id.is_bangding_phone)
    TextView is_bangding_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.setting);
        setupView();
        setupEvent();
        setupData();
    }

    private void setupView() {
    }

    private void setupEvent() {
    }

    private void setupData() {
        if (AppManager.getClientUser().isCheckPhone) {
            is_bangding_phone.setText(R.string.already_bangding);
        } else {
            is_bangding_phone.setText(R.string.un_bangding);
        }
    }


    @OnClick({R.id.banding_phone_lay, R.id.modify_pwd_lay, R.id.quit})
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.banding_phone_lay:
                //0=注册1=找回密码2=验证绑定手机
                intent.setClass(this, FindPwdActivity.class);
                intent.putExtra(ValueKey.INPUT_PHONE_TYPE, 2);
                startActivity(intent);
                finish();
                break;
            case R.id.modify_pwd_lay:
                intent.setClass(this, ModifyPwdActivity.class);
                startActivity(intent);
                break;
            case R.id.quit:
                showQuitDialog();
                break;
        }
    }

    class LogoutTask extends LogoutRequest {
        @Override
        public void onPostExecute(String s) {
            ProgressDialogUtils.getInstance(SettingActivity.this).dismiss();
            MobclickAgent.onProfileSignOff();
            release();
            NotificationManager.getInstance().cancelNotification();
            finishAll();
            PreferencesUtils.setIsLogin(SettingActivity.this, false);
            Intent intent = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(
                            getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        @Override
        public void onErrorExecute(String error) {
            ProgressDialogUtils.getInstance(SettingActivity.this).dismiss();
            ToastUtil.showMessage(error);
        }
    }

    /**
     * 显示退出dialog
     */
    private void showQuitDialog() {
        new AlertDialog.Builder(this)
                .setItems(R.array.quit_items,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                switch (which) {
                                    case 0:
                                        ProgressDialogUtils.getInstance(SettingActivity.this).show(R.string.dialog_logout_tips);
                                        new LogoutTask().request();
                                        break;
                                    case 1:
                                        exitApp();
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
