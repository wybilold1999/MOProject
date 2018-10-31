package com.cyanbirds.momo.receiver;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.cyanbirds.momo.net.request.UploadTokenRequest;
import com.cyanbirds.momo.utils.PushMsgUtil;
import com.huawei.hms.support.api.push.PushReceiver;

import org.json.JSONObject;

/**
 * Created by wangyb on 2018/10/31
 */
public class HUAWEIPushRevicer extends PushReceiver {
    @Override
    public void onToken(Context context, String token, Bundle extras) {
        if (!TextUtils.isEmpty(token)) {
            new UploadTokenRequest().request("", "", token);
        }
    }

    //透传消息
    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        try {
            //CP可以自己解析消息内容，然后做相应的处理
            String content = new String(msg, "UTF-8");
            JSONObject jsonObject = new JSONObject(content);
            PushMsgUtil.getInstance().handlePushMsg(false, jsonObject.optString("msg_content"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //该方法会在设置标签、点击打开通知栏消息、点击通知栏上的按钮之后被调用
    public void onEvent(Context context, Event event, Bundle extras) {
        /*if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            String msgContent = "";
            String message = extras.getString(BOUND_KEY.pushMsgKey);
            JsonArray jsonArray = new JsonParser().parse(message).getAsJsonArray();
            if (jsonArray.get(0) != null) {
                JsonObject notifyObj = jsonArray.get(0).getAsJsonObject();
                int notifyId = notifyObj.get(BOUND_KEY.pushNotifyId).getAsInt();
                if (0 != notifyId) {
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(notifyId);
                }
            }
            if (jsonArray.size() == 2 && jsonArray.get(1) != null) {
                JsonObject msgObj = jsonArray.get(1).getAsJsonObject();
                msgContent = msgObj.get(BOUND_KEY.pushMsgKey).getAsString();
                PushMsgUtil.getInstance().handlePushMsg(false, msgContent);
            }
        }
        super.onEvent(context, event, extras);*/
    }
    @Override
    public void onPushState(Context context, boolean pushState) {
    }
}
