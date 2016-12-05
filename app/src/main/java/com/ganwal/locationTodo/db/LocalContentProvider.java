package com.ganwal.locationTodo.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;


public class LocalContentProvider extends ContentProvider{

    private static final String TAG = LocalContentProvider.class.getSimpleName();

    private LocalDatabaseHelper localDBHelper;

    /* ********************* UriMatcher *************************** */

    private static final UriMatcher sUriMatcher =  new UriMatcher(UriMatcher.NO_MATCH);
    //uri Matcher codes
    private static final int ALL_USERS = 1;
    private static final int USER_ID = 2;
    private static final int ALL_USER_TODOS = 3;
    private static final int TODO_ID = 4;
    private static final int ALL_TODOS = 5;
    //private static final int USER_ID_GEOFENCE_ID = 5;


    static {
        //add uris to match
        sUriMatcher.addURI(ContentProviderContract.CONTENT_AUTHORITY,
                ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO,
                ALL_TODOS);
        sUriMatcher.addURI(ContentProviderContract.CONTENT_AUTHORITY,
                ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO + "/#",
                ALL_USER_TODOS);
        sUriMatcher.addURI(ContentProviderContract.CONTENT_AUTHORITY,
                ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO + "/#/#",
                TODO_ID);
        sUriMatcher.addURI(ContentProviderContract.CONTENT_AUTHORITY,
                ContentProviderContract.UserEntry.TABLE_USER + "/#",
                USER_ID);
        sUriMatcher.addURI(ContentProviderContract.CONTENT_AUTHORITY,
                ContentProviderContract.UserEntry.TABLE_USER,
                ALL_USERS);
    }


    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ALL_USERS:
                return  ContentProviderContract.UserEntry.CONTENT_DIR_TYPE;
            case USER_ID:
                return  ContentProviderContract.UserEntry.CONTENT_ITEM_TYPE;
            case ALL_USER_TODOS:
                return  ContentProviderContract.LocationTodoEntry.CONTENT_DIR_TYPE;
            case TODO_ID:
                return  ContentProviderContract.LocationTodoEntry.CONTENT_ITEM_TYPE;
            case ALL_TODOS:
                return  ContentProviderContract.LocationTodoEntry.CONTENT_DIR_TYPE;
            default:
                return null;
        }
    }

    @Override
    public boolean onCreate() {
        localDBHelper = new LocalDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert: uri:"+uri);
        Log.d(TAG, "insert: values:"+values);
        SQLiteDatabase database = localDBHelper.getWritableDatabase();
        int uriType = sUriMatcher.match(uri);
        boolean syncToNetwork = true;
        long new_id;
        switch (uriType) {
            case ALL_USERS:
                Log.d(TAG, "insert: In ALL_USERS");
                new_id = database.insert(ContentProviderContract.UserEntry.TABLE_USER, null, values);
                Log.d(TAG, "Inserted user, new_id:"+new_id);
                if(new_id > 0) {
                    return ContentProviderContract.UserEntry.getUserUri(new_id);
                }
                break;
            case ALL_TODOS:
                new_id = database.insert(ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO, null, values);
                Log.d(TAG, "Inserted user locationTodo, new_id:"+new_id);
                if(new_id > 0) {
                    return ContentProviderContract.LocationTodoEntry.getLocationTodoUri(new_id); //we are returning this back but don't use it anywhere
                }
                break;
            default:
                throw new UnsupportedOperationException("Can't determine the operation. Uri:"+uri);
        }
        //notify the registered observer for the change
        getContext().getContentResolver().notifyChange(uri, null, syncToNetwork);

        return null;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete: uri:"+uri);
        Log.d(TAG, "delete: selection:"+selection);
        Log.d(TAG, "delete: selectionArgs:"+selectionArgs);
        boolean syncToNetwork = false;
        SQLiteDatabase database = localDBHelper.getWritableDatabase();
        int uriType = sUriMatcher.match(uri);
        int rowsDeleted= 0;
        switch (uriType) {
            case TODO_ID:
                Log.d(TAG, "delete: In TODO_ID");
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    Log.d(TAG, "delete: selection empty");
                    rowsDeleted = database.delete(ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO,
                            ContentProviderContract.LocationTodoEntry._ID + "=" + id,
                            null);
                } else {
                    Log.d(TAG, "delete: selection not empty");
                    rowsDeleted = database.delete(ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO,
                            ContentProviderContract.LocationTodoEntry._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                Log.d(TAG, "delete: rowsDeleted:"+rowsDeleted);
                break;
            default:
                throw new UnsupportedOperationException("Can't determine the operation. Uri:"+uri);
        }
        Log.d(TAG, "delete: syncToNetwork:"+syncToNetwork);
        getContext().getContentResolver().notifyChange(uri, null, syncToNetwork);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG, "update: uri:"+uri);
        Log.d(TAG, "update: values:"+values);
        Log.d(TAG, "update: selection:"+selection);
        Log.d(TAG, "update: selectionArgs:"+selectionArgs);
        boolean syncToNetwork = false;
        //check the Updated column
        String updateStr =  values.getAsString(ContentProviderContract.LocationTodoEntry.COL_UPDATED);
        Log.d(TAG, "update: str:"+updateStr);
        if(updateStr != null && updateStr.equalsIgnoreCase("true")) {
            syncToNetwork = true;
        }
        //check the deleted column
        String deleteStr =  values.getAsString(ContentProviderContract.LocationTodoEntry.COL_DELETED);
        Log.d(TAG, "Delete: str:"+deleteStr);
        if(deleteStr != null && deleteStr.equalsIgnoreCase("true")) {
            syncToNetwork = true;
        }
        SQLiteDatabase database = localDBHelper.getWritableDatabase();
        int uriType = sUriMatcher.match(uri);
        int rowsUpdated = 0;
        switch (uriType) {
            case TODO_ID:
                Log.d(TAG, "update: In TODO_ID");
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    Log.d(TAG, "update: selection empty");
                    rowsUpdated = database.update(ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO,
                            values,
                            ContentProviderContract.LocationTodoEntry._ID + "=" + id,
                            null);
                } else {
                    Log.d(TAG, "update: selection not empty");
                    rowsUpdated = database.update(ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO,
                            values,
                            ContentProviderContract.LocationTodoEntry._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                Log.d(TAG, "update: rowsUpdated:"+rowsUpdated);
                break;
            case USER_ID:
                Log.d(TAG, "update: In USER_ID");
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    Log.d(TAG, "update: selection empty");
                    rowsUpdated = database.update(ContentProviderContract.UserEntry.TABLE_USER,
                            values,
                            ContentProviderContract.UserEntry._ID + "=" + id,
                            null);
                } else {
                    Log.d(TAG, "update: selection not empty");
                    rowsUpdated = database.update(ContentProviderContract.UserEntry.TABLE_USER,
                            values,
                            ContentProviderContract.UserEntry._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                Log.d(TAG, "update: rowsUpdated:"+rowsUpdated);
                break;
            default:
                throw new UnsupportedOperationException("Can't determine the operation. Uri:"+uri);
        }

        Log.d(TAG, "update: syncToNetwork:"+syncToNetwork);
        getContext().getContentResolver().notifyChange(uri, null, syncToNetwork);
        return rowsUpdated;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        SQLiteDatabase database = localDBHelper.getReadableDatabase();
        int uriType = sUriMatcher.match(uri);
        Log.d(TAG, "Querying DB. uri:" + uri + "\n uriType:"+uriType);
        switch(uriType){
            case ALL_USER_TODOS:{
                retCursor = database.query(
                        ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            case TODO_ID:{
                retCursor = database.query(
                        ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO,
                        projection,
                        ContentProviderContract.LocationTodoEntry._ID+ " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            case USER_ID:{
                retCursor = database.query(
                        ContentProviderContract.UserEntry.TABLE_USER,
                        projection,
                        selection, //ContentProviderContract.UserEntry._ID + " = ?",
                        selectionArgs,//new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            case ALL_USERS:{
                retCursor = database.query(
                        ContentProviderContract.UserEntry.TABLE_USER,
                        projection,
                        selection, //ContentProviderContract.UserEntry._ID + " = ?",
                        selectionArgs,//new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            case ALL_TODOS:{
                retCursor = database.query(
                        ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            default:{
                throw new UnsupportedOperationException("Can't determine the operation. Uri:"+uri);
            }
        }
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = localDBHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

     /* @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = localDBHelper.getWritableDatabase();
        int uriType = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        //Deleting the movie. Movie's child records; reviews and videos are using foreign key reference,
        // they will be deleted when movie is deleted
        switch (uriType) {
            case ALL_MOVIES:
                rowsDeleted = database.delete(ContentProviderContract.MovieEntry.TABLE_MOVIE, selection,
                        selectionArgs);
                break;
            case MOVIE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = database.delete(ContentProviderContract.MovieEntry.TABLE_MOVIE,
                            ContentProviderContract.MovieEntry._ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = database.delete(ContentProviderContract.MovieEntry.TABLE_MOVIE,
                            ContentProviderContract.MovieEntry._ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new UnsupportedOperationException("Can't determine the operation. Uri:"+uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }
*/


}
