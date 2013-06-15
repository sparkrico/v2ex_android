package com.sparkrico.v2ex.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.style.URLSpan;
import android.view.View;

import com.sparkrico.v2ex.MemberFragment;

public class URLSpanV2ex extends URLSpan {

	public URLSpanV2ex(String url) {
		super(url);
	}

	@Override
	public void onClick(View widget) {
		if(getURL().indexOf("www.v2ex.com/member/")>0){
			Uri uri = Uri.parse(getURL());
			Context context = widget.getContext();
			String username = "";
			final List<String> segments = uri.getPathSegments();
	         if (segments.size() > 1) {
	        	 username = segments.get(1);
	         }
			Intent intent = new Intent(
					context,
					MemberFragment.class);
			intent.putExtra("username", username);
			context.startActivity(intent);
		}else{
			Context context = widget.getContext();
//			Uri uri = Uri.parse(getURL());
//			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//			intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
//			context.startActivity(intent);
			HtmlUtil.openUrl(context, getURL());
		}
	}
}
