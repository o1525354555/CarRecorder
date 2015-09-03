package com.carrecorder.test;

import android.content.Context;
import junit.*;
import junit.framework.TestCase;

import com.carrecorder.utils.CopyFileFromAssets;

public class TestCopyFileFromAssets extends TestCase{
	
	private void testCopy(Context context) {
		String path = context.getFilesDir().getAbsolutePath();
		String name = "go.wav";
		CopyFileFromAssets.copy(context, name, path, name);
	}
}
