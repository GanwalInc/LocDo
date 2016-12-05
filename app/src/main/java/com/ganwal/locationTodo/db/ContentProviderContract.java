package com.ganwal.locationTodo.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class ContentProviderContract {

    public static final String CONTENT_AUTHORITY = "com.ganwal.locationTodo.db.LocalContentProvider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String ACTION_WIDGET_DATA_UPDATED = "com.ganwal.locationTodo.db.ACTION_WIDGET_DATA_UPDATED";


    /********************************** User *****************************************/
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_USER = "USER";

        public static final String _ID = "_id";
        public static final String COL_CLOUD_ID = "cloud_id";
        public static final String COL_GOOGLE_ID = "google_id";
        public static final String COL_NAME = "name";
        public static final String COL_EMAIL = "email";
        public static final String COL_CREATE_DATE = "create_date";
        public static final String COL_LAST_UPDATE_DATE = "last_update_date";
        public static final String COL_LAST_LOGIN_DATE = "last_login_date";
        public static final String COL_UPDATED = "updated";
        public static final String COL_DELETED= "deleted";

        public  static String[] projections = {_ID, COL_CLOUD_ID, COL_GOOGLE_ID, COL_NAME,
                COL_EMAIL, COL_CREATE_DATE, COL_LAST_UPDATE_DATE, COL_LAST_LOGIN_DATE,
                COL_UPDATED, COL_DELETED};


        //build the uris
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_USER).build();

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + TABLE_USER;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + TABLE_USER;

        public static Uri getUserUri (long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static ContentValues loadContentValues(User user) {
            ContentValues values = new ContentValues();
            //values.put(UserEntry._ID, user.getId());
            values.put(UserEntry.COL_CLOUD_ID, user.getCloudId());
            values.put(UserEntry.COL_GOOGLE_ID, user.getGoogleId());
            values.put(UserEntry.COL_NAME, user.getName());
            values.put(UserEntry.COL_EMAIL, user.getEmail());
            values.put(UserEntry.COL_CREATE_DATE, user.getCreateDate());
            values.put(UserEntry.COL_LAST_UPDATE_DATE, user.getLastUpdateDate());
            values.put(UserEntry.COL_LAST_LOGIN_DATE, user.getLastLoginDate());
            values.put(UserEntry.COL_UPDATED, user.getUpdated());
            values.put(UserEntry.COL_DELETED, user.getDeleted());
            return values;
        }


        public static User cursorToUser(Cursor cursor) {
            User user = new User();
            int i = -1;
            user.setId(cursor.getLong(++i));
            user.setCloudId(cursor.getString(++i));
            user.setGoogleId(cursor.getString(++i));
            user.setName(cursor.getString(++i));
            user.setEmail(cursor.getString(++i));
            user.setCreateDate(cursor.getLong(++i));
            user.setLastUpdateDate(cursor.getLong(++i));
            user.setLastLoginDate(cursor.getLong(++i));
            user.setUpdated(cursor.getInt(++i) == 1 ? true:false);
            user.setDeleted(cursor.getInt(++i) == 1 ? true:false);
            return user;
        }
    }



    /********************************** LocationTodo *****************************************/
    public static class LocationTodoEntry implements BaseColumns {
        //table name
        public static final String TABLE_LOCATION_TODO = "location_todo";
        //movie table columns
        public static final String _ID = "_id";
        public static final String COL_CLOUD_ID = "cloud_id";
        public static final String COL_USER_ID = "user_id"; // the _id column from the User table
        public static final String COL_NAME = "name";
        public static final String COL_SUMMARY = "summary";
        public static final String COL_LOCATION_ALERT = "location_alert";
        public static final String COL_GEOFENCE_ID = "geofence_id";
        public static final String COL_LATITUDE = "latitude";
        public static final String COL_LONGITUDE = "longitude";
        public static final String COL_LOCATION_DESCR = "location_descr";
        public static final String COL_RADIUS = "radius";
        public static final String COL_PRIORITY = "priority";
        public static final String COL_DUE_DATE = "due_date";
        public static final String COL_COMPLETED = "completed";
        public static final String COL_CREATE_DATE = "create_date";
        public static final String COL_LAST_UPDATE_DATE = "last_update_date";
        public static final String COL_UPDATED = "updated";
        public static final String COL_DELETED = "deleted";

        public  static String[] projections = { _ID, COL_CLOUD_ID, COL_USER_ID, COL_NAME,
                COL_SUMMARY, COL_LOCATION_ALERT, COL_GEOFENCE_ID, COL_LATITUDE,
                COL_LONGITUDE, COL_LOCATION_DESCR, COL_RADIUS, COL_PRIORITY,
                COL_DUE_DATE, COL_COMPLETED,
                COL_CREATE_DATE, COL_LAST_UPDATE_DATE, COL_UPDATED, COL_DELETED };


        //build the uris
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_LOCATION_TODO).build();

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + TABLE_LOCATION_TODO;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + TABLE_LOCATION_TODO;

        public static Uri getLocationTodoUri (long todoId) {
            return ContentUris.withAppendedId(CONTENT_URI, todoId);
            //return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static Uri getLocationTodoUserUri(long userId) {
            return ContentUris.withAppendedId(CONTENT_URI, userId);
            //return BASE_CONTENT_URI.buildUpon().appendPath(userId).build();
        }

        public static Uri getLocationTodoWithUserUri(long userId,long todoId) {
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, userId);
            return ContentUris.withAppendedId(uri, todoId);
        }



        public static ContentValues loadContentValues(LocationTodo todo) {
            ContentValues values = new ContentValues();
            values.put(LocationTodoEntry.COL_CLOUD_ID, todo.getCloudId());
            values.put(LocationTodoEntry.COL_USER_ID, todo.getUserId());
            values.put(LocationTodoEntry.COL_NAME, todo.getName());
            values.put(LocationTodoEntry.COL_SUMMARY, todo.getSummary());
            values.put(LocationTodoEntry.COL_LOCATION_ALERT, todo.getLocationAlert());
            values.put(LocationTodoEntry.COL_GEOFENCE_ID, todo.getGeofenceID());
            values.put(LocationTodoEntry.COL_LATITUDE, todo.getLatitude());
            values.put(LocationTodoEntry.COL_LONGITUDE, todo.getLongitude());
            values.put(LocationTodoEntry.COL_LOCATION_DESCR, todo.getLocationDescr());
            values.put(LocationTodoEntry.COL_RADIUS, todo.getRadius());
            values.put(LocationTodoEntry.COL_PRIORITY, todo.getPriority());
            values.put(LocationTodoEntry.COL_DUE_DATE, todo.getDueDate());
            values.put(LocationTodoEntry.COL_COMPLETED, todo.getCompleted());
            values.put(LocationTodoEntry.COL_CREATE_DATE, todo.getCreateDate());
            values.put(LocationTodoEntry.COL_LAST_UPDATE_DATE, todo.getLastUpdateDate());
            values.put(LocationTodoEntry.COL_UPDATED, todo.getUpdated());
            values.put(LocationTodoEntry.COL_DELETED, todo.getDeleted());
            return values;
        }


        public static LocationTodo cursorToLocationTodo(Cursor cursor) {
            LocationTodo locTodo = new LocationTodo();
            int i = -1;
            locTodo.setId(cursor.getLong(++i));
            locTodo.setCloudId(cursor.getString(++i));
            locTodo.setUserId(cursor.getLong(++i));
            locTodo.setName(cursor.getString(++i));
            locTodo.setSummary(cursor.getString(++i));
            locTodo.setLocationAlert(cursor.getInt(++i) == 1 ? true:false);
            locTodo.setGeofenceID(cursor.getInt(++i));
            locTodo.setLatitude(cursor.getFloat(++i));
            locTodo.setLongitude(cursor.getFloat(++i));
            locTodo.setLocationDescr(cursor.getString(++i));
            locTodo.setRadius(cursor.getFloat(++i));
            locTodo.setPriority(cursor.getInt(++i));
            locTodo.setDueDate(cursor.getLong(++i));
            locTodo.setCompleted(cursor.getInt(++i) == 1 ? true:false);
            locTodo.setCreateDate(cursor.getLong(++i));
            locTodo.setLastUpdateDate(cursor.getLong(++i));
            locTodo.setUpdated(cursor.getInt(++i) == 1 ? true:false);
            locTodo.setDeleted(cursor.getInt(++i) == 1 ? true:false);
            return locTodo;
        }

    }


}
