package com.cyanbirds.momo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.adapter.AttentionMeAdapter;
import com.cyanbirds.momo.adapter.MyAttentionAdapter;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.FollowModel;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.request.FollowListRequest;
import com.cyanbirds.momo.ui.widget.CircularProgress;
import com.cyanbirds.momo.ui.widget.DividerItemDecoration;
import com.cyanbirds.momo.ui.widget.WrapperLinearLayoutManager;
import com.cyanbirds.momo.utils.DensityUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2016-01-13 22:17 GMT+8
 * @email 395044952@qq.com
 * 自己被别的用户关注
 */
public class AttentionMeActivity extends BaseActivity {
    private RecyclerView mRecyclerView;
    private CircularProgress mCircularProgress;
    private TextView mNoUserinfo;
    private AttentionMeAdapter mAdapter;
    private List<FollowModel> mFollowModels;
    private int pageNo = 1;
    private int pageSize = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attention_me);
        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
        }
        setupView();
        setupEvent();
        setupData();
    }

    private void setupView(){
        mCircularProgress = (CircularProgress) findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mNoUserinfo = (TextView) findViewById(R.id.info);
        LinearLayoutManager manager = new WrapperLinearLayoutManager(this);
        manager.setOrientation(LinearLayout.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL, DensityUtil
                .dip2px(this, 12), DensityUtil.dip2px(
                this, 12)));
    }

    private void setupEvent(){

    }

    private void setupData(){
        if (AppManager.getClientUser().is_vip) {
            pageSize = 200;
        }
        mFollowModels = new ArrayList<>();
        mAdapter = new AttentionMeAdapter(AttentionMeActivity.this);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mCircularProgress.setVisibility(View.VISIBLE);
        new FollowListTask().request("followFormeList", pageNo, pageSize);
    }

    private AttentionMeAdapter.OnItemClickListener mOnItemClickListener = new AttentionMeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            FollowModel followModel = mFollowModels.get(position);
            Intent intent = new Intent(AttentionMeActivity.this, PersonalInfoActivity.class);
            intent.putExtra(ValueKey.USER_ID, String.valueOf(followModel.userId));
            startActivity(intent);
        }
    };

    class FollowListTask extends FollowListRequest {
        @Override
        public void onPostExecute(List<FollowModel> followModels) {
            mCircularProgress.setVisibility(View.GONE);
            if(followModels != null && followModels.size() > 0 && followModels.size() > 10){
                if (!AppManager.getClientUser().is_vip) {//如果不是vip，移除前面3个
                    mAdapter.setIsShowFooter(true);
                    List<String> urls = new ArrayList<>(3);
                    urls.add(followModels.get(0).faceUrl);
                    urls.add(followModels.get(1).faceUrl);
                    urls.add(followModels.get(2).faceUrl);
                    mAdapter.setFooterFaceUrls(urls);
                    followModels.remove(0);
                    followModels.remove(1);
                }
                mCircularProgress.setVisibility(View.GONE);
                mFollowModels.addAll(followModels);
                mAdapter.setFollowModels(mFollowModels);
            } else {
                mAdapter.setIsShowFooter(false);
                if (followModels != null) {
                    mFollowModels.addAll(followModels);
                }
                mAdapter.setFollowModels(mFollowModels);
            }
            if (mFollowModels != null && mFollowModels.size() > 0) {
                mNoUserinfo.setVisibility(View.GONE);
            } else {
                mNoUserinfo.setVisibility(View.VISIBLE);
            }
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
