package com.cyanbirds.momo.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.utils.ToastUtil;
import com.cyanbirds.momo.utils.VibratorUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wangyb on 2017/6/12.
 * 描述：拨打电话
 */

public class VoipCallActivity extends BaseActivity {

    @BindView(R.id.portrait)
    SimpleDraweeView mPortrait;
    @BindView(R.id.nick_name)
    TextView mNickName;
    @BindView(R.id.answer)
    ImageView mAnswer;
    @BindView(R.id.decline)
    ImageView mDecline;

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voip_call);
        ButterKnife.bind(this);
        setupData();
    }

    private void setupData() {
        String faceUrl = getIntent().getStringExtra(ValueKey.IMAGE_URL);
        String nickName = getIntent().getStringExtra(ValueKey.USER_NAME);
        if (!TextUtils.isEmpty(faceUrl)) {
            mPortrait.setImageURI(Uri.parse(faceUrl));
        }
        if (!TextUtils.isEmpty(nickName)) {
            mNickName.setVisibility(View.VISIBLE);
            mNickName.setText(String.format(this.getResources().getString(R.string.calling), nickName));
        }
        timer = new CountDownTimer(50000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                timer.cancel();
                finish();
            }

        };
        timer.start();
        VibratorUtil.start();
    }

    @OnClick({R.id.answer, R.id.decline})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.answer:
                break;
            case R.id.decline:
                ToastUtil.showMessage(R.string.decline_call);
                VibratorUtil.cancel();
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VibratorUtil.cancel();
    }
}
