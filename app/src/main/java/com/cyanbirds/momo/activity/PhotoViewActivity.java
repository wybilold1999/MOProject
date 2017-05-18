package com.cyanbirds.momo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.net.request.AddLoveRequest;
import com.cyanbirds.momo.net.request.SendGreetRequest;
import com.cyanbirds.momo.ui.widget.CircularProgress;
import com.cyanbirds.momo.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import me.relex.photodraweeview.PhotoDraweeView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * @author wangyb
 * @Description:图片浏览
 * @Date:2015年7月8日上午11:09:50
 */
public class PhotoViewActivity extends BaseActivity implements View.OnClickListener, PhotoViewAttacher.OnViewTapListener {
    private ImageView mIvLove;
    private RelativeLayout mLoveLayout;
    private LinearLayout mMsgLayout;
    private PhotoDraweeView mPhotoView;
    private RelativeLayout mBottomLayout;
    private CircularProgress mCircularProgress;
    private PhotoViewAttacher mAttacher;

    private String mCurImgUrl; //当前显示图片的url
    private ClientUser mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);
        setupView();
        setupEvent();
        setupData();
    }

    private void setupView(){
        mPhotoView = (PhotoDraweeView) findViewById(R.id.photo_drawee_view);
        mCircularProgress = (CircularProgress) findViewById(R.id.progress_bar);
        mLoveLayout = (RelativeLayout) findViewById(R.id.love_layout);
        mIvLove = (ImageView) findViewById(R.id.iv_love);
        mMsgLayout = (LinearLayout) findViewById(R.id.ll_msg);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);

        mAttacher = new PhotoViewAttacher(mPhotoView);

    }

    private void setupEvent() {
        mMsgLayout.setOnClickListener(this);
        mLoveLayout.setOnClickListener(this);
        mPhotoView.setOnClickListener(this);
        mAttacher.setOnViewTapListener(this);
    }

    /**
     * 设置数据
     */
    private void setupData() {
        mCurImgUrl = getIntent().getStringExtra(ValueKey.IMAGE_URL);
        mUser = (ClientUser) getIntent().getSerializableExtra(ValueKey.USER);
        String fromActivity = getIntent().getStringExtra(ValueKey.FROM_ACTIVITY);
        if (!TextUtils.isEmpty(fromActivity)) {
            mBottomLayout.setVisibility(View.GONE);
        } else {
            mBottomLayout.setVisibility(View.VISIBLE);
        }
        if(!TextUtils.isEmpty(mCurImgUrl)){
            mPhotoView.setImageURI(Uri.parse(mCurImgUrl));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.love_layout :
                mIvLove.setImageResource(R.mipmap.love_focused);
                new SenderGreetTask().request(String.valueOf(mUser.userId));
                new AddLoveTask().request(String.valueOf(mUser.userId));
                break;
            case R.id.ll_msg :
                Intent intent = new Intent();
                intent.setClass(this, ChatActivity.class);
                intent.putExtra(ValueKey.USER, mUser);
                startActivity(intent);
                break;
            case R.id.photo_drawee_view :
                finish();
                break;
        }
    }

    @Override
    public void onViewTap(View view, float x, float y) {
        finish();
    }

    class SenderGreetTask extends SendGreetRequest {
        @Override
        public void onPostExecute(String s) {
            ToastUtil.showMessage(s);
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    class AddLoveTask extends AddLoveRequest {
        @Override
        public void onPostExecute(String s) {
        }

        @Override
        public void onErrorExecute(String error) {
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
}
