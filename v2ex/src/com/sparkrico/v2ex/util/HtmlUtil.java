package com.sparkrico.v2ex.util;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.widget.TextView;

public class HtmlUtil {
	
	private static final String domain = "http://www.v2ex.com";

	public static String formatAtLink(String content){
		if(TextUtils.isEmpty(content))
			return "";
		return content.replace("<a href=\"/", "<a href=\""+domain+"/");
	}
	
	public static void formatHtml(TextView tv, String content){
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(Html.fromHtml(formatAtLink(content),
						null, null));
		HtmlUtil.linkMember(tv);
	}
	
	public static void linkMember(TextView tv) {
		CharSequence text = tv.getText();
		if (text instanceof Spannable) {
			int end = text.length();
			Spannable sp = (Spannable) tv.getText();
			URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
			SpannableStringBuilder style = new SpannableStringBuilder(text);
			style.clearSpans();// should clear old spans
			for (URLSpan url : urls) {
				URLSpanV2ex myURLSpan = new URLSpanV2ex(url.getURL());
				style.setSpan(myURLSpan, sp.getSpanStart(url),
						sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			}
			tv.setText(style);
		}
	}
}
