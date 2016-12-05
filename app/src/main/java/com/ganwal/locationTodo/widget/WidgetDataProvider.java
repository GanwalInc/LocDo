package com.ganwal.locationTodo.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ganwal.locationTodo.R;
import com.ganwal.locationTodo.db.ContentProviderContract;
import com.ganwal.locationTodo.db.LocationTodo;
import com.ganwal.locationTodo.service.HelperUtility;
import com.ganwal.locationTodo.ui.AppSettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = WidgetDataProvider.class.getSimpleName();

    private List<LocationTodo> tasks;
    Context mContext;
    Intent mIntent;

    public WidgetDataProvider() {
    }

    public WidgetDataProvider(Context mContext, Intent mIntent) {
        this.mContext = mContext;
        this.mIntent = mIntent;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: at 1 ");
        getTasks();
    }

    @Override
    public void onDataSetChanged() {
        getTasks();
    }

    @Override
    public void onDestroy() {
        tasks = null;
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteView = new RemoteViews(mContext.getPackageName(), R.layout.task_priority_widget_item);
        LocationTodo locationTodo = tasks.get(position);
        remoteView.setTextViewText(R.id.task_id, locationTodo.getId() + "");
        remoteView.setTextViewText(R.id.name, locationTodo.getName());
        remoteView.setTextViewText(R.id.due_date,
                HelperUtility.convertLongToString(locationTodo.getDueDate()));
        int sdk = Build.VERSION.SDK_INT;
        //launch the detail activity when user clicks on individual items
        Bundle extras = new Bundle();
        extras.putLong(HelperUtility.EXTRA_TODO_ID, locationTodo.getId());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        remoteView.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return tasks.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    private void getTasks() {
        Log.d(TAG, "getTasks: at 1");
        final long token = Binder.clearCallingIdentity();
        try {
            //find the user from the preferences get widgets data for it
            //Put the current logged in user's userId in the prefs
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            long userId = prefs.getLong(AppSettingsActivity.PREF_CURRENT_USER_ID, 0);
            Log.d(TAG, "getTasks: userId:"+userId);
            if (userId  > 0) {
                //now get the tasks for this widget
                tasks = new ArrayList<LocationTodo>();
                String sortOrderQueryParam = " due_date asc";
                StringBuffer selection = new StringBuffer().
                        append(ContentProviderContract.LocationTodoEntry.COL_USER_ID + " = ? and ").
                        append(ContentProviderContract.LocationTodoEntry.COL_PRIORITY + " IN (?, ?) and ").
                        append(ContentProviderContract.LocationTodoEntry.COL_COMPLETED + " = ? and ").
                        append(ContentProviderContract.LocationTodoEntry.COL_DELETED + " = ? ");
                Log.d(TAG, "getTasks: userId:" + userId);
                String[] selectionArgs = new String[]{userId + "", "1", "2", "0", "0"};
                Log.d(TAG, "getTasks: at 2 selectionArgs:" + selectionArgs);

                //get the list of tasks from ContentProvider
                Cursor cursor = mContext.getContentResolver().query(
                        ContentProviderContract.LocationTodoEntry.getLocationTodoUserUri(userId),
                        ContentProviderContract.LocationTodoEntry.projections,
                        selection.toString(),
                        selectionArgs,
                        sortOrderQueryParam);
                Log.d(TAG, "getTasks: b4 executing query");
                if (cursor != null && cursor.getCount() > 0) {
                    Log.d(TAG, "getTasks: cursor:" + cursor);

                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        Log.d(TAG, "getTasks: task:");
                        tasks.add(ContentProviderContract.LocationTodoEntry.
                                cursorToLocationTodo(cursor));
                        cursor.moveToNext();
                    }
                }
                cursor.close();

            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }


    }
}
