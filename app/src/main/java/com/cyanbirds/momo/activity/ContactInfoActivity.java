package com.cyanbirds.momo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.adapter.PhotosAdapter;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.db.ContactSqlManager;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.entity.Contact;
import com.cyanbirds.momo.listener.ModifyContactsListener;
import com.cyanbirds.momo.net.request.GetUserPictureRequest;
import com.cyanbirds.momo.ui.widget.NoScrollGridView;
import com.cyanbirds.momo.utils.ToastUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * 
 * @ClassName:ContactInfoActivity.java
 * @Description:通讯录信息
 * @author Administrator
 * @Date:2015年5月19日上午9:33:28
 *
 */
public class ContactInfoActivity extends BaseActivity implements OnClickListener {

	private SimpleDraweeView mPortrait;
	private TextView mUserName;
	private TextView mUserId;
	private TextView mAge;
	private TextView mConstellation;
	private TextView mSignatrue;
	private FancyButton mSendMessage;
	private FancyButton mAddFriend;
	private NoScrollGridView mGridView;
	private PhotosAdapter mAdapter;

	private Contact mContact;// 平台通讯录
	private String imagePath;
	private int pageNo = 1;
	private int pageSize = 6;
	private String from;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_info);
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
		mPortrait = (SimpleDraweeView) findViewById(R.id.portrait);
		mUserName = (TextView) findViewById(R.id.user_name);
		mUserId = (TextView) findViewById(R.id.user_id);
		mAge = (TextView) findViewById(R.id.age);
		mConstellation = (TextView) findViewById(R.id.constellation);
		mSignatrue = (TextView) findViewById(R.id.signatrue);
		mSendMessage = (FancyButton) findViewById(R.id.send_message);
		mAddFriend = (FancyButton) findViewById(R.id.add_friend);
		mGridView = (NoScrollGridView) findViewById(R.id.img_contents);
	}

	/**
	 * 设置事件
	 */
	private void setupEvent() {
		mSendMessage.setOnClickListener(this);
		mPortrait.setOnClickListener(this);
		mAddFriend.setOnClickListener(this);
	}

	/**
	 * 设置数据
	 */
	private void setupData() {
		mContact = (Contact) getIntent().getSerializableExtra(
				ValueKey.CONTACT);
		from = getIntent().getStringExtra(ValueKey.FROM_ACTIVITY);
		if (!TextUtils.isEmpty(from)) {
			mAddFriend.setVisibility(View.VISIBLE);
		} else {
			mAddFriend.setVisibility(View.GONE);
		}
		if (mContact != null) {
			new GetUserPicTask().request(mContact.userId, pageNo, pageSize);
			setContactInfo(mContact);
		}
	}

	class GetUserPicTask extends GetUserPictureRequest {

		@Override
		public void onPostExecute(List<String> strings) {
			if (strings != null && strings.size() > 0) {
				mGridView.setVisibility(View.VISIBLE);
				mAdapter = new PhotosAdapter(ContactInfoActivity.this, strings, mGridView);
				mGridView.setAdapter(mAdapter);
			} else {
				mGridView.setVisibility(View.GONE);
			}
		}

		@Override
		public void onErrorExecute(String error) {
		}

	}


	/**
	 * 设置通讯录信息
	 */
	private void setContactInfo(Contact contact) {
		imagePath = contact.face_url;
		mPortrait.setImageURI(Uri.parse(contact.face_url));
		if (contact.sex.equals(Contact.Gender.FEMALE)) {
			mUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.mipmap.contact_female, 0);
		} else {
			mUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.mipmap.contact_male, 0);
		}
		if (!TextUtils.isEmpty(contact.user_name)) {
			mUserName.setText(contact.user_name);
		}
		if (!TextUtils.isEmpty(contact.userId)) {
			mUserId.setText("ID:" + contact.userId);
		}
		if (!TextUtils.isEmpty(contact.birthday)) {
			mAge.setText(contact.birthday);
		}
		if (!TextUtils.isEmpty(contact.constellation)) {
			mConstellation.setText(contact.constellation);
		}
		if (!TextUtils.isEmpty(contact.signature)) {
			mSignatrue.setText(contact.signature);
		}
	}


	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
			case R.id.send_message:
				ClientUser clientUser = new ClientUser();
				clientUser.userId = mContact.userId;
				clientUser.user_name = mContact.user_name;
				clientUser.face_url = mContact.face_url;
				intent.putExtra(ValueKey.USER, clientUser);
				intent.setClass(this, ChatActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				this.finish();
				break;
			case R.id.add_friend:
				ModifyContactsListener.getInstance().notifyAddDataChanged(mContact);
				ModifyContactsListener.getInstance().notifyDataChanged(mContact);
				ToastUtil.showMessage(R.string.action_add_friends_success);
				finish();
				break;
			case R.id.portrait:
				intent.setClass(this, PhotoViewActivity.class);
				intent.putExtra(ValueKey.IMAGE_URL, imagePath);
				startActivity(intent);
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (TextUtils.isEmpty(from)) {
			getMenuInflater().inflate(R.menu.contact_info_menu, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_delete_friends) {
			ContactSqlManager.getInstance(this).deleteContactById(mContact.userId);
			ModifyContactsListener.getInstance().notifyDeleteDataChanged(mContact.userId);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
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
