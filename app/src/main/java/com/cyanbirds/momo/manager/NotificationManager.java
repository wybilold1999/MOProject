package com.cyanbirds.momo.manager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.db.ConversationSqlManager;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.entity.IMessage;
import com.cyanbirds.momo.receiver.NotificationReceiver;
import com.cyanbirds.momo.utils.PreferencesUtils;

/**
 * 通知管理
 * Created by Administrator on 2016/3/14.
 */
public class NotificationManager {

    public static NotificationManager mInstance;
    public static Context mContext;

    public static NotificationManager getInstance() {
        if (mInstance == null) {
            mInstance = new NotificationManager(AppManager.getContext());
        }

        return mInstance;
    }

    private NotificationManager(Context context) {
        mContext = context;
    }

    /**
     * 显示消息通知
     *
     * @param message
     */
    public void showMessageNotification(IMessage message) {
        try {
            Notification notification = getNotification(message);
            if (notification != null) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat
                        .from(mContext);
                notification.flags = (Notification.FLAG_AUTO_CANCEL | notification.flags);
                notificationManager.notify(0, notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置消息通知数据
     *
     * @param message
     * @return
     */
    public Notification getNotification(IMessage message) {
        if (message == null)
            return null;
        cancelNotification();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                mContext);
        String title;
        String content;
        int unReadNum = ConversationSqlManager.getInstance(mContext).getAnalyticsUnReadConversation();
        // 显示消息详情
        if (PreferencesUtils.getShowMessageInfo(mContext)) {
            if (TextUtils.isEmpty(message.sender_name)) {
                title = mContext.getResources().getString(R.string.app_name);
            } else {
                title = message.sender_name;
            }
            content = getTickerText(message);
        } else {
            int unContactNum = ConversationSqlManager
                    .getInstance(mContext).getConversationUnReadNum();
            title = mContext.getResources().getString(R.string.app_name);
            content = String.format(
                    mContext.getResources().getString(R.string.notification_tips), unContactNum, unReadNum);
        }
        builder.setTicker(title + "\n" + message.content);
        // 设置通知内容的标题
        builder.setContentTitle(title);
        builder.setContentText(content);
        // 设置打开通知，该通知自动消失
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        // 是否震动
        if (PreferencesUtils.getNoticeShock(mContext)) {
            builder.setVibrate(new long[]{300, 100, 300, 100});
        }
        // 是否显示消息声音
        if (PreferencesUtils.getNoticeVoice(mContext)) {
            builder.setDefaults(Notification.DEFAULT_SOUND);
        }
        builder.setNumber(unReadNum);
        // 通知时间
        builder.setWhen(System.currentTimeMillis());

        ClientUser clientUser = new ClientUser();
        clientUser.user_name = title;
        clientUser.userId = message.sender;

        Intent intent = new Intent(mContext, NotificationReceiver.class);
        intent.putExtra(ValueKey.USER, clientUser);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 设置通知将要启动程序的intent
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        return notification;
    }

    /**
     * 获取显示的内容
     *
     * @param message
     * @return
     */
    public final String getTickerText(IMessage message) {
        if (message.msgType == IMessage.MessageType.TEXT) {
            return message.content;
        } else if (message.msgType == IMessage.MessageType.VOICE) {
            return mContext.getResources().getString(R.string.voice_symbol);
        } else if (message.msgType == IMessage.MessageType.IMG) {
            return mContext.getResources().getString(R.string.image_symbol);
        } else if (message.msgType == IMessage.MessageType.LOCATION) {
            return mContext.getResources().getString(R.string.location_symbol);
        }
        return "";
    }

    /**
     * 关闭通知
     */
    public void cancelNotification() {
        NotificationManagerCompat.from(mContext).cancelAll();
    }

}
