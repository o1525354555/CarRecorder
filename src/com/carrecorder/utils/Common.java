package com.carrecorder.utils;

import java.text.DecimalFormat;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.carrecorder.db.table.Record;

public class Common {
	public static String formatDouble(double num) {
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(num);
	}
	/**
	 * 匹配第一个符合要求的字符串返回
	 * @param str
	 * @param regEx
	 * @return
	 */
	public static String regularStr(String str,String regEx)
	{
		try{
			Pattern pat = Pattern.compile(regEx);
			Matcher mat = pat.matcher(str);
			mat.find();
			return mat.group(0);
		}catch(Exception e)
		{
			
		}
		return "";
	}
	@SuppressWarnings("unused")
	public static boolean hasElement(Vector<Record> showedRecords,Record element)
	{
		for (int i=0;i<showedRecords.size();i++)
		{
			if(showedRecords.get(i).getId()==element.getId())
				return true;
		}
		return false;
	}
}
