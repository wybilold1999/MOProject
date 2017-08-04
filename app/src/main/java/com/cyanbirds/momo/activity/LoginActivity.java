package com.cyanbirds.momo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyanbirds.momo.CSApplication;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.config.AppConstants;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.eventtype.LocationEvent;
import com.cyanbirds.momo.eventtype.WeinXinEvent;
import com.cyanbirds.momo.helper.IMChattingHelper;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.request.DownloadFileRequest;
import com.cyanbirds.momo.net.request.QqLoginRequest;
import com.cyanbirds.momo.net.request.UserLoginRequest;
import com.cyanbirds.momo.net.request.WXLoginRequest;
import com.cyanbirds.momo.utils.AESEncryptorUtil;
import com.cyanbirds.momo.utils.CheckUtil;
import com.cyanbirds.momo.utils.FileAccessorUtils;
import com.cyanbirds.momo.utils.Md5Util;
import com.cyanbirds.momo.utils.PreferencesUtils;
import com.cyanbirds.momo.utils.ProgressDialogUtils;
import com.cyanbirds.momo.utils.ToastUtil;
import com.cyanbirds.momo.utils.Util;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Administrator on 2016/4/23.
 */
public class LoginActivity extends BaseActivity {
    @BindView(R.id.login_account)
    EditText loginAccount;
    @BindView(R.id.login_pwd)
    EditText loginPwd;
    @BindView(R.id.btn_login)
    FancyButton btnLogin;
    @BindView(R.id.forget_pwd)
    TextView forgetPwd;
    @BindView(R.id.weixin_login)
    ImageView weiXinLogin;
    @BindView(R.id.qq_login)
    ImageView qqLogin;

    public static Tencent mTencent;
    private UserInfo mInfo;
    private String token;
    private String openId;

    private String mPhoneNum;
    private String channelId;
    private boolean activityIsRunning;
    private String mCurrrentCity;//定位到的城市
    private String curLat;
    private String curLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
        }
        setupData();

        channelId = CheckUtil.getAppMetaData(this, "UMENG_CHANNEL");
    }

    private void setupData(){
        if (mTencent == null) {
            mTencent = Tencent.createInstance(AppConstants.mAppid, this);
        }
        mPhoneNum = getIntent().getStringExtra(ValueKey.PHONE_NUMBER);
        if(!TextUtils.isEmpty(mPhoneNum)){
            loginAccount.setText(mPhoneNum);
            loginAccount.setSelection(mPhoneNum.length());
        }
        mCurrrentCity = getIntent().getStringExtra(ValueKey.LOCATION);
        curLat = getIntent().getStringExtra(ValueKey.LATITUDE);
        curLon = getIntent().getStringExtra(ValueKey.LONGITUDE);
    }

    @OnClick({R.id.btn_login, R.id.forget_pwd, R.id.qq_login, R.id.weixin_login})
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.btn_login:
                if(checkInput()){
                    String cryptLoginPwd = AESEncryptorUtil.crypt(loginPwd.getText().toString().trim(),
                            AppConstants.SECURITY_KEY);
                    ProgressDialogUtils.getInstance(this).show(R.string.dialog_request_login);
                    new UserLoginTask().request(loginAccount.getText().toString().trim(),
                            cryptLoginPwd, mCurrrentCity);
                }
                break;
            case R.id.forget_pwd:
                //0=注册1=找回密码2=验证绑定手机
                intent.setClass(this, FindPwdActivity.class);
                intent.putExtra(ValueKey.INPUT_PHONE_TYPE, 1);
                intent.putExtra(ValueKey.LOCATION, mCurrrentCity);
                startActivity(intent);
                break;
            case R.id.qq_login:
                ProgressDialogUtils.getInstance(this).show(R.string.wait);
                if (!mTencent.isSessionValid() &&
                        mTencent.getQQToken().getOpenId() == null) {
                    mTencent.login(this, "all", loginListener);
                }
                break;
            case R.id.weixin_login:
                ProgressDialogUtils.getInstance(this).show(R.string.wait);
                SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = "wechat_sdk_demo_test";
                CSApplication.api.sendReq(req);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void weiXinLogin(WeinXinEvent event) {
        ProgressDialogUtils.getInstance(LoginActivity.this).show(R.string.dialog_request_login);
        new WXLoginTask().request(event.code, channelId, mCurrrentCity);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getCity(LocationEvent event) {
        mCurrrentCity = event.city;
    }

    class WXLoginTask extends WXLoginRequest {
        @Override
        public void onPostExecute(ClientUser clientUser) {
            MobclickAgent.onProfileSignIn(String.valueOf(AppManager
                    .getClientUser().userId));
            if(!new File(FileAccessorUtils.FACE_IMAGE,
                    Md5Util.md5(clientUser.face_url) + ".jpg").exists()
                    && !TextUtils.isEmpty(clientUser.face_url)){
                new DownloadPortraitTask().request(clientUser.face_url,
                        FileAccessorUtils.FACE_IMAGE,
                        Md5Util.md5(clientUser.face_url) + ".jpg");
            }
            clientUser.currentCity = mCurrrentCity;
            clientUser.latitude = curLat;
            clientUser.longitude = curLon;
            AppManager.setClientUser(clientUser);
            AppManager.saveUserInfo();
            AppManager.getClientUser().loginTime = System.currentTimeMillis();
            PreferencesUtils.setLoginTime(LoginActivity.this, System.currentTimeMillis());
            IMChattingHelper.getInstance().sendInitLoginMsg();
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finishAll();
        }

        @Override
        public void onErrorExecute(String error) {
            ProgressDialogUtils.getInstance(LoginActivity.this).dismiss();
            ToastUtil.showMessage(error);
        }
    }

    class UserLoginTask extends UserLoginRequest {
        @Override
        public void onPostExecute(ClientUser clientUser) {
            hideSoftKeyboard();
            MobclickAgent.onProfileSignIn(String.valueOf(AppManager
                    .getClientUser().userId));
            if(!new File(FileAccessorUtils.FACE_IMAGE,
                    Md5Util.md5(clientUser.face_url) + ".jpg").exists()
                    && !TextUtils.isEmpty(clientUser.face_url)){
                new DownloadPortraitTask().request(clientUser.face_url,
                        FileAccessorUtils.FACE_IMAGE,
                        Md5Util.md5(clientUser.face_url) + ".jpg");
            }
            clientUser.currentCity = mCurrrentCity;
            clientUser.latitude = curLat;
            clientUser.longitude = curLon;
            AppManager.setClientUser(clientUser);
            AppManager.saveUserInfo();
            AppManager.getClientUser().loginTime = System.currentTimeMillis();
            PreferencesUtils.setLoginTime(LoginActivity.this, System.currentTimeMillis());
            IMChattingHelper.getInstance().sendInitLoginMsg();
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finishAll();
        }

        @Override
        public void onErrorExecute(String error) {
            ProgressDialogUtils.getInstance(LoginActivity.this).dismiss();
            ToastUtil.showMessage(error);
        }
    }

    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
            initOpenidAndToken(values);
            updateUserInfo();
        }
    };

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            if (null == response) {
                ToastUtil.showMessage("登录失败");
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                ToastUtil.showMessage("登录失败");
                return;
            }
            doComplete((JSONObject)response);
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e) {
            Util.toastMessage(LoginActivity.this, "onError: " + e.errorDetail);
            Util.dismissDialog();
        }

        @Override
        public void onCancel() {
            Util.toastMessage(LoginActivity.this, "取消授权");
        }
    }

    public  void initOpenidAndToken(JSONObject jsonObject) {
        try {
            token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
        } catch(Exception e) {
        }
    }

    private void updateUserInfo() {
        if (mTencent != null && mTencent.isSessionValid()) {
            IUiListener listener = new IUiListener() {
                @Override
                public void onError(UiError e) {
                }
                @Override
                public void onComplete(final Object response) {
                    if (activityIsRunning) {
                        ProgressDialogUtils.getInstance(LoginActivity.this).show(R.string.dialog_request_login);
                    }
                    new QqLoginTask().request(token, openId, channelId, mCurrrentCity);
                }

                @Override
                public void onCancel() {

                }
            };
            mInfo = new UserInfo(this, mTencent.getQQToken());
            mInfo.getUserInfo(listener);
        } else {
        }
    }

    class QqLoginTask extends QqLoginRequest {
        @Override
        public void onPostExecute(ClientUser clientUser) {
            MobclickAgent.onProfileSignIn(String.valueOf(AppManager
                    .getClientUser().userId));
            if(!new File(FileAccessorUtils.FACE_IMAGE,
                    Md5Util.md5(clientUser.face_url) + ".jpg").exists()
                    && !TextUtils.isEmpty(clientUser.face_url)){
                new DownloadPortraitTask().request(clientUser.face_url,
                        FileAccessorUtils.FACE_IMAGE,
                        Md5Util.md5(clientUser.face_url) + ".jpg");
            }
            clientUser.currentCity = mCurrrentCity;
            clientUser.latitude = curLat;
            clientUser.longitude = curLon;
            AppManager.setClientUser(clientUser);
            AppManager.saveUserInfo();
            AppManager.getClientUser().loginTime = System.currentTimeMillis();
            PreferencesUtils.setLoginTime(LoginActivity.this, System.currentTimeMillis());
            IMChattingHelper.getInstance().sendInitLoginMsg();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finishAll();
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_LOGIN ||
                requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginListener);
        }
    }


    class DownloadPortraitTask extends DownloadFileRequest {
        @Override
        public void onPostExecute(String s) {
            AppManager.getClientUser().face_local = s;
            PreferencesUtils.setFaceLocal(LoginActivity.this, s);
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    /**
     * 验证输入
     */
    private boolean checkInput() {
        String message = "";
        boolean bool = true;
        if (TextUtils.isEmpty(loginAccount.getText().toString())) {
            message = getResources().getString(R.string.input_phone_or_account);
            bool = false;
        } else if (TextUtils.isEmpty(loginPwd.getText().toString())) {
            message = getResources().getString(R.string.input_password);
            bool = false;
        }
        if (!bool)
            ToastUtil.showMessage(message);
        return bool;
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityIsRunning = true;
        ProgressDialogUtils.getInstance(this).dismiss();
        MobclickAgent.onPageStart(this.getClass().getName());
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityIsRunning = false;
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ProgressDialogUtils.getInstance(this).dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
