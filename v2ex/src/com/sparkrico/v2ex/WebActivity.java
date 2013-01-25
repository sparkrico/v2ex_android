package com.sparkrico.v2ex;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.umeng.analytics.MobclickAgent;

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
		progressBar.setVisibility(View.VISIBLE);
		webView = (WebView) findViewById(R.id.webview);
		
		setupWebview();
		setupWebviewSettings();

		webView.loadUrl(url);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 执行系统返回按钮时优先执行WebView的goBack()
			if (webView.canGoBack()) {
				webView.goBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void setupWebview(){
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
		webView.setDownloadListener(new DownloadListener() {
			
			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype, long contentLength) {
				 Uri uri = Uri.parse(url);  
				 Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
				 startActivity(intent);
			}
		});
	}
	
	private void setupWebviewSettings() {
		WebSettings webSettings = webView.getSettings();
		
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setUseWideViewPort(true);
		
//		webSettings.setAppCacheEnabled(true);
//		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
	}

}
