package com.rj.soft.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.rj.soft.global.Cookie;

/**
 * Cookie工具类
 * 
 * @author cjy
 * @since 2018年2月5日
 * @version 1.0
 * 
 */
public class CookieUtils {

	public static Cookie getCookie(File file) {
		Cookie cookie = new Cookie();
		try {
			Properties props = new Properties();
			InputStream is = new FileInputStream(file);
			props.load(is);
			cookie.setRemUser(props.getProperty("RemUser"));
			cookie.setRemPass(props.getProperty("RemPass"));
			cookie.setUserName(props.getProperty("RemUserName"));
			cookie.setPassWord(props.getProperty("RemPassWord"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cookie;
	}

}
