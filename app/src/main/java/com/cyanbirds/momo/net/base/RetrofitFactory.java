package com.cyanbirds.momo.net.base;

import android.support.annotation.NonNull;

import com.cyanbirds.momo.BuildConfig;
import com.cyanbirds.momo.config.AppConstants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Meiji on 2017/4/22.
 */

public class RetrofitFactory {

    private volatile static Retrofit retrofit;
    private volatile static OkHttpClient okHttpClient;

    @NonNull
    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (RetrofitFactory.class) {
                if (retrofit == null) {

                    retrofit = new Retrofit.Builder()
                            .baseUrl(AppConstants.BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static OkHttpClient initOkHttpClient() {
        if (okHttpClient == null) {
            synchronized (RetrofitFactory.class) {
                if (okHttpClient == null) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true);
                    // Log 拦截器
                    if (BuildConfig.DEBUG) {
                        builder = initInterceptor(builder);
                    }
                    okHttpClient = builder.build();
                }
            }
        }
        return okHttpClient;
    }

    private static OkHttpClient.Builder initInterceptor(OkHttpClient.Builder builder) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(interceptor);
        return builder;
    }
}
