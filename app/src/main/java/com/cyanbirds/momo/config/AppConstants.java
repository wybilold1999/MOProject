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
	
	public static final String BASE_URL = "http://120.77.65.198/TanLoveServer/";
//	public static final String BASE_URL = "http://192.168.1.101/TanLoveServer/";

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
	public static final String YUNTONGXUN_ID = "8a216da856c131340156d6ea887f1466";
	public static final String YUNTONGXUN_TOKEN = "56cc98f3e9129eeb5352cc5d58f4edd1";

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
	public static final String SMS_INIT_KEY = "1705ac31c7ae6";
	public static final String SMS_INIT_SECRET = "334da8403952168a0bb65d8870ea35af";

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
