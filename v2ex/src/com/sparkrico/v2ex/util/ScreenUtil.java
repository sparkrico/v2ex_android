package com.sparkrico.v2ex.util;

import android.content.Context;
import android.util.DisplayMetrics;

import com.sparkrico.v2ex.model.Member;
import com.sparkrico.v2ex.model.MemberMini;

public class ScreenUtil {

	public static float getScreenDensity (Context context){
		DisplayMetrics dm = new DisplayMetrics();  
		dm = context.getResources().getDisplayMetrics();  
		return dm.density;
	}
	
	/**
	 * ¸ù¾Ý³ß´çÑ¡Ôñavatar size
	 * @param isLarge
	 * @param member
	 * @return
	 */
	public static String choiceAvatarSize(float density, MemberMini member){
		if(density <=1.0)
			return member.getAvatar_mini();
		else if (density == 1.5)
			return member.getAvatar_normal();
		else
			return member.getAvatar_large();
	}
	
	public static String choiceAvatarSize(float density, Member member){
		if(density <=1.0)
			return member.getAvatar_mini();
		else if (density == 1.5)
			return member.getAvatar_normal();
		else
			return member.getAvatar_large();
	}
}
