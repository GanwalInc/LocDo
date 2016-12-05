package com.ganwal.locationTodo.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.ganwal.locationTodo.R;
import com.ganwal.locationTodo.db.ContentProviderContract;
import com.ganwal.locationTodo.db.LocationTodo;
import com.ganwal.locationTodo.db.User;
import com.ganwal.locationTodo.ui.LoginActivity;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TodoSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = TodoSyncAdapter.class.getSimpleName();
    ContentResolver mContentResolver;
    LocationTODOServiceInterface mServiceEndpoint;


    public TodoSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        String webServiceURL = context.getString(R.string.locdo_webservice_base_url);
        mContentResolver = context.getContentResolver();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(webServiceURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mServiceEndpoint = retrofit.create(LocationTODOServiceInterface.class);

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        Log.d(TAG, "TodoSyncAdapter.onPerformSync: REACHED HERE");
        Log.d(TAG, "onPerformSync: account:"+account);
        Log.d(TAG, "onPerformSync: extras:"+extras);
        Log.d(TAG, "onPerformSync: authority:"+authority);
        Log.d(TAG, "onPerformSync: provider:"+provider);
        Log.d(TAG, "onPerformSync: syncResult:"+syncResult);


        /*AccountManager am = AccountManager.get(this.getContext());
        AccountManagerFuture<Bundle> future = am.getAuthToken(account, "cp", null, false, null, null);
        Log.d(TAG, "onPerformSync: future:"+future);
        Bundle authTokenBundle = null;
        try {
            authTokenBundle = future.getResult();
            Log.d(TAG, "onPerformSync: authTokenBundle:"+authTokenBundle);
            for (String key: authTokenBundle.keySet()) {
                Log.d (TAG, "onPerformSync: bundle key:" +key );
                Log.d (TAG, "onPerformSync: bundle value"+ authTokenBundle.get(key));
            }
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }*/

        updateUsers();

        //handle all the local delete rows, delete them at server too
        deleteTodos();

        //Push the tasks rows that are updated/inserted locally
        updateTodos(account);
    }

    private void updateUsers() {
        //Get all users rows that are updated/inserted
        Uri uri = ContentProviderContract.BASE_CONTENT_URI.buildUpon().
                appendPath(ContentProviderContract.UserEntry.TABLE_USER).build();
        Log.d(TAG, "onPerformSync: uri:"+uri);
        Cursor userCursor = mContentResolver.query(uri,
                ContentProviderContract.UserEntry.projections,
                ContentProviderContract.UserEntry.COL_UPDATED+ " = ?",
                new String[]{"1"},
                null);
        Log.d(TAG, "onPerformSync: userCursor:"+userCursor);
        Log.d(TAG, "onPerformSync: No. of User rows to be updated:"+userCursor.getCount());

        if (userCursor != null && userCursor.getCount() > 0) {
            Log.d(TAG, "onPerformSync: Found "+userCursor.getCount()+" rows in User data that need to be " +
                    "updated on server");
            userCursor.moveToFirst();
            User user = ContentProviderContract.UserEntry.cursorToUser(userCursor);
            Log.d(TAG, "onPerformSync: Sending User to Webservice:"+user);
            // TODO - Using this temporarily - find better way
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
            String token = prefs.getString(LoginActivity.PREF_TOKEN, null);
            Call<User> call = mServiceEndpoint.createUser(HelperUtility.AUTH_HEADER_PREFIX + token,
                    user);

            Log.d(TAG, "onPerformSync: Webservice call executed call:"+call);
            try {
                User retUser= call.execute().body();
                Log.d(TAG, "updateUsers retUser: "+retUser);
                if(retUser != null) {
                    Log.d(TAG, "onPerformSync: Webservice returned user:"+retUser);
                    //load local user object with the one returned from cloud
                    if(retUser.getCloudId() != null) {
                        user.setCloudId(retUser.getCloudId());
                        user.setUpdated(false);
                        //now update this user in local db so we don't send them to server again
                        mContentResolver.update(
                                ContentProviderContract.UserEntry.getUserUri(user.getId()),
                                ContentProviderContract.UserEntry.loadContentValues(user),
                                null,
                                null);
                    }
                }
            } catch (IOException e ){
                Log.e(TAG, "onPerformSync: Error in rest call for saving User:"+e.getMessage());
                e.printStackTrace();
            }
        }
        userCursor.close();
    }

    private void updateTodos(Account account) {
        //Get all todos rows that are updated/inserted
        Uri uri = ContentProviderContract.BASE_CONTENT_URI.buildUpon().
                appendPath(ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO).build();
        Log.d(TAG, "onPerformSync: uri:"+uri);
        Cursor cursor = mContentResolver.query(uri,
                ContentProviderContract.LocationTodoEntry.projections,
                ContentProviderContract.LocationTodoEntry.COL_UPDATED+ " = ?",
                new String[]{"1"},
                null);
        Log.d(TAG, "onPerformSync: Updated rows cursor:"+cursor);
        Log.d(TAG, "onPerformSync: No. of Todo rows to be updated:"+cursor.getCount());

        if (cursor != null && cursor.getCount() > 0) {
            Log.d(TAG, "onPerformSync: Found "+cursor.getCount()+" rows in Todo data that need to be " +
                    "updated on server");
            cursor.moveToFirst();
            long userID = -1;
            String cloudUserIDStr = null;
            while (!cursor.isAfterLast()) {
                LocationTodo locationTodo = ContentProviderContract.LocationTodoEntry.cursorToLocationTodo(cursor);
                //only pull the cloudUserID from user data if its not there before
                if(cloudUserIDStr == null || locationTodo.getUserId() != userID) {
                    userID = locationTodo.getUserId();
                    cloudUserIDStr = getCloudUserId(locationTodo.getUserId());
                }
                if(TextUtils.isEmpty(cloudUserIDStr)) {
                    Log.e(TAG, "updateTodos: Can't sync user todos with cloud, the user needs to be synched first");
                    return;
                }
                Log.d(TAG, "onPerformSync: Sending Todo to Webservice cloudUserID long:" + Long.parseLong(cloudUserIDStr));

                // TODO - Using this temporarily - find better way
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
                String token = prefs.getString(LoginActivity.PREF_TOKEN, null);
                Log.d(TAG, "onPerformSync: token:"+token);
                //TODO - Sending one rest call for each updated todo, can we bunch the calls together
                Call<LocationTodo> call = mServiceEndpoint.createToDoByUser(
                        HelperUtility.AUTH_HEADER_PREFIX + token, Long.parseLong(cloudUserIDStr), locationTodo);
                Log.d(TAG, "onPerformSync: Webservice call executed call:" + call);
                try {
                    LocationTodo returnedLocationTodo = call.execute().body();
                    Log.d(TAG, "onPerformSync: Webservice returned todo:" + returnedLocationTodo);
                    if(returnedLocationTodo != null) {
                        //load local user object with the one returned from cloud
                        locationTodo.setUpdated(false);
                        locationTodo.setCloudId(returnedLocationTodo.getCloudId());
                    }

                } catch (IOException e) {
                    Log.e(TAG, "onPerformSync: Error in rest call for saving Todo:" + e.getMessage());
                    e.printStackTrace();
                }
                //now update this task in local db so we don't send it to server again
                mContentResolver.update(
                        ContentProviderContract.LocationTodoEntry.getLocationTodoWithUserUri(
                                locationTodo.getUserId(),
                                locationTodo.getId()),
                        ContentProviderContract.LocationTodoEntry.loadContentValues(locationTodo),
                        null,
                        null);
                cursor.moveToNext();
            }
        }
        cursor.close();
    }

    private void deleteTodos() {
        //Get all todos rows that are mark deleted
        Uri uri = ContentProviderContract.BASE_CONTENT_URI.buildUpon().
                appendPath(ContentProviderContract.LocationTodoEntry.TABLE_LOCATION_TODO).build();
        Log.d(TAG, "onPerformSync.deleteTodos: uri:"+uri);
        Cursor cursor = mContentResolver.query(uri,
                ContentProviderContract.LocationTodoEntry.projections,
                ContentProviderContract.LocationTodoEntry.COL_DELETED+ " = ?",
                new String[]{"1"},
                null);
        Log.d(TAG, "onPerformSync: Deleted rows cursor:"+cursor);
        Log.d(TAG, "onPerformSync: No. of Deleted rows to be updated:"+cursor.getCount());

        if (cursor != null && cursor.getCount() > 0) {
            Log.d(TAG, "onPerformSync: Found "+cursor.getCount()+" rows in Todo data that need to be " +
                    "deleted on server");
            cursor.moveToFirst();
            long userID = -1;
            String cloudUserIDStr = null;
            while (!cursor.isAfterLast()) {
                LocationTodo locationTodo = ContentProviderContract.LocationTodoEntry.cursorToLocationTodo(cursor);
                //only send request for deletion to web service if updated their otherwise don't bother
                if(!TextUtils.isEmpty(locationTodo.getCloudId())) {
                    //only pull the cloudUserID from user data if its not there before
                    if(cloudUserIDStr == null || locationTodo.getUserId() != userID) {
                        userID = locationTodo.getUserId();
                        cloudUserIDStr = getCloudUserId(locationTodo.getUserId());
                    }
                    if(TextUtils.isEmpty(cloudUserIDStr)) {
                        Log.e(TAG, "updateTodos: Can't sync user todos with cloud, the user needs to be synched first");
                        return;
                    }
                    Log.d(TAG, "onPerformSync: Sending Todo to Webservice cloudUserID:" + Long.parseLong(cloudUserIDStr));
                    // TODO - Using this temporarily - find better way
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
                    String token = prefs.getString(LoginActivity.PREF_TOKEN, null);
                    //TODO - Sending one rest call for each updated todos, can we bunch the calls together
                    Call<ResponseBody> call = mServiceEndpoint.deleteToDoByUser(HelperUtility.AUTH_HEADER_PREFIX + token,
                            Long.parseLong(cloudUserIDStr), Long.parseLong(locationTodo.getCloudId()));
                    Log.d(TAG, "onPerformSync: Webservice call executed call:" + call);
                    try {
                        ResponseBody response = call.execute().body();
                        Log.d(TAG, "onPerformSync: Webservice delete returned response:" + response);
                        //load local user object with the one returned from cloud
                        locationTodo.setDeleted(false);
                    } catch (IOException e) {
                        Log.d(TAG, "onPerformSync: Error in rest call for deleting Todo:" + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "onPerformSync: Todo was never sent to server..so deleting it locally");
                }
                //now update this todos in local db so we don't send them to server again
                mContentResolver.delete(
                        ContentProviderContract.LocationTodoEntry.getLocationTodoWithUserUri(
                                locationTodo.getUserId(),
                                locationTodo.getId()),
                        null,
                        null);
                cursor.moveToNext();
            }
        }
        cursor.close();
    }


    private String getCloudUserId(Long userId) {
        Cursor cursor = mContentResolver.query(ContentProviderContract.UserEntry.CONTENT_URI,
                ContentProviderContract.UserEntry.projections,
                ContentProviderContract.UserEntry._ID+ " = ? and " +
                        ContentProviderContract.UserEntry.COL_DELETED+ " = ?",
                new String[]{userId + "", "0"},
                null);
        if (cursor != null && cursor.getCount() > 0) {
            Log.d(TAG, "getCloudUserId: Found user in db");
            cursor.moveToFirst();
            User currentUser = ContentProviderContract.UserEntry.cursorToUser(cursor);
            return currentUser.getCloudId();
        }
        return null;
    }
}
