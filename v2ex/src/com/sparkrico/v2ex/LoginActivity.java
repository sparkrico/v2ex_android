package com.sparkrico.v2ex;

import java.util.List;

import org.apache.http.cookie.Cookie;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.sparkrico.v2ex.util.ApiUtil;

public class LoginActivity extends FragmentActivity implements OnClickListener{

	PersistentCookieStore myCookieStore;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.login);
		
		findViewById(android.R.id.button1).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		
		myCookieStore = new PersistentCookieStore(this);
		asyncHttpClient.setCookieStore(myCookieStore);
		
//		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4); 
//		nameValuePairs.add(new BasicNameValuePair("u", "sparkrico"));
//		nameValuePairs.add(new BasicNameValuePair("p", ApiUtil.pwd));
//		nameValuePairs.add(new BasicNameValuePair("once","39113"));
//		nameValuePairs.add(new BasicNameValuePair("next","http://www.baidu.com"));
		
		RequestParams params = new RequestParams();
		params.put("u", "sparkrico");
		params.put("p", ApiUtil.pwd);
		params.put("once", "39113");
		
		asyncHttpClient.post(ApiUtil.login, params, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);
				Log.d("", content);
				List<Cookie> list = myCookieStore.getCookies();
				for (Cookie cookie : list) {
					Log.d("", "cookie name: " + cookie.getName());
				}
			}
		});
	}
}
