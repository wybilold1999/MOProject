package com.cyanbirds.momo.view;

import com.cyanbirds.momo.activity.base.IBasePresenter;
import com.cyanbirds.momo.activity.base.IBaseView;
import com.cyanbirds.momo.entity.ClientUser;

public interface IUserLoginLogOut {

    interface View extends IBaseView<Presenter> {
        void loginLogOutSuccess(ClientUser clientUser);
    }

    interface Presenter extends IBasePresenter {
        void onUserLogin(String account, String pwd);
        void onWXLogin(String code);
        void onQQLogin(String token, String openId);
        void onRegist(ClientUser clientUser, String channel);
        void onLogOut();
        void onHWLogin(String hwOpenId);
    }

    interface CheckSmsCodeView extends IBaseView<CheckSmsCodePresenter> {
        void checkSmsCode(int checkCode);
    }

    interface CheckSmsCodePresenter extends IBasePresenter {
        void checkSmsCode(String code, String phoneNum, int mPhoneType);
    }
}
