package com.sparkrico.v2ex;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sparkrico.v2ex.model.Topic;
import com.sparkrico.v2ex.util.ApiUtil;

public class TopicsFragment extends ListFragment{

	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	
	SimpleAdapter simpleAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		String[] birds = new String[]{"a","b","c"};
//		ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(getActivity(), 
//				android.R.layout.simple_list_item_1, android.R.id.text1, birds);
//		setListAdapter(colorAdapter);
		
		simpleAdapter = new SimpleAdapter(getActivity(), data, 
				android.R.layout.simple_list_item_1, 
				new String[]{"name"}, 
				new int[]{android.R.id.text1});
		setListAdapter(simpleAdapter);
		
//		loadAllNodes(getArguments().getString("nodename"));
		loadAllNodes("ios");
	}
	
	private void loadAllNodes(String nodename){
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.get(String.format(ApiUtil.topics_show, "","","",nodename), new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);
				
				Gson gson = new Gson();
				
				Type collectionType = new TypeToken<Collection<Topic>>(){}.getType();
				
				Collection<Topic> list = gson.fromJson(content, collectionType);
				
				data.clear();
				
				Map<String, String> map;
				for (Topic topic : list) {
					map = new HashMap<String, String>();
					map.put("name", topic.getTitle());
					data.add(map);
				}
				
				simpleAdapter.notifyDataSetChanged();
			}
		});
	}
}
