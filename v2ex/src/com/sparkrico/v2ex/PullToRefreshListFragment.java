package com.sparkrico.v2ex;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class PullToRefreshListFragment extends Fragment {

	static final int INTERNAL_EMPTY_ID = 0x00ff0001;
	
	PullToRefreshListView mList;
	
	View mEmptyView;
	TextView mStandardEmptyView;
	
	public void setupPullToRefreshListView(View v){
		mList = (PullToRefreshListView) v.findViewById(android.R.id.list);
		mStandardEmptyView = (TextView) v
				.findViewById(INTERNAL_EMPTY_ID);
		if (mStandardEmptyView == null) {
			mEmptyView = v.findViewById(android.R.id.empty);
		} else {
			mStandardEmptyView.setVisibility(View.GONE);
		}
	}
}
