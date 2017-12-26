package com.cyanbirds.momo.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.adapter.TabFragmentAdapter;
import com.cyanbirds.momo.fragment.MyStickerFragment;
import com.cyanbirds.momo.fragment.StickerMarketFragment;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 表情相关
 */
public class ExpressionActivity extends BaseActivity  {

	@BindView(R.id.tabs)
	TabLayout mTabLayout;
	@BindView(R.id.viewpager)
	ViewPager mViewpager;

	private List<String> tabList;
	private List<Fragment> fragmentList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_make_money);
		ButterKnife.bind(this);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
		setupViews();
	}

	/**
	 * 设置视图
	 */
	private void setupViews() {
		tabList = new ArrayList<>(2);
		fragmentList = new ArrayList<>(2);
		tabList.add("下载表情");
		tabList.add("我的表情");
		mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
		mTabLayout.addTab(mTabLayout.newTab().setText(tabList.get(0)));//添加tab选项卡
		mTabLayout.addTab(mTabLayout.newTab().setText(tabList.get(1)));

		StickerMarketFragment stickerMarketFragment = new StickerMarketFragment();
		MyStickerFragment myStickerFragment = new MyStickerFragment();
		fragmentList.add(stickerMarketFragment);
		fragmentList.add(myStickerFragment);

		TabFragmentAdapter fragmentAdapter = new TabFragmentAdapter(
				getSupportFragmentManager(), fragmentList, tabList);
		mViewpager.setAdapter(fragmentAdapter);//给ViewPager设置适配器
		mTabLayout.setupWithViewPager(mViewpager);//将TabLayout和ViewPager关联起来。
		mTabLayout.setTabsFromPagerAdapter(fragmentAdapter);
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
