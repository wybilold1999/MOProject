package com.cyanbirds.momo.entity;


import java.io.Serializable;

/**
 * 
 * @Description:用户相册模型
 * @author wangyb
 * @Date:2015年8月9日下午6:55:36
 */
public class PictureModel implements Serializable{

	/** 图片id */
	public int id;
	/** 用户id */
	public int usersId;
	/** 性别 */
	public int sex;
	/** 用户生日 */
	public String birthday;
	/** 创建时间 */
	public String createTime;
	/** 图片大小 */
	public int size;
	/** 图片宽 */
	public int width;
	/** 图片高 */
	public int height;
	/** 图片地址 */
	public String path;
	/** 图片格式 */
	public String form;
	/** 用户头像 */
	public String faceUrl;
	/** 用户昵称 */
	public String nickname;
	/** 用户所在距离 */
	public Double distance;
	/** 用户类型 */
	public int UType;
	/** 是否会员 */
	public boolean isVip;
	/** 用户所在城市 */
	public String city;
	/** 年龄 */
	public String age;
	/** 星座 */
	public String constellation;

}
