package com.sparkrico.v2ex;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
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
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.image.SmartImageView;
import com.sparkrico.v2ex.model.Member;
import com.sparkrico.v2ex.model.Topic;
import com.sparkrico.v2ex.util.ApiUtil;
import com.sparkrico.v2ex.util.DateUtil;
import com.sparkrico.v2ex.util.HtmlUtil;
import com.sparkrico.v2ex.util.ScreenUtil;
import com.sparkrico.v2ex.util.ThemeUtil;
import com.sparkrico.v2ex.util.VersionUtils;
import com.umeng.analytics.MobclickAgent;

public class MemberFragment extends FragmentActivity implements
		OnItemClickListener {

	String username;

	SmartImageView ivFace;
	TextView tvId;
	TextView tvUser;
	TextView tvCreated;
	TextView tvTitle;
	TextView tvBio;
	
	View viewLayout;

	ListView listView;
	List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

	ProgressBar loading;

	SimpleAdapter simpleAdapter;

	float density;
	
	int[] color = new int[2];

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.topic);
		
		if (VersionUtils.OverHONEYCOMB()) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		color = ThemeUtil.getThemeInfo(this);

		density = ScreenUtil.getScreenDensity(this);

		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			final List<String> segments = getIntent().getData()
					.getPathSegments();
			if (segments.size() > 1) {
				username = segments.get(1);
			}
		} else
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

	@Override
	protected void onStop() {
		super.onStop();
		((App) getApplication()).getAsyncHttpClient().cancelRequests(
				getApplicationContext(), true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setupViews() {
		View v = getLayoutInflater().inflate(R.layout.member_top, null);

		ivFace = (SmartImageView) v.findViewById(R.id.image);
		tvId = (TextView) v.findViewById(R.id.id);
		tvUser = (TextView) v.findViewById(R.id.user);
		tvCreated = (TextView) v.findViewById(R.id.created);
		tvTitle = (TextView) v.findViewById(R.id.title);
		tvBio = (TextView) v.findViewById(R.id.bio);
		
		viewLayout = (View) v.findViewById(R.id.layout);

		listView = (ListView) findViewById(android.R.id.list);
		listView.addHeaderView(v, "", false);
		listView.setOnItemClickListener(this);
		
		tvBio.setBackgroundColor(color[1]);
		viewLayout.setBackgroundColor(color[1]);
		
		listView.setBackgroundColor(color[1]);

		loading = (ProgressBar) findViewById(R.id.loading);

		findViewById(R.id.control).setVisibility(View.GONE);
	}

	private void initTop(Member member) {
		//
		tvCreated.setTextColor(color[0]);
		tvId.setTextColor(color[0]);
		tvUser.setTextColor(color[0]);
		tvTitle.setTextColor(color[0]);
		tvBio.setTextColor(color[0]);
		
		//
		ivFace.setImageUrl(ScreenUtil.choiceAvatarSize(density, member));
		tvCreated.setText(DateUtil.formatDate(member.getCreated()));
		tvId.setText(member.getId() + "");
		tvUser.setText(member.getLocation());
		tvTitle.setText(member.getUsername());
		tvBio.setMovementMethod(LinkMovementMethod.getInstance());
		tvBio.setText(Html.fromHtml(HtmlUtil.formatAtLink(member.getBio()),
				null, null));
	}

	private void setupListView() {
		simpleAdapter = new SimpleAdapter(this, data, R.layout.topic_list_item,
				new String[] { "title", "node", "username", "replies", "image",
						"date" }, new int[] { R.id.title, R.id.node, R.id.user,
						R.id.replies, R.id.image, R.id.last });
		simpleAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if(view instanceof TextView){
					if(!"0".equals(view.getTag()))
							((TextView)view).setTextColor(color[0]);
					((TextView)view).setText(textRepresentation);
					return true;
				}else if (view instanceof SmartImageView) {
					((SmartImageView) view).setImageUrl((String) data);
					return true;
				}
				return false;
			}
		});
		listView.setAdapter(simpleAdapter);
	}

	private void loadMember(String url) {
		((App) getApplication()).getAsyncHttpClient().get(url,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, String content) {
						super.onSuccess(statusCode, content);

						try {
							Gson gson = new Gson();

							Member member = gson
									.fromJson(content, Member.class);
							initTop(member);
						} catch (JsonSyntaxException e) {
							e.printStackTrace();
						} catch (Exception e) {
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
		((App) getApplication()).getAsyncHttpClient().get(url,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, String content) {
						super.onSuccess(statusCode, content);

						try {
							Gson gson = new Gson();

							Type collectionType = new TypeToken<Collection<Topic>>() {
							}.getType();

							Collection<Topic> list = gson.fromJson(content,
									collectionType);

							data.clear();

							Map<String, Object> map;
							for (Topic topic : list) {
								map = new HashMap<String, Object>();
								map.put("image", ScreenUtil.choiceAvatarSize(
										density, topic.getMember()));
								map.put("title", topic.getTitle());
								map.put("node", topic.getNode().getName());
								map.put("username", topic.getMember()
										.getUsername());
								map.put("replies", "" + topic.getReplies());
								map.put("date", DateUtil.timeAgo(topic
										.getLast_touched()));

								map.put("topic", topic);
								data.add(map);
							}

							simpleAdapter.notifyDataSetChanged();

						} catch (JsonSyntaxException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			if(parent.getCount()>0){
				Map<String, Object> map = (Map<String, Object>) parent.getAdapter().getItem(position);
				
				Topic topic = (Topic) map.get("topic");
				//
				Intent intent = new Intent(this, TopicFragment.class);
				intent.putExtra("topic", topic);
				startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
