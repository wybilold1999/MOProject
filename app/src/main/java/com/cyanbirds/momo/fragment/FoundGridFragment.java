package com.cyanbirds.momo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.adapter.FoundGridAdapter;
import com.cyanbirds.momo.db.ContactSqlManager;
import com.cyanbirds.momo.entity.Contact;
import com.cyanbirds.momo.listener.ModifyContactsListener;
import com.cyanbirds.momo.net.request.ContactsRequest;
import com.cyanbirds.momo.ui.widget.CircularProgress;
import com.cyanbirds.momo.ui.widget.DividerGridItemDecoration;
import com.cyanbirds.momo.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wangyb on 2017/6/29.
 * 描述：
 */

public class FoundGridFragment extends Fragment implements ModifyContactsListener.OnDataChangedListener {

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar)
    CircularProgress mProgressBar;
    private View rootView;

    private List<Contact> mNetContacts;//网络请求的联系人
    private List<Contact> mContacts;//通讯录已存在的好友
    private int pageIndex = 1;
    private int pageSize = 50;
    private String GENDER = ""; //空表示查询和自己性别相反的用户
    /**
     * 0:同城 1：缘分 2：颜值  -1:就是全国
     */
    private String mUserScopeType = "0";

    private FoundGridAdapter mAdapter;
    private GridLayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_grid, null);
            ButterKnife.bind(this, rootView);
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
        layoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
    }

    private void setupEvent(){
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        ModifyContactsListener.getInstance().addOnDataChangedListener(this);
    }

    private void setupData() {
        mNetContacts = new ArrayList<>();
        mAdapter = new FoundGridAdapter(mNetContacts, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.VISIBLE);
        new ContactsTask().request(pageIndex, pageSize, GENDER, mUserScopeType);

        mContacts = ContactSqlManager.getInstance(getActivity()).queryAllContactsByFrom(true);
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //获取最后一个完全显示的ItemPosition
                int totalItemCount = manager.getItemCount();
                if (manager.findLastVisibleItemPosition() == (totalItemCount -1)) {
                    //加载更多
                    //请求数据
                    new ContactsTask().request(++pageIndex, pageSize, GENDER, mUserScopeType);
                }
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
}
