package com.cyanbirds.momo.fragment;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.CardActivity;
import com.cyanbirds.momo.activity.PersonalInfoActivity;
import com.cyanbirds.momo.adapter.FindLoveAdapter;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.request.GetFindLoveRequest;
import com.cyanbirds.momo.net.request.GetRealUserRequest;
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
public class FindLoveFragment extends Fragment implements OnRefreshListener, View.OnClickListener{
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;
    private FloatingActionButton mFab;
    private CircularProgress mProgress;
    private View rootView;
    private View searchView;
    private RadioButton sex_male;
    private RadioButton sex_female;
    private RadioGroup mSexGroup;
    private RadioButton all_country;
    private RadioButton same_city;
    private RadioGroup rg_area;

    private FindLoveAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    private List<ClientUser> mClientUsers;
    private final long freshSpan = 3 * 60 * 1000;//3分钟之后才运行下拉刷新
    private long freshTime = 0;
    private int pageIndex = 1;
    private int pageSize = 150;
    private String GENDER = ""; //空表示查询和自己性别相反的用户
	/**
     * 0:同城 1：缘分 2：颜值  -1:就是全国
     */
    private String mUserScopeType = "-1";

    private final String SAME_CITY = "0";
    private final String BEAUTIFUL = "2";
    private final String ALL_COUNTRY = "-1";

    private static final String FRAGMENT_INDEX = "fragment_index";
    private int mCurIndex = -1;
    private boolean isNewData = false;

    public static FindLoveFragment newInstance(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt(FRAGMENT_INDEX, index);
        FindLoveFragment fragment = new FindLoveFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_find_love, null);
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
                R.string.tab_find_love);
        return rootView;
    }

    private void setupViews() {
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mProgress = (CircularProgress) rootView.findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        mSwipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

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

    private void setupEvent() {
        mSwipeRefresh.setOnRefreshListener(this);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mFab.setOnClickListener(this);
    }

    private void setupData() {
        //获得索引值
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCurIndex = bundle.getInt(FRAGMENT_INDEX);
        }
        switch (mCurIndex) {
            case 0 :
                mUserScopeType = BEAUTIFUL;
                break;
            case 1 :
                mUserScopeType = SAME_CITY;
                break;
            case 2 :
                mUserScopeType = ALL_COUNTRY;
                break;
        }

        mClientUsers = new ArrayList<>();
        mAdapter = new FindLoveAdapter(mClientUsers, getActivity());
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mProgress.setVisibility(View.VISIBLE);
        if("男".equals(AppManager.getClientUser().sex)){
            GENDER = "FeMale";
        } else {
            GENDER = "Male";
        }
        if("-1".equals(AppManager.getClientUser().userId) ||
                "-2".equals(AppManager.getClientUser().userId) ||
                "-3".equals(AppManager.getClientUser().userId)){ //客服登陆，获取真实用户
            new GetRealLoveUsersTask().request(pageIndex, pageSize, GENDER);
        } else {
            new GetFindLoveTask().request(pageIndex, pageSize, GENDER, mUserScopeType);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showSearchDialog();
        return super.onOptionsItemSelected(item);
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
                if("-1".equals(AppManager.getClientUser().userId) ||
                        "-2".equals(AppManager.getClientUser().userId) ||
                        "-3".equals(AppManager.getClientUser().userId)){ //客服登陆，获取真实用户
                    new GetRealLoveUsersTask().request(++pageIndex, pageSize, GENDER);
                } else {
                    new GetFindLoveTask().request(++pageIndex, pageSize, GENDER, mUserScopeType);
                }
            }
        }
    };

    private FindLoveAdapter.OnItemClickListener mOnItemClickListener = new FindLoveAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            ClientUser clientUser = mAdapter.getItem(position);
            if (null != clientUser) {
                Intent intent = new Intent(getActivity(), PersonalInfoActivity.class);
                intent.putExtra(ValueKey.USER_ID, clientUser.userId);
                intent.putExtra(ValueKey.FROM_ACTIVITY, "FindLoveFragment");
                startActivity(intent);
            }
        }
    };

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), CardActivity.class);
        startActivity(intent);
    }

    class GetFindLoveTask extends GetFindLoveRequest {
        @Override
        public void onPostExecute(List<ClientUser> userList) {
            freshTime = System.currentTimeMillis();
            mProgress.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
            if (pageIndex == 1) {//进行筛选的时候，滑动到顶部
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
            if(userList == null || userList.size() == 0){
                mAdapter.setIsShowFooter(false);
                mAdapter.notifyDataSetChanged();
                ToastUtil.showMessage(R.string.no_more_data);
            } else {
                mClientUsers.addAll(userList);
                mAdapter.setIsShowFooter(true);
                mAdapter.setClientUsers(mClientUsers);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            ToastUtil.showMessage(error);
            mProgress.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
            mAdapter.setIsShowFooter(false);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 获取真实的用户
     */
    class GetRealLoveUsersTask extends GetRealUserRequest {
        @Override
        public void onPostExecute(List<ClientUser> clientUsers) {
            mProgress.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
            if(clientUsers == null || clientUsers.size() == 0){
                mAdapter.setIsShowFooter(false);
                mAdapter.notifyDataSetChanged();
//                ToastUtil.showMessage(R.string.no_more_data);
            } else {
                mClientUsers.addAll(clientUsers);
                mAdapter.setIsShowFooter(true);
                mAdapter.setClientUsers(mClientUsers);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            mSwipeRefresh.setRefreshing(false);
            mProgress.setVisibility(View.GONE);
            mAdapter.setIsShowFooter(false);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 筛选dialog
     */
    private void showSearchDialog(){
        initSearchDialogView();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.search_option));
        builder.setView(searchView);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        pageIndex = 1;
                        mClientUsers.clear();
                        if("-1".equals(AppManager.getClientUser().userId) ||
                                "-2".equals(AppManager.getClientUser().userId) ||
                                "-3".equals(AppManager.getClientUser().userId)){ //客服登陆，获取真实用户
                            new GetRealLoveUsersTask().request(pageIndex, pageSize, GENDER);
                        } else {
                            new GetFindLoveTask().request(pageIndex, pageSize, GENDER, mUserScopeType);
                        }
                        mProgress.setVisibility(View.VISIBLE);
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    /**
     * 初始化筛选对话框
     */
    private void initSearchDialogView(){
        searchView = LayoutInflater.from(getActivity()).inflate(R.layout.item_search, null);
        rg_area = (RadioGroup) searchView.findViewById(R.id.rg_area);
        all_country = (RadioButton) searchView.findViewById(R.id.all_country);
        same_city = (RadioButton) searchView.findViewById(R.id.same_city);
        mSexGroup = (RadioGroup) searchView.findViewById(R.id.rg_sex);
        sex_male = (RadioButton) searchView.findViewById(R.id.sex_male);
        sex_female = (RadioButton) searchView.findViewById(R.id.sex_female);
        if("Male".equals(GENDER)){
            sex_male.setChecked(true);
        } else if("FeMale".equals(GENDER)){
            sex_female.setChecked(true);
        }
        mSexGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == sex_male.getId()){
                    GENDER = "Male";
                } else if(checkedId == sex_female.getId()){
                    GENDER = "FeMale";
                }
            }
        });
        if (mUserScopeType.equals(ALL_COUNTRY)) {
            all_country.setChecked(true);
        } else {
            same_city.setChecked(true);
        }
        rg_area.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == all_country.getId()) {
                    mUserScopeType = ALL_COUNTRY;
                } else if (checkedId == same_city.getId()) {
                    mUserScopeType = SAME_CITY;
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        mSwipeRefresh.setRefreshing(true);
        if (System.currentTimeMillis() > freshTime + freshSpan) {
            getFreshData();
        } else {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    private void getFreshData() {
        if("-1".equals(AppManager.getClientUser().userId) ||
                "-2".equals(AppManager.getClientUser().userId) ||
                "-3".equals(AppManager.getClientUser().userId)){ //客服登陆，获取真实用户
            new GetRealLoveFreshFindLoveTask().request(++pageIndex, pageSize, GENDER);
        } else {
            new GetFreshFindLoveTask().request(++pageIndex, pageSize, GENDER, mUserScopeType);
        }
    }

    class GetFreshFindLoveTask extends GetFindLoveRequest{
        @Override
        public void onPostExecute(List<ClientUser> userList) {
            mProgress.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
            if(userList == null || userList.size() == 0){//没有数据了就又从第一页开始查找
                pageIndex = 1;
            } else {
                freshTime = System.currentTimeMillis();
                mClientUsers.clear();
                mClientUsers.addAll(userList);
                mAdapter.setIsShowFooter(false);
                mAdapter.setClientUsers(mClientUsers);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            mProgress.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
            ToastUtil.showMessage(error);
        }
    }

    class GetRealLoveFreshFindLoveTask extends GetRealUserRequest{
        @Override
        public void onPostExecute(List<ClientUser> userList) {
            mProgress.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
            if(userList == null || userList.size() == 0){//没有数据了就又从第一页开始查找
                ToastUtil.showMessage(R.string.no_more_data);
            } else {
                mClientUsers.clear();
                mClientUsers.addAll(userList);
                mAdapter.setIsShowFooter(false);
                mAdapter.setClientUsers(mClientUsers);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            mProgress.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mSwipeRefresh) {
            mSwipeRefresh.setOnRefreshListener(null);
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
