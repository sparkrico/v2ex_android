package com.sparkrico.v2ex.util;

import java.util.Comparator;
import java.util.Map;

/**
 * 
 * @author xiecheng(sparkrico@yahoo.com.cn)
 *
 */
public class ComparableNode implements Comparator<Map<String, String>> {

	private static final String KEY = "name";
	
	@Override
	public int compare(Map<String, String> lhs, Map<String, String> rhs) {
		return lhs.get(KEY).compareTo(rhs.get(KEY));
	}

	
}
