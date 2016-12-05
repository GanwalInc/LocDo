package com.ganwal.locationTodo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AuthenticatorService extends Service {
    private static final String TAG = AuthenticatorService.class.getSimpleName();

    private SyncAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        Log.d(TAG, "AuthenticatorService.onCreate: at 1");
        mAuthenticator = new SyncAuthenticator(this);
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: intent:"+intent);
        return mAuthenticator.getIBinder();
    }
}
