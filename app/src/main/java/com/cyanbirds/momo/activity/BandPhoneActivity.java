package com.cyanbirds.momo.activity;

import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.EditText;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.IUserApi;
import com.cyanbirds.momo.net.base.RetrofitFactory;
import com.cyanbirds.momo.utils.CheckUtil;
import com.cyanbirds.momo.utils.ProgressDialogUtils;
import com.cyanbirds.momo.utils.ToastUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import mehdi.sakout.fancybuttons.FancyButton;
import okhttp3.ResponseBody;

/**
 * @author wangyb
 * @Description:绑定手机
 * @Date:2015年7月13日下午2:21:46
 */
public class BandPhoneActivity extends BaseActivity {

    @BindView(R.id.phone_num)
    EditText mPhoneNum;
    @BindView(R.id.next)
    FancyButton mNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_phone);
        ButterKnife.bind(this);
        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
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

    @OnClick(R.id.next)
    public void onViewClicked() {
        if (checkInput()) {
            ProgressDialogUtils.getInstance(this).show(R.string.wait);
            AppManager.getClientUser().isCheckPhone = true;
            AppManager.getClientUser().mobile = mPhoneNum.getText().toString();
            RetrofitFactory.getRetrofit().create(IUserApi.class)
                    .updateUserInfo(AppManager.getClientUser().sessionId, getParam(AppManager.getClientUser()))
                    .subscribeOn(Schedulers.io())
                    .map(responseBody -> {
                        AppManager.setClientUser(AppManager.getClientUser());
                        AppManager.saveUserInfo();
                        JsonObject obj = new JsonParser().parse(responseBody.string()).getAsJsonObject();
                        int code = obj.get("code").getAsInt();
                        return code;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                    .subscribe(integer -> {
                        ProgressDialogUtils.getInstance(BandPhoneActivity.this).dismiss();
                        if (integer == 0) {//绑定成功
                            ToastUtil.showMessage(R.string.bangding_success);
                        } else {
                            ToastUtil.showMessage(R.string.bangding_faile);
                        }
                        finish();
                    }, throwable -> {
                        ProgressDialogUtils.getInstance(BandPhoneActivity.this).dismiss();
                        ToastUtil.showMessage(R.string.network_requests_error);
                    });
        }
    }

    private ArrayMap<String, String> getParam(ClientUser clientUser) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("sex", clientUser.sex);
        params.put("nickName", clientUser.user_name);
        params.put("faceurl", clientUser.face_url);
        if(!TextUtils.isEmpty(clientUser.personality_tag)){
            params.put("personalityTag", clientUser.personality_tag);
        }
        if(!TextUtils.isEmpty(clientUser.part_tag)){
            params.put("partTag", clientUser.part_tag);
        }
        if(!TextUtils.isEmpty(clientUser.intrest_tag)){
            params.put("intrestTag", clientUser.intrest_tag);
        }
        params.put("age", String.valueOf(clientUser.age));
        params.put("signature", clientUser.signature == null ? "" : clientUser.signature);
        params.put("qq", clientUser.qq_no == null ? "" : clientUser.qq_no);
        params.put("wechat", clientUser.weixin_no == null ? "" : clientUser.weixin_no);
        params.put("publicSocialNumber", String.valueOf(clientUser.publicSocialNumber));
        params.put("emotionStatus", clientUser.state_marry == null ? "" : clientUser.state_marry);
        params.put("tall", clientUser.tall == null ? "" : clientUser.tall);
        params.put("weight", clientUser.weight == null ? "" : clientUser.weight);
        params.put("constellation", clientUser.constellation == null ? "" : clientUser.constellation);
        params.put("occupation", clientUser.occupation == null ? "" : clientUser.occupation);
        params.put("education", clientUser.education == null ? "" : clientUser.education);
        params.put("purpose", clientUser.purpose == null ? "" : clientUser.purpose);
        params.put("loveWhere", clientUser.love_where == null ? "" : clientUser.love_where);
        params.put("doWhatFirst", clientUser.do_what_first == null ? "" : clientUser.do_what_first);
        params.put("conception", clientUser.conception == null ? "" : clientUser.conception);
        params.put("isDownloadVip", String.valueOf(clientUser.is_download_vip));
        params.put("goldNum", String.valueOf(clientUser.gold_num));
        params.put("phone", clientUser.mobile);
        params.put("isCheckPhone", String.valueOf(clientUser.isCheckPhone));
        return params;
    }

    /**
     * 验证输入
     */
    private boolean checkInput() {
        String message = "";
        boolean bool = true;
        if (TextUtils.isEmpty(mPhoneNum.getText().toString())) {
            message = getResources().getString(R.string.input_phone);
            bool = false;
        } else if (!CheckUtil.isMobileNO(mPhoneNum.getText().toString())) {
            message = getResources().getString(
                    R.string.input_phone_number_error);
            bool = false;
        }
        if (!bool)
            ToastUtil.showMessage(message);
        return bool;
    }
}
