package com.cyanbirds.momo.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 作者：wangyb
 * 时间：2017/3/26 16:58
 * 描述：
 */
@Entity
public class LocationModel {
	@Id(autoincrement = true)
	public Long id;
	@Property
	@NotNull
	@Unique
	public String userId;
	@Property
	@NotNull
	public String latitude;
	@Property
	@NotNull
	public String longitude;
	@Generated(hash = 1256513859)
	public LocationModel(Long id, @NotNull String userId, @NotNull String latitude,
			@NotNull String longitude) {
		this.id = id;
		this.userId = userId;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	@Generated(hash = 536868411)
	public LocationModel() {
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
	public String getLatitude() {
		return this.latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return this.longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
}
