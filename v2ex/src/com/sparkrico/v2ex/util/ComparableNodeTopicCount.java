package com.sparkrico.v2ex.util;

import java.util.Comparator;
import java.util.Map;

public class ComparableNodeTopicCount implements Comparator<Map<String, String>> {

	private static final String KEY = "topics";
	
	@Override
	public int compare(Map<String, String> lhs, Map<String, String> rhs) {
		return (Integer.parseInt(rhs.get(KEY))) - (Integer.parseInt(lhs.get(KEY)));
	}
	
}
