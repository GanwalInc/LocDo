package com.ganwal.locationTodo.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ganwal.locationTodo.R;
import com.ganwal.locationTodo.db.ContentProviderContract;
import com.ganwal.locationTodo.db.LocationTodo;
import com.ganwal.locationTodo.service.GeoFenceHelper;
import com.ganwal.locationTodo.service.HelperUtility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DetailFragment extends Fragment {
	
	private static final String TAG = DetailFragment.class.getSimpleName();

	private LocationTodo mLocationTodo = null;

	private EditText mNameField = null;
	
	private EditText mDescrField = null;

    private CheckBox mLocationAlertCheckBox = null;
	
	private Button mEnterLocationButton = null;
	
	private EditText mLocationDescrField = null;
	
	private EditText mRadiusField = null;

	private Spinner mPrioritySpinner = null;

	private Button mDateButton = null;
	
	private CheckBox mDoneCheckBox = null;
	
	FloatingActionButton mSaveFab;

    private static final String DIALOG_DATE = "date";
	
	private static final int REQUEST_CODE_DATE = 0;
	
	private static final int REQUEST_CODE_LOCATION = 1;

    private static final float FLOAT_DEFAULT = 0.0f;
	
	private static Random randomGenerator = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		Log.d(TAG, "Before creating Data Source");
		long userId = getActivity().getIntent().getLongExtra(HelperUtility.EXTRA_USER_ID, 0);
		long todoId = getActivity().getIntent().getLongExtra(HelperUtility.EXTRA_TODO_ID, 0);
		Log.d(TAG, "onCreate: userId:"+userId);
		Log.d(TAG, "onCreate: todoId:"+todoId);
        long todaysDate = new Date().getTime();
		//create new or update
		if(todoId == 0 || todoId < 0) {
			Log.d(TAG, "Creating NEW LocationTodo");
			mLocationTodo = new LocationTodo();
            mLocationTodo.setUserId(userId);
			mLocationTodo.setDueDate(todaysDate);
            mLocationTodo.setCreateDate(todaysDate);
            mLocationTodo.setLastUpdateDate(todaysDate);
		} else {
			Log.d(TAG, "Finding LocationTodo with userId:" + userId +" todoId:"+todoId);
            Uri uri = ContentProviderContract.LocationTodoEntry.getLocationTodoWithUserUri(userId, todoId);
            Log.d(TAG, "Finding LocationTodo with Uri:"+uri);
			String[] whereIds = {String.valueOf(todoId), "0"};
    		Cursor cursor = this.getActivity().getContentResolver().query(
					uri,
					ContentProviderContract.LocationTodoEntry.projections,
					ContentProviderContract.LocationTodoEntry._ID+ " = ?," +
					ContentProviderContract.LocationTodoEntry.COL_DELETED+ " = ?",
					whereIds,
					null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				mLocationTodo = ContentProviderContract.LocationTodoEntry.cursorToLocationTodo(cursor);
			}
			cursor.close();
            mLocationTodo.setLastUpdateDate(todaysDate);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_detail, parent, false);
		//handle all the data elements
		handleNameField(v);
		handleDescrField(v);
        handleLocationAlertCheckBox(v);
		handleEnterLocationButton(v);
		handleLocationDescrField(v);
		handleRadiusField(v);
		handlePrioritySpinner(v);
		handleDateButton(v);
		handleDoneCheckBox(v);
		handleSaveButton(v);
		return v;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == REQUEST_CODE_DATE) {
			Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
			mLocationTodo.setDueDate(date.getTime());
			updateDateText();
		}
		if (requestCode == REQUEST_CODE_LOCATION) {
			//user found the desired location from the autocomplete
			if(data.getStringArrayExtra(EnterLocationActivity.EXTRA_GEO_LOCATION) != null) {
				String[] locDetails = data.getStringArrayExtra(
						EnterLocationActivity.EXTRA_GEO_LOCATION);
				//update details page with entered location
				updateLocation(locDetails);
			}
		}
	}


	private void updateDateText() {
		Date d = new Date(mLocationTodo.getDueDate());
		if (d != null) {
			CharSequence c = DateFormat.format("EEEE, MMM dd, yyyy", d);
			mDateButton.setText(c);
            mDateButton.setContentDescription(mDateButton.getText());
		}
	}
	
	private void updateLocation(String[] locDetails) {
		if(locDetails != null && locDetails.length ==3) {
			Float lat = parseCoordinate(locDetails[0]);
			mLocationTodo.setLatitude(lat);
			Float lng = parseCoordinate(locDetails[1]);
			mLocationTodo.setLongitude(lng);
			StringBuffer sb = new StringBuffer()
					.append(locDetails[2])
					.append("(Latitude:"+lat + " Longitude:"+lng +")");
			mLocationTodo.setLocationDescr(sb.toString());
			mLocationDescrField.setText(sb.toString());
		} else {
			Log.e(TAG, "updateLocation: Unable to get location details:" + locDetails );
		}
	}
	
	/* ****************** Handle all data fields ********************** */
	
	private void handleNameField(View v) {
		mNameField = (EditText) v.findViewById(R.id.name);
		mNameField.setText(mLocationTodo.getName());
        mNameField.setContentDescription(mNameField.getText());
		mNameField.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence c, int start, int before, int count) {
				mLocationTodo.setName(c.toString().trim());
			}
			public void beforeTextChanged(CharSequence c, int start, int count,
					int after) {}

			public void afterTextChanged(Editable c) {}
		});
	}
	
	private void handleDescrField(View v) {
		mDescrField = (EditText) v.findViewById(R.id.descr);
		mDescrField.setText(mLocationTodo.getSummary());
        mDescrField.setContentDescription(mDescrField.getText());
		mDescrField.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence c, int start, int before, int count) {
				mLocationTodo.setSummary(c.toString().trim());
			}
			public void beforeTextChanged(CharSequence c, int start, int count,
					int after) {}
			public void afterTextChanged(Editable c) {}
		});
	}

    private void handleLocationAlertCheckBox(View view) {
        mLocationAlertCheckBox = (CheckBox)view.findViewById(R.id.enableLocationAlert);
        mLocationAlertCheckBox.setChecked(mLocationTodo.getLocationAlert());
        mLocationAlertCheckBox.setContentDescription(mLocationAlertCheckBox.getText());
        mLocationAlertCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isLocationAlert) {
				mLocationTodo.setLocationAlert(isLocationAlert);
				//if location alert is checked make sure location info is entered
				if (isLocationAlert) {
					Toast t = Toast.makeText(getActivity(), R.string.enter_location,
							Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});
    }
	
	private void handleEnterLocationButton(View v) {
		mEnterLocationButton = (Button) v.findViewById(R.id.enterLocation);
		mEnterLocationButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), EnterLocationActivity.class);
                String[] locList = new String[3];
                locList[0] = Float.toString(mLocationTodo.getLatitude());
                locList[1] = Float.toString(mLocationTodo.getLongitude());
                locList[2] = mLocationTodo.getLocationDescr();
                i.putExtra(EnterLocationActivity.EXTRA_GEO_LOCATION, locList);
				startActivityForResult(i, REQUEST_CODE_LOCATION);
			}
		});
	}
	

	private void handleLocationDescrField(View v) {
		mLocationDescrField = (EditText) v.findViewById(R.id.location_descr);
		mLocationDescrField.setText(mLocationTodo.getLocationDescr());
        mLocationDescrField.setContentDescription(mLocationDescrField.getText());
		mLocationDescrField.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence c, int start, int before, int count) {
				if (c != null) {
					mLocationTodo.setLocationDescr(c.toString().trim());
				}
			}
			public void beforeTextChanged(CharSequence c, int start, int count,
					int after) {}
			public void afterTextChanged(Editable c) {}
		});
	}
	
	private void handleRadiusField(View v) {
		mRadiusField = (EditText) v.findViewById(R.id.radius);
		mRadiusField.setText(floatToString(mLocationTodo.getRadius()));
		mRadiusField.setContentDescription(mRadiusField.getText());

		mRadiusField.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence c, int start, int before, int count) {
				if (c != null) {
					mLocationTodo.setRadius(stringToFloat(c.toString().trim()));
				}
			}
			public void beforeTextChanged(CharSequence c, int start, int count,
					int after) {}
			public void afterTextChanged(Editable c) {}
		});
	}

	private void handlePrioritySpinner(View view) {
        final String[] priorityIdArray = getResources().getStringArray(R.array.priority_values);
		mPrioritySpinner = (Spinner) view.findViewById(R.id.priority);
		int arrayIndex = 0;
        for (int i = 0; i < priorityIdArray.length; i++) {
			if(Integer.parseInt(priorityIdArray[i]) == mLocationTodo.getPriority()) {
				arrayIndex = i;
				break;
			}
		}
		mPrioritySpinner.setSelection(arrayIndex);
        mPrioritySpinner.setContentDescription(mPrioritySpinner.getSelectedItemId()+"");
        mPrioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int idInt = new Integer(id+"");//we know its int value
                mLocationTodo.setPriority(Integer.parseInt(priorityIdArray[idInt]));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
	}
	
	private void handleDateButton(View v) {
		mDateButton = (Button) v.findViewById(R.id.dueDate);
		updateDateText();
		mDateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				FragmentManager fm = getActivity().getSupportFragmentManager();
				DatePickerFragment dialog = DatePickerFragment.newInstance(HelperUtility.
						convertLongToDate(mLocationTodo.
								getDueDate()));
				dialog.setTargetFragment(DetailFragment.this, REQUEST_CODE_DATE);
				dialog.show(fm, DIALOG_DATE);
			}
		});
	}
	
	private void handleDoneCheckBox(View view) {
		mDoneCheckBox = (CheckBox)view.findViewById(R.id.completed);
		mDoneCheckBox.setChecked(mLocationTodo.getCompleted());
		mDoneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mLocationTodo.setCompleted(isChecked);
			}
		});
    }


	private void handleSaveButton(View v) {
		mSaveFab = (FloatingActionButton) v.findViewById(R.id.saveFAB);
		mSaveFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Save button is clicked");
				//first check if user input is valid
				if (!isInputValid()) {
					return;
				}
				//don't worry about geofence first insert/update the locationtodo in db
				Log.d(TAG, "All input is valid, save now");
				saveToDoInDb();
				//now see if we need to add/remove fence
				boolean goToListPage = true;
				boolean saveInDb = false;
				//only create geofence if task is not completed and location alert checkbox is enabled
				// and there is no existing geoFence
				if (mLocationTodo.getLocationAlert()
						&& !mLocationTodo.getCompleted()
						&& mLocationTodo.getGeofenceID() < 1) {
					//Get user permission first, then after getting user permission save in db
					Log.d(TAG, "Adding Geo Fence");
					if (hasGeoFencePermission()) {
						mLocationTodo.setGeofenceID(addGeoFence());
						saveInDb = true;
					} else {
						goToListPage = false;
					}
				}
				if (!mLocationTodo.getLocationAlert()
						|| mLocationTodo.getCompleted()) {
					//make sure there is an existing geofence first
					if (mLocationTodo.getGeofenceID() > 0) {
						Log.d(TAG, "Removing Geo Fence");
						if (removeGeoFence(mLocationTodo.getGeofenceID())) {
							mLocationTodo.setGeofenceID(0);
							saveInDb = true;
							Log.d(TAG, "Geo Fence ID removed");
						}
					}
				}
				if (saveInDb) {
					saveToDoInDb();
				}
				if (goToListPage) {
					goToListActivity();
				}
			}});
	}

	private boolean isInputValid() {
		return validateName() && validateLocationEntered();
	}

    private boolean validateName() {
        if(TextUtils.isEmpty(mLocationTodo.getName())) {
            mNameField.setError(getString(R.string.name_required));
            return false;
        }
        return true;
    }

	private boolean validateLocationEntered() {
        if(mLocationTodo.getLocationAlert() &&
                (mLocationTodo.getLatitude() == FLOAT_DEFAULT || mLocationTodo.getLongitude() == FLOAT_DEFAULT ) ) {
			mLocationDescrField.setError(getString(R.string.location_required));
            return false;
        }
        return true;
    }

	
	/* ************************************************************* */

	private String floatToString(float f) {
		return String.valueOf(f);
	}

	private float stringToFloat(String s) {
		float f = 0.0f;
		try {
			f = Float.valueOf(s);
		} catch (Exception ex) {
			Log.e(TAG, "Error converting String to Float value:"+ex.getMessage());
			ex.printStackTrace();
		}
		return f;
	}
	
	//TODO - remove this, get it from sql lite db after saving
	private int getRandomGeoFenceID() {
		if(randomGenerator == null) {
			randomGenerator = new Random();
		}
		return randomGenerator.nextInt(1000);
	}

    private Float parseCoordinate(String coordinateStr){
		Float f = null;
        if(coordinateStr != null && !coordinateStr.isEmpty()) {
            //we only need precision till 6th decimal place, thats more than plenty
            int index = coordinateStr.indexOf(".");
            int end = index + 7;
            if(coordinateStr.length() < end) {
                end = coordinateStr.length();
            }
            coordinateStr = coordinateStr.substring(0, end);
			try {
				f = Float.parseFloat(coordinateStr);
			} catch (NumberFormatException e) {
				Log.e(TAG, "Error parsing Location Coordinates:"+e.getMessage());
				e.printStackTrace();
			}
		}
        return  f;
    }

	private boolean hasGeoFencePermission() {
		if (ContextCompat.checkSelfPermission(getActivity(),
				android.Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "Don't have permission to create geofence");
			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
					Manifest.permission.ACCESS_FINE_LOCATION)) {
				Log.d(TAG, "Don't have permission to create geofence, user has denied permission in past");
				//TODO - how should I handle this?
				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.

			} else {
				Log.d(TAG, "Asking user for permission");
				// No explanation needed, we can request the permission.
				requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						GeoFenceHelper.PERMISSIONS_ACCESS_FINE_LOCATION_CODE);

			}
			return false;
		} else {
			return true;
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case GeoFenceHelper.PERMISSIONS_ACCESS_FINE_LOCATION_CODE: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "permission was granted by user, add geofence");
					//add geofence to the GooglePlaces LocationService
					int geoFenceID = addGeoFence();
					//update locationtodo in db
					mLocationTodo.setGeofenceID(geoFenceID);
					Log.d(TAG, "Saving LocationTodo in DB, added geofence with ID:"+geoFenceID);
					saveToDoInDb();
					//go to list activity
					goToListActivity();
				} else {
					Log.e(TAG, "onRequestPermissionsResult: USER DENIED PERMISSION " +
							"TO APP ACCESS_FINE_LOCATION");
				}
			}
		}
	}


	private boolean  removeGeoFence(long geoFenceID){
        GeoFenceHelper geoHelper = new GeoFenceHelper(getActivity());
        Log.d(TAG, "Removing Geo Fence with ID:"+geoFenceID);
        List<String> geoFences = new ArrayList<String>();
        geoFences.add(Long.toString(geoFenceID));
        geoHelper.removeGeoFence(geoFences);
        Log.d(TAG, "Request to remove Geo Fence with ID:"+geoFenceID +" sent SUCCESSFULLY");
        return true;
    }

	private int  addGeoFence(){
		GeoFenceHelper geoHelper = new GeoFenceHelper(getActivity());
		int geoFenceID  = getRandomGeoFenceID();
		Log.d(TAG, "Adding Geo Fence with ID:"+geoFenceID);
		geoHelper.addGeoFence(Integer.toString(geoFenceID),
				mLocationTodo.getLatitude(),
				mLocationTodo.getLongitude(),
				mLocationTodo.getRadius());
		Log.d(TAG, "Request to add Geo Fence with ID:"+geoFenceID +" sent SUCCESSFULLY");
		return geoFenceID;
	}


	private void saveToDoInDb() {
		//insert or update we will set the update flag to true
		mLocationTodo.setUpdated(true);
		mLocationTodo.setDeleted(false);

		//insert
		if (mLocationTodo.getId() <= 0) {
			Uri createdTodoUri = getActivity().getContentResolver().insert(
					ContentProviderContract.LocationTodoEntry.CONTENT_URI,
					ContentProviderContract.LocationTodoEntry.loadContentValues(mLocationTodo));
			if (createdTodoUri != null) {
				mLocationTodo.setId(ContentUris.parseId(createdTodoUri));
				Log.d(TAG, "Saved Location TODO:" + mLocationTodo);
			} else {
				Log.e(TAG, "Error Saving Location TODO:" + mLocationTodo);
			}

		} else {
			//update
			int noOfRowsUpdated =
					getActivity().getContentResolver().update(
							ContentProviderContract.LocationTodoEntry.getLocationTodoWithUserUri(
									mLocationTodo.getUserId(),mLocationTodo.getId()),
							ContentProviderContract.LocationTodoEntry.loadContentValues(mLocationTodo),
							ContentProviderContract.LocationTodoEntry._ID+ " = ?",
							new String[]{mLocationTodo.getId()+""});
			Log.d(TAG, "Updated Location TODO:" + mLocationTodo);
			if (noOfRowsUpdated > 0) {
				Log.d(TAG, "Saved Location TODO:" + mLocationTodo);
			} else {
				Log.e(TAG, "Error Saving Location TODO:" + mLocationTodo);
			}
		}
		//notifying widgets about data update
		Log.d(TAG, "Refresh widgets..sending the broadcast");
		Intent dataRefreshIntent = new Intent(
				ContentProviderContract.ACTION_WIDGET_DATA_UPDATED).setPackage(getActivity().getPackageName());
		getActivity().sendBroadcast(dataRefreshIntent);
	}

	private void goToListActivity() {
		Intent i = new Intent(getActivity(), ListActivity.class);
		i.putExtra(HelperUtility.EXTRA_USER_ID, mLocationTodo.getUserId());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}



}
