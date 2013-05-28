package com.sparkrico.v2ex;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
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

	Topic mTopic;

	SmartImageView ivFace;
	TextView tvNode;
	TextView tvUser;
	TextView tvLast;
	TextView tvTitle;
	TextView tvContent;

	ListView listView;
	List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

	Button buttonPrev, buttonNext;

	ProgressBar loading;

	TopicAdapter topicAdapter;

	float density;

	long topic_id;

	boolean leftRight = false;
	
	Handler mHandler = new Handler(){
		
		public void dispatchMessage(android.os.Message msg) {
			Toast.makeText(getApplicationContext(), String.valueOf(msg.obj), Toast.LENGTH_SHORT).show();
		};
	};

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.topic);

		setupViews();
		setupListView();

		density = ScreenUtil.getScreenDensity(this);
		mTopic = (Topic) getIntent().getSerializableExtra("topic");
		if (mTopic == null) {
			topic_id = getIntent().getLongExtra("id", 0);
			loadTopic(String.format(ApiUtil.topics_show, "" + topic_id, "", "",
					""), null);
		} else {
			topic_id = mTopic.getId();
			initTop(mTopic);
			loadReplies(String.format(ApiUtil.replies_show, "" + topic_id, ""));
		}
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

	DialogFragment newFragment;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//
			if (leftRight) {
				newFragment = NoticeAlertDialogFragment.newInstance(
						android.R.string.dialog_alert_title,
						R.string.dialog_content);
				newFragment.setCancelable(false);
				newFragment.show(getSupportFragmentManager(), "notice_dialog");
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
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

		//
		buttonPrev = (Button) findViewById(R.id.prev);
		buttonPrev.setOnClickListener(this);
		buttonNext = (Button) findViewById(R.id.next);
		buttonNext.setOnClickListener(this);

		findViewById(R.id.open_url).setOnClickListener(this);
	}

	/**
	 * init topic info
	 * 
	 * @param topic
	 */
	private void initTop(Topic topic) {
		ivFace.setImageUrl(ScreenUtil.choiceAvatarSize(density,
				topic.getMember()));
		tvLast.setText(DateUtil.timeAgo(topic.getCreated()) + " | "
				+ topic.getId() + "\n"
				+ DateUtil.timeAgo(topic.getLast_touched()));
		tvNode.setText(topic.getNode().getTitle());
		tvUser.setText(topic.getMember().getUsername());
		tvTitle.setText(topic.getTitle());
		HtmlUtil.formatHtml(tvContent, topic.getContent_rendered());
	}

	private void setupListView() {
		topicAdapter = new TopicAdapter(data);
		listView.setAdapter(topicAdapter);
	}

	private void loadTopic(String url, final ProgressDialog pd) {
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
							if (list.isEmpty())
								mHandler.sendMessage(Message.obtain(mHandler, 0, "没有topic_id为 " + topic_id + " 的主题"));
							else {
								for (Topic topic : list) {
									mTopic = topic;
									initTop(topic);
									data.clear();
									topicAdapter.notifyDataSetChanged();
									loadReplies(String.format(
											ApiUtil.replies_show,
											"" + topic_id, ""));
									break;
								}
							}
						} catch (JsonSyntaxException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFinish() {
						super.onFinish();
						if (pd.isShowing() && pd != null) {
							pd.dismiss();
						}
					}
				});
	}

	private void loadReplies(String url) {
		((App) getApplication()).getAsyncHttpClient().get(url,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, String content) {
						super.onSuccess(statusCode, content);

						try {
							Gson gson = new Gson();

							Type collectionType = new TypeToken<Collection<Reply>>() {
							}.getType();

							Collection<Reply> list = gson.fromJson(content,
									collectionType);

							data.clear();

							Map<String, Object> map;
							for (Reply reply : list) {
								map = new HashMap<String, Object>();
								map.put("image", ScreenUtil.choiceAvatarSize(
										density, reply.getMember()));
								map.put("content", HtmlUtil.formatAtLink(reply
										.getContent_rendered()));
								map.put("username", reply.getMember()
										.getUsername());
								map.put("thanks", "" + reply.getThanks());
								map.put("date", DateUtil.timeAgo(reply
										.getLast_modified()));

								data.add(map);
							}

							topicAdapter.notifyDataSetChanged();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.image:
			try {
				Intent intent = new Intent(getApplicationContext(),
						MemberFragment.class);
				intent.putExtra("username", mTopic.getMember().getUsername());
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.prev:
			leftRight = true;
			((App) getApplication()).getAsyncHttpClient().cancelRequests(
					getApplicationContext(), true);
			topic_id--;
			ProgressDialog pd = ProgressDialog.show(this, "", "Loading...",
					true);
			loadTopic(String.format(ApiUtil.topics_show, "" + topic_id, "", "",
					""), pd);
			break;
		case R.id.next:
			leftRight = true;
			((App) getApplication()).getAsyncHttpClient().cancelRequests(
					getApplicationContext(), true);
			topic_id++;
			ProgressDialog pd1 = ProgressDialog.show(this, "", "Loading...",
					true);
			loadTopic(String.format(ApiUtil.topics_show, "" + topic_id, "", "",
					""), pd1);
			break;
		case R.id.open_url:
			HtmlUtil.openUrl(this, mTopic.getUrl());
			break;

		default:
			break;
		}

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

	// 点击了确定
	public void doPositiveClick() {
		leftRight = false;
		finish();
	}

	// 点击了取消
	public void doNegativeClick() {
	}

	public static class NoticeAlertDialogFragment extends DialogFragment {

		/**
		 * 
		 * @param title
		 *            标题
		 * @param message
		 *            内容
		 * @param notice_version
		 *            版本
		 * @return
		 */
		public static NoticeAlertDialogFragment newInstance(int title,
				int message) {
			NoticeAlertDialogFragment frag = new NoticeAlertDialogFragment();
			Bundle args = new Bundle();
			args.putInt("title", title);
			args.putInt("message", message);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int title = getArguments().getInt("title");
			int message = getArguments().getInt("message");

			return new AlertDialog.Builder(getActivity())
					.setIcon(R.drawable.ic_launcher)
					.setTitle(title)
					.setMessage(message)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((TopicFragment) getActivity())
											.doPositiveClick();
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((TopicFragment) getActivity())
											.doNegativeClick();
								}
							}).create();
		}

	}

}
