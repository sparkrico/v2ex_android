package com.sparkrico.v2ex;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sparkrico.v2ex.model.Node;
import com.sparkrico.v2ex.util.ApiUtil;
import com.sparkrico.v2ex.util.ComparableNodeName;
import com.sparkrico.v2ex.util.ComparableNodeTopicCount;
import com.sparkrico.v2ex.util.DateUtil;
import com.sparkrico.v2ex.util.FileUtil;
import com.sparkrico.v2ex.util.HelpUtil;
import com.sparkrico.v2ex.util.SharedPreferencesUtils;
import com.sparkrico.v2ex.util.VersionUtils;

public class NodeMenuFragment extends PullToRefreshListFragment implements
		OnClickListener, OnItemClickListener {

	List<Map<String, String>> data = new ArrayList<Map<String, String>>();

	SimpleAdapter simpleAdapter;

	TextView tvCurrent;
	LinearLayout bottomeLayout;

	int type;

	public enum OrderType {
		ABC, HOT
	}

	ToggleButton toggleButton;
	ProgressBar progressBar;
	EditText etSearch;
	TextView tvAppVersion;

	Handler mHandler = new Handler() {

		public void dispatchMessage(android.os.Message msg) {
			Toast.makeText(getActivity(), String.valueOf(msg.obj),
					Toast.LENGTH_SHORT).show();
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		type = SharedPreferencesUtils.getNodeListType(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.node_list, null);

		tvCurrent = (TextView) v.findViewById(R.id.current);
		bottomeLayout = (LinearLayout) v.findViewById(R.id.bottom_bar);
		bottomeLayout.setOnClickListener(this);
		toggleButton = (ToggleButton) v.findViewById(R.id.toggle);
		toggleButton.setOnClickListener(this);
		progressBar = (ProgressBar) v.findViewById(android.R.id.progress);
		tvAppVersion = (TextView) v.findViewById(R.id.app_version);

		if (VersionUtils.OverHONEYCOMB()) {
			SearchView searchView = (SearchView) v
					.findViewById(R.id.search_view);
			searchView
					.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

						@Override
						public boolean onQueryTextSubmit(String query) {
							return false;
						}

						@Override
						public boolean onQueryTextChange(String newText) {
							newText = newText.isEmpty() ? "" : newText;
							simpleAdapter.getFilter().filter(newText);
							return false;
						}
					});
			searchView.setOnCloseListener(new SearchView.OnCloseListener() {
				
				@Override
				public boolean onClose() {
					simpleAdapter = new SimpleAdapter(getActivity(), data,
							R.layout.node_list_item, new String[] { "title" },
							new int[] { android.R.id.text1 });
					mList.setAdapter(simpleAdapter);
					return false;
				}
			});
		}

		setupPullToRefreshListView(v);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		toggleButton.setChecked(type == OrderType.HOT.ordinal());

		tvAppVersion.setText(HelpUtil.getVersionName(getActivity()));

		simpleAdapter = new SimpleAdapter(getActivity(), data,
				R.layout.node_list_item, new String[] { "title" },
				new int[] { android.R.id.text1 });
		mList.setAdapter(simpleAdapter);
		mList.setOnItemClickListener(this);
		mList.setOnScrollListener(new ListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (type == OrderType.ABC.ordinal()) {
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
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (data.size() > 0) {
					String current = data.get(firstVisibleItem).get("name");
					if (!TextUtils.isEmpty(current))
						tvCurrent.setText(String.valueOf(current.toUpperCase()
								.charAt(0)));
					else
						tvCurrent.setText("");
				}
			}
		});
		mList.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				loadAllNotesFromUrl();
			}
		});

		loadAllNodes();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			if (parent.getAdapter().getCount() <= 0)
				return;
			Map<String, String> map = (Map<String, String>) parent.getAdapter()
					.getItem(position);

			Fragment newContent = new TopicsFragment(map.get("name"),
					map.get("title"));
			if (newContent != null)
				switchFragment(newContent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadAllNodes() {
		// load from cache
		String content = FileUtil.getNodeCache(getActivity());
		//
		if (!TextUtils.isEmpty(content)) {
			// load from cache
			mList.getLoadingLayoutProxy().setLastUpdatedLabel(getUpdateLabel());
			handleResult(content);
		} else {
			// load from url
			loadAllNotesFromUrl();
		}
	}

	private void loadAllNotesFromUrl() {
		((App) getActivity().getApplication()).getAsyncHttpClient().get(
				ApiUtil.nodes_all, new AsyncHttpResponseHandler() {

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
					public void onSuccess(int statusCode, String content) {
						super.onSuccess(statusCode, content);
						if (TextUtils.isEmpty(content)) {
							mHandler.sendMessage(Message.obtain(mHandler, 0,
									"获取失败，请重试！"));
							return;
						}
						// save to cache
						FileUtil.putNodeCache(getActivity(), content);
						SharedPreferencesUtils.putNodeCacheDateTime(
								getActivity(), DateUtil.formatDate(System
										.currentTimeMillis() / 1000));
						mList.getLoadingLayoutProxy().setLastUpdatedLabel(
								getUpdateLabel());
						//
						handleResult(content);
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
				});
	}

	/**
	 * 处理content
	 * 
	 * @param content
	 */
	private void handleResult(String content) {
		try {
			Gson gson = new Gson();

			Type collectionType = new TypeToken<Collection<Node>>() {
			}.getType();

			Collection<Node> list = gson.fromJson(content, collectionType);

			if (list != null && list.size() > 0) {
				data.clear();
				Map<String, String> map = null;

				for (Node node : list) {
					map = new HashMap<String, String>();
					map.put("title", node.getTitle());
					map.put("name", node.getName());
					map.put("topics", "" + node.getTopics());
					data.add(map);
				}
			}

			OrderNode();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mList.onRefreshComplete();
		}
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

	/**
	 * 排序
	 */
	private void OrderNode() {
		// abc or hot order
		if (type == OrderType.ABC.ordinal()) {
			ComparableNodeName comparableNodeName = new ComparableNodeName();
			Collections.sort(data, comparableNodeName);
		} else {
			ComparableNodeTopicCount comparableNodeTopicCount = new ComparableNodeTopicCount();
			Collections.sort(data, comparableNodeTopicCount);
		}

		// add lastest
		Map<String, String> map = new HashMap<String, String>();
		map.put("title", getString(R.string.latest));
		map.put("name", "");
		map.put("topics", "0");
		data.add(0, map);

		simpleAdapter.notifyDataSetChanged();
	}

	/**
	 * 取缓存时间
	 * 
	 * @return
	 */
	private String getUpdateLabel() {
		return getString(R.string.cached_at)
				+ SharedPreferencesUtils.getNodeCacheDateTime(getActivity());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.toggle:
			try {
				type = ((ToggleButton) v).isChecked() ? OrderType.HOT.ordinal()
						: OrderType.ABC.ordinal();
				// get type
				SharedPreferencesUtils.setNodeListType(getActivity(), type);

				if (data.size() > 0)
					data.remove(0);
				OrderNode();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.bottom_bar:
			// mList.setSelection(0);
			break;
		}
	}

}
