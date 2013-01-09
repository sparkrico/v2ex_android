package com.sparkrico.v2ex;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sparkrico.v2ex.model.Reply;
import com.sparkrico.v2ex.model.Topic;
import com.sparkrico.v2ex.util.ApiUtil;

public class TopicFragment extends FragmentActivity{
	
	Topic topic;
	
	ImageView ivFace;
	TextView tvNode;
	TextView tvUser;
	TextView tvLast;
	TextView tvTitle;
	TextView tvContent;
	
	ListView listView;
	List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

	SimpleAdapter simpleAdapter;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.topic);
		
		topic = (Topic) getIntent().getSerializableExtra("topic");
		
		setupViews();
		initTop();
		setupListView();
		
		loadReplies(String.format(ApiUtil.replies_show, ""+topic.getId(), ""));
	}
	
	private void setupViews(){
		View v = getLayoutInflater().inflate(R.layout.topic_top, null);
		
		ivFace = (ImageView) v.findViewById(R.id.image);
		tvNode = (TextView) v.findViewById(R.id.node);
		tvUser = (TextView) v.findViewById(R.id.user);
		tvLast = (TextView) v.findViewById(R.id.last);
		tvTitle = (TextView) v.findViewById(R.id.title);
		tvContent = (TextView) v.findViewById(R.id.content);
		
		listView = (ListView) findViewById(android.R.id.list);
		listView.addHeaderView(v, "", false);
	}
	
	private void initTop(){
		tvNode.setText(topic.getNode().getTitle());
		tvUser.setText(topic.getMember().getUsername());
		tvTitle.setText(topic.getTitle());
		tvContent.setText(topic.getContent());
	}
	
	private void setupListView(){
		simpleAdapter = new SimpleAdapter(this, data,
				R.layout.reply_item, 
				new String[] { "content", "username",}, 
				new int[] { R.id.content, R.id.user });
		listView.setAdapter(simpleAdapter);
	}
	
	private void loadReplies(String url) {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);

				Gson gson = new Gson();

				Type collectionType = new TypeToken<Collection<Reply>>() {
				}.getType();

				Collection<Reply> list = gson.fromJson(content, collectionType);

				data.clear();

				Map<String, Object> map;
				for (Reply reply : list) {
					map = new HashMap<String, Object>();
					map.put("image", reply.getMember().getAvatar_normal());
					map.put("content", reply.getContent());
					map.put("username", reply.getMember().getUsername());
					map.put("thanks", "" + reply.getThanks());
					
					data.add(map);
				}

				simpleAdapter.notifyDataSetChanged();
			}
		});
	}
}

