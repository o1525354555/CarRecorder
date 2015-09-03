package com.carrecorder.utils.time;

public class TimeUtil {
	/*
	 * 格式化时间
	 */
	public static String format(int i) {
		String s = i + "";
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}
}
