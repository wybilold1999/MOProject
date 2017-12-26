package com.cyanbirds.momo.net.request;

import com.cyanbirds.momo.CSApplication;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.entity.ExpressionGroup;
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
 * @ClassName:ExpressionRequest.java
 * @Description:在线表情请求
 * @author zxj
 * @Date:2015年6月12日上午11:37:21
 */
public class ExpressionRequest extends ResultPostExecute<List<ExpressionGroup>> {

	public void request() {
		Call<ResponseBody> call = AppManager.getUserService().getExpressionGroup();
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

	/**
	 * 解析json
	 * 
	 * @param json
	 */
	private void parseJson(String json) {
		try {
			String decryptData = AESOperator.getInstance().decrypt(json);
			JsonObject obj = new JsonParser().parse(decryptData).getAsJsonObject();
			int code = obj.get("code").getAsInt();
			if (code != 0) {
				onErrorExecute(CSApplication.getInstance().getResources()
						.getString(R.string.data_load_error));
				return;
			}
			List<ExpressionGroup> groups = null;
			String data = obj.get("data").getAsString();
			JsonArray jsonArray = new JsonParser().parse(data).getAsJsonArray();
			if (jsonArray != null && jsonArray.size() > 0) {
				groups = new ArrayList<>();
				for (int i = 0; i < jsonArray.size(); i++) {
					ExpressionGroup expressionGroup = new ExpressionGroup();
					JsonObject object = jsonArray.get(i).getAsJsonObject();
					expressionGroup.cover = object.get("cover").getAsString();
					expressionGroup.id_pic_themes = object.get("id").getAsInt();
					expressionGroup.name = object.get("name").getAsString();
					expressionGroup.zip = object.get("zip").getAsString();
					groups.add(expressionGroup);
				}
			}
			onPostExecute(groups);
		} catch (Exception e) {
			onErrorExecute(CSApplication.getInstance().getResources()
					.getString(R.string.data_parser_error));
		}
	}

}
