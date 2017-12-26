package com.cyanbirds.momo.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.adapter.PhotosAdapter;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.ui.widget.NoScrollGridView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者：wangyb
 * 时间：2016/12/15 17:29
 * 描述：
 */
public class PersonalPhotoActivity extends BaseActivity {

	@BindView(R.id.img_contents)
	NoScrollGridView mImgContents;

	private PhotosAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_photo);
		ButterKnife.bind(this);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
		initData();
	}

	private void initData() {
		List<String> imgUrls = getIntent().getStringArrayListExtra(ValueKey.IMAGE_URL);
		mAdapter = new PhotosAdapter(this, imgUrls , mImgContents);
		mImgContents.setAdapter(mAdapter);
	}
}
