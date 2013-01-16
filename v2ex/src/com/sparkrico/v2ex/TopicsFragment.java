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
import android.widget.SimpleAdapter.ViewBinder;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.image.SmartImageView;
import com.sparkrico.v2ex.model.Topic;
import com.sparkrico.v2ex.util.ApiUtil;
import com.sparkrico.v2ex.util.DateUtil;
import com.sparkrico.v2ex.util.ScreenUtil;

public class TopicsFragment extends ListFragment {

	List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

	SimpleAdapter simpleAdapter;
	
	AsyncHttpClient asyncHttpClient;
	
	float density;
	
	public TopicsFragment(String node, String title) {
		Bundle bundle = new Bundle();
		bundle.putString("node", node);
		bundle.putString("title", title);
		
		setArguments(bundle);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		asyncHttpClient = new AsyncHttpClient();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		density = ScreenUtil.getScreenDensity(getActivity());
		
		simpleAdapter = new SimpleAdapter(getActivity(), data,
				R.layout.topic_list_item, new String[] { "title", "node",
						"username", "replies", "image", "date" }, new int[] { R.id.title,
						R.id.node, R.id.user, R.id.replies, R.id.image, R.id.last });
		simpleAdapter.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if(view instanceof SmartImageView){
					((SmartImageView)view).setImageUrl((String)data);
					return true;
				}
				return false;
			}
		});
		setListAdapter(simpleAdapter);

		String node = getArguments().getString("node");
		if (TextUtils.isEmpty(node))
			loadAllNodes(ApiUtil.topics_latest);
		else
			loadAllNodes(String.format(ApiUtil.topics_show, "", "", "", node));
		
		String title = getArguments().getString("title");
		if (TextUtils.isEmpty(node))
			getActivity().setTitle(R.string.latest);
		else
			getActivity().setTitle(title);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		asyncHttpClient.cancelRequests(getActivity(), true);
	}

	private void loadAllNodes(String url) {
		asyncHttpClient.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);

				Gson gson = new Gson();

				Type collectionType = new TypeToken<Collection<Topic>>() {}.getType();

				try{
					Collection<Topic> list = gson.fromJson(content, collectionType);
	
					data.clear();
					
					Map<String, Object> map;
					for (Topic topic : list) {
						map = new HashMap<String, Object>();
						map.put("image", ScreenUtil.choiceAvatarSize(density, topic.getMember()));
						map.put("title", topic.getTitle());
						map.put("node", topic.getNode().getName());
						map.put("username", topic.getMember().getUsername());
						map.put("replies", "" + topic.getReplies());
						map.put("date", DateUtil.timeAgo(topic.getLast_touched()));
						
						map.put("topic", topic);
						data.add(map);
					}
	
					simpleAdapter.notifyDataSetChanged();
				
				}catch (JsonSyntaxException e){
					e.printStackTrace();
				}
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
