package com.cyanbirds.momo.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.util.List;

/**
 * 
 * @ClassName:ExpressionGroup.java
 * @Description:在线表情分组
 * @author zxj
 * @Date:2015年6月12日下午1:46:49
 */
@Entity
public class ExpressionGroup {

	/** 存储数据库的id */
	@Id(autoincrement = true)
	public Long id;
	/** 表情组的代表图片路径（无前缀） */
	@Property
	@NotNull
	public String cover;
	/** 表情组id */
	@Property
	@NotNull
	public int id_pic_themes;
	/** 表情组名字 */
	@Property
	@NotNull
	public String name;
	/** 压缩包路径（无前缀） */
	@Property
	@NotNull
	public String zip;
	/** 单组表情信息 */
	@Transient
	public List<Expression> expressions;
	/** 是否已经下载 */
	@Transient
	public int status = ExpressionGroupStatus.NO_DOWNLOAD;
	/**下载进度*/
	@Transient
	public int progress;

	@Generated(hash = 1365131776)
	public ExpressionGroup(Long id, @NotNull String cover, int id_pic_themes,
			@NotNull String name, @NotNull String zip) {
		this.id = id;
		this.cover = cover;
		this.id_pic_themes = id_pic_themes;
		this.name = name;
		this.zip = zip;
	}

	@Generated(hash = 496454900)
	public ExpressionGroup() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCover() {
		return this.cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public int getId_pic_themes() {
		return this.id_pic_themes;
	}

	public void setId_pic_themes(int id_pic_themes) {
		this.id_pic_themes = id_pic_themes;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getZip() {
		return this.zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	/**
	 * 表情状态
	 */
	public class ExpressionGroupStatus {
		/** 未下载 */
		public static final int NO_DOWNLOAD = 0;
		/** 正在下载 */
		public static final int DOWNLOAD = 1;
		/** 已下载 */
		public static final int ALREADY_DOWNLOAD = 2;
	}

}
