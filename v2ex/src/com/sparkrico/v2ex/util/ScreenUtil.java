package com.sparkrico.v2ex.util;

import android.content.Context;
import android.util.DisplayMetrics;

import com.sparkrico.v2ex.model.MemberMini;

public class ScreenUtil {

	public static boolean isLargeScreen(Context context){
		DisplayMetrics dm = new DisplayMetrics();  
		dm = context.getResources().getDisplayMetrics();  
		return dm.density > 1.5;
	}
	
	/**
	 * ¸ù¾Ý³ß´çÑ¡Ôñavatar size
	 * @param isLarge
	 * @param member
	 * @return
	 */
	public static String choiceAvatarSize(boolean isLarge, MemberMini member){
		return isLarge?member.getAvatar_large():
			member.getAvatar_normal();
	}
}
