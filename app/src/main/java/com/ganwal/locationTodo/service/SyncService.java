package com.ganwal.locationTodo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class SyncService extends Service {

    private static final String TAG = SyncService.class.getSimpleName();

    private static TodoSyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        Log.d(TAG, "SyncService.onCreate: at 1");
        synchronized (sSyncAdapterLock) {
            if(sSyncAdapter == null) {
                sSyncAdapter = new TodoSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: intent:"+intent);
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
