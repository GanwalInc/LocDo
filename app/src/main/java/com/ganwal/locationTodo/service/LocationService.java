package com.ganwal.locationTodo.service;

import android.util.Log;

import com.ganwal.locationTodo.LocationPlace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LocationService {
	
	private static final String TAG = LocationService.class.getName();
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String PLACES_API_AUTOCOMPLETE = "/autocomplete";
	private static final String PLACES_API_DETAILS = "/details";
	private static final String PLACES_API_JSON = "/json";

	//get list of auto-complete suggestions from Google places API
	public List<LocationPlace> getSuggestion(String userInput) {
		List<LocationPlace> results = new ArrayList<LocationPlace>();
		StringBuilder sb = new StringBuilder(PLACES_API_BASE
				+ PLACES_API_AUTOCOMPLETE + PLACES_API_JSON);
	/*	sb.append("?key=" + R.string.google_api_key);*/
		sb.append("&components=country:us");

		URL url;
		try {
			sb.append("&input=" + URLEncoder.encode(userInput, "utf8"));
			url = new URL(sb.toString());
			StringBuilder resultStr = getResults(url);
			results = parseAutoCompleteJSONOutput(resultStr.toString());
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "getSuggestion: Error encoding URL", e);
		} catch (MalformedURLException e) {
			Log.e(TAG, "getSuggestion: Error creating URL", e);
		}
		// parse the result
		return results;
	}

	//get the details of a location user has picked by calling Google Places API details page
	public List<String> getDetails(String userInput) {
		List<String> results = new ArrayList<String>();
		StringBuilder sb = new StringBuilder(PLACES_API_BASE
				+ PLACES_API_DETAILS + PLACES_API_JSON);
		/*sb.append("?key=" + R.string.google_api_key);*/
		URL url;
		try {
			sb.append("&placeid=" + URLEncoder.encode(userInput, "utf8"));
			url = new URL(sb.toString());
			Log.d(TAG, "Details executing URL:" + sb.toString());
			StringBuilder resultStr = getResults(url);
			results = parseDetailsJSONOutput(resultStr.toString());
			
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "getDetails: Error encoding URL", e);
		} catch (MalformedURLException e) {
			Log.e(TAG, "getDetails: Error creating URL", e);
		}
		// parse the result
		return results;
	}

	/* ****************** Private methods ********************** */
	// get output from Google Places API
	private StringBuilder getResults(URL url) {
		HttpURLConnection conn = null;
		StringBuilder result = new StringBuilder();
		try {
            Log.d(TAG, "Opening URL for Google Play Services:"+url.toString());
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
            Log.d(TAG, "Remove it later  Inputstream:"+in.toString());
			int readChar;
			char[] charBuffer = new char[1024];
			while ((readChar = in.read(charBuffer)) != -1) {
				result.append(charBuffer, 0, readChar);
			}
		} catch (IOException e) {
			Log.e(TAG, "getResults: Error connecting to Places API", e);
            Log.e(TAG, "Error is"+e.toString() );
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}

	// parse the output returned back from google places API
	private List<LocationPlace> parseAutoCompleteJSONOutput(String jsonResults) {
		List<LocationPlace> resultList = null;
		try {
			// Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

			// Extract the Place descriptions from the results
			resultList = new ArrayList<LocationPlace>(predsJsonArray.length());
			for (int i = 0; i < predsJsonArray.length(); i++) {
				JSONObject placeObj = predsJsonArray.getJSONObject(i);
				LocationPlace place = new LocationPlace(placeObj.getString("place_id"), 
						placeObj.getString("description"));
				resultList.add(place);
			}

		} catch (JSONException e) {
			Log.e(TAG, "parseJSONOutput: Cannot process JSON results", e);
		}
		return resultList;

	}

	// parse the output returned back from google places API
	private List<String> parseDetailsJSONOutput(String jsonResults) {
		List<String> resultList = null;
		try {
			Log.d(TAG, "Details returned result:" + jsonResults);
			// Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults);
			JSONObject resObj = jsonObj.getJSONObject("result");
			String name = resObj.getString("name");
			String addr = resObj.getString("formatted_address");
			JSONObject geoObj = resObj.getJSONObject("geometry");
			JSONObject locObj = geoObj.getJSONObject("location");
			resultList = new ArrayList<String>();
			String latStr = locObj.getString("lat");
			resultList.add(latStr);
			String lngStr = locObj.getString("lng");
			resultList.add(lngStr);
			String desc = name + " "+ "\n" + addr;
			resultList.add(desc);
		} catch (JSONException e) {
			Log.e(TAG, "parseJSONOutput: Cannot process JSON results", e);
		}
		return resultList;
	}

}
