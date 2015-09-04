package com.carrecorder.utils;

import java.text.DecimalFormat;

public class Common {
	public static String formatDouble(double num) {
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(num);
	}
}
