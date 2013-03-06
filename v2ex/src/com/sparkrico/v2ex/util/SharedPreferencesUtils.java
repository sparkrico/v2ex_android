package com.sparkrico.v2ex.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * SharedPreferences ToolKits
 * @author xiecheng(sparkrico@yahoo.com.cn)
 *
 */
public class SharedPreferencesUtils {
	
	private static final String NAME = "v2ex";
	private static final String NODE_CACHE_DATETIME = "node_cache_datetime";
	
	public static SharedPreferences getSharedPreferences(Context context){
		return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
	}
	
	public static void putNodeCacheDateTime(Context context, String str){
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putString(NODE_CACHE_DATETIME, str);
		editor.commit();
	}
	
	public static String getNodeCacheDateTime(Context context){
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getString(NODE_CACHE_DATETIME, "");
	}
	
}
