package com.cyanbirds.momo.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

/**
 * 
 * @ClassName:Contact
 * @Description:通讯录实体
 * @Author:wangyb
 * @Date:2015年5月7日下午3:22:24
 *
 */
@Entity
public class Contact implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id(autoincrement = true)
	public Long id;
	/** 用户ID **/
	@Property
	@NotNull
	@Unique
	public String userId;
	/** 昵称 **/
	@Property
	public String nickname;
	/** 用户名 **/
	@Property
	public String user_name;
	/** 生日 **/
	@Property
	public String birthday;
	/** 头像URL **/
	@Property
	public String face_url;
	/** 性别[MALE、FEMALE] **/
	@Property
	public String sex;
	/** 个性签名 **/
	@Property
	public String signature;
	/** 备注昵称 **/
	@Property
	public String rename;
	/** 昵称拼音首字母 */
	@Property
	public String pyInitial;
	/** 备注首字母拼音 */
	@Property
	public String conRemarkPYShort;
	@Property
	public String state_marry;
	@Property
	public String constellation;
	@Property
	public boolean isFromAdd;

	@Generated(hash = 1192323034)
	public Contact(Long id, @NotNull String userId, String nickname,
				   String user_name, String birthday, String face_url, String sex,
				   String signature, String rename, String pyInitial, String conRemarkPYShort,
				   String state_marry, String constellation, boolean isFromAdd) {
		this.id = id;
		this.userId = userId;
		this.nickname = nickname;
		this.user_name = user_name;
		this.birthday = birthday;
		this.face_url = face_url;
		this.sex = sex;
		this.signature = signature;
		this.rename = rename;
		this.pyInitial = pyInitial;
		this.conRemarkPYShort = conRemarkPYShort;
		this.state_marry = state_marry;
		this.constellation = constellation;
		this.isFromAdd = isFromAdd;
	}

	@Generated(hash = 672515148)
	public Contact() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getUser_name() {
		return this.user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getBirthday() {
		return this.birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getFace_url() {
		return this.face_url;
	}

	public void setFace_url(String face_url) {
		this.face_url = face_url;
	}

	public String getSex() {
		return this.sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getSignature() {
		return this.signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getRename() {
		return this.rename;
	}

	public void setRename(String rename) {
		this.rename = rename;
	}

	public String getPyInitial() {
		return this.pyInitial;
	}

	public void setPyInitial(String pyInitial) {
		this.pyInitial = pyInitial;
	}

	public String getConRemarkPYShort() {
		return this.conRemarkPYShort;
	}

	public void setConRemarkPYShort(String conRemarkPYShort) {
		this.conRemarkPYShort = conRemarkPYShort;
	}

	public String getState_marry() {
		return this.state_marry;
	}

	public void setState_marry(String state_marry) {
		this.state_marry = state_marry;
	}

	public String getConstellation() {
		return this.constellation;
	}

	public void setConstellation(String constellation) {
		this.constellation = constellation;
	}

	public boolean getIsFromAdd() {
		return this.isFromAdd;
	}

	public void setIsFromAdd(boolean isFromAdd) {
		this.isFromAdd = isFromAdd;
	}

	/** 性别 **/
	public class Gender implements Serializable {
		private static final long serialVersionUID = 1L;
		/** 男 **/
		public static final String MALE = "MALE";
		/** 女 **/
		public static final String FEMALE = "FEMALE";
	}
}
