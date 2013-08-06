package com.sparkrico.v2ex;

import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.sparkrico.v2ex.util.HelpUtil;
import com.sparkrico.v2ex.util.SharedPreferencesUtils;
import com.sparkrico.v2ex.util.ThemeUtil;
import com.sparkrico.v2ex.util.VersionUtils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends SlidingFragmentActivity {

	private Fragment mContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.responsive_content_frame);

		UmengUpdateAgent.update(this);
		UmengUpdateAgent.setUpdateOnlyWifi(false);

		// check if the content frame contains the menu frame
		if (findViewById(R.id.menu_frame) == null) {
			if (VersionUtils.OverHONEYCOMB()) {
				ActionBar actionBar = getActionBar();
				actionBar.setDisplayHomeAsUpEnabled(true);
			}

			setBehindContentView(R.layout.menu_frame);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu()
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			getSlidingMenu().setTouchModeBehind(SlidingMenu.TOUCHMODE_FULLSCREEN);
			// show home as up so we can toggle
			// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			
			getSlidingMenu().setSecondaryMenu(R.layout.menu_frame_two);
		} else {
			// add a dummy view
			View v = new View(this);
			setBehindContentView(v);
			getSlidingMenu().setSlidingEnabled(false);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

		// set the Above View Fragment
		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent");
		if (mContent == null)
			mContent = new TopicsFragment("", "");
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, mContent).commit();
		// set the Behind View Fragment
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new NodeMenuFragment()).commit();
		//
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.menu_frame_two, new RecentListFragment()).commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindScrollScale(0.25f);
		sm.setFadeDegree(0.25f);
		
		boolean GooglePlayInstalled = GooglePlayInstalled();
		//welcome to Andorid 4.3
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
				GooglePlayInstalled &&
				!SharedPreferencesUtils.getBoolean(this, SharedPreferencesUtils.HAS_SHOW_APPOPS_KEY)){
			SharedPreferencesUtils.putBoolean(this, SharedPreferencesUtils.HAS_SHOW_APPOPS_KEY, true);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(android.R.string.dialog_alert_title)
			.setMessage(R.string.appops_attention)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).create();
			
			builder.show();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if(getSlidingMenu().isMenuShowing())
				getSlidingMenu().showContent(true);
			else
				moveTaskToBack(true);
	        return true;
	    }
		return super.onKeyDown(keyCode, event);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		int type = SharedPreferencesUtils.getThemeType(this);
		menu.findItem(R.id.menu_theme).setTitle(type == 0? R.string.menu_theme_night:R.string.menu_theme);
		
		try{
			boolean GooglePlayInstalled = GooglePlayInstalled();
			menu.findItem(R.id.menu_goole_play).setVisible(GooglePlayInstalled);
			menu.findItem(R.id.menu_appops).setVisible(GooglePlayInstalled&&
					Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2);
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}
	
	private boolean GooglePlayInstalled(){
		PackageManager packageManager = getPackageManager();
		
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setPackage("com.android.vending");
		
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 
				PackageManager.MATCH_DEFAULT_ONLY);
		return list != null && list.size()>0;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			getSlidingMenu().showMenu(true);
			return true;
		case R.id.menu_goole_play:
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=com.sparkrico.v2ex"));
			startActivity(intent);
			return true;
		case R.id.menu_appops:
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=com.sparkrico.appops"));
			startActivity(intent);
			return true;
		case R.id.menu_theme:
			int type = SharedPreferencesUtils.getThemeType(this);
			SharedPreferencesUtils.setThemeType(this, type==1?0:1);
			ThemeNotify fragment = (ThemeNotify) getSupportFragmentManager().findFragmentById(R.id.content_frame);
			fragment.Notify();
			fragment = (ThemeNotify) getSupportFragmentManager().findFragmentById(R.id.menu_frame);
			fragment.Notify();
			return true;
		case R.id.menu_about:
			HelpUtil.showAbout(this);
			break;
		case R.id.menu_nav:
			showNav();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void switchContent(final Fragment fragment) {
		mContent = fragment;
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				getSlidingMenu().showContent();
			}
		}, 50);
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//屏幕转向
		dialog = null;
	};

	Dialog dialog;

	GridView gridView;
	NodeAdapter nodeAdapter;
	
	int[] color = new int[2];

	private void showNav() {
		color = ThemeUtil.getThemeInfo(this);
		if(dialog != null){
			((TextView)dialog.findViewById(R.id.title)).setTextColor(color[0]);
			dialog.findViewById(R.id.nav_title_layout).setBackgroundColor(color[1]);
			
			dialog.show();
			return;
		}
		
		
		dialog = new Dialog(this, R.style.city_dialog);
		dialog.setContentView(R.layout.activity_nav);
		dialog.setCanceledOnTouchOutside(true);
		
		dialog.findViewById(R.id.nav_title_layout).setBackgroundColor(color[1]);
		dialog.findViewById(R.id.nav_title_layout).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		dialog.findViewById(R.id.close).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			dialog.findViewById(R.id.dashline).setLayerType(
					View.LAYER_TYPE_SOFTWARE, null);
		}
		((TextView)dialog.findViewById(R.id.title)).setTextColor(color[0]);
		
		DisplayMetrics displayMetrics = new DisplayMetrics();
		
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(displayMetrics);
		
		boolean land = false;
		//计算GridView高度(pixel)
		int space = (int) (200 * displayMetrics.density);
		int rotation = display.getRotation();
		if(rotation==Surface.ROTATION_0 ||
				rotation==Surface.ROTATION_180)
			space = (int) (200 * displayMetrics.density);
		else{
			land = true;
		}
		//TODO 自定义numColumn
		
		int custom_h = display.getHeight() - space;
		int custom_w = display.getWidth() - space;
		
		gridView = (GridView) dialog.findViewById(R.id.gridview);
		gridView.setLayoutParams(new LayoutParams(land?custom_w:LayoutParams.MATCH_PARENT, 
				land?LayoutParams.MATCH_PARENT:custom_h));
		nodeAdapter = new NodeAdapter(this);
		gridView.setAdapter(nodeAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String[] ss = (String[]) parent.getAdapter().getItem(position);
				if (ss[0].startsWith("-") || ss[1].startsWith("-"))
					return;

				Fragment newContent = new TopicsFragment(ss[0], ss[1]);
				if (newContent != null) {
					dialog.cancel();
					switchContent(newContent);
				}
			}
		});

		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		dialogWindow.setAttributes(lp);

		dialogWindow.setWindowAnimations(land?R.style.dialog_land_style:R.style.dialog_style);

		dialog.show();

	}

	public class NodeAdapter extends BaseAdapter {

		private LayoutInflater mLayoutInflater;

		String[] nav_titles;
		String[] nav_names;

		public NodeAdapter(Context c) {
			mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			nav_titles = mContent.getResources().getStringArray(
					R.array.node_nav_title);
			nav_names = mContent.getResources().getStringArray(
					R.array.node_nav_name);
		}

		@Override
		public int getCount() {
			return nav_titles.length;
		}

		@Override
		public Object getItem(int position) {
			return new String[] { nav_names[position], nav_titles[position] };
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getViewTypeCount() {
			return 3;
		}

		@Override
		public int getItemViewType(int position) {
			if ("-".equals(nav_titles[position]))
				return 2;
			else if (nav_titles[position].startsWith("-"))
				return 1;
			else
				return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HolderView holderView;
			int type = getItemViewType(position);
			if (convertView == null) {

				if (type == 1) {
					convertView = mLayoutInflater.inflate(
							R.layout.node_nav_category, null, false);
					
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
						convertView.findViewById(R.id.dashline).setLayerType(
								View.LAYER_TYPE_SOFTWARE, null);
					}
				} else
					convertView = mLayoutInflater.inflate(
							android.R.layout.simple_list_item_1, null, false);

				holderView = new HolderView();
				holderView.tv = (TextView) convertView
						.findViewById(android.R.id.text1);
				convertView.setTag(holderView);
			} else {
				holderView = (HolderView) convertView.getTag();
			}

			holderView.tv.setText(type == 0 ? nav_titles[position]
					: nav_titles[position].substring(1));
			if (type == 1) {
				holderView.tv.setTextColor(Color.WHITE);
				holderView.tv
						.setBackgroundResource(R.drawable.list_activated_holo);
			} else {
				holderView.tv.setTextColor(color[0]);
				holderView.tv.setBackgroundColor(color[1]);
			}
			return convertView;
		}

		class HolderView {
			TextView tv;
		}

	}
}
