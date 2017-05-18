package com.cyanbirds.momo.eventtype;

/**
 * 作者：wangyb
 * 时间：2016/11/1 14:58
 * 描述：小米授权登陆的事件
 */
public class XMEvent {
	public String xmOAuthResults;//授权登陆之后获取的xmOAuthResults，用于上传服务器

	public XMEvent(String result) {
		this.xmOAuthResults = result;
	}
}
