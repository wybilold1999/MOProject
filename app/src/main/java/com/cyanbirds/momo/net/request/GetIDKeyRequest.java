package com.cyanbirds.momo.net.request;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.cyanbirds.momo.config.AppConstants;
import com.cyanbirds.momo.entity.AllKeys;
import com.cyanbirds.momo.entity.IDKey;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.base.ResultPostExecute;
import com.cyanbirds.momo.utils.AESOperator;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by wangyb on 2017/5/17.
 * 描述：获取微信id
 */

public class GetIDKeyRequest extends ResultPostExecute<AllKeys> {

    public void request() {
        Call<ResponseBody> call = AppManager.getUserService().getIdKeys();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        parseJson(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        onErrorExecute("");
                    } finally {
                        response.body().close();
                    }
                } else {
                    onErrorExecute("");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                onErrorExecute("");
            }
        });
    }

    private void parseJson(String json){
        try {
            String decryptData = AESOperator.getInstance().decrypt(json);
            if (!TextUtils.isEmpty(decryptData)) {
                Gson gson = new Gson();
                AllKeys keys = gson.fromJson(decryptData, AllKeys.class);
                if (null != keys) {
                    onPostExecute(keys);
                } else {
                    onErrorExecute("");
                }
            } else {
                onErrorExecute("");
            }
        } catch (Exception e) {
            onErrorExecute("");
        }
    }

}
