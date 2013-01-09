package com.sparkrico.v2ex;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sparkrico.v2ex.model.Topic;
import com.sparkrico.v2ex.util.ApiUtil;

public class TopicsFragment extends ListFragment {

	List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

	SimpleAdapter simpleAdapter;
	
	public TopicsFragment(String node) {
		Bundle bundle = new Bundle();
		bundle.putString("node", node);
		
		setArguments(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(android.R.layout.list_content, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		simpleAdapter = new SimpleAdapter(getActivity(), data,
				R.layout.topic_list_item, new String[] { "title", "node",
						"username", "replies" }, new int[] { R.id.title,
						R.id.node, R.id.user, R.id.replies });
		setListAdapter(simpleAdapter);

		String node = getArguments().getString("node");
		if (TextUtils.isEmpty(node))
			loadAllNodes(ApiUtil.topics_latest);
		else
			loadAllNodes(String.format(ApiUtil.topics_show, "", "", "", node));
	}

	private void loadAllNodes(String url) {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);

				Gson gson = new Gson();

				Type collectionType = new TypeToken<Collection<Topic>>() {
				}.getType();

				Collection<Topic> list = gson.fromJson(content, collectionType);

				data.clear();

				Map<String, Object> map;
				for (Topic topic : list) {
					map = new HashMap<String, Object>();
					map.put("image", topic.getMember().getAvatar_normal());
					map.put("title", topic.getTitle());
					map.put("node", topic.getNode().getName());
					map.put("username", topic.getMember().getUsername());
					map.put("replies", "" + topic.getReplies());
					
					map.put("topic", topic);
					data.add(map);
				}

				simpleAdapter.notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Topic topic = (Topic) data.get(position).get("topic");
		
		Intent intent = new Intent(getActivity(), TopicFragment.class);
		intent.putExtra("topic", topic);
		startActivity(intent);
	}
}
