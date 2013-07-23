package com.sparkrico.v2ex.util;

import android.content.Context;

import com.sparkrico.v2ex.R;

public class ThemeUtil {
	
	public static final int THEME_TYPE_CLASSIC = 0;
	public static final int THEME_TYPE_NIGHT = 1;

	/**
	 * 
	 * @param mContext
	 * @return
	 */
	public static int[] getThemeInfo(Context mContext) {
		int color = 0;
		int bg_color = 0;
		int drawable_count = 0;
		try {
			int type = SharedPreferencesUtils.getThemeType(mContext);

			if (type == THEME_TYPE_NIGHT) {
//				Context context = mContext.createPackageContext(
//						"com.sparkrico.v2ex.skin.nightdream",
//						Context.CONTEXT_IGNORE_SECURITY);
//				color = context.getResources().getColor(0x7f030000);
				color = mContext.getResources().getColor(R.color.text_color_night);
				bg_color = mContext.getResources().getColor(R.color.text_color_bg_night);
				drawable_count = R.drawable.replies;
			} else{
				color = mContext.getResources().getColor(R.color.text_color);
				bg_color = mContext.getResources().getColor(R.color.text_color_bg);
				drawable_count = R.drawable.replies;
			}
		} catch (Exception e) {
			e.printStackTrace();
			color = mContext.getResources().getColor(R.color.text_color);
		}
		return new int[]{color, bg_color, drawable_count};
	}
}
