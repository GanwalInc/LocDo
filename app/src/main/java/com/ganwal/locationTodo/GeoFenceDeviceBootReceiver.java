package com.ganwal.locationTodo;


import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.ganwal.locationTodo.service.RegisterGeoFenceService;

public class GeoFenceDeviceBootReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = GeoFenceDeviceBootReceiver.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Device boot complete received, calling service to register all active geofences");
        Intent service = new Intent(context, RegisterGeoFenceService.class);

        // Start the service, keeping the device awake while it is launching.
        Log.i("GeoFenceDeviceBootReceiver", "Starting service @ " + SystemClock.elapsedRealtime());
        startWakefulService(context, service);
    }

}
