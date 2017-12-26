package com.cyanbirds.momo.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;

/**
 * 
 * @ClassName:Expression.java
 * @Description:在线表情
 * @author wangyb
 * @Date:2015年6月12日下午1:51:42
 */
@Entity
public class Expression {

	/** 存取在数据库中的id*/
	@Id(autoincrement = true)
	public Long id;
	/** 表情文字描述 */
	@Property
	@NotNull
	public String note;
	/** 表情图片id */
	@Property
	@NotNull
	public String pic_id;
	/** 表情主题 */
	@Property
	@NotNull
	public String theme;
	/** 表情主题id */
	@Property
	@NotNull
	public int theme_id;
	/** 表情的图片类型后缀 */
	@Property
	@NotNull
	public String type;
	/** 表情服务器路径（无前缀） */
	@Property
	@NotNull
	public String url;
	@Generated(hash = 1540775386)
	public Expression(Long id, @NotNull String note, @NotNull String pic_id,
			@NotNull String theme, int theme_id, @NotNull String type, @NotNull String url) {
		this.id = id;
		this.note = note;
		this.pic_id = pic_id;
		this.theme = theme;
		this.theme_id = theme_id;
		this.type = type;
		this.url = url;
	}
	@Generated(hash = 1211687862)
	public Expression() {
	}
	public Long getId() {
		return this.id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNote() {
		return this.note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getPic_id() {
		return this.pic_id;
	}
	public void setPic_id(String pic_id) {
		this.pic_id = pic_id;
	}
	public String getTheme() {
		return this.theme;
	}
	public void setTheme(String theme) {
		this.theme = theme;
	}
	public int getTheme_id() {
		return this.theme_id;
	}
	public void setTheme_id(int theme_id) {
		this.theme_id = theme_id;
	}
	public String getType() {
		return this.type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUrl() {
		return this.url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
