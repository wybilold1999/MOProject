package com.cyanbirds.momo.net.request;
import com.cyanbirds.momo.CSApplication;
import com.cyanbirds.momo.R;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.base.ResultPostExecute;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2016-05-03 10:38 GMT+8
 * @email 395044952@qq.com
 */
public class AddLoveRequest extends ResultPostExecute<Boolean> {
    public void request(final String loveId){
        Call<ResponseBody> call = AppManager.getLoveService().addLove(AppManager.getClientUser().sessionId, loveId);
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

    private void parseJson(String json){
        try {
            JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
            int code = obj.get("code").getAsInt();
            if(code == 0){
                onPostExecute(obj.get("data").getAsBoolean());
            } else {
                onErrorExecute("");
            }
        } catch (Exception e) {
            onErrorExecute("");
        }
    }
}
