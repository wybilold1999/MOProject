package com.cyanbirds.momo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.YuanFenModel;
import com.cyanbirds.momo.net.request.AddLoveRequest;
import com.cyanbirds.momo.net.request.GetYuanFenUserRequest;
import com.cyanbirds.momo.net.request.SendGreetRequest;
import com.cyanbirds.momo.utils.ToastUtil;
import com.stone.card.CardDataItem;
import com.stone.card.CardSlidePanel;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：wangyb
 * 时间：2016/9/18 20:07
 * 描述：卡片
 */
public class CardActivity extends BaseActivity implements CardSlidePanel.CardSwitchListener {

	@BindView(R.id.card_left_btn)
	Button mCardLeftBtn;
	@BindView(R.id.personal_btn)
	Button mPersonalBtn;
	@BindView(R.id.card_right_btn)
	Button mCardRightBtn;
	@BindView(R.id.image_slide_panel)
	CardSlidePanel mImageSlidePanel;

	private List<CardDataItem> dataList = new ArrayList<CardDataItem>();
	private List<YuanFenModel> models;
	private int curUserId;

	private int pageNo = 0;
	private int pageSize = 200;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card);
		ButterKnife.bind(this);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}

		setupEvent();
		setupData();
	}

	private void setupEvent() {
		mImageSlidePanel.setCardSwitchListener(this);
	}

	private void setupData() {
		CardDataItem dataItem = null;
		models = (List<YuanFenModel>) getIntent().getSerializableExtra(ValueKey.USER);
		for (YuanFenModel model : models) {
			dataItem = new CardDataItem();
			dataItem.userId = model.uid;
			dataItem.userName = model.nickname;
			dataItem.imagePath = model.faceUrl;
			dataItem.city = model.city;
			dataItem.age = model.age;
			dataItem.constellation = model.constellation;
			dataItem.distance = model.distance == null ? 0.00 : model.distance;
			dataItem.signature = model.signature;
			dataItem.pictures = model.pictures;
			dataList.add(dataItem);
		}
		mImageSlidePanel.fillData(dataList);

	}

	@Override
	public void onShow(int index) {
		curUserId = dataList.get(index).userId;
		if (index >= dataList.size() - 3) {
			//请求数据
			new GetYuanFenUserTask().request(pageNo, pageSize);
		}
	}

	@Override
	public void onCardVanish(int index, int type) {
		if (type == 1) {//向右滑
			new SenderGreetTask().request(String.valueOf(curUserId));
			new AddLoveRequest().request(String.valueOf(curUserId));
		}
	}

	@Override
	public void onItemClick(View cardImageView, int index) {
		Intent intent = new Intent(this, PersonalInfoActivity.class);
		intent.putExtra(ValueKey.USER_ID, String.valueOf(dataList.get(index).userId));
		startActivity(intent);
	}

	@OnClick({R.id.personal_btn})
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.personal_btn:
				Intent intent = new Intent(this, PersonalInfoActivity.class);
				intent.putExtra(ValueKey.USER_ID, String.valueOf(curUserId));
				startActivity(intent);
				break;
		}
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

	class GetYuanFenUserTask extends GetYuanFenUserRequest {
		@Override
		public void onPostExecute(List<YuanFenModel> yuanFenModels) {
			if (yuanFenModels != null && yuanFenModels.size() > 0) {
				for (YuanFenModel model : yuanFenModels) {
					CardDataItem dataItem = new CardDataItem();
					dataItem.userId = model.uid;
					dataItem.userName = model.nickname;
					dataItem.imagePath = model.faceUrl;
					dataItem.city = model.city;
					dataItem.age = model.age;
					dataItem.constellation = model.constellation;
					dataItem.distance = model.distance == null ? 0.00 : model.distance;
					dataItem.signature = model.signature;
					dataItem.pictures = model.pictures;
					dataList.add(dataItem);
				}
				mImageSlidePanel.fillData(dataList);
			}
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
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


	private void prepareDataList() {
		int num = imagePaths.length;

		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < num; i++) {
				CardDataItem dataItem = new CardDataItem();
				dataItem.userName = "name=" + i;
				dataItem.imagePath = imagePaths[i];
				dataItem.imageNum = (int) (Math.random() * 6);
				dataList.add(dataItem);
			}
		}
	}

	String[] imagePaths = new String[]{"http://file.ynet.com/2/1609/18/11747137.jpg",
			"http://file.ynet.com/2/1609/18/11747138.jpg", "http://file.ynet.com/2/1605/07/11271444.jpg",
			"http://file.ynet.com/2/1605/07/11271021.jpg", "http://jiangsu.china.com.cn/uploadfile/2016/0918/1474190769213910.jpg",
			"http://jiangsu.china.com.cn/uploadfile/2016/0918/1474190767625296.jpg",
			"http://c.hiphotos.baidu.com/baike/c0%3Dbaike92%2C5%2C5%2C92%2C30/sign=84e2412ad0160924c828aa49b56e5e9f/7acb0a46f21fbe09597ce5686b600c338744adaf.jpg",
			"http://h.hiphotos.baidu.com/baike/c0%3Dbaike80%2C5%2C5%2C80%2C26/sign=1430a2f37af0f736ccf344536b3cd87c/342ac65c1038534344a613ad9113b07ecb8088d5.jpg",
			"http://photocdn.sohu.com/20151225/mp50597254_1451035687527_10_th_fv23.jpeg",
			"http://imgsrc.baidu.com/forum/w%3D580/sign=034d5ea2bf389b5038ffe05ab534e5f1/f5b5c9ea15ce36d3e0a5c78c3cf33a87e850b1ed.jpg",
			"http://n1.itc.cn/img8/wb/smccloud/2015/05/07/143099871291046796.JPEG"};

}
