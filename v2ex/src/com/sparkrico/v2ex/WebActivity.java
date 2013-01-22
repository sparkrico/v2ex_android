package com.sparkrico.v2ex;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebActivity extends FragmentActivity {

	WebView webView;
	ProgressBar progressBar;

	String url;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.webview);

		url = getIntent().getStringExtra("url");
		if (TextUtils.isEmpty(url))
			finish();
		progressBar = (ProgressBar) findViewById(android.R.id.progress);
		webView = (WebView) findViewById(R.id.webview);

		progressBar.setVisibility(View.VISIBLE);
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				progressBar.setProgress(newProgress);
			}

		});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
				view.setInitialScale((int)(100*view.getScale()));
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
//				Toast.makeText(WebActivity.this, "Oh no! " + description,
//						Toast.LENGTH_SHORT).show();
			}
		});
		setupWebviewSettings();

		webView.loadUrl(url);
	}
	
	private void setupWebviewSettings() {
		WebSettings webSettings = webView.getSettings();
		
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
		webView.getSettings().setLoadWithOverviewMode(true);
	    webView.getSettings().setUseWideViewPort(true);
		
//		webSettings.setAppCacheEnabled(true);
//		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
	}

}
