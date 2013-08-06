package com.sparkrico.v2ex.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.sparkrico.v2ex.model.Topic;
import com.sparkrico.v2ex.provider.Recent.Recents;
import com.sparkrico.v2ex.util.ScreenUtil;

public class RecentController {

	public static void insertRecent(Context context, Topic topic, float density) {
		ContentValues values = new ContentValues();
		try {
			values.put(Recents.COLUMN_NAME_TITLE,
					topic.getTitle());
			values.put(Recents.COLUMN_NAME_TOPIC_ID,
					String.valueOf(topic.getId()));
			values.put(Recents.COLUMN_NAME_FACE_URL,
					ScreenUtil.choiceAvatarSize(density,
							topic.getMember()));
			values.put(Recents.COLUMN_NAME_USER,
					topic.getMember().getUsername());
			values.put(Recents.COLUMN_NAME_NODE,
					topic.getNode().getTitle());
			
			values.put(Recents.COLUMN_NAME_TOPIC_CREATED,
					topic.getCreated());
			values.put(Recents.COLUMN_NAME_CONTENT_RENDERED,
					topic.getContent_rendered());

		} catch (Exception e) {
			e.printStackTrace();
		}

		context.getContentResolver().insert(Recent.Recents.CONTENT_URI, values);
	}

	public static void deleteRecent(Context context, long id) {
		Uri uri = ContentUris.withAppendedId(Recent.Recents.CONTENT_URI, id);
		context.getContentResolver().delete(uri, null, null);
	}
	
	public static void clearRecent(Context context){
		context.getContentResolver().delete(Recent.Recents.CONTENT_URI, null, null);
	}
}
