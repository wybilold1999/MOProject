package com.cyanbirds.momo.net.request;

import android.support.v4.util.ArrayMap;

import com.cyanbirds.momo.CSApplication;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.entity.Contact;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.base.ResultPostExecute;
import com.cyanbirds.momo.utils.AESOperator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * 
 * @ClassName:ContactsRequest
 * @Description:请求通讯录
 * @Author:zxj
 * @Date:2015年5月7日下午2:09:50
 *
 */
public class ContactsRequest extends ResultPostExecute<List<Contact>> {

	public void request(final int pageNo, final int pageSize,
						final String gender, final String mUserScopeType) {
		ArrayMap<String, String> params = new ArrayMap<>();
		params.put("pageNo", String.valueOf(pageNo));
		params.put("pageSize", String.valueOf(pageSize));
		params.put("gender", gender);
		params.put("user_scope_type", mUserScopeType);
		Call<ResponseBody> call = AppManager.getUserService().getContactList(AppManager.getClientUser().sessionId, params);
		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
				if(response.isSuccessful()){
					try {
						parseJson(response.body().string());
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						response.body().close();
					}
				} else {
					onErrorExecute(CSApplication.getInstance()
							.getResources()
							.getString(R.string.network_requests_error));
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				onErrorExecute(CSApplication.getInstance()
						.getResources()
						.getString(R.string.network_requests_error));
			}
		});
	}

	private void parseJson(String json) {
		try {
			String decryptData = AESOperator.getInstance().decrypt(json);
			JsonObject obj = new JsonParser().parse(decryptData).getAsJsonObject();
			int code = obj.get("code").getAsInt();
			if (code == 1) {
				onErrorExecute(obj.get("msg").getAsString());
				return;
			} else if(code == 3){
				onErrorExecute(obj.get("msg").getAsString());
				return;
			}
			String strData = obj.get("data").getAsString();
			JsonArray data = new JsonParser().parse(strData).getAsJsonArray();
			List<Contact> userList = new ArrayList<>();
			for(int i = 0; i < data.size(); i++){
				Contact contact = new Contact();
				JsonObject jsonObject = data.get(i).getAsJsonObject();
				contact.userId = jsonObject.get("uid").getAsString();
				contact.user_name = jsonObject.get("nickname").getAsString();
				contact.face_url = jsonObject.get("faceUrl").getAsString();
				contact.signature = jsonObject.get("signature").getAsString();
				contact.sex = jsonObject.get("sex").getAsInt() == 1 ? Contact.Gender.MALE : Contact.Gender.FEMALE;
				contact.state_marry = jsonObject.get("emotionStatus").getAsString();
				contact.birthday = jsonObject.get("age").getAsString();
				contact.constellation = jsonObject.get("constellation").getAsString();
				contact.isFromAdd = false;
				userList.add(contact);
			}
			onPostExecute(userList);
		} catch (Exception e) {
			onErrorExecute(CSApplication.getInstance().getResources()
					.getString(R.string.data_parser_error));
		}
	}
}
