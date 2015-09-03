package com.carrecorder.conf;

import android.annotation.SuppressLint;

@SuppressLint("SdCardPath") 
public class EnvConf {
	public static final String AUDIO_PATH_WEAK_LIGHT_1 = "/sdcard/Android/speedradio.wav";
	public static final String AUDIO_PATH_WEAK_LIGHT_2 = "/sdcard/Android/lightradio2.wav";
	public static final String AUDIO_PATH_SLOW_SPEED = "/sdcard/Android/lightrido.wav";
	public static final String AUDIO_PATH_NOTICE_1 = "/sdcard/Android/right.wav";
	public static final String AUDIO_PATH_NOTICE_2 = "/sdcard/Android/go.wav";
	
	public static final String NOTICE_SLOWDOWN_SEPPD = "车速太快，请注意减速行驶";
	public static final String NOTICE_WEAK_LIGHT_1 = "光线较暗请开灯";
	public static final String NOTICE_WEAK_LIGHT_2 = "光线较暗请立即开灯";
	public static final String NOTICE_WEAK_LIGHT_MUTIL_LINE = "光线较暗\n请立即开灯";
	public static final String NOTICE_GOOD_LIGHT = "光线良好";
	public static final String NOTICE_TUTORIAL_1 = "点击右侧按钮开始记录";
	public static final String NOTICE_TUTORIAL_2 = "开始监测";

	
	
}
