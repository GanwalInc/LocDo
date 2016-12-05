package com.ganwal.locationTodo.service;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;


public class GeoFenceHelper implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    //TODO - Do I need to make GeoFenceHelper/Service a singleton???
    //TODO - revist this class, is the whole layout ok, will it handle multiple add/remove requests at one time, especially the member variables
    private static final String TAG = GeoFenceHelper.class.getName();

    public enum REQUEST_TYPE {ADD, REMOVE}

    private GoogleApiClient mGoogleApiClient;
    List<Geofence> mGeoFences;

    List<String> mRemoveIds;
    private final Context mCurrentContext;
    private PendingIntent mGeofencePendingIntent;
    private boolean mInProgress;
    private REQUEST_TYPE mRequestType;

    public static final String ACTION_GEOFENCES_ADDED = "ACTION_GEOFENCES_ADDED";
    public static final String ACTION_GEOFENCES_REMOVED = "ACTION_GEOFENCES_REMOVED";
    public static final String CATEGORY_LOCATION_SERVICES = "CATEGORY_LOCATION_SERVICES";
    public static final String EXTRA_GEOFENCE_STATUS = "EXTRA_GEOFENCE_STATUS";
    public static final String ACTION_GEOFENCE_ERROR = "ACTION_GEOFENCE_ERROR";

    public static final int PERMISSIONS_ACCESS_FINE_LOCATION_CODE = 100;


    public GeoFenceHelper(Activity activity) {
        mCurrentContext = activity;
        mInProgress = false;
        mGeoFences = null;
        mRemoveIds = null;
        mGeofencePendingIntent = null;
    }

    public GeoFenceHelper(Context context) {
        this.mCurrentContext = context;
        mInProgress = false;
        mGeoFences = null;
        mRemoveIds = null;
        mGeofencePendingIntent = null;
    }


    /*************** Methods *********************/
    public void addGeoFence(String geofenceID, float latitude, float longitude, float radius) {
        if (!isGooglePlayServiceAvailable()) {
            return;
        }
        if (mInProgress) {
            Log.d(TAG, "Request to add/remove geofence is already in progress, wait..");
            return;
        }
        mInProgress = true;
        mRequestType = REQUEST_TYPE.ADD;
        Log.d(TAG, "addGeoFence: Adding geofence with geofenceID:"+geofenceID);
        //convert miles to meters
        radius = covertMilesToMeters(radius);
        Geofence fence = new Geofence.Builder()
                .setRequestId(geofenceID)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        if (mGeoFences == null || mGeoFences.isEmpty()) {
            mGeoFences = new ArrayList<Geofence>();
        }
        mGeoFences.add(fence);
        mGoogleApiClient = getGoogleApiClient();
        mGoogleApiClient.connect();
    }

    public void removeGeoFence(List<String> geofenceIds) {
        if (!isGooglePlayServiceAvailable()) {
            return;
        }
        if (mInProgress) {
            Log.d(TAG, "Request to add/remove geofence is already in progress, wait..");
            return;
        }
        mInProgress = true;
        mRequestType = REQUEST_TYPE.REMOVE;
        mGoogleApiClient = getGoogleApiClient();
        mRemoveIds = geofenceIds;
        mGoogleApiClient.connect();


    }

    private boolean isGooglePlayServiceAvailable() {
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mCurrentContext);
        if (result == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Services available");
            return true;
        } else {
            if (mCurrentContext.getClass().equals(Activity.class)) {
                Log.d(TAG, "Google Play Services not available. Showing the error dialog");
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(result, (Activity) mCurrentContext, 0);
                if (errorDialog != null) {
                    errorDialog.show();
                }
            } else {
                Log.e(TAG, "Google Play Services not available. Can't do much.");
            }
        }
        return false;
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mCurrentContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    /************************************************ Interface Methods ********************************************/

    /************* Methods GoogleApiClient.ConnectionCallbacks ***************/
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "In ConnectionCallbacks.onConnected");
        Log.d(TAG, "Request to add/remove geofence is already in progress, wait..");
        if(mRequestType == REQUEST_TYPE.ADD) {
            Log.d(TAG, "onConnected, request to add geofence received");
            // Create a PendingIntent for Location Services to send when geofence transition occurs
            mGeofencePendingIntent = createRequestPendingIntent();
            try {
                // Send a request to add the current geofence
                //We have already asked user for the permission, so should not get the SecurityException
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequest(),
                        mGeofencePendingIntent
                ).setResultCallback(this);
            } catch (SecurityException e) {
                Log.e(TAG, "onConnected: Error adding GeoFence:"+e.getMessage());
                e.printStackTrace();
            } finally {
            }
        } else if(mRequestType == REQUEST_TYPE.REMOVE) {
            // Send a request to remove the current geofence
            Log.d(TAG, "onConnected, request to remove geofence received");
            LocationServices.GeofencingApi.removeGeofences(getGoogleApiClient(), mRemoveIds)
                    .setResultCallback(this);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mInProgress = false;
        mGoogleApiClient = null;
        Log.d(TAG, "In ConnectionCallbacks.onConnectionSuspended");
    }

    /************* Methods GoogleApiClient.OnConnectionFailedListener ***************/

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /************* Methods ResultCallback ***************/
    @Override
    public void onResult(Status status) {
        Log.d(TAG, "onResult: status:"+status);
        Intent broadcastIntent = new Intent();
        String taskStr = null;
        String msg;

        if (mRequestType == REQUEST_TYPE.ADD) {
            taskStr = "added";
        } else if (mRequestType == REQUEST_TYPE.REMOVE) {
            taskStr = "removed";
        }
        Log.d(TAG, "onResult at 2:removelater");

        if (status.isSuccess()) {
            msg = "Successfully " + taskStr + " geofences";
            Log.d(TAG, msg);
            broadcastIntent.setAction(mRequestType.toString())
                    .addCategory(CATEGORY_LOCATION_SERVICES)
                    .putExtra(EXTRA_GEOFENCE_STATUS, msg);
        } else {
            msg = "Error " + taskStr + " geofences. Error Code:" + status.getStatusCode()
                    + " Error Message:" + status.getStatusMessage();
            Log.e(TAG, msg);
            broadcastIntent.setAction(ACTION_GEOFENCE_ERROR)
                    .addCategory(CATEGORY_LOCATION_SERVICES)
                    .putExtra(EXTRA_GEOFENCE_STATUS, msg);
        }

        // Broadcast whatever result occurred
        LocalBroadcastManager.getInstance(mCurrentContext).sendBroadcast(broadcastIntent);

        // Disconnect the location client
        mInProgress = false;
        getGoogleApiClient().disconnect();
    }


    /************* Private Methods  ***************/


	/**
	 * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
	 * Also specifies how the geofence notifications are initially triggered.
	 */
	private GeofencingRequest getGeofencingRequest() {
		GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
		// The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
		// GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
		// is already inside that geofence.
		builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
		// Add the geofences to be monitored by geofencing service.
		builder.addGeofences(mGeoFences);
		// Return a GeofencingRequest.
		return builder.build();
	}

	/**
     * Get a PendingIntent to send with the request to add Geofences. Location Services issues
     * the Intent inside this PendingIntent whenever a geofence transition occurs for the current
     * list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent createRequestPendingIntent() {

        // If the PendingIntent already exists
		Log.d(TAG, "createRequestPendingIntent at 1:removelater");
        if (mGeofencePendingIntent != null) {
            // Return the existing intent
			Log.d(TAG, "createRequestPendingIntent at 2:removelater");
            return mGeofencePendingIntent;
        // If no PendingIntent exists
        } else {
            // Create an Intent pointing to the IntentService
			Log.d(TAG, "createRequestPendingIntent at 3:removelater");
            Intent intent = new Intent(mCurrentContext, ReceiveTransitionsIntentService.class);
			Log.d(TAG, "createRequestPendingIntent at 4:removelater");
            return PendingIntent.getService(mCurrentContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
    
    private GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient == null) {
			buildGoogleApiClient();
        }
        return mGoogleApiClient;
    }
    
	private float covertMilesToMeters(float mi) {
		return mi * 1609.34f;
	}
	
}