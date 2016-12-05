package com.ganwal.locationTodo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalDatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = LocalDatabaseHelper.class.getSimpleName();
	private static final String DATABASE_NAME = "locationTodo.db";
	private static final int DATABASE_VERSION = 1;

	public LocalDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}

	@Override
	public void onOpen(SQLiteDatabase db){
		super.onOpen(db);
		db.execSQL("PRAGMA foreign_keys=ON");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("PRAGMA foreign_keys=ON;");
		db.execSQL(getUserCreateSQL());
		db.execSQL(getLocationTodoCreateSQL());
		db.execSQL(getLocationTodoCreateIndexSQL());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nothing to upgrade rightnow in sqllite db.
	}

	public static final String getUserCreateSQL() {
		StringBuilder createSQL = new StringBuilder();
		createSQL.append("create table ").append(ContentProviderContract.UserEntry.TABLE_USER+ "(")
				.append(ContentProviderContract.UserEntry._ID + " integer primary key autoincrement, ")
				.append(ContentProviderContract.UserEntry.COL_CLOUD_ID + " TEXT ,")
				.append(ContentProviderContract.UserEntry.COL_GOOGLE_ID + " TEXT NOT NULL,")
				.append(ContentProviderContract.UserEntry.COL_NAME + " TEXT,")
				.append(ContentProviderContract.UserEntry.COL_EMAIL + " TEXT,")
				.append(ContentProviderContract.UserEntry.COL_CREATE_DATE + " INTEGER,")
				.append(ContentProviderContract.UserEntry.COL_LAST_UPDATE_DATE + " INTEGER,")
				.append(ContentProviderContract.UserEntry.COL_LAST_LOGIN_DATE + " INTEGER,")
				.append(ContentProviderContract.UserEntry.COL_UPDATED+ " INTEGER,")
				.append(ContentProviderContract.UserEntry.COL_DELETED+ " INTEGER,")
				.append(" CONSTRAINT cloud_id_unique UNIQUE (" +
								ContentProviderContract.LocationTodoEntry.COL_CLOUD_ID+")")
				.append(")");
		Log.d(TAG, "Create User Table SQL:" + createSQL.toString());
		return createSQL.toString();
	}

	public static final String getLocationTodoCreateSQL() {
		StringBuilder createSQL = new StringBuilder();
		createSQL.append("create table ").append(ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO+ "(")
				.append(ContentProviderContract.LocationTodoEntry._ID + " integer primary key autoincrement, ")
				.append(ContentProviderContract.LocationTodoEntry.COL_USER_ID + " integer not null, ")
				.append(ContentProviderContract.LocationTodoEntry.COL_CLOUD_ID + " TEXT ,")
				.append(ContentProviderContract.LocationTodoEntry.COL_NAME + " TEXT NOT NULL,")
				.append(ContentProviderContract.LocationTodoEntry.COL_SUMMARY + " TEXT,")
				.append(ContentProviderContract.LocationTodoEntry.COL_LOCATION_ALERT + " INTEGER,")
				.append(ContentProviderContract.LocationTodoEntry.COL_GEOFENCE_ID + " INTEGER,")
				.append(ContentProviderContract.LocationTodoEntry.COL_LATITUDE + " FLOAT,")
				.append(ContentProviderContract.LocationTodoEntry.COL_LONGITUDE + " FLOAT,")
				.append(ContentProviderContract.LocationTodoEntry.COL_LOCATION_DESCR + " TEXT,")
				.append(ContentProviderContract.LocationTodoEntry.COL_RADIUS + " FLOAT,")
				.append(ContentProviderContract.LocationTodoEntry.COL_PRIORITY + " INTEGER,")
				.append(ContentProviderContract.LocationTodoEntry.COL_DUE_DATE + " INTEGER,")
				.append(ContentProviderContract.LocationTodoEntry.COL_COMPLETED + " INTEGER,")
				.append(ContentProviderContract.LocationTodoEntry.COL_CREATE_DATE + " INTEGER,")
				.append(ContentProviderContract.LocationTodoEntry.COL_LAST_UPDATE_DATE + " INTEGER,")
				.append(ContentProviderContract.LocationTodoEntry.COL_UPDATED+ " INTEGER, ")
				.append(ContentProviderContract.LocationTodoEntry.COL_DELETED+ " INTEGER, ")
				.append("FOREIGN KEY(" + ContentProviderContract.LocationTodoEntry.COL_USER_ID+ ") ")
				.append("REFERENCES " + ContentProviderContract.UserEntry.TABLE_USER+"(")
				.append(ContentProviderContract.UserEntry._ID +")")
				.append(" ON DELETE CASCADE ")
				.append(" CONSTRAINT cloud_id_unique UNIQUE (" +
						ContentProviderContract.LocationTodoEntry.COL_CLOUD_ID+")")
				.append(")");

		Log.d(TAG, "Create LocationTodo Table SQL:"+createSQL.toString());
		return createSQL.toString();
	}

	public static final String getLocationTodoCreateIndexSQL() {
		StringBuilder createIndexSQL = new StringBuilder();
		createIndexSQL.append("CREATE INDEX user_id_index ON ")
		.append(ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO +"(")
		.append(ContentProviderContract.LocationTodoEntry.COL_USER_ID + ")");
		Log.d(TAG, "Create LocationTodo Create Index SQL:"+createIndexSQL.toString());
		return createIndexSQL.toString();
	}


}
