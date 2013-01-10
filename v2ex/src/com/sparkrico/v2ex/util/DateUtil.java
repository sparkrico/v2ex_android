package com.sparkrico.v2ex.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

public class DateUtil {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

	public static String formatDate(long d){
		return sdf.format(new Date(d));
	}
	
	public static String timeAgo(long d){
		 PrettyTime p = new PrettyTime(new Locale("ZH_CN"));
		 return p.format(new Date(d*1000));
	}
}
