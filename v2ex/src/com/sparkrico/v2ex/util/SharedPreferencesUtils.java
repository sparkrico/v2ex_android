package com.sparkrico.v2ex.util;

import com.sparkrico.v2ex.NodeMenuFragment.OrderType;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * SharedPreferences ToolKits
 * 
 * @author xiecheng(sparkrico@yahoo.com.cn)
 * 
 */
public class SharedPreferencesUtils {

	private static final String NAME = "v2ex";
	private static final String NODE_CACHE_DATETIME = "node_cache_datetime";
	private static final String TOPICS_LAST_UPDATE_DATETIME = "topics_last_update_datetime";

	private static final String THEME_TYPE = "theme_type";

	private static final String NODE_LIST_TYPE_KEY = "node_list_type";

	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
	}

	public static void putNodeCacheDateTime(Context context, String str) {
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putString(NODE_CACHE_DATETIME, str);
		editor.commit();
	}

	public static String getNodeCacheDateTime(Context context) {
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getString(NODE_CACHE_DATETIME, "");
	}

	public static void putTopicsLastUpdateDateTime(Context context, String str) {
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putString(TOPICS_LAST_UPDATE_DATETIME, str);
		editor.commit();
	}

	public static String getTopicsLastUpdateDateTime(Context context) {
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getString(TOPICS_LAST_UPDATE_DATETIME, "");
	}

	public static int getNodeListType(Context context) {
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getInt(NODE_LIST_TYPE_KEY,
				OrderType.HOT.ordinal());
	}

	public static void setNodeListType(Context context, int type) {
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putInt(NODE_LIST_TYPE_KEY, type);
		editor.commit();
	}

	public static int getThemeType(Context context) {
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getInt(THEME_TYPE, 0);
	}

	public static void setThemeType(Context context, int type) {
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putInt(THEME_TYPE, type);
		editor.commit();
	}
}
