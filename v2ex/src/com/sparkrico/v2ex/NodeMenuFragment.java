package com.sparkrico.v2ex;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sparkrico.v2ex.model.Node;
import com.sparkrico.v2ex.util.ApiUtil;
import com.sparkrico.v2ex.util.ComparableNode;

public class NodeMenuFragment extends ListFragment{
	
	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	
	SimpleAdapter simpleAdapter;
	
	TextView tvCurrent;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.node_list, null);
		tvCurrent = (TextView) v.findViewById(R.id.current);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		simpleAdapter = new SimpleAdapter(getActivity(), data, 
				android.R.layout.simple_list_item_1, 
				new String[]{"title"}, 
				new int[]{android.R.id.text1});
		setListAdapter(simpleAdapter);
		getListView().setOnScrollListener(new ListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
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
					
					//add all
					map = new HashMap<String, String>();
					map.put("title", getString(R.string.latest));
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
					
					ComparableNode comparableNode = new ComparableNode();
					
					Collections.sort(data, comparableNode);
					
					simpleAdapter.notifyDataSetChanged();
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
}
