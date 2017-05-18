package com.cyanbirds.momo.net.request;

import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.AsyncTask;

import com.cyanbirds.momo.activity.LoginActivity;
import com.cyanbirds.momo.config.AppConstants;
import com.cyanbirds.momo.eventtype.XMEvent;
import com.cyanbirds.momo.utils.CheckUtil;
import com.xiaomi.account.openauth.XMAuthericationException;
import com.xiaomi.account.openauth.XiaomiOAuthConstants;
import com.xiaomi.account.openauth.XiaomiOAuthFuture;
import com.xiaomi.account.openauth.XiaomiOAuthResults;
import com.xiaomi.account.openauth.XiaomiOAuthorize;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

/**
 * 作者：wangyb
 * 时间：2016/11/12 11:34
 * 描述：获取小米token
 */
public class GetMiAccessTokenRequest extends AsyncTask<Void, Void, String>{

	private Context mContext;
	private XiaomiOAuthFuture<XiaomiOAuthResults> mFuture;

	public GetMiAccessTokenRequest(Context context,
								   XiaomiOAuthFuture<XiaomiOAuthResults> future) {
		mContext = context;
		mFuture = future;
	}

	@Override
	protected String doInBackground(Void... params) {
		String result = "";
		try {
			XiaomiOAuthResults results = mFuture.getResult();
			if (results.hasError()) {
				int errorCode = results.getErrorCode();
				result = results.getErrorMessage();
			} else {
				String accessToken = results.getAccessToken();
				String macKey = results.getMacKey();
				String macAlgorithm = results.getMacAlgorithm();
				result = accessToken + ";" + macKey + ";" + macAlgorithm;
			}
		} catch (IOException e1) {
			// error
		} catch (OperationCanceledException e1) {
			// user cancel
		} catch (XMAuthericationException e1) {
			// error
		}
		return result;
	}

	@Override
	protected void onPostExecute(String s) {
		EventBus.getDefault().post(new XMEvent(s));
	}
}
