package com.sparkrico.v2ex;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.image.SmartImageView;
import com.sparkrico.v2ex.model.Topic;
import com.sparkrico.v2ex.util.ApiUtil;
import com.sparkrico.v2ex.util.DateUtil;
import com.sparkrico.v2ex.util.ScreenUtil;
import com.sparkrico.v2ex.util.SharedPreferencesUtils;
import com.sparkrico.v2ex.util.ThemeUtil;

public class TopicsFragment extends PullToRefreshListFragment implements
		OnItemClickListener, ThemeNotify {

	List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

	SimpleAdapter simpleAdapter;

	ProgressBar progressBar;

	float density;

	Handler mHandler = new Handler() {

		public void dispatchMessage(android.os.Message msg) {
			if(getActivity() != null)
				Toast.makeText(getActivity(), String.valueOf(msg.obj),
						Toast.LENGTH_SHORT).show();
		};
	};
	
	public TopicsFragment() {
	}

	public TopicsFragment(String node, String title) {
		Bundle bundle;
		if(getArguments() == null)
			bundle = new Bundle();
		else
			bundle = getArguments();
		bundle.putString("node", node);
		bundle.putString("title", title);

		setArguments(bundle);
	}
	
	int[] color = new int[2];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		color = ThemeUtil.getThemeInfo(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.list, null);
		progressBar = (ProgressBar) v.findViewById(android.R.id.progress);

		setupPullToRefreshListView(v);
		mList.setMode(PullToRefreshBase.Mode.DISABLED);
		mList.setBackgroundColor(color[1]);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		density = ScreenUtil.getScreenDensity(getActivity());

		simpleAdapter = new SimpleAdapter(getActivity(), data,
				R.layout.topic_list_item, new String[] { "title", "node",
						"username", "replies", "image", "date" }, new int[] {
						R.id.title, R.id.node, R.id.user, R.id.replies,
						R.id.image, R.id.last });
		simpleAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if( view instanceof TextView){
					if(!"0".equals(view.getTag())){
							((TextView)view).setTextColor(color[0]);
							((TextView)view).setText(textRepresentation);
					}else{
						((TextView)view).setText(textRepresentation);
//						view.setBackgroundColor(color[2]);
					}
					return true;
				}else if (view instanceof SmartImageView) {
					((SmartImageView) view).setImageUrl((String) data);
					return true;
				}
				return false;
			}
		});
		mList.setAdapter(simpleAdapter);
		mList.setOnItemClickListener(this);
		mList.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				loadTopics();
			}
		});
		
		if(savedInstanceState != null){
			Log.d("", savedInstanceState.toString());
		}

		loadTopics();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle("arguments", getArguments());
	}

	@Override
	public void onStop() {
		super.onStop();
		((App) getActivity().getApplication()).getAsyncHttpClient()
				.cancelRequests(getActivity(), true);
	}

	private void loadTopics() {
		String node = getArguments().getString("node");
		if (TextUtils.isEmpty(node))
			loadAllTopics(ApiUtil.topics_latest);
		else
			loadAllTopics(String.format(ApiUtil.topics_show, node));

		String title = getArguments().getString("title");
		if (TextUtils.isEmpty(node))
			getActivity().setTitle(R.string.latest);
		else
			getActivity().setTitle(title);
	}

	private String getUpdateLabel() {
		return getString(R.string.updated_at)
				+ SharedPreferencesUtils
						.getTopicsLastUpdateDateTime(getActivity());
	}

	private void loadAllTopics(String url) {
		((App) getActivity().getApplication()).getAsyncHttpClient().get(url,
				new AsyncHttpResponseHandler() {

					@Override
					public void onStart() {
						super.onStart();
						progressBar.setVisibility(View.VISIBLE);
					}

					@Override
					public void onFinish() {
						super.onFinish();
						progressBar.setVisibility(View.GONE);
						mList.onRefreshComplete();
					}

					@Override
					public void onFailure(Throwable error, String content) {
						super.onFailure(error, content);
						if (TextUtils.isEmpty(content)) {
							mHandler.sendMessage(Message.obtain(mHandler, 0,
									"获取失败，请重试！"));
							return;
						}
					}

					@Override
					public void onSuccess(int statusCode, String content) {
						super.onSuccess(statusCode, content);
						if (TextUtils.isEmpty(content)) {
							mHandler.sendMessage(Message.obtain(mHandler, 0,
									"获取失败，请重试！"));
							return;
						}

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

							SharedPreferencesUtils.putTopicsLastUpdateDateTime(
									getActivity(), DateUtil.formatDate(System
											.currentTimeMillis() / 1000));
							mList.getLoadingLayoutProxy().setLastUpdatedLabel(
									getUpdateLabel());
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

				Intent intent = new Intent(getActivity(), TopicFragment.class);
				intent.putExtra("topic", topic);
				startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void Notify() {
		color = ThemeUtil.getThemeInfo(getActivity());
		mList.setBackgroundColor(color[1]);
		simpleAdapter.notifyDataSetChanged();
	};
}
