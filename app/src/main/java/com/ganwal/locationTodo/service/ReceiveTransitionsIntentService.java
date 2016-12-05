package com.ganwal.locationTodo.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ganwal.locationTodo.R;
import com.ganwal.locationTodo.db.ContentProviderContract;
import com.ganwal.locationTodo.db.LocationTodo;
import com.ganwal.locationTodo.ui.DetailActivity;
import com.ganwal.locationTodo.ui.ListActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class ReceiveTransitionsIntentService extends IntentService {
	
	private static final String TAG = ReceiveTransitionsIntentService.class.getSimpleName();

	public ReceiveTransitionsIntentService() {
		super("ReceiveTransitionsIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(GeoFenceHelper.CATEGORY_LOCATION_SERVICES);
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event != null) {
            if (event.hasError()) {
                int errorCode = event.getErrorCode();
                Log.e(TAG, "Location Client Error Code:" + errorCode);
                // Broadcast the error locally to other components in this app
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
            } else {
                int transition = event.getGeofenceTransition();
                List<Geofence> geofences = event.getTriggeringGeofences();
                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    Log.d(TAG, "GeoFence Enter detected for geofences:" + geofences);
                    // Post a notification
                    sendNotification(transition, geofences);
                } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    Log.d(TAG, "GeoFence Exit detected for geofences:" + geofences);
                    sendNotification(transition, geofences);
                    // An invalid transition was reported
                } else {
                    // Always log as an error
                    Log.e(TAG, "Found Transition type:" + transition);
                }
            }
        }
	}

    @SuppressLint("NewApi")
	private void sendNotification(int transition, List<Geofence> geofences) {
        //get the name of todos for which geofence is detected
        String[] taskNames = new String[geofences.size()];
        long geoFenceId = 0;
        for (int index = 0; index < geofences.size() ; index++) {
            //get the todos that are associated with this geofence
            String[] strArr =  new String[]{geofences.get(index).getRequestId()};
            Cursor cursor = getContentResolver().query(
                    ContentProviderContract.LocationTodoEntry.CONTENT_URI,
                    ContentProviderContract.LocationTodoEntry.projections,
                    ContentProviderContract.LocationTodoEntry.COL_GEOFENCE_ID+ " = ?",
                    strArr,
                    null);
            LocationTodo locationTodo = null;
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                locationTodo = ContentProviderContract.LocationTodoEntry.cursorToLocationTodo(cursor);
                cursor.close();
            }
            if(locationTodo != null) {
                taskNames[index] =  locationTodo.getName();
            }
            if(index == 0) {
                geoFenceId = locationTodo.getId();
            }
        }
        String names = TextUtils.join(", ", taskNames);
        //now build the notification
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	stackBuilder.addParentStack(ListActivity.class);
        Intent notificationIntent = null;
        //If there is only one geofence triggered take user to details page, else go to list page
        if(geoFenceId > 0 && geofences.size() == 1) {
            notificationIntent = new Intent(getApplicationContext(), DetailActivity.class);
            notificationIntent.putExtra(HelperUtility.EXTRA_TODO_ID, geoFenceId);
        } else {
            notificationIntent = new Intent(getApplicationContext(),ListActivity.class);
        }
        stackBuilder.addNextIntent(notificationIntent);
        String msgTitle = null;
        String msgText = null;
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            msgTitle = getString(R.string.add_fence_msg_title);
            msgText = getString(R.string.add_fence_msg, names);
        }  else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            msgTitle = getString(R.string.remove_fence_msg_title);
            msgText = getString(R.string.remove_fence_msg, names);
        }
        // Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        // Set the notification contents
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(msgTitle)
                .setContentText(msgText)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(notificationPendingIntent)
                .setSmallIcon(R.drawable.ic_stat_locdo);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ContextCompat.getColor(this, R.color.primary));
        }

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

}
