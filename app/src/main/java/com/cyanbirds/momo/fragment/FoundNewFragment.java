package com.cyanbirds.momo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.adapter.FoundNewAdapter;
import com.cyanbirds.momo.db.ContactSqlManager;
import com.cyanbirds.momo.entity.Contact;
import com.cyanbirds.momo.listener.ModifyContactsListener;
import com.cyanbirds.momo.net.request.ContactsRequest;
import com.cyanbirds.momo.ui.widget.CircularProgress;
import com.cyanbirds.momo.ui.widget.DividerItemDecoration;
import com.cyanbirds.momo.ui.widget.WrapperLinearLayoutManager;
import com.cyanbirds.momo.utils.DensityUtil;
import com.cyanbirds.momo.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangyb
 * @datetime: 2015-12-20 11:33 GMT+8
 * @email: 395044952@qq.com
 * @description:
 */
public class FoundNewFragment extends Fragment implements ModifyContactsListener.OnDataChangedListener {
    private RecyclerView mRecyclerView;
    private CircularProgress mProgressBar;
    private View rootView;
    private FoundNewAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    private List<Contact> mNetContacts;//网络请求的联系人
    private List<Contact> mContacts;//通讯录已存在的好友
    private int pageIndex = 1;
    private int pageSize = 50;
    private String GENDER = ""; //空表示查询和自己性别相反的用户
    /**
     * 0:同城 1：缘分 2：颜值  -1:就是全国
     */
    private String mUserScopeType = "0";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_found, null);
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
                R.string.tab_found);
        return rootView;
    }

    private void setupViews() {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        mProgressBar = (CircularProgress) rootView.findViewById(R.id.progress_bar);
        layoutManager = new WrapperLinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL, DensityUtil
                .dip2px(getActivity(), 12), DensityUtil.dip2px(
                getActivity(), 12)));
    }

    private void setupEvent(){
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        ModifyContactsListener.getInstance().addOnDataChangedListener(this);
    }

    private void setupData() {
        mNetContacts = new ArrayList<>();
        mAdapter = new FoundNewAdapter(mNetContacts, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.VISIBLE);
        new ContactsTask().request(pageIndex, pageSize, GENDER, mUserScopeType);

        mContacts = ContactSqlManager.getInstance(getActivity()).queryAllContactsByFrom(true);
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        private int lastVisibleItem;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            lastVisibleItem = layoutManager.findLastVisibleItemPosition();
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && lastVisibleItem + 1 == mAdapter.getItemCount()
                    && mAdapter.isShowFooter()) {
                //加载更多
                //请求数据
                new ContactsTask().request(++pageIndex, pageSize, GENDER, mUserScopeType);
            }
        }
    };

    @Override
    public void onDataChanged(Contact contact) {
        if (contact != null) {
            for (Contact clientUser : mNetContacts) {
                if (contact.userId.equals(clientUser.userId)) {
                    mNetContacts.remove(clientUser);
                    break;
                }
            }
            mAdapter.setClientUsers(mNetContacts);
        }
    }

    @Override
    public void onDeleteDataChanged(String userId) {
    }

    @Override
    public void onAddDataChanged(Contact contact) {

    }

    class ContactsTask extends ContactsRequest {
        @Override
        public void onPostExecute(List<Contact> result) {
            if (mContacts != null && mContacts.size() > 0) {
                List<Contact> clientUsers = new ArrayList<>();
                clientUsers.addAll(result);
                for (Contact contact : mContacts) {
                    for (Contact clientUser : clientUsers) {
                        if (contact.userId.equals(clientUser.userId)) {
                            result.remove(clientUser);
                            break;
                        }
                    }
                }
            }

            mProgressBar.setVisibility(View.GONE);
            if (pageIndex == 1) {//进行筛选的时候，滑动到顶部
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
            if(result == null || result.size() == 0){
                mAdapter.setIsShowFooter(false);
                mAdapter.notifyDataSetChanged();
                ToastUtil.showMessage(R.string.no_more_data);
            } else {
                mNetContacts.addAll(result);
                mAdapter.setIsShowFooter(true);
                mAdapter.setClientUsers(mNetContacts);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            ToastUtil.showMessage(error);
            mProgressBar.setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        ModifyContactsListener.getInstance().removeOnDataChangedListener(this);
    }
}
