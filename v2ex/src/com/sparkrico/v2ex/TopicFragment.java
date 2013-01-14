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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.image.SmartImageView;
import com.sparkrico.v2ex.model.Reply;
import com.sparkrico.v2ex.model.Topic;
import com.sparkrico.v2ex.util.ApiUtil;
import com.sparkrico.v2ex.util.DateUtil;
import com.sparkrico.v2ex.util.HtmlUtil;
import com.sparkrico.v2ex.util.ScreenUtil;
import com.umeng.analytics.MobclickAgent;

public class TopicFragment extends FragmentActivity implements
		View.OnClickListener {

	Topic topic;

	SmartImageView ivFace;
	TextView tvNode;
	TextView tvUser;
	TextView tvLast;
	TextView tvTitle;
	TextView tvContent;

	ListView listView;
	List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

	ProgressBar loading;

	// SimpleAdapter simpleAdapter;

	TopicAdapter topicAdapter;

	float density;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.topic);

		density = ScreenUtil.getScreenDensity(this);

		topic = (Topic) getIntent().getSerializableExtra("topic");

		setupViews();
		initTop();
		setupListView();

		loadReplies(String.format(ApiUtil.replies_show, "" + topic.getId(), ""));
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

	private void setupViews() {
		View v = getLayoutInflater().inflate(R.layout.topic_top, null);

		ivFace = (SmartImageView) v.findViewById(R.id.image);
		ivFace.setTag(0);
		ivFace.setOnClickListener(this);
		tvNode = (TextView) v.findViewById(R.id.node);
		tvUser = (TextView) v.findViewById(R.id.user);
		tvLast = (TextView) v.findViewById(R.id.last);
		tvTitle = (TextView) v.findViewById(R.id.title);
		tvContent = (TextView) v.findViewById(R.id.content);

		listView = (ListView) findViewById(android.R.id.list);
		listView.addHeaderView(v, "", false);

		loading = (ProgressBar) findViewById(R.id.loading);
	}

	private void initTop() {
		ivFace.setImageUrl(ScreenUtil.choiceAvatarSize(density,
				topic.getMember()));
		tvLast.setText(DateUtil.timeAgo(topic.getLast_touched()) + " | " + topic.getId());
		tvNode.setText(topic.getNode().getTitle());
		tvUser.setText(topic.getMember().getUsername());
		tvTitle.setText(topic.getTitle());
		HtmlUtil.formatHtml(tvContent, topic.getContent_rendered());
		
//		tvContent.setMovementMethod(LinkMovementMethod.getInstance());
//		tvContent
//				.setText(Html.fromHtml(
//						HtmlUtil.formatAtLink(topic.getContent_rendered()),
//						null, null));
//		HtmlUtil.linkMember(tvContent);
	}

	private void setupListView() {
		topicAdapter = new TopicAdapter(data);
		listView.setAdapter(topicAdapter);
	}

	// private void setupListView(){
	// simpleAdapter = new SimpleAdapter(this, data,
	// R.layout.reply_item,
	// new String[] { "content", "username", "image", "date"},
	// new int[] { R.id.content, R.id.user, R.id.image, R.id.last });
	// simpleAdapter.setViewBinder(new ViewBinder() {
	//
	// @Override
	// public boolean setViewValue(View view, Object data,
	// String textRepresentation) {
	// if(view instanceof TextView){
	// ((TextView)view).setMovementMethod(LinkMovementMethod.getInstance());
	// ((TextView)view).setText(Html.fromHtml((String)data, null, null));
	// return true;
	// } else if (view instanceof SmartImageView){
	// ((SmartImageView)view).setImageUrl((String)data);
	// view.setTag(1);
	// view.setOnClickListener(TopicFragment.this);
	// return true;
	// }
	// return false;
	// }
	// });
	// listView.setAdapter(simpleAdapter);
	// }
	//
	private void loadReplies(String url) {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);

				Gson gson = new Gson();

				Type collectionType = new TypeToken<Collection<Reply>>() {
				}.getType();

				try {
					Collection<Reply> list = gson.fromJson(content,
							collectionType);

					data.clear();

					Map<String, Object> map;
					for (Reply reply : list) {
						map = new HashMap<String, Object>();
						map.put("image",
								ScreenUtil.choiceAvatarSize(density,
										reply.getMember()));
						map.put("content", HtmlUtil.formatAtLink(reply
								.getContent_rendered()));
						map.put("username", reply.getMember().getUsername());
						map.put("thanks", "" + reply.getThanks());
						map.put("date",
								DateUtil.timeAgo(reply.getLast_modified()));

						data.add(map);
					}

					topicAdapter.notifyDataSetChanged();
				} catch (JsonSyntaxException e) {
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

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(getApplicationContext(),
				MemberFragment.class);
		intent.putExtra("username", topic.getMember().getUsername());
		startActivity(intent);
	}

	public class TopicAdapter extends BaseAdapter {

		List<Map<String, Object>> mData;

		public TopicAdapter(List<Map<String, Object>> data) {
			this.mData = data;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(R.layout.reply_item,
						parent, false);
				holder.tvUser = (TextView) convertView.findViewById(R.id.user);
				holder.tvLast = (TextView) convertView.findViewById(R.id.last);
				holder.tvContent = (TextView) convertView
						.findViewById(R.id.content);
				holder.smartImageView = (SmartImageView) convertView
						.findViewById(R.id.image);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			bindView(position, holder);
			return convertView;
		}

		/**
		 * 绑定数据到View
		 * 
		 * @param position
		 * @param viewHolder
		 */
		private void bindView(int position, ViewHolder viewHolder) {
			final Map<String, Object> dataSet = mData.get(position);
			if (dataSet == null) {
				return;
			}
			viewHolder.tvUser.setText((String) dataSet.get("username"));
			viewHolder.tvLast.setText((String) dataSet.get("date"));
			viewHolder.tvContent.setMovementMethod(LinkMovementMethod
					.getInstance());
			viewHolder.tvContent.setText(Html.fromHtml(
					(String) dataSet.get("content"), null, null));
			HtmlUtil.linkMember(viewHolder.tvContent);
			if (viewHolder.smartImageView != null) {
				viewHolder.smartImageView.setImageUrl((String) dataSet
						.get("image"));
				final String username = (String) dataSet.get("username");
				viewHolder.smartImageView
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent intent = new Intent(
										getApplicationContext(),
										MemberFragment.class);
								intent.putExtra("username", username);
								startActivity(intent);
							}
						});
			}
		}

		public class ViewHolder {
			/**
			 * SmartImageView是异步加载Url图像的ImageView
			 */
			public SmartImageView smartImageView;
			public TextView tvUser;
			public TextView tvLast;
			public TextView tvContent;
		}
	}

}
