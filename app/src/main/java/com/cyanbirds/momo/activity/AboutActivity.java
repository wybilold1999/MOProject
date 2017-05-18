package com.cyanbirds.momo.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.manager.AppManager;
import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @Description:关于
 * @author wangyb
 * @Date:2015年7月13日下午2:21:46
 */
public class AboutActivity extends BaseActivity {

	private TextView mVersionInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
		setupViews();
		setupEvent();
		setupData();
	}

	/**
	 * 设置视图
	 */
	private void setupViews() {
		mVersionInfo = (TextView) findViewById(R.id.version_info);
	}

	/**
	 * 设置事件
	 */
	private void setupEvent() {
	}

	/**
	 * 设置数据
	 */
	private void setupData() {
		mVersionInfo.setText(getResources().getString(R.string.app_name)  + " "
				+ AppManager.getVersion());
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
