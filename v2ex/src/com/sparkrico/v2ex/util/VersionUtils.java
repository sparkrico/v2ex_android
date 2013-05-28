package com.sparkrico.v2ex.util;

import android.os.Build;

public class VersionUtils {

	public static boolean OverHONEYCOMB() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}
}
