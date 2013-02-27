package com.sparkrico.v2ex;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sparkrico.v2ex.model.Node;
import com.sparkrico.v2ex.util.ApiUtil;
import com.sparkrico.v2ex.util.ComparableNodeName;
import com.sparkrico.v2ex.util.ComparableNodeTopicCount;
import com.sparkrico.v2ex.util.DateUtil;
import com.sparkrico.v2ex.util.FileUtil;
import com.sparkrico.v2ex.util.HelpUtil;
import com.sparkrico.v2ex.util.SharedPreferencesUtils;

public class NodeMenuFragment extends ListFragment implements OnClickListener{
	
	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	
	SimpleAdapter simpleAdapter;
	
	TextView tvCurrent;
	RelativeLayout relativeLayout;
	
	SharedPreferences sharedPreferences;
	int type;
	
	ToggleButton toggleButton; 
	ToggleButton toggleButtonRefresh;
	Button buttonRefresh;
	ProgressBar progressBar;
	EditText etSearch;
	TextView tvAppVersion;
	
	private static final String NODE_CACHE = "/nodes.cache";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = getActivity().getPreferences(Activity.MODE_PRIVATE);
		type = sharedPreferences.getInt("node_list_type", 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.node_list, null);
		tvCurrent = (TextView) v.findViewById(R.id.current);
		relativeLayout = (RelativeLayout) v.findViewById(R.id.bottom_bar);
		relativeLayout.setOnClickListener(this);
		toggleButton = (ToggleButton) v.findViewById(R.id.toggle);
		toggleButton.setOnClickListener(this);
		buttonRefresh = (Button) v.findViewById(R.id.refresh);
		buttonRefresh.setOnClickListener(this);
		toggleButtonRefresh = (ToggleButton) v.findViewById(R.id.show_refresh);
		toggleButtonRefresh.setOnClickListener(this);
		progressBar = (ProgressBar) v.findViewById(android.R.id.progress);
		tvAppVersion = (TextView) v.findViewById(R.id.app_version);
//		etSearch = (EditText) v.findViewById(R.id.search);
//		etSearch.addTextChangedListener(new SearchTextWatcher());
		return v;
	}
	
	class SearchTextWatcher implements TextWatcher{

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			simpleAdapter.getFilter().filter(s);
			simpleAdapter.notifyDataSetChanged();
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
		
	}
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		toggleButton.setChecked(type == 1);
		
		tvAppVersion.setText(HelpUtil.getVersionName(getActivity()));
		
		simpleAdapter = new SimpleAdapter(getActivity(), data, 
				android.R.layout.simple_list_item_1, 
				new String[]{"title"}, 
				new int[]{android.R.id.text1});
		setListAdapter(simpleAdapter);
		getListView().setOnScrollListener(new ListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(type == 0){
					switch (scrollState) {
					case OnScrollListener.SCROLL_STATE_IDLE:
						tvCurrent.setVisibility(View.GONE);
						break;
					case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					case OnScrollListener.SCROLL_STATE_FLING:
						tvCurrent.setVisibility(View.VISIBLE);
						break;
	
					default:
						break;
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(data.size()>0){
					String current = data.get(firstVisibleItem).get("name");
					if(!TextUtils.isEmpty(current))
						tvCurrent.setText(String.valueOf(current.toUpperCase().charAt(0)));
					else
						tvCurrent.setText("");
				}
			}
		});
		
		loadAllNodes();
	}
	
	private void loadAllNodes(){
		//load from cache
		String content = getCache();
		//
		if(!TextUtils.isEmpty(content)){
			Log.d("", "load from cache");
			handleResult(content);
		}else{
			//load from url
			loadAllNotesFromUrl();
		}
	}
	
	private void loadAllNotesFromUrl(){
		//load from url
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.get(ApiUtil.nodes_all, new AsyncHttpResponseHandler(){
			
			@Override
			public void onStart() {
				super.onStart();
				progressBar.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				progressBar.setVisibility(View.GONE);
			}
			
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);
				//save to cache
				putCache(content);
				SharedPreferencesUtils.putNodeCacheDateTime(getActivity(), System.currentTimeMillis());
				//
				handleResult(content);
			}
			
			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
				Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private String getCache(){
		String content = "";
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(
					getActivity().getCacheDir().getAbsolutePath() + NODE_CACHE);
			content = FileUtil.readInputStreamToString(fis);
			fis.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
                if(fis != null) {
                	fis.close();
                }
            } catch (IOException e) {}
		}
		return content;
	}
	
	private void putCache(String content){
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(
					getActivity().getCacheDir().getAbsolutePath() + NODE_CACHE);
			fos.write(content.getBytes());
			fos.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
                if(fos != null) {
                	fos.flush();
                	fos.close();
                }
            } catch (IOException e) {}
		}
	}
	
	private void handleResult(String content){
		Gson gson = new Gson();
		
		Type collectionType = new TypeToken<Collection<Node>>(){}.getType();
		
		try{
			Collection<Node> list = gson.fromJson(content, collectionType);
			
			data.clear();
			
			Map<String, String> map;
			
			if(list != null){
				for (Node node : list) {
					map = new HashMap<String, String>();
					map.put("title", node.getTitle());
					map.put("name", node.getName());
					map.put("topics", ""+node.getTopics());
					data.add(map);
				}
			}
			
			OrderNode();
		} catch (JsonSyntaxException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = new TopicsFragment(data.get(position).get("name"),
				data.get(position).get("title"));
		if (newContent != null)
			switchFragment(newContent);
	}
	
	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof MainActivity) {
			MainActivity ra = (MainActivity) getActivity();
			ra.switchContent(fragment);
		}
	}
	
	private void OrderNode(){
		//abc or hot order
		if(type == 0){
			ComparableNodeName comparableNodeName = new ComparableNodeName();
			Collections.sort(data, comparableNodeName);
		} else{
			ComparableNodeTopicCount comparableNodeTopicCount = new ComparableNodeTopicCount();
			Collections.sort(data, comparableNodeTopicCount);
		}
		
		//add all
		Map<String, String> map = new HashMap<String, String>();
		map.put("title", getString(R.string.latest));
		map.put("name", "");
		map.put("topics", "0");
		data.add(0, map);
		
		simpleAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
	    switch(v.getId()) {
	        case R.id.toggle:
	        	type = ((ToggleButton)v).isChecked()?1:0;
	        	
	        	Editor editor = sharedPreferences.edit();
	        	editor.putInt("node_list_type", type);
	        	editor.commit();
	        	
	        	data.remove(0);
	        	OrderNode();
	            break;
	        case R.id.refresh:
	        	loadAllNotesFromUrl();
	        	break;
	        case R.id.show_refresh:
	        	buttonRefresh.setText("»º´æ:"+DateUtil.formatDate(SharedPreferencesUtils.getNodeCacheDateTime(getActivity())/1000));
	        	buttonRefresh.setVisibility(toggleButtonRefresh.isChecked()?
	        			View.VISIBLE:View.GONE);
	        	break;
	        case R.id.bottom_bar:
	        	getListView().setSelection(0);
	        	break;
	    }
	}
}
