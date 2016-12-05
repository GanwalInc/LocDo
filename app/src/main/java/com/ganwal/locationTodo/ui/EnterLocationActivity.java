package com.ganwal.locationTodo.ui;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;

import com.ganwal.locationTodo.LocationPlace;
import com.ganwal.locationTodo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Iterator;

public class EnterLocationActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback {
	
	private static final String TAG = EnterLocationActivity.class.getSimpleName();
    private static final int AUTO_COMPLETE_THRESHOLD = 5;

    public static final String EXTRA_GEO_LOCATION = "com.ganwal.android.locationTodo.geolocation";

    private static final LatLngBounds WORLD_LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-90, -180), new LatLng(90, 180));

    private static final double BIAS_RADIUS_DEGREES = 1.0;

    private AutoCompleteTextView mEnterLocationField = null;

    private EditText mLatitudeField = null;

    private EditText mLongitudeField = null;

    private Button mBackButton = null;

    private String[] mDetails = null;

    private GoogleApiClient mGoogleApiClient;

    private GoogleApiClient mGoogleLocationApiClient;
    private GoogleMap mMap;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_enter);

        buildGoogleApiClient();
        //to get user current location
        buildGoogleLocationApiClient();
        String[] locDetails = getIntent().getStringArrayExtra(EnterLocationActivity.EXTRA_GEO_LOCATION);
        if(locDetails != null) {
            mDetails = locDetails;
        }
        handleLocationField();
        handleLatitudeField();
        handleLongitudeField();
        handleBackButton();
        handleMapFragment();
	}

    private void handleLocationField() {
        mEnterLocationField = (AutoCompleteTextView) findViewById(R.id.enter_location_auto);
        if(mDetails != null && mDetails[2] != null) {
            mEnterLocationField.setText(mDetails[2]);
        }
        mEnterLocationField.setThreshold(AUTO_COMPLETE_THRESHOLD);
        mEnterLocationField.setAdapter(
                new LocationAutoCompleteAdapter(this, android.R.layout.simple_list_item_1));
        //add listener to handle selection
        mEnterLocationField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                LocationPlace item = (LocationPlace) adapterView.getItemAtPosition(position);
                String placeId = item.getPlaceID();
                //send another request to google APIs to get more details about place, like latitude and longitude
                mDetails = getPlaceDetails(placeId);
            }
        });
    }

    private void handleLatitudeField() {
        mLatitudeField = (EditText) findViewById(R.id.latitude);
        if(mDetails != null && mDetails[0] != null) {
            mLatitudeField.setText(mDetails[0]);
        }
        mLatitudeField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (c != null) {
                    //mDetails[0] = c.toString().trim(); //TODO - handle user entering longitude and latitude
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable c) {
            }
        });
    }

    private void handleLongitudeField() {
        mLongitudeField = (EditText) findViewById(R.id.longitude);
        if(mDetails != null && mDetails[1] != null) {
            mLongitudeField.setText(mDetails[1]);
        }
        mLongitudeField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (c != null) {
                    //mDetails[1] = c.toString().trim();/TODO - handle user entering longitude and latitude
                }
            }
            public void beforeTextChanged(CharSequence c, int start, int count,
                                          int after) {}

            public void afterTextChanged(Editable c) {}
        });
    }

    private void handleMapFragment() {
        MapFragment frag1 = (MapFragment)this.getFragmentManager().findFragmentById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().
                findFragmentById(R.id.map);
        if(frag1 != null) {
            Log.d(TAG, "handleMapFragment: Calling MapAsync");
            frag1.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMap();
    }

    private void handleBackButton() {
        mBackButton = (Button) findViewById(R.id.back);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Back button is clicked...go back");
                Intent i = new Intent();
                //i.addFlags()
                i.putExtra(EXTRA_GEO_LOCATION, mDetails);
                setResult(AppCompatActivity.RESULT_OK, i);
                finish();
            }
        });
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    Intent intent = NavUtils.getParentActivityIntent(this);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    NavUtils.navigateUpTo(this, intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        mGoogleLocationApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        mGoogleLocationApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connection to Google APIs establised");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Google APIs connection suspended.");
    }


    private void updateLocationScreen() {
        if(mDetails != null && mDetails[0] != null && mDetails[1] != null) {
            Log.d(TAG, "update Location Screen: Location Details:"+mDetails[0]+":::"+mDetails[1]+":::"+mDetails[2]);
            String latStr = mDetails[0];
            String longStr = mDetails[1];
            mLatitudeField.setText(latStr);
            mLongitudeField.setText(longStr);
            updateMap();
        } else {
            Log.e(TAG, "Error Updating Location Screen: Location Details:"+mDetails);
        }
    }

    private void updateMap() {
        if(mDetails != null && mDetails[0] != null && mDetails[1] != null) {
            Log.d(TAG, "Updating Map: Location Details:"+mDetails[0]+":::"+mDetails[1]+":::"+mDetails[2]);
            String latStr = mDetails[0];
            String longStr = mDetails[1];
            //now update the map
            LatLng selectedPlace = new LatLng(Float.parseFloat(latStr), Float.parseFloat(longStr));
            mMap.addMarker(new MarkerOptions().position(selectedPlace).title("Marker at "+mDetails[2]));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPlace, 15f));
        } else {
            Log.e(TAG, "Error Updating Map: Location Details:"+mDetails);
        }
    }

    /**
     * Builds a GoogleApiClient.
     */
    private synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building google API client");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Builds a GoogleApiClient.
     */
    private synchronized void buildGoogleLocationApiClient() {
        Log.i(TAG, "Building google Location API client");
        mGoogleLocationApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    private LatLngBounds buildAutoCompleteBounds() {

        //auto complete bias around the center of user's current location
        LatLngBounds bounds;

        Location lastLocation = null;
        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleLocationApiClient);
        } catch (SecurityException e) {
            Log.e(TAG, "buildAutoCompleteBounds: Error:"+e.getStackTrace());
        } finally {
        }
        if(lastLocation != null ) {
            Log.i(TAG, "Current Location Coordinates:" + lastLocation.getLatitude()
                    + ", " + lastLocation.getLongitude());
            LatLng northEast = new LatLng(lastLocation.getLatitude() + BIAS_RADIUS_DEGREES,
                    lastLocation.getLongitude() + BIAS_RADIUS_DEGREES);
            LatLng southWest = new LatLng(lastLocation.getLatitude() - BIAS_RADIUS_DEGREES,
                    lastLocation.getLongitude() - BIAS_RADIUS_DEGREES);
            bounds = LatLngBounds.builder()
                    .include(northEast)
                    .include(southWest)
                    .build();
        }
        else {
            bounds = WORLD_LAT_LNG_BOUNDS;
        }
        return bounds;

    }
    private String[] getPlaceDetails(String placeId) {
        String[] details = null;
        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG, "Getting details for place: " + placeId);
            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            PlaceDetailsCallback resultCallback = new PlaceDetailsCallback();
            placeResult.setResultCallback(resultCallback);
        } else {
            Log.e(TAG, "Google API client is not connected for autocomplete query.");
        }
        return details;
    }


    /* ****************** For Search suggestions/auto-complete ********************** */
    public class LocationAutoCompleteAdapter extends ArrayAdapter<LocationPlace> implements Filterable {
        private ArrayList<LocationPlace> resultList;

        private Context mContext;

        public LocationAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            mContext = context;
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public LocationPlace getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        if (mGoogleApiClient.isConnected()) {
                            Log.i(TAG, "Starting autocomplete query for: " + constraint.toString());
                            //we'll bias the autocomplete results using current location,
                            // that way the closest results are displayed first

                            // Submit the query to the autocomplete API and retrieve a PendingResult that will
                            // contain the results when the query completes.
                            PendingResult<AutocompletePredictionBuffer> pendingResult = Places.GeoDataApi
                                    .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                            buildAutoCompleteBounds(), null);
                            AutoCompleteCallback resultCallback = new AutoCompleteCallback();
                            pendingResult.setResultCallback(resultCallback);
                            //resultList = resultCallback.getmPredications();
                            Log.i(TAG, "Found: " + resultList + " autocomplete predictions");
                        } else {
                            Log.e(TAG, "Google API client is not connected for autocomplete query.");
                        }
					}
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }


    }

    /************************************************************************************************ */

    private class AutoCompleteCallback implements ResultCallback<AutocompletePredictionBuffer> {

        @Override
        public void onResult(AutocompletePredictionBuffer predictionBuffer) {

            // Make sure the Google Play service query completed successfully
            if (predictionBuffer != null && predictionBuffer.getStatus().isSuccess()) {
                Log.i(TAG, "Query completed. Received " + predictionBuffer.getCount() + " predictions.");
                ArrayList<LocationPlace> predications = new ArrayList<LocationPlace>(predictionBuffer.getCount());
                Iterator<AutocompletePrediction> itr = predictionBuffer.iterator();
                while (itr.hasNext()) {
                    AutocompletePrediction predication = itr.next();
                    LocationPlace place = new LocationPlace(predication.getPlaceId(), predication.getFullText(null).toString());
                    predications.add(place);
                }
                //update the view for array adapter by calling notifyDataSetChanged
                LocationAutoCompleteAdapter adapter = (LocationAutoCompleteAdapter)mEnterLocationField.getAdapter();
                adapter.resultList = predications;
                if(adapter.resultList != null && !adapter.resultList.isEmpty()) {
                    adapter.notifyDataSetChanged();
                }
            } else {
                Log.e(TAG, "Error getting autocomplete predictions from API call: " +
                        predictionBuffer.getStatus().toString());
            }
            predictionBuffer.release();
        }
    }


    /************************************************************************************************ */

    private class PlaceDetailsCallback implements ResultCallback<PlaceBuffer> {

        @Override
        public void onResult(PlaceBuffer placeBuffer) {

            // Confirm that the query completed successfully, otherwise log error
            if (placeBuffer != null && placeBuffer.getStatus().isSuccess()
                    && placeBuffer.getCount() > 0) {
                Place place = placeBuffer.get(0);
                Log.i(TAG, "Query completed. Getting details of place " + place);
                mDetails = new String[3];

                mDetails[0] = String.format("%.6f", place.getLatLng().latitude);
                mDetails[1] = String.format("%.6f", place.getLatLng().longitude);
                //new DecimalFormat("##.#####").format(place.getLatLng().latitude);
                //String.valueOf(place.getLatLng().latitude);
                //details[1] = String.valueOf(place.getLatLng().longitude);
                mDetails[2] = String.valueOf(place.getAddress());
                updateLocationScreen();
            } else {
                if (placeBuffer == null) {
                    Log.e(TAG, "Error getting place details. Details are: " + placeBuffer);
                } else if (!placeBuffer.getStatus().isSuccess()) {
                    Log.e(TAG, "Error getting place details. Returned status code : "
                            + placeBuffer.getStatus().toString());
                } else if (placeBuffer.getCount() < 1) {
                    Log.e(TAG, "Error getting place details. No details found for selected place");
                }
            }
            placeBuffer.release();
        }
    }




}

