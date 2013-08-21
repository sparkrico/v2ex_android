package com.sparkrico.v2ex;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.loopj.android.image.SmartImageView;
import com.sparkrico.v2ex.model.MemberMini;
import com.sparkrico.v2ex.model.Node;
import com.sparkrico.v2ex.model.Topic;
import com.sparkrico.v2ex.provider.Recent;
import com.sparkrico.v2ex.provider.RecentController;
import com.sparkrico.v2ex.util.ThemeUtil;

public class RecentListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
	ThemeNotify, OnItemClickListener{
	
	View viewGroup;
	
	ListView listView;
	TextView tvEmpty;
	
	SimpleCursorAdapter mAdapter;
	
	String mCurFilter = null;
	
	int[] color = new int[2];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		color = ThemeUtil.getThemeInfo(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		viewGroup = inflater.inflate(R.layout.recent_list, container, false);
		listView = (ListView) viewGroup.findViewById(R.id.list);
		tvEmpty = (TextView) viewGroup.findViewById(R.id.empty);
		listView.setEmptyView(tvEmpty);
        
		viewGroup.findViewById(R.id.clear_recent).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RecentController.clearRecent(getActivity());
				mAdapter.notifyDataSetChanged();
			}
		});
		return viewGroup;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        
		listView.setBackgroundColor(color[1]);
		tvEmpty.setTextColor(color[0]);
		viewGroup.setBackgroundColor(color[1]);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new SimpleCursorAdapter(getActivity(),
        		R.layout.topic_list_item, null,
        		new String[] { Recent.Recents.COLUMN_NAME_TITLE, 
        				Recent.Recents.COLUMN_NAME_NODE,
        				Recent.Recents.COLUMN_NAME_USER, 
						Recent.Recents.COLUMN_NAME_FACE_URL,
						Recent.Recents.COLUMN_NAME_TOPIC_ID
						}, new int[] {
						R.id.title, R.id.node, R.id.user, 
						R.id.image, R.id.replies}, 0);
        mAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				String textRepresentation = cursor.getString(columnIndex);
				if(cursor.getColumnIndex(Recent.Recents.COLUMN_NAME_TOPIC_ID)
						== columnIndex){
					view.setVisibility(View.INVISIBLE);
					return true;
				}else
					view.setVisibility(View.VISIBLE);
				
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
					((SmartImageView) view).setImageUrl(textRepresentation);
					return true;
				}
				return false;
			}
		});
        //add footer view
//        View v = getActivity().getLayoutInflater().inflate(R.layout.recent_footer, null, false);
//        v.findViewById(R.id.clear_recent).setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				RecentController.clearRecent(getActivity());
//				mAdapter.notifyDataSetChanged();
//			}
//		});
//        listView.addFooterView(v);
        
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			if(mAdapter.getCount()>0){
				mAdapter.getItem(position);
				
//				"username", "replies", "image", "date"
				
				Cursor cursor = (Cursor) mAdapter.getItem(position);
				
				Topic topic = new Topic();
				topic.setId(cursor.getLong(cursor.getColumnIndex(Recent.Recents.COLUMN_NAME_TOPIC_ID)));
				topic.setTitle(cursor.getString(cursor.getColumnIndex(
						Recent.Recents.COLUMN_NAME_TITLE)));
				topic.setContent_rendered(cursor.getString(cursor.getColumnIndex(
						Recent.Recents.COLUMN_NAME_CONTENT_RENDERED)));
				topic.setCreated(cursor.getLong(cursor.getColumnIndex(
						Recent.Recents.COLUMN_NAME_CREATE_DATE)));
				
				Node node = new Node();
				node.setTitle(cursor.getString(cursor.getColumnIndex(Recent.Recents.COLUMN_NAME_NODE)));
				topic.setNode(node);
				
				MemberMini member = new MemberMini();
				member.setUsername(cursor.getString(cursor.getColumnIndex(
						Recent.Recents.COLUMN_NAME_USER)));
				member.setAvatar_large(cursor.getString(cursor.getColumnIndex(
						Recent.Recents.COLUMN_NAME_FACE_URL)));
				member.setAvatar_mini(cursor.getString(cursor.getColumnIndex(
						Recent.Recents.COLUMN_NAME_FACE_URL)));
				member.setAvatar_normal(cursor.getString(cursor.getColumnIndex(
						Recent.Recents.COLUMN_NAME_FACE_URL)));
				topic.setMember(member);

				Intent intent = new Intent(getActivity(), TopicFragment.class);
				intent.putExtra("topic", topic);
				intent.putExtra("id", cursor.getLong(cursor.getColumnIndex(Recent.Recents.COLUMN_NAME_TOPIC_ID)));
				
				startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri;
        if (mCurFilter != null) {
            baseUri = Uri.withAppendedPath(Recent.Recents.CONTENT_FILTER_URI, Uri.encode(mCurFilter));
        } else {
            baseUri = Recent.Recents.CONTENT_URI;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
//        String select = "((" + People.DISPLAY_NAME + " NOTNULL) AND ("
//                + People.DISPLAY_NAME + " != '' ))";
        return new CursorLoader(getActivity(), baseUri,
                null, null, null,
                Recent.Recents.COLUMN_NAME_CREATE_DATE + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);

        // The list should now be shown.
//        if (isResumed()) {
//            setListShown(true);
//        } else {
//            setListShownNoAnimation(true);
//        }
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
	}

	@Override
	public void Notify() {
		color = ThemeUtil.getThemeInfo(getActivity());
		listView.setBackgroundColor(color[1]);
		tvEmpty.setTextColor(color[0]);
		viewGroup.setBackgroundColor(color[1]);
		mAdapter.notifyDataSetChanged();
	}
}
