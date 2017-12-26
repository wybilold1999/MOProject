package com.cyanbirds.momo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.adapter.BigramHeaderAdapter;
import com.cyanbirds.momo.adapter.ContactsAdapter;
import com.cyanbirds.momo.db.ContactSqlManager;
import com.cyanbirds.momo.entity.Contact;
import com.cyanbirds.momo.listener.ModifyContactsListener;
import com.cyanbirds.momo.listener.ModifyContactsListener.OnDataChangedListener;
import com.cyanbirds.momo.net.request.ContactsRequest;
import com.cyanbirds.momo.ui.widget.CircularProgress;
import com.cyanbirds.momo.utils.PinYinUtil;
import com.cyanbirds.momo.utils.StringUtil;
import com.cyanbirds.momo.utils.ToastUtil;
import com.eowise.recyclerview.stickyheaders.OnHeaderClickListener;
import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.eowise.recyclerview.stickyheaders.StickyHeadersItemDecoration;
import com.lb.recyclerview_fast_scroller.RecyclerViewFastScroller;
import com.umeng.analytics.MobclickAgent;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 
 * @ClassName:ContactsFragment
 * @Description:通讯录
 * @Author:zxj
 * @Date:2015年5月5日下午7:20:35
 *
 */
public class ContactsFragment extends Fragment implements OnHeaderClickListener, OnDataChangedListener {

	private View rootView;
	private RecyclerView mRecyclerView;
	private ContactsAdapter mAdapter;
	private RecyclerViewFastScroller mFastScroller;
	private CircularProgress mProgress;
	private LinearLayoutManager layoutManager;

	private List<Contact> mContacts;
	private List<String> items;
	private BigramHeaderAdapter mBigramHeaderAdapter;
	private StickyHeadersItemDecoration top;

	private int pageIndex = 1;
	private int pageSize = 30;
	private String GENDER = ""; //空表示查询和自己性别相反的用户
	/**
	 * 0:同城 1：缘分 2：颜值  -1:就是全国
	 */
	private String mUserScopeType = "1";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_contacts, null);
			setupViews();
			setupEvent();
			setupData();
			setHasOptionsMenu(true);
		}
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
				R.string.tab_contacts);
		return rootView;
	}


	/**
	 * 设置视图
	 */
	private void setupViews() {
		mProgress = (CircularProgress) rootView.findViewById(R.id.progress_bar);
		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
		layoutManager = new LinearLayoutManager(
				getActivity(), LinearLayoutManager.VERTICAL, false);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(layoutManager);
		mFastScroller = (RecyclerViewFastScroller) rootView
				.findViewById(R.id.fastscroller);
		mFastScroller.setViewsToUse(
				R.layout.recycler_view_fast_scroller__fast_scroller,
				R.id.fastscroller_bubble, R.id.fastscroller_handle);
	}

	/**
	 * 设置事件
	 */
	private void setupEvent() {
		ModifyContactsListener.getInstance().addOnDataChangedListener(this);
	}

	/**
	 * 设置数据
	 */
	private void setupData() {
		mContacts = new ArrayList<>();
		items = new ArrayList<>();
		mAdapter = new ContactsAdapter(getActivity(), mContacts);

		mBigramHeaderAdapter = new BigramHeaderAdapter(items);

		top = new StickyHeadersBuilder().setAdapter(mAdapter)
				.setRecyclerView(mRecyclerView)
				.setStickyHeadersAdapter(mBigramHeaderAdapter)
				.setOnHeaderClickListener(this).build();

		mRecyclerView.addItemDecoration(top);
		mRecyclerView.setAdapter(mAdapter);
		mFastScroller.setRecyclerView(mRecyclerView);

		mProgress.setVisibility(View.VISIBLE);
		List<Contact> contactList = ContactSqlManager.getInstance(getActivity()).queryAllContacts();
		if (contactList != null && contactList.size() > 0) {
			getNotifyContact(contactList);
		} else {
			new ContactsTask().request(pageIndex, pageSize, GENDER, mUserScopeType);
		}
	}

	@Override
	public void onDataChanged(Contact contact) {

	}

	@Override
	public void onDeleteDataChanged(String userId) {
		for (int i = 0; i < mContacts.size(); i++) {
			if (userId.equals(mContacts.get(i).userId)) {
				mContacts.remove(i);
				items.remove(i);
				mAdapter.notifyItemRemoved(i);
				mAdapter.notifyItemChanged(mAdapter
						.getItemCount() - 1);

				// 移除剩下的拼音首字母
//				items.remove(items.size() - 1);
//				items.add("");
//				mContactsAdapter.notifyItemRangeRemoved(items.size() - 1, 1);
//				mContactsAdapter.notifyItemRangeChanged(items.size() - 1, 1);
				break;
			}
		}
	}

	@Override
	public void onAddDataChanged(Contact contact) {
		contact.isFromAdd = true;
		List<Contact> contactList = new ArrayList<>();
		contactList.addAll(mContacts);
		contactList.add(contact);
		mContacts.clear();
		items.clear();
		getNotifyContact(contactList);
	}

	/**
	 * 获取通讯录
	 */
	class ContactsTask extends ContactsRequest {
		@Override
		public void onPostExecute(List<Contact> result) {
			if (null != result && result.size() > 0) {
				mContacts.clear();
				items.clear();
				getNotifyContact(result);
			}
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
			mProgress.setVisibility(View.GONE);
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 更新通讯录
	 */
	public void getNotifyContact(List<Contact> contacts) {
		if (contacts != null && contacts.size() != 0) {
			mContacts.addAll(contacts);
			// 排序
			Collections.sort(mContacts, new Comparator<Contact>() {
				@Override
				public int compare(Contact lhs, Contact rhs) {
					String l = sortLetter(lhs);
					String r = sortLetter(rhs);
					String s1 = l;
					String s2 = r;
					return Collator.getInstance(Locale.US).compare(s1, s2);
				}
			});
		}
		/** 遍历出非字母通讯录 */
		List<Contact> list = new ArrayList<>();
		for (int i = 0; i < mContacts.size(); i++) {
			Contact c = mContacts.get(i);
			String l = sortLetter(c);
			if (!StringUtil.checkLetter(l)) {
				list.add(c);
			}
		}
		/** 吧非字母通讯录添加到集合后面 */
		mContacts.removeAll(list);
		mContacts.addAll(list);
		/** 得到通讯录拼音集合 */
		for (int i = 0; i < mContacts.size(); i++) {
			Contact c = mContacts.get(i);
			String l = sortLetter(c);
			if (!StringUtil.checkLetter(l)) {
				l = "#";
			}
			items.add(l);
		}
		ContactSqlManager.getInstance(getActivity()).inserContacts(mContacts);
		mProgress.setVisibility(View.GONE);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 回去需要排序的字母
	 * 
	 * @param c
	 */
	public String sortLetter(Contact c) {
		String l;
		if (!TextUtils.isEmpty(c.rename)
				&& !TextUtils.isEmpty(c.conRemarkPYShort)) {
			l = c.conRemarkPYShort;
		} else if (!TextUtils.isEmpty(c.nickname)
				&& !TextUtils.isEmpty(c.pyInitial)) {
			l = c.pyInitial;
		} else if (!TextUtils.isEmpty(c.user_name)) {
			l = Character.toString(PinYinUtil.getPinYin(c.user_name).charAt(0));
		} else {
			l = c.userId;
		}
		c.conRemarkPYShort = l;
		return l;
	}


	@Override
	public void onHeaderClick(View header, long headerId) {

	}


	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getName());
		if (mRecyclerView != null) {
			mFastScroller.setRecyclerView(mRecyclerView);
		}
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
