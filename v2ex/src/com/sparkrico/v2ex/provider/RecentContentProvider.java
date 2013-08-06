package com.sparkrico.v2ex.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class RecentContentProvider extends ContentProvider{

	private static final String TAG = "RecentContentProvider";

	private static final String DATABASE_NAME = "v2ex.db";
	private static final int DATABASE_VERSION = 1;

	private static HashMap<String, String> sRecentProjectionMap;

	private static final int RECENTS = 1;
	private static final int RECENT_ID = 2;
	private static final int RECENT_FILTER = 3;

	/**
	 * A UriMatcher instance
	 */
	private static final UriMatcher sUriMatcher;

	// Handle to a new DatabaseHelper.
	private DatabaseHelper mOpenHelper;

	static {

		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		sUriMatcher.addURI(Recent.AUTHORITY, "recents", RECENTS);
		sUriMatcher.addURI(Recent.AUTHORITY, "recents/#", RECENT_ID);
		sUriMatcher.addURI(Recent.AUTHORITY, "recents/filter/*", RECENT_FILTER);

		sRecentProjectionMap = new HashMap<String, String>();

		sRecentProjectionMap.put(Recent.Recents._ID, Recent.Recents._ID);
		sRecentProjectionMap.put(Recent.Recents.COLUMN_NAME_TITLE,
				Recent.Recents.COLUMN_NAME_TITLE);
		sRecentProjectionMap.put(Recent.Recents.COLUMN_NAME_TOPIC_ID,
				Recent.Recents.COLUMN_NAME_TOPIC_ID);
		sRecentProjectionMap.put(Recent.Recents.COLUMN_NAME_FACE_URL,
				Recent.Recents.COLUMN_NAME_FACE_URL);
		sRecentProjectionMap.put(Recent.Recents.COLUMN_NAME_USER,
				Recent.Recents.COLUMN_NAME_USER);
		sRecentProjectionMap.put(Recent.Recents.COLUMN_NAME_NODE,
				Recent.Recents.COLUMN_NAME_NODE);
		
		sRecentProjectionMap.put(Recent.Recents.COLUMN_NAME_TOPIC_CREATED,
				Recent.Recents.COLUMN_NAME_TOPIC_CREATED);
		sRecentProjectionMap.put(Recent.Recents.COLUMN_NAME_CONTENT_RENDERED,
				Recent.Recents.COLUMN_NAME_CONTENT_RENDERED);

		sRecentProjectionMap.put(Recent.Recents.COLUMN_NAME_CREATE_DATE,
				Recent.Recents.COLUMN_NAME_CREATE_DATE);
		sRecentProjectionMap.put(Recent.Recents.COLUMN_NAME_MODIFICATION_DATE,
				Recent.Recents.COLUMN_NAME_MODIFICATION_DATE);
	}

	static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {

			// calls the super constructor, requesting the default cursor
			// factory.
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + Recent.Recents.TABLE_NAME + " ("
					+ Recent.Recents._ID + " INTEGER PRIMARY KEY,"
					+ Recent.Recents.COLUMN_NAME_TITLE + " STRING,"
					+ Recent.Recents.COLUMN_NAME_TOPIC_ID + " STRING,"
					+ Recent.Recents.COLUMN_NAME_FACE_URL + " STRING,"

					+ Recent.Recents.COLUMN_NAME_USER + " STRING,"
					+ Recent.Recents.COLUMN_NAME_NODE + " STRING,"
					
					+ Recent.Recents.COLUMN_NAME_TOPIC_CREATED + " INTEGER,"
					+ Recent.Recents.COLUMN_NAME_CONTENT_RENDERED + " TEXT,"

					+ Recent.Recents.COLUMN_NAME_CREATE_DATE + " INTEGER,"
					+ Recent.Recents.COLUMN_NAME_MODIFICATION_DATE + " INTEGER"
					+ ");");

			db.execSQL("CREATE UNIQUE INDEX 'key_index' ON 'Recents' ('"
					+Recent.Recents.COLUMN_NAME_TOPIC_ID+"');");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Logs that the database is being upgraded
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");

			// Kills the table and existing data
			db.execSQL("DROP TABLE IF EXISTS Recents");

			// Recreates the database with a new version
			onCreate(db);
		}

	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(Recent.Recents.TABLE_NAME);
		switch (sUriMatcher.match(uri)) {
		case RECENTS:
			qb.setProjectionMap(sRecentProjectionMap);
			break;
		case RECENT_ID:
			qb.setProjectionMap(sRecentProjectionMap);
			qb.appendWhere(Recent.Recents._ID + // the name of the ID column
					"=" +
					// the position of the note ID itself in the incoming URI
					uri.getPathSegments().get(
							Recent.Recents.RECENT_ID_PATH_POSITION));
			break;
		case RECENT_FILTER:
			qb.setProjectionMap(sRecentProjectionMap);
			qb.appendWhere(Recent.Recents.COLUMN_NAME_TITLE + // the name
																	// of the ID
																	// column
					" like '%" +
					// the position of the note ID itself in the incoming URI
					Uri.decode(uri.getPathSegments().get(
							Recent.Recents.RECENT_FILER_PATH_POSITION)) + "%'");
			break;
		default:
			// If the URI doesn't match any of the known patterns, throw an
			// exception.
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		String orderBy;
		// If no sort order is specified, uses the default
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = Recent.Recents.DEFAULT_SORT_ORDER;
		} else {
			// otherwise, uses the incoming sort order
			orderBy = sortOrder;
		}

		// Opens the database object in "read" mode, since no writes need to be
		// done.
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		/*
		 * Performs the query. If no problems occur trying to read the database,
		 * then a Cursor object is returned; otherwise, the cursor variable
		 * contains null. If no records were selected, then the Cursor object is
		 * empty, and Cursor.getCount() returns 0.
		 */
		Cursor c = qb.query(db, // The database to query
				projection, // The columns to return from the query
				selection, // The columns for the where clause
				selectionArgs, // The values for the where clause
				null, // don't group the rows
				null, // don't filter by row groups
				orderBy // The sort order
				);

		// Tells the Cursor what URI to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case RECENTS:
			return Recent.Recents.CONTENT_TYPE;
		case RECENT_ID:
			return Recent.Recents.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != RECENTS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		ContentValues values;

		// If the incoming values map is not null, uses it for the new values.
		if (initialValues != null) {
			values = new ContentValues(initialValues);

		} else {
			// Otherwise, create a new value map
			values = new ContentValues();
		}

		// Gets the current system time in milliseconds
		Long now = Long.valueOf(System.currentTimeMillis());

		// If the values map doesn't contain the creation date, sets the value
		// to the current time.
		if (values.containsKey(Recent.Recents.COLUMN_NAME_CREATE_DATE) == false) {
			values.put(Recent.Recents.COLUMN_NAME_CREATE_DATE, now);
		}

		// If the values map doesn't contain the modification date, sets the
		// value to the current
		// time.
		if (values.containsKey(Recent.Recents.COLUMN_NAME_MODIFICATION_DATE) == false) {
			values.put(Recent.Recents.COLUMN_NAME_MODIFICATION_DATE, now);
		}

		// If the values map doesn't contain a title, sets the value to the
		// default title.
		// if (values.containsKey(Config.Recents..COLUMN_NAME_TITLE) == false) {
		// Resources r = Resources.getSystem();
		// values.put(Config.Recents.COLUMN_NAME_TITLE,
		// r.getString(android.R.string.untitled));
		// }

		// If the values map doesn't contain note text, sets the value to an
		// empty string.
		// if (values.containsKey(Config.Recents.COLUMN_NAME_VALUE) == false) {
		// values.put(Config.Recents.COLUMN_NAME_VALUE, "");
		// }

		// Opens the database object in "write" mode.
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		// Performs the insert and returns the ID of the new note.
		long rowId = db.insertWithOnConflict(Recent.Recents.TABLE_NAME, // The
																		// table
																		// to
																		// insert
																		// into.
				Recent.Recents.COLUMN_NAME_TOPIC_ID, // A hack, SQLite sets
														// this column value to
														// null
				// if values is empty.
				values, // A map of column names, and the values to insert
						// into the columns.
				SQLiteDatabase.CONFLICT_REPLACE);

		// If the insert succeeded, the row ID exists.
		if (rowId > 0) {
			// Creates a URI with the note ID pattern and the new row ID
			// appended to it.
			Uri noteUri = ContentUris.withAppendedId(
					Recent.Recents.CONTENT_ID_URI_BASE, rowId);

			// Notifies observers registered against this provider that the data
			// changed.
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		// If the insert didn't succeed, then the rowID is <= 0. Throws an
		// exception.
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String finalWhere;

		int count;

		// Does the delete based on the incoming URI pattern.
		switch (sUriMatcher.match(uri)) {

		// If the incoming pattern matches the general pattern for notes, does a
		// delete
		// based on the incoming "where" columns and arguments.
		case RECENTS:
			count = db.delete(Recent.Recents.TABLE_NAME, // The database table
															// name
					where, // The incoming where clause column names
					whereArgs // The incoming where clause values
					);
			break;

		// If the incoming URI matches a single note ID, does the delete based
		// on the
		// incoming data, but modifies the where clause to restrict it to the
		// particular note ID.
		case RECENT_ID:
			/*
			 * Starts a final WHERE clause by restricting it to the desired note
			 * ID.
			 */
			finalWhere = Recent.Recents._ID + // The ID column name
					" = " + // test for equality
					uri.getPathSegments(). // the incoming note ID
							get(Recent.Recents.RECENT_ID_PATH_POSITION);

			// If there were additional selection criteria, append them to the
			// final
			// WHERE clause
			if (where != null) {
				finalWhere = finalWhere + " AND " + where;
			}

			// Performs the delete.
			count = db.delete(Recent.Recents.TABLE_NAME, // The database table
															// name.
					finalWhere, // The final WHERE clause
					whereArgs // The incoming where clause values.
					);
			break;

		// If the incoming pattern is invalid, throws an exception.
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		/*
		 * Gets a handle to the content resolver object for the current context,
		 * and notifies it that the incoming URI changed. The object passes this
		 * along to the resolver framework, and observers that have registered
		 * themselves for the provider are notified.
		 */
		getContext().getContentResolver().notifyChange(uri, null);

		// Returns the number of rows deleted.
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		String finalWhere;

		// Does the update based on the incoming URI pattern
		switch (sUriMatcher.match(uri)) {

		// If the incoming URI matches the general notes pattern, does the
		// update based on
		// the incoming data.
		case RECENTS:

			// Does the update and returns the number of rows updated.
			count = db.update(Recent.Recents.TABLE_NAME, // The database table
															// name.
					values, // A map of column names and new values to use.
					where, // The where clause column names.
					whereArgs // The where clause column values to select on.
					);
			break;

		// If the incoming URI matches a single note ID, does the update based
		// on the incoming
		// data, but modifies the where clause to restrict it to the particular
		// note ID.
		case RECENT_ID:
			// From the incoming URI, get the note IDo
			String noteId = uri.getPathSegments().get(
					Recent.Recents.RECENT_ID_PATH_POSITION);

			/*
			 * Starts creating the final WHERE clause by restricting it to the
			 * incoming note ID.
			 */
			finalWhere = Recent.Recents._ID + // The ID column name
					" = " + // test for equality
					uri.getPathSegments(). // the incoming note ID
							get(Recent.Recents.RECENT_ID_PATH_POSITION);

			// If there were additional selection criteria, append them to the
			// final WHERE
			// clause
			if (where != null) {
				finalWhere = finalWhere + " AND " + where;
			}

			// Does the update and returns the number of rows updated.
			count = db.update(Recent.Recents.TABLE_NAME, // The database table
															// name.
					values, // A map of column names and new values to use.
					finalWhere, // The final WHERE clause to use
								// placeholders for whereArgs
					whereArgs // The where clause column values to select on, or
								// null if the values are in the where argument.
					);
			break;
		// If the incoming pattern is invalid, throws an exception.
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		/*
		 * Gets a handle to the content resolver object for the current context,
		 * and notifies it that the incoming URI changed. The object passes this
		 * along to the resolver framework, and observers that have registered
		 * themselves for the provider are notified.
		 */
		getContext().getContentResolver().notifyChange(uri, null);

		// Returns the number of rows updated.
		return count;
	}

	DatabaseHelper getOpenHelperForTest() {
		return mOpenHelper;
	}
}
