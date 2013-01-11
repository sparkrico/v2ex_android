package com.sparkrico.v2ex.util;

public class HtmlUtil {
	
	private static final String domain = "http://www.v2ex.com";

	public static String formatAtLink(String content){
		return content.replace("@<a href=\"", "@<a href=\""+domain);
	}
}
