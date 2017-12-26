package com.cyanbirds.momo.utils;

import android.text.TextUtils;

import java.util.ArrayList;


/**
 * 
 * @ClassName:PinYin
 * @Description:汉字转拼音工具类
 * @author Administrator
 * @Date:2015年5月18日下午2:32:47
 *
 */
public class PinYinUtil {

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static String getInitialPinYin(String input) {
		try {
			if (TextUtils.isEmpty(input)) {
				return "";
			}
			char[] s = input.toCharArray();
			StringBuilder sbinitial = new StringBuilder();
			for (char c : s) {
				ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(
						String.valueOf(c));
				StringBuilder sb = new StringBuilder();
				if (tokens != null && tokens.size() > 0) {
					for (HanziToPinyin.Token token : tokens) {
						if (HanziToPinyin.Token.PINYIN == token.type) {
							sb.append(token.target);
						} else {
							sb.append(token.source);
						}
					}
				}
				if (TextUtils.isEmpty(sb.toString().toLowerCase()))
					continue;
				sbinitial.append(sb.toString().toLowerCase().substring(0, 1));
			}
			return sbinitial.toString().toUpperCase();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 汉字转拼音
	 * 
	 * @param input
	 * @return
	 */
	public static String getPinYin(String input) {
		try {
			if (TextUtils.isEmpty(input)) {
				return "";
			}
			ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(input);
			StringBuilder sb = new StringBuilder();
			if (tokens != null && tokens.size() > 0) {
				for (HanziToPinyin.Token token : tokens) {
					if (HanziToPinyin.Token.PINYIN == token.type) {
						sb.append(token.target);
					} else {
						sb.append(token.source);
					}
				}
			}
			return sb.toString().toLowerCase();
		} catch (Exception e) {
			return "";
		}
	}
}
