package com.ganwal.locationTodo.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.ganwal.locationTodo.R;
import com.ganwal.locationTodo.db.ContentProviderContract;
import com.ganwal.locationTodo.ui.DetailActivity;

public class TaskPriorityWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = TaskPriorityWidgetProvider.class.getSimpleName();

    private long mUserId;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int i = 0; i < appWidgetIds.length; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(LOG_TAG, "onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(LOG_TAG, "onDisabled");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(LOG_TAG, "onReceive intent:"+intent);
        Log.d(LOG_TAG, "onReceive intent action:"+intent.getAction());
        if (ContentProviderContract.ACTION_WIDGET_DATA_UPDATED.equals(intent.getAction())) {
            Log.d(LOG_TAG, "onReceive got update action");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.task_priority_list);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.task_priority_widget);
        Intent serviceIntent = new Intent(context, WidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setRemoteAdapter(context, views);
        } else {
            setRemoteAdapterV11(context, views);
        }

        views.setEmptyView(R.id.task_priority_list, R.id.empty_view);

        //set the pending intent template
        Intent detailIntent = new Intent(context, DetailActivity.class);
        detailIntent.setAction(Intent.ACTION_VIEW);
        detailIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        detailIntent.setData(Uri.parse(detailIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent detailsPendingIntent = PendingIntent.getActivity(context, 0, detailIntent, 0);
        views.setPendingIntentTemplate(R.id.task_priority_list, detailsPendingIntent);
        //update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.task_priority_list,
                new Intent(context, WidgetService.class));
    }


    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.task_priority_list,
                new Intent(context, WidgetService.class));
    }


}
