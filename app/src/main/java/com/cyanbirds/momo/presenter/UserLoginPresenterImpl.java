package com.cyanbirds.momo.presenter;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.cyanbirds.momo.CSApplication;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.helper.IMChattingHelper;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.IUserApi;
import com.cyanbirds.momo.net.base.RetrofitFactory;
import com.cyanbirds.momo.utils.CheckUtil;
import com.cyanbirds.momo.utils.JsonUtils;
import com.cyanbirds.momo.utils.PreferencesUtils;
import com.cyanbirds.momo.view.IUserLoginLogOut;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.umeng.analytics.MobclickAgent;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class UserLoginPresenterImpl implements IUserLoginLogOut.Presenter {

    private WeakReference<IUserLoginLogOut.View> mViewWeakReference;

    public UserLoginPresenterImpl(IUserLoginLogOut.View view) {
        mViewWeakReference = new WeakReference<>(view);
    }

    @Override
    public void onUserLogin(String account, String pwd) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("account", account);
        params.put("pwd", pwd);
        params.put("deviceName", AppManager.getDeviceName());
        params.put("appVersion", String.valueOf(AppManager.getVersionCode()));
        params.put("systemVersion", AppManager.getDeviceSystemVersion());
        params.put("deviceId", AppManager.getDeviceId());
        params.put("channel", CheckUtil.getAppMetaData(CSApplication.getInstance(), "UMENG_CHANNEL"));
        params.put("province", PreferencesUtils.getCurrentProvince(CSApplication.getInstance()));
        params.put("latitude", PreferencesUtils.getLatitude(CSApplication.getInstance()));
        params.put("longitude", PreferencesUtils.getLongitude(CSApplication.getInstance()));
        params.put("loginTime", String.valueOf(PreferencesUtils.getLoginTime(CSApplication.getInstance())));
        params.put("currentCity", PreferencesUtils.getCurrentCity(CSApplication.getInstance()));
        Observable<ClientUser> observable = RetrofitFactory.getRetrofit().create(IUserApi.class)
                .userLogin(AppManager.getClientUser().sessionId, params)
                .subscribeOn(Schedulers.io())
                .flatMap(responseBody -> {
                    ClientUser clientUser = JsonUtils.parseClientUser(responseBody.string());
                    return Observable.just(clientUser);
                })
                .doOnNext(clientUser -> {
                    MobclickAgent.onProfileSignIn(String.valueOf(clientUser.userId));
                    clientUser.currentCity = PreferencesUtils.getCurrentCity(CSApplication.getInstance());
                    clientUser.latitude = PreferencesUtils.getLatitude(CSApplication.getInstance());
                    clientUser.longitude = PreferencesUtils.getLongitude(CSApplication.getInstance());
                    AppManager.setClientUser(clientUser);
                    AppManager.saveUserInfo();
                    AppManager.getClientUser().loginTime = System.currentTimeMillis();
                    PreferencesUtils.setLoginTime(CSApplication.getInstance(), System.currentTimeMillis());
                    IMChattingHelper.getInstance().sendInitLoginMsg();
                })
                .observeOn(AndroidSchedulers.mainThread());
        if (mViewWeakReference.get() != null) {
            observable.as(mViewWeakReference.get().bindAutoDispose());
        }
        observable.subscribe(clientUser -> {
                    if (mViewWeakReference.get() != null) {
                        mViewWeakReference.get().loginLogOutSuccess(clientUser);
                    } else {
                        return;
                    }
                },
                throwable -> {
                    if (mViewWeakReference.get() != null) {
                        if (throwable instanceof NullPointerException) {
                            mViewWeakReference.get().loginLogOutSuccess(null);
                        } else {
                            mViewWeakReference.get().onShowNetError();
                        }
                    } else {
                        return;
                    }
                });
    }

    @Override
    public void onWXLogin(String code) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("code", code);
        params.put("device_name", AppManager.getDeviceName());
        params.put("channel", CheckUtil.getAppMetaData(CSApplication.getInstance(), "UMENG_CHANNEL"));
        params.put("platform", "weichat");
        params.put("version", String.valueOf(AppManager.getVersionCode()));
        params.put("os_version", AppManager.getDeviceSystemVersion());
        params.put("device_id", AppManager.getDeviceId());
        params.put("province", PreferencesUtils.getCurrentProvince(CSApplication.getInstance()));
        params.put("latitude", PreferencesUtils.getLatitude(CSApplication.getInstance()));
        params.put("longitude", PreferencesUtils.getLongitude(CSApplication.getInstance()));
        params.put("loginTime", String.valueOf(PreferencesUtils.getLoginTime(CSApplication.getInstance())));
        params.put("currentCity", PreferencesUtils.getCurrentCity(CSApplication.getInstance()));
        if (!TextUtils.isEmpty(AppManager.getClientUser().sex)) {
            params.put("sex", AppManager.getClientUser().sex);
        }
        Observable<ClientUser> observable = RetrofitFactory.getRetrofit().create(IUserApi.class)
                .wxLogin(AppManager.getClientUser().sessionId, params)
                .subscribeOn(Schedulers.io())
                .flatMap(responseBody -> {
                    ClientUser clientUser = JsonUtils.parseClientUser(responseBody.string());
                    return Observable.just(clientUser);
                })
                .doOnNext(clientUser -> {
                    MobclickAgent.onProfileSignIn(String.valueOf(clientUser.userId));
                    clientUser.currentCity = PreferencesUtils.getCurrentCity(CSApplication.getInstance());
                    clientUser.latitude = PreferencesUtils.getLatitude(CSApplication.getInstance());
                    clientUser.longitude = PreferencesUtils.getLongitude(CSApplication.getInstance());
                    AppManager.setClientUser(clientUser);
                    AppManager.saveUserInfo();
                    AppManager.getClientUser().loginTime = System.currentTimeMillis();
                    PreferencesUtils.setLoginTime(CSApplication.getInstance(), System.currentTimeMillis());
                    IMChattingHelper.getInstance().sendInitLoginMsg();
                })
                .observeOn(AndroidSchedulers.mainThread());
        if (mViewWeakReference.get() != null) {
            observable.as(mViewWeakReference.get().bindAutoDispose());
        }
        observable.subscribe(clientUser -> {
                    if (mViewWeakReference.get() != null) {
                        mViewWeakReference.get().loginLogOutSuccess(clientUser);
                    } else {
                        return;
                    }
                },
                throwable -> {
                    if (mViewWeakReference.get() != null) {
                        if (throwable instanceof NullPointerException) {
                            mViewWeakReference.get().loginLogOutSuccess(null);
                        } else {
                            mViewWeakReference.get().onShowNetError();
                        }
                    } else {
                        return;
                    }
                });

    }

    @Override
    public void onQQLogin(String token, String openId) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("token", token);
        params.put("openid", openId);
        params.put("channel", CheckUtil.getAppMetaData(CSApplication.getInstance(), "UMENG_CHANNEL"));
        params.put("platform", "qq");
        params.put("device_name", AppManager.getDeviceName());
        params.put("version", String.valueOf(AppManager.getVersionCode()));
        params.put("os_version", AppManager.getDeviceSystemVersion());
        params.put("device_id", AppManager.getDeviceId());
        params.put("province", PreferencesUtils.getCurrentProvince(CSApplication.getInstance()));
        params.put("latitude", PreferencesUtils.getLatitude(CSApplication.getInstance()));
        params.put("longitude", PreferencesUtils.getLongitude(CSApplication.getInstance()));
        params.put("loginTime", String.valueOf(PreferencesUtils.getLoginTime(CSApplication.getInstance())));
        params.put("currentCity", PreferencesUtils.getCurrentCity(CSApplication.getInstance()));
        if (!TextUtils.isEmpty(AppManager.getClientUser().sex)) {
            params.put("sex", AppManager.getClientUser().sex);
        }
        Observable<ClientUser> observable = RetrofitFactory.getRetrofit().create(IUserApi.class)
                .qqLogin(AppManager.getClientUser().sessionId, params)
                .subscribeOn(Schedulers.io())
                .flatMap(responseBody -> {
                    ClientUser clientUser = JsonUtils.parseClientUser(responseBody.string());
                    return Observable.just(clientUser);
                })
                .doOnNext(clientUser -> {
                    MobclickAgent.onProfileSignIn(String.valueOf(clientUser.userId));
                    clientUser.currentCity = PreferencesUtils.getCurrentCity(CSApplication.getInstance());
                    clientUser.latitude = PreferencesUtils.getLatitude(CSApplication.getInstance());
                    clientUser.longitude = PreferencesUtils.getLongitude(CSApplication.getInstance());
                    AppManager.setClientUser(clientUser);
                    AppManager.saveUserInfo();
                    AppManager.getClientUser().loginTime = System.currentTimeMillis();
                    PreferencesUtils.setLoginTime(CSApplication.getInstance(), System.currentTimeMillis());
                    IMChattingHelper.getInstance().sendInitLoginMsg();
                })
                .observeOn(AndroidSchedulers.mainThread());
        if (mViewWeakReference.get() != null) {
            observable.as(mViewWeakReference.get().bindAutoDispose());
        }
        observable.subscribe(clientUser -> {
                    if (mViewWeakReference.get() != null) {
                        mViewWeakReference.get().loginLogOutSuccess(clientUser);
                    } else {
                        return;
                    }
                },
                throwable -> {
                    if (mViewWeakReference.get() != null) {
                        if (throwable instanceof NullPointerException) {
                            mViewWeakReference.get().loginLogOutSuccess(null);
                        } else {
                            mViewWeakReference.get().onShowNetError();
                        }
                    } else {
                        return;
                    }
                });
    }

    @Override
    public void onRegist(ClientUser clientUser, String channel) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("upwd", clientUser.userPwd);
        params.put("nickname", clientUser.user_name);
        params.put("phone", clientUser.mobile);
        params.put("sex", clientUser.sex);
        params.put("age", String.valueOf(clientUser.age));
        params.put("channel", channel);
        params.put("regDeviceName", AppManager.getDeviceName());
        params.put("regVersion", String.valueOf(AppManager.getVersionCode()));
        params.put("regPlatform", "phone");
        params.put("reg_the_way", "0");
        params.put("regSystemVersion", AppManager.getDeviceSystemVersion());
        params.put("deviceId", AppManager.getDeviceId());
        if (!TextUtils.isEmpty(clientUser.currentCity)) {
            params.put("currentCity", clientUser.currentCity);
        } else {
            params.put("currentCity", "");
        }
        params.put("province", PreferencesUtils.getCurrentProvince(CSApplication.getInstance()));
        params.put("latitude", PreferencesUtils.getLatitude(CSApplication.getInstance()));
        params.put("longitude", PreferencesUtils.getLongitude(CSApplication.getInstance()));
        Observable<ClientUser> observable = RetrofitFactory.getRetrofit().create(IUserApi.class)
                .userRegister(params)
                .subscribeOn(Schedulers.io())
                .flatMap(responseBody -> {
                    ClientUser user = JsonUtils.parseClientUser(responseBody.string());
                    return Observable.just(user);
                })
                .doOnNext(user -> {
                    MobclickAgent.onProfileSignIn(String.valueOf(user.userId));
                    user.currentCity = clientUser.currentCity;
                    user.latitude = PreferencesUtils.getLatitude(CSApplication.getInstance());
                    user.longitude = PreferencesUtils.getLongitude(CSApplication.getInstance());
                    AppManager.setClientUser(user);
                    AppManager.saveUserInfo();
                    AppManager.getClientUser().loginTime = System.currentTimeMillis();
                    PreferencesUtils.setLoginTime(CSApplication.getInstance(), System.currentTimeMillis());
                    IMChattingHelper.getInstance().sendInitLoginMsg();
                })
                .observeOn(AndroidSchedulers.mainThread());
        if (mViewWeakReference.get() != null) {
            observable.as(mViewWeakReference.get().bindAutoDispose());
        }
        observable.subscribe(mClientUser -> {
                    if (mViewWeakReference.get() != null) {
                        mViewWeakReference.get().loginLogOutSuccess(mClientUser);
                    } else {
                        return;
                    }
                },
                throwable -> {
                    if (mViewWeakReference.get() != null) {
                        if (throwable instanceof NullPointerException) {
                            mViewWeakReference.get().loginLogOutSuccess(null);
                        } else {
                            mViewWeakReference.get().onShowNetError();
                        }
                    } else {
                        return;
                    }
                });
    }

    @Override
    public void onLogOut() {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("deviceName", AppManager.getDeviceName());
        params.put("appVersion", String.valueOf(AppManager.getVersionCode()));
        params.put("systemVersion", AppManager.getDeviceSystemVersion());
        params.put("deviceId", AppManager.getDeviceId());
        Observable<ClientUser> observable = RetrofitFactory.getRetrofit().create(IUserApi.class)
                .userLogout(AppManager.getClientUser().sessionId, params)
                .subscribeOn(Schedulers.io())
                .map(responseBody -> {
                    ClientUser clientUser = new ClientUser();
                    JsonObject obj = new JsonParser().parse(responseBody.string()).getAsJsonObject();
                    int code = obj.get("code").getAsInt();
                    if (code == 1) {
                        clientUser.age = 1;//用age代表code
                    } else {
                        clientUser.age = 0;
                    }
                    return clientUser;
                })
                .observeOn(AndroidSchedulers.mainThread());
        if (mViewWeakReference.get() != null) {
            observable.as(mViewWeakReference.get().bindAutoDispose());
        }
        observable.subscribe(mClientUser -> {
                    if (mViewWeakReference.get() != null) {
                        mViewWeakReference.get().loginLogOutSuccess(mClientUser);
                    } else {
                        return;
                    }
                },
                throwable -> {
                    if (mViewWeakReference.get() != null) {
                        mViewWeakReference.get().onShowNetError();
                    } else {
                        return;
                    }
                });
    }

    @Override
    public void onHWLogin(String hwOpenId) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("channel", CheckUtil.getAppMetaData(CSApplication.getInstance(), "UMENG_CHANNEL"));
        params.put("platform", "HW");
        params.put("openId", hwOpenId);
        params.put("device_name", AppManager.getDeviceName());
        params.put("version", String.valueOf(AppManager.getVersionCode()));
        params.put("os_version", AppManager.getDeviceSystemVersion());
        params.put("device_id", AppManager.getDeviceId());
        params.put("province", PreferencesUtils.getCurrentProvince(CSApplication.getInstance()));
        params.put("latitude", PreferencesUtils.getLatitude(CSApplication.getInstance()));
        params.put("longitude", PreferencesUtils.getLongitude(CSApplication.getInstance()));
        params.put("loginTime", String.valueOf(PreferencesUtils.getLoginTime(CSApplication.getInstance())));
        params.put("currentCity", PreferencesUtils.getCurrentCity(CSApplication.getInstance()));
        if (!TextUtils.isEmpty(AppManager.getClientUser().sex)) {
            params.put("sex", AppManager.getClientUser().sex);
        }
        Observable<ClientUser> observable = RetrofitFactory.getRetrofit().create(IUserApi.class)
                .hwLogin(AppManager.getClientUser().sessionId, params)
                .subscribeOn(Schedulers.io())
                .flatMap(responseBody -> {
                    ClientUser clientUser = JsonUtils.parseClientUser(responseBody.string());
                    return Observable.just(clientUser);
                })
                .doOnNext(clientUser -> {
                    MobclickAgent.onProfileSignIn(String.valueOf(clientUser.userId));
                    clientUser.currentCity = PreferencesUtils.getCurrentCity(CSApplication.getInstance());
                    clientUser.latitude = PreferencesUtils.getLatitude(CSApplication.getInstance());
                    clientUser.longitude = PreferencesUtils.getLongitude(CSApplication.getInstance());
                    AppManager.setClientUser(clientUser);
                    AppManager.saveUserInfo();
                    AppManager.getClientUser().loginTime = System.currentTimeMillis();
                    PreferencesUtils.setLoginTime(CSApplication.getInstance(), System.currentTimeMillis());
                    IMChattingHelper.getInstance().sendInitLoginMsg();
                })
                .observeOn(AndroidSchedulers.mainThread());
        if (mViewWeakReference.get() != null) {
            observable.as(mViewWeakReference.get().bindAutoDispose());
        }
        observable.subscribe(clientUser -> {
                    if (mViewWeakReference.get() != null) {
                        mViewWeakReference.get().loginLogOutSuccess(clientUser);
                    } else {
                        return;
                    }
                },
                throwable -> {
                    if (mViewWeakReference.get() != null) {
                        if (throwable instanceof NullPointerException) {
                            mViewWeakReference.get().loginLogOutSuccess(null);
                        } else {
                            mViewWeakReference.get().onShowNetError();
                        }
                    } else {
                        return;
                    }
                });
    }

    @Override
    public void detachView() {
        if (mViewWeakReference != null) {
            mViewWeakReference.clear();
            mViewWeakReference = null;
        }
    }
}
