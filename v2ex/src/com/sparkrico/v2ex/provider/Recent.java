package com.sparkrico.v2ex.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class Recent {

	public static final String AUTHORITY = "com.sparkrico.v2ex.provider";

	public Recent() {
	}

	public static final class Recents implements BaseColumns {

		private Recents() {
		}

		public static final String TABLE_NAME = "recents";

		private static final String SCHEME = "content://";

		private static final String PATH_RECENTS = "/recents";

		private static final String PATH_RECENT_ID = "/recents/";

		public static final int RECENT_ID_PATH_POSITION = 1;
		public static final int RECENT_FILER_PATH_POSITION = 2;

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_RECENTS);

		public static final Uri CONTENT_FILTER_URI = Uri.parse(SCHEME
				+ AUTHORITY + PATH_RECENTS + "/filter");

		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME
				+ AUTHORITY + PATH_RECENT_ID);

		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME
				+ AUTHORITY + PATH_RECENT_ID + "/#");

		/*
		 * MIME type definitions
		 */

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.v2ex.recent";

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.v2ex.recent";

		public static final String DEFAULT_SORT_ORDER = "modified DESC";

		/*
		 * Column definitions
		 */

		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_TOPIC_ID = "topic_id";
		public static final String COLUMN_NAME_FACE_URL = "face_url";
		public static final String COLUMN_NAME_NODE = "node";
		public static final String COLUMN_NAME_USER = "user";
		
		public static final String COLUMN_NAME_TOPIC_CREATED = "topic_created";
		public static final String COLUMN_NAME_CONTENT_RENDERED = "content_rendered";

		public static final String COLUMN_NAME_CREATE_DATE = "created";
		public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";
	}
}
