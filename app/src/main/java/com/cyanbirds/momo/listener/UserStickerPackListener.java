package com.cyanbirds.momo.listener;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @Description:用户贴图包更新
 * @author zxj
 * @Date:2015年9月28日下午4:07:05
 */
public class UserStickerPackListener {

	private static UserStickerPackListener mInstance;
	private static List<OnUserStickerPackListener> mListener;

	public static UserStickerPackListener getInstance() {
		if (mInstance == null) {
			mInstance = new UserStickerPackListener();
			mListener = new ArrayList<OnUserStickerPackListener>();
		}
		return mInstance;
	}

	public void addOnUserStickerPackListener(OnUserStickerPackListener listener) {
		mListener.add(listener);
	}

	public void removeOnUserStickerPackListener(
			OnUserStickerPackListener listener) {
		mListener.remove(listener);
	}

	public interface OnUserStickerPackListener {
		void onDeleteStickerPack(int packId);
	}

	public void notifyDeleteStickerPack(int packId) {
		if (mListener != null) {
			for (OnUserStickerPackListener l : mListener) {
				l.onDeleteStickerPack(packId);
			}
		}
	}

}
