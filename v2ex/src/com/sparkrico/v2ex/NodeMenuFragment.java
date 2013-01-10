package com.sparkrico.v2ex;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sparkrico.v2ex.model.Node;
import com.sparkrico.v2ex.util.ApiUtil;

public class NodeMenuFragment extends ListFragment{
	
	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	
	SimpleAdapter simpleAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
				new String[]{"title"}, 
				new int[]{android.R.id.text1});
		setListAdapter(simpleAdapter);
		
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
				
				Collection<Node> list = gson.fromJson(content, collectionType);
				
				data.clear();
				
				Map<String, String> map;
				
				//add all
				map = new HashMap<String, String>();
				map.put("title", "È«²¿");
				map.put("name", "");
				data.add(map);
				
				if(list != null){
					for (Node node : list) {
						map = new HashMap<String, String>();
						map.put("title", node.getTitle());
						map.put("name", node.getName());
						data.add(map);
					}
				}
				
				simpleAdapter.notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = new TopicsFragment(data.get(position).get("name"));
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
}
