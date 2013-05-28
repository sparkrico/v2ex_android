package com.sparkrico.v2ex;

import android.app.Application;

import com.loopj.android.http.AsyncHttpClient;

public class App extends Application {

	private AsyncHttpClient asyncHttpClient = null;

	public AsyncHttpClient getAsyncHttpClient() {
		if (asyncHttpClient == null)
			asyncHttpClient = new AsyncHttpClient();
		return asyncHttpClient;
	}

}
