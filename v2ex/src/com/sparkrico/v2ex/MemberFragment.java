package com.sparkrico.v2ex;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.image.SmartImageView;
import com.sparkrico.v2ex.model.Member;
import com.sparkrico.v2ex.model.Topic;
import com.sparkrico.v2ex.util.ApiUtil;
import com.sparkrico.v2ex.util.DateUtil;
import com.sparkrico.v2ex.util.HtmlUtil;
import com.sparkrico.v2ex.util.ScreenUtil;
import com.umeng.analytics.MobclickAgent;

public class MemberFragment extends FragmentActivity implements OnItemClickListener{

	String username;
	
	SmartImageView ivFace;
	TextView tvId;
	TextView tvUser;
	TextView tvCreated;
	TextView tvTitle;
	TextView tvBio;
	
	ListView listView;
	List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
	
	ProgressBar loading;

	SimpleAdapter simpleAdapter;
	
	float density;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.topic);
		
		density = ScreenUtil.getScreenDensity(this);
		
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
	         final List<String> segments = getIntent().getData().getPathSegments();
	         if (segments.size() > 1) {
	        	 username = segments.get(1);
	         }
		}else
			username = getIntent().getStringExtra("username");
		
		setTitle(username);
		
		setupViews();
		setupListView();
		
		loadMember(String.format(ApiUtil.members_show, username));
		loadTopics(String.format(ApiUtil.topics_show, "", username, "", ""));
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
	
	private void setupViews(){
		View v = getLayoutInflater().inflate(R.layout.member_top, null);
		
		ivFace = (SmartImageView) v.findViewById(R.id.image);
		tvId= (TextView) v.findViewById(R.id.id);
		tvUser = (TextView) v.findViewById(R.id.user);
		tvCreated = (TextView) v.findViewById(R.id.created);
		tvTitle = (TextView) v.findViewById(R.id.title);
		tvBio = (TextView) v.findViewById(R.id.bio);
		
		listView = (ListView) findViewById(android.R.id.list);
		listView.addHeaderView(v, "", false);
		listView.setOnItemClickListener(this);
		
		loading = (ProgressBar) findViewById(R.id.loading);
	}
	
	private void initTop(Member member){
		ivFace.setImageUrl(ScreenUtil.choiceAvatarSize(density, member));
		tvCreated.setText(DateUtil.formatDate(member.getCreated()));
		tvId.setText(member.getId()+"");
		tvUser.setText(member.getLocation());
		tvTitle.setText(member.getUsername());
		tvBio.setMovementMethod(LinkMovementMethod.getInstance());
		tvBio.setText(Html.fromHtml(HtmlUtil.formatAtLink(member.getBio()),
				null, null));
	}
	
	private void setupListView(){
		simpleAdapter = new SimpleAdapter(this, data,
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
		listView.setAdapter(simpleAdapter);
	}
	
	private void loadMember(String url) {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);

				Gson gson = new Gson();

				try{
					Member member = gson.fromJson(content, Member.class);
					initTop(member);
				} catch (JsonSyntaxException e){
					e.printStackTrace();
				}
			}
			
			@Override
			public void onStart() {
				super.onStart();
				loading.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				loading.setVisibility(View.GONE);
			}
		});
	}
	
	private void loadTopics(String url) {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Topic topic = (Topic) data.get(position-1).get("topic");
		
		Intent intent = new Intent(this, TopicFragment.class);
		intent.putExtra("topic", topic);
		startActivity(intent);
	}
}
