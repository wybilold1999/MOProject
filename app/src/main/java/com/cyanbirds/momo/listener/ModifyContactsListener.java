package com.cyanbirds.momo.listener;


import com.cyanbirds.momo.entity.Contact;

import java.util.ArrayList;

/**
 * 
 * @ClassName:ModifyContactsLstener
 * @Description:通讯录修改监听
 * @author wangyb
 * @Date:2015年5月23日下午8:21:14
 *
 */
public class ModifyContactsListener {

	private static ModifyContactsListener mNoticeCenter;

	private ArrayList<OnDataChangedListener> mOnDataChangedListener;
	
	// singleton 確保只有一個實體
	private ModifyContactsListener() {
	}

	public static ModifyContactsListener getInstance() {
		if (null == mNoticeCenter) {
			mNoticeCenter = new ModifyContactsListener();
			mNoticeCenter.init();
		}
		return mNoticeCenter;
	}

	private void init() {
		mOnDataChangedListener = new ArrayList<OnDataChangedListener>();
	}

	// observe pattern
	public interface OnDataChangedListener {
		public void onDataChanged(Contact contact);
		public void onDeleteDataChanged(String userId);
		public void onAddDataChanged(Contact contact);
	}

	public void addOnDataChangedListener(OnDataChangedListener listener) {
		mOnDataChangedListener.add(listener);
	}

	public void removeOnDataChangedListener(OnDataChangedListener listener) {
		mOnDataChangedListener.remove(listener);
	}

	public void notifyDataChanged(Contact contact) {
		for (OnDataChangedListener listener : mOnDataChangedListener) {
			if (listener != null) {
				listener.onDataChanged(contact);
			}
		}
	}
	public void notifyDeleteDataChanged(String userId) {
		for (OnDataChangedListener listener : mOnDataChangedListener) {
			if (listener != null) {
				listener.onDeleteDataChanged(userId);
			}
		}
	}
	public void notifyAddDataChanged(Contact contact) {
		for (OnDataChangedListener listener : mOnDataChangedListener) {
			if (listener != null) {
				listener.onAddDataChanged(contact);
			}
		}
	}
}
