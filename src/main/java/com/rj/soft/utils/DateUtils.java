package com.rj.soft.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期/时间工具类
 * 
 * @author cjy
 * @since 2018年2月5日
 * @version 1.0
 * 
 */
public class DateUtils {

	/**
	 * 获取当前时间，e.g：yyyy-MM-dd HH:mm:ss
	 * 
	 * @return
	 */
	public static String getNowDate() {
		Date currentTime = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(currentTime);
	}

	/**
	 * 获取当前时间戳
	 * 
	 * @param millis
	 * @return
	 */
	public static String getCurrentTime(long millis) {
		return new Timestamp(millis).toString();
	}

}
