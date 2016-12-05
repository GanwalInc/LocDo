package com.ganwal.locationTodo.service;


import android.app.IntentService;

import android.content.Intent;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;
import com.ganwal.locationTodo.GeoFenceDeviceBootReceiver;
import com.ganwal.locationTodo.db.ContentProviderContract;
import com.ganwal.locationTodo.db.LocationTodo;

import java.util.ArrayList;
import java.util.List;

public class RegisterGeoFenceService extends IntentService {

    private static final String TAG = RegisterGeoFenceService.class.getSimpleName();

    public RegisterGeoFenceService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Start Running Service @ " + SystemClock.elapsedRealtime());
        Cursor cursor = getContentResolver().query(
                ContentProviderContract.LocationTodoEntry.CONTENT_URI,
                ContentProviderContract.LocationTodoEntry.projections,
                ContentProviderContract.LocationTodoEntry.COL_LOCATION_ALERT+ " = ?, " +
                ContentProviderContract.LocationTodoEntry.COL_COMPLETED+ " = ?",
                new String[]{"1", "0"},
                null);
        List<LocationTodo> todoList = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            todoList = new ArrayList<LocationTodo>();
            while (!cursor.isAfterLast()) {
                todoList.add(ContentProviderContract.LocationTodoEntry.cursorToLocationTodo(cursor));
                cursor.moveToNext();
            }
        }
        if(todoList != null && !todoList.isEmpty()) {
            Log.d(TAG, "Found " + todoList.size() +" geofences, registering them again");
            for (int index = 0; index < todoList.size() ; index++) {
                //register the geo fence again
                GeoFenceHelper geoHelper = new GeoFenceHelper(this);
                LocationTodo currentTodo = todoList.get(index);
                Log.d(TAG, "Re-registering  Geo Fence with ID:" + currentTodo.getGeofenceID());
                geoHelper.addGeoFence(Long.toString(currentTodo.getGeofenceID()),
                        currentTodo.getLatitude(),
                        currentTodo.getLongitude(),
                        currentTodo.getRadius());
                Log.d(TAG, "Request to Re-register Geo Fence with ID:" + currentTodo.getGeofenceID() + " sent SUCCESSFULLY");
            }

        }
        Log.i(TAG, "Completed service @ " + SystemClock.elapsedRealtime());
        GeoFenceDeviceBootReceiver.completeWakefulIntent(intent);
    }





}
