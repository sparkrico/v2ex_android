package com.sparkrico.v2ex;

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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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

public class NodeMenuFragment extends ListFragment implements OnClickListener{
	
	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	
	SimpleAdapter simpleAdapter;
	
	TextView tvCurrent;
	RelativeLayout relativeLayout;
	
	SharedPreferences sharedPreferences;
	int type;
	
	ToggleButton toggleButton; 
	
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
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		toggleButton.setChecked(type == 1);
		
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
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.get(ApiUtil.nodes_all, new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);
				
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
		});
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
	        case R.id.bottom_bar:
	        	getListView().setSelection(0);
	        	break;
	    }
	}
}
