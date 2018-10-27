package com.cyanbirds.momo.manager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.OSS;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.entity.FederationToken;
import com.cyanbirds.momo.entity.IMessage;
import com.cyanbirds.momo.utils.PreferencesUtils;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mmkv.MMKV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.cyanbirds.momo.utils.PreferencesUtils.SETTINGS_CURRENT_CITY;
import static com.cyanbirds.momo.utils.PreferencesUtils.SETTINGS_RL_ACCOUNT;
import static com.cyanbirds.momo.utils.PreferencesUtils.SETTINGS_RL_FACE_LOCAL;
import static com.cyanbirds.momo.utils.PreferencesUtils.SETTINGS_RL_IS_LOGIN;
import static com.cyanbirds.momo.utils.PreferencesUtils.SETTINGS_RL_PASSWORD;
import static com.cyanbirds.momo.utils.PreferencesUtils.SETTINGS_RL_SESSIONID;
import static com.cyanbirds.momo.utils.PreferencesUtils.SETTINGS_RL_USER_MOBILE;
import static com.cyanbirds.momo.utils.PreferencesUtils.SETTINGS_RL_USER_USER_NAME;
import static com.cyanbirds.momo.utils.PreferencesUtils.SETTINGS_SEX;

/**
 * 
 * @ClassName:AppManager
 * @Description:APP管理类
 * @Author:wangyb
 * @Date:2015年5月4日下午5:30:07
 *
 */
public class AppManager {

	/** Android 应用上下文 */
	private static Context mContext = null;
	/** 包名 */
	public static String pkgName = "com.cyanbirds.momo";
	/** 用户登录信息 */
	private static ClientUser mClientUser;
	/**
	 * 点击通知栏消息，进入Main之后，需要所有的通知栏消息，
	 * 有些推送进入main之后，来的比较晚，用这个变量判断
	 * 是否通过通知栏进入main，是的话，其他后来的消息就
	 * 取消通知栏
	 */
	public static boolean isMsgClick = false;
	/** Activity管理对象 **/
	private static ActivityManager mActivityManager = null;
	/** 运行的任务集体 **/
	private static List<RunningTaskInfo> tasksInfo = null;
	/**
	 * 进入聊天界面当前聊天联系人id
	 */
	public static String currentChatTalker = null;

	private static IWXAPI sIWX_PAY_API;
	private static IWXAPI sIWXAPI;

	private static MMKV sMMKV;

	private static ExecutorService mExecutorService;

	private static OSS mOSS;

	private static FederationToken mOSSFederationToken;

	public static FederationToken getFederationToken() {
		if(mOSSFederationToken==null){
			mOSSFederationToken=new FederationToken();
		}
		return mOSSFederationToken;
	}

	public static FederationToken setFederationToken(FederationToken token) {
		return mOSSFederationToken = token;
	}

	public static OSS getOSS() {
		return mOSS;
	}

	public static void setOSS(OSS oss) {
		mOSS = oss;
	}

	public static ExecutorService getExecutorService() {
		if (mExecutorService == null) {
			mExecutorService = Executors.newFixedThreadPool(3);
		}
		return mExecutorService;
	}

	/**
	 * 设置用户信息
	 * 
	 * @param user
	 */
	public static void setClientUser(ClientUser user) {
		mClientUser = user;
	}

	/**
	 * 获取用户信息
	 * 
	 * @return
	 */
	public static ClientUser getClientUser() {
		return mClientUser;
	}

	/**
	 * 获取包名
	 * 
	 * @return
	 */
	public static String getPackageName() {
		return pkgName;
	}

	/**
	 * 返回上下文对象
	 * 
	 * @return
	 */
	public static Context getContext() {
		return mContext;
	}

	/**
	 * 设置上下文对象
	 * 
	 * @param context
	 */
	public static void setContext(Context context) {
		mContext = context;
		pkgName = context.getPackageName();
	}

	/**
	 * 获取应用程序版本名称
	 * 
	 * @return 版本名称
	 */
	public static String getVersion() {
		String version = "0.0.0";
		if (mContext == null) {
			return version;
		}
		try {
			PackageInfo packageInfo = mContext.getPackageManager()
					.getPackageInfo(getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return version;
	}

	/**
	 * 获取应用版本号
	 * 
	 * @return 版本号
	 */
	public static int getVersionCode() {
		int code = 1;
		if (mContext == null) {
			return code;
		}
		try {
			PackageInfo packageInfo = mContext.getPackageManager()
					.getPackageInfo(getPackageName(), 0);
			code = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return code;
	}

	/**
	 * 获取设备ID
	 * 
	 * @return
	 */
	public static String getDeviceId() {
		String deviceId = "";
		try {
			// 获取ID
			TelephonyManager tm = (TelephonyManager) mContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			String id = tm.getDeviceId();
			// 获取mac地址
			String macSerial = null;
			String str = "";
			Process pp = Runtime.getRuntime().exec(
					"cat /sys/class/net/wlan0/address ");
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			for (; null != str;) {
				str = input.readLine();
				if (str != null) {
					macSerial = str.trim();
					break;
				}
			}
			deviceId = "Android_" + id + "_" + macSerial;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return deviceId;
	}

	/**
	 * 获取设备名称
	 * @return
     */
	public static String getDeviceName(){
		return new Build().MANUFACTURER;
	}

	/**
	 * 获取系统版本
	 * @return
     */
	public static String getDeviceSystemVersion(){
		return Build.VERSION.RELEASE;
	}

	/**
	 * 判断手机是否处于锁屏状态
	 * 
	 * @param context
	 * @return true 表示处于锁屏状态，
	 */
	public static boolean isScreenLocked(Context context) {
		boolean flag = false;
		KeyguardManager mKeyguardManager = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 判断程序是否在前台运行
	 * @param context
	 * @return
	 */
	public static boolean isAppIsInBackground(Context context) {
		boolean isInBackground = true;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
			List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
				//前台程序
				if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					for (String activeProcess : processInfo.pkgList) {
						if (activeProcess.equals(context.getPackageName())) {
							isInBackground = false;
						}
					}
				}
			}
		} else {
			List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
			ComponentName componentInfo = taskInfo.get(0).topActivity;
			if (componentInfo.getPackageName().equals(context.getPackageName())) {
				isInBackground = false;
			}
		}

		return isInBackground;
	}

	/**
	 * 判断3C是否在栈顶
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static boolean MyActivityIsTop(Context context) {
		boolean isTop = false;
		if (null == mActivityManager) {
			mActivityManager = ((ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE));
		}
		if (null != mActivityManager) {
			tasksInfo = mActivityManager.getRunningTasks(1);
			if (null != tasksInfo && tasksInfo.size() > 0) {
				if (pkgName.equals(tasksInfo.get(0).topActivity
						.getPackageName())) {// 3c应用在栈顶
					isTop = true;
				}
			}
		}
		return isTop;
	}

	/**
	 * 判断应用是否已经启动
	 *
	 * @param context     一个context
	 * @param packageName 要判断应用的包名
	 * @return boolean
	 */
	public static boolean isAppAlive(Context context, String packageName) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager
				.getRunningAppProcesses();
		for (int i = 0; i < processInfos.size(); i++) {
			if (processInfos.get(i).processName.equals(packageName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取当前栈顶的activity的名称
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getTopActivity(Context context) {
		if (null == mActivityManager) {
			mActivityManager = ((ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE));
		}
		if (null != mActivityManager) {
			tasksInfo = mActivityManager.getRunningTasks(1);
			if (null != tasksInfo && tasksInfo.size() > 0) {
				// return
				// (tasksInfo.get(0).topActivity.getShortClassName()).toString();
				return tasksInfo.get(0).topActivity.getClassName();
			} else {
				return null;
			}
		}
		return null;
	}

	/**
	 * 现在消息通知
	 *
	 * @param message
	 */
	public static void showNotification(IMessage message) {
		if (checkNeedMsgNotify(message)) {
			NotificationManager.getInstance().showMessageNotification(
					message);
		}
	}

	private static boolean checkNeedMsgNotify(IMessage message) {
		if (TextUtils.isEmpty(AppManager.currentChatTalker) ||
				!message.talker.equals(String.valueOf(AppManager.currentChatTalker))) {
			return true;
		}
		return false;
	}

	public static void installApk(File file) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		mContext.startActivity(intent);
	}

	/**
	 * 获取UUID作为群聊唯一标识
	 * 
	 * @return
	 */
	public static String getUUID() {
		String strUUID = java.util.UUID.randomUUID().toString();
		return strUUID;
	}

	/**
	 * 是否登录判读
	 * 
	 * @return
	 */
	public static boolean isLogin() {
		/*if (getClientUser() == null || TextUtils.isEmpty(getClientUser().userId)
				|| !PreferencesUtils.getIsLogin(mContext)) {
			return false;
		}

		return true;*/
		if (getClientUser() == null || TextUtils.isEmpty(getClientUser().userId)
				|| !sMMKV.decodeBool(SETTINGS_RL_IS_LOGIN, false)) {
			return false;
		}

		return true;
	}

	/**
	 * 获取需要上传到oss路径
	 *
	 * @param
	 * @return
	 */
	public static String getOSSFacePath() {
		String path = "tan_love/img/tl_" + getUUID() + ".jpg";
		return path;
	}

	public static IWXAPI getIWXAPI() {
		return sIWXAPI;
	}

	public static void setIWXAPI(IWXAPI IWXAPI) {
		sIWXAPI = IWXAPI;
	}

	public static IWXAPI getIWX_PAY_API() {
		return sIWX_PAY_API;
	}

	public static void setIWX_PAY_API(IWXAPI IWX_PAY_API) {
		sIWX_PAY_API = IWX_PAY_API;
	}

	public static MMKV getMMKV() {
		return sMMKV;
	}

	public static void setMMKV(MMKV MMKV) {
		sMMKV = MMKV;
	}

	public static String getProcessName(int pid) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
			String processName = reader.readLine();
			if (!TextUtils.isEmpty(processName)) {
				processName = processName.trim();
			}
			return processName;
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 设置用户信息
	 */
	public static void setUserInfo() {
		try {
			/*String userId = PreferencesUtils.getAccount(mContext);
			String mobile = PreferencesUtils.getUserMobile(mContext);
			String pwd = PreferencesUtils.getPassword(mContext);
			String userName = PreferencesUtils.getUserName(mContext);
			String face_local = PreferencesUtils.getFaceLocal(mContext);
			String sessionId = PreferencesUtils.getSessionid(mContext);
			ClientUser clientUser = new ClientUser();
			clientUser.userId = userId;
			clientUser.mobile = mobile;
			clientUser.userPwd = pwd;
			clientUser.user_name = userName;
			clientUser.face_local = face_local;
			clientUser.sessionId = sessionId;
			clientUser.currentCity = PreferencesUtils.getCurrentCity(mContext);
			clientUser.sex = PreferencesUtils.getSettingsSex(mContext);*/
			String userId = sMMKV.decodeString(SETTINGS_RL_ACCOUNT, "");
			String mobile = sMMKV.decodeString(SETTINGS_RL_USER_MOBILE, "");
			String pwd = sMMKV.decodeString(SETTINGS_RL_PASSWORD, "");
			String userName = sMMKV.decodeString(SETTINGS_RL_USER_USER_NAME, "");
			String face_local = sMMKV.decodeString(SETTINGS_RL_FACE_LOCAL, "");
			String sessionId = sMMKV.decodeString(SETTINGS_RL_SESSIONID, "");
			ClientUser clientUser = new ClientUser();
			clientUser.userId = userId;
			clientUser.mobile = mobile;
			clientUser.userPwd = pwd;
			clientUser.user_name = userName;
			clientUser.face_local = face_local;
			clientUser.sessionId = sessionId;
			clientUser.currentCity = sMMKV.decodeString(SETTINGS_CURRENT_CITY, "");
			clientUser.sex = sMMKV.decodeString(SETTINGS_SEX, "");
			setClientUser(clientUser);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存用户信息
	 */
	public static void saveUserInfo() {
		try {
			sMMKV.encode(SETTINGS_RL_ACCOUNT, getClientUser().userId);
			sMMKV.encode(SETTINGS_RL_PASSWORD, getClientUser().userPwd);
			sMMKV.encode(SETTINGS_RL_FACE_LOCAL, getClientUser().face_local);
			sMMKV.encode(SETTINGS_RL_USER_MOBILE, getClientUser().mobile);
			sMMKV.encode(SETTINGS_RL_USER_USER_NAME, getClientUser().user_name);
			sMMKV.encode(SETTINGS_RL_SESSIONID, getClientUser().sessionId);
			sMMKV.encode(SETTINGS_RL_IS_LOGIN, true);
			sMMKV.encode(SETTINGS_CURRENT_CITY, getClientUser().currentCity);
			sMMKV.encode(SETTINGS_SEX, getClientUser().sex);
			/*PreferencesUtils.setAccount(mContext, getClientUser().userId);
			PreferencesUtils.setPassword(mContext, getClientUser().userPwd);
			PreferencesUtils.setFaceLocal(mContext, getClientUser().face_local);
			PreferencesUtils.setUserMobile(mContext, getClientUser().mobile);
			PreferencesUtils.setUserName(mContext, getClientUser().user_name);
			PreferencesUtils.setSessionId(mContext, getClientUser().sessionId);
			PreferencesUtils.setIsLogin(mContext, true);
			PreferencesUtils.setCurrentCity(mContext, getClientUser().currentCity);
			PreferencesUtils.setSettingsSex(mContext, getClientUser().sex);*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 释放数据库
	 */
	public static void release() {
	}

}
