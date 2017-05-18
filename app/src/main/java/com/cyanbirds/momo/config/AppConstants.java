package com.cyanbirds.momo.config;

import com.xiaomi.account.openauth.XiaomiOAuthConstants;

/**
 * 
 * @ClassName:Constants
 * @Description:定义全局常量
 * @Author:wangyb
 * @Date:2015年5月12日上午9:26:45
 *
 */
public class AppConstants {
	
	public static final String BASE_URL = "http://120.77.65.198/MOLoveServer/";
//	public static final String BASE_URL = "http://192.168.1.101/MOLoveServer/";

	/**
	 * 密码加密密匙
	 */
	public static final String SECURITY_KEY = "ABCD1234abcd5678";

	/**
	 * 请求位置的高德web api的key
	 */
	public static final String WEB_KEY = "d64c0240c9790d4c56498b152a4ca193";

	/**
	 *容联云IM
	 */
	public static final String YUNTONGXUN_ID = "8aaf07085c1ab70d015c1b4c9ceb0049";
	public static final String YUNTONGXUN_TOKEN = "104176ee7192c50c3b062c2b23280bf7";

	/**
	 * QQ登录的appid和appkey
	 */
	public static final String mAppid = "1105589350";

	/**
	 * 微信登录
	 */
	public static final String WEIXIN_ID = "wx67e05e39a35f64ec";

	/**
	 * 短信
	 */
	public static final String SMS_INIT_KEY = "1df9989ba62c6";
	public static final String SMS_INIT_SECRET = "731d4eaafa4f01e82a60375c864ebf64";

	/**
	 * 小米推送appid
	 */
	public static final String MI_PUSH_APP_ID = "2882303761517524610";
	/**
	 * 小米推送appkey
	 */
	public static final String MI_PUSH_APP_KEY = "5721752438610";

	public static final String MI_ACCOUNT_REDIRECT_URI = "http://www.cyanbirds.cn";

	public static final int[] MI_SCOPE = new int[]{XiaomiOAuthConstants.SCOPE_PROFILE, XiaomiOAuthConstants.SCOPE_OPEN_ID};

	/**
	 * 阿里图片节点
	 */
	public static final String OSS_IMG_ENDPOINT = "http://real-love-server.img-cn-shenzhen.aliyuncs.com/";

	public static final String WX_PAY_PLATFORM = "wxpay";

	public static final String ALI_PAY_PLATFORM = "alipay";

	public static final String MZ_APP_ID = "110446";
	public static final String MZ_APP_KEY = "0e29617af61244ae9ac57c19083d7bc7";

}
