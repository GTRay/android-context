/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.fsu.cs.contextprovider;

import android.app.ExpandableListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import edu.fsu.cs.contextprovider.ContextListActivity.ContextListItem;
import edu.fsu.cs.contextprovider.monitor.LocationMonitor;
import edu.fsu.cs.contextprovider.monitor.MovementMonitor;
import edu.fsu.cs.contextprovider.rpc.ContextProviderService;
import edu.fsu.cs.contextprovider.rpc.IContextProviderService;
import edu.fsu.cs.contextprovider.sensor.TelephonyService;
import edu.fsu.cs.contextprovider.weather.GoogleWeatherHandler;
import edu.fsu.cs.contextprovider.weather.WeatherSet;

/**
 * Demonstrates expandable lists backed by a Simple Map-based adapter
 */
public class ContextExpandableListActivity extends ExpandableListActivity {
	private static final String TAG = "ContextExpandableListActivity";

	private static final String NAME = "NAME";
	private static final String VALUE = "VALUE";

	private ExpandableListAdapter mAdapter;

	private static final int REFRESH_ID = Menu.FIRST + 1;
	private static final int ADD_ID = Menu.FIRST + 2;
	private static final int EDIT_ID = Menu.FIRST + 3;
	private static final int DELETE_ID = Menu.FIRST + 4;
	private static final int GEO_ID = Menu.FIRST + 5;
	private static final int WEATHER_ID = Menu.FIRST + 6;
	private static final int PHONE_ID = Menu.FIRST + 7;
	private static final int SMS_ID = Menu.FIRST + 8;

	public static boolean running = false;

	List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
	List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();

	public static IContextProviderService mService = null;

	Vector<ContextListItem> Clist = new Vector<ContextListItem>();
	// ArrayAdapter<ContextListItem> adapter = null;

	private ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IContextProviderService.Stub.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName name) {
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		running = true;
		Intent intent = null;
		/* Start GPS Service */
		intent = new Intent(this.getApplicationContext(), edu.fsu.cs.contextprovider.sensor.GPSService.class);
		startService(intent);

		/* Start Network Service */
		intent = new Intent(this.getApplicationContext(), edu.fsu.cs.contextprovider.sensor.NetworkService.class);
		startService(intent);

		/* Start Accelerometer Service */
		intent = new Intent(this.getApplicationContext(), edu.fsu.cs.contextprovider.sensor.AccelerometerService.class);
		startService(intent);

		/* Start movement context */
		MovementMonitor.StartThread(5);

		/* Start LocationMonitor */
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		LocationMonitor.StartThread(5, geocoder);

		/* Start System/Phone/SMS State Monitor Services */
		intent = new Intent(this.getApplicationContext(), edu.fsu.cs.contextprovider.sensor.TelephonyService.class);
		startService(intent);

		/* Start ContextProviderService */
		bindService(new Intent(this, ContextProviderService.class), conn, Context.BIND_AUTO_CREATE);

		refreshLocation();
		refreshMovement();
		refreshProximity();
		refreshWeather();
		refreshSystem();
		refreshTelephony();
		refreshDerived();

		// Set up our adapter
		mAdapter = new SimpleExpandableListAdapter(this, groupData, android.R.layout.simple_expandable_list_item_1, new String[] { NAME, VALUE }, new int[] { android.R.id.text1, android.R.id.text2 },
				childData, android.R.layout.simple_expandable_list_item_2, new String[] { NAME, VALUE }, new int[] { android.R.id.text1, android.R.id.text2 });

		setListAdapter(mAdapter);
	}

	private void refresh() {
		if (mService == null) {
			return;
		}
		Map<String, String> cntx = null;
		try {
			cntx = (Map<String, String>) mService.getAll();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//groupData.remove(0);
		groupData.clear();
		childData.clear();
		
		
		refreshLocation();
		refreshMovement();
		refreshProximity();
//		refreshWeather();
//		refreshSystem();
//		refreshTelephony();
//		refreshDerived();
		
		
		BaseExpandableListAdapter tester = (BaseExpandableListAdapter) mAdapter;
		tester.notifyDataSetChanged();
		// tester.notifyDataSetInvalidated();

		for (Map.Entry<String, String> entry : cntx.entrySet()) {
			// ContextListItem item = new ContextListItem();
			// item.setName(entry.getKey());
			// item.setValue(entry.getValue());
			// adapter.add(item);
		}
	}

	private void refreshLocation() {
		if (mService == null) {
			return;
		}
		Map<String, String> locationMap = new HashMap<String, String>();
		groupData.add(locationMap);
		locationMap.put(NAME, "Location");
		locationMap.put(VALUE, "Location");
		List<Map<String, String>> location = new ArrayList<Map<String, String>>();
		for (int j = 0; j < 5; j++) {
			Map<String, String> curChildMap = new HashMap<String, String>();
			location.add(curChildMap);
			curChildMap.put(NAME, "Child " + j);
			curChildMap.put(VALUE, "Value");
		}
		childData.add(location);		
	}

	private void refreshMovement() {
		if (mService == null) {
			return;
		}
	Map<String, String> movementMap = new HashMap<String, String>();
	groupData.add(movementMap);
	movementMap.put(NAME, "Movement");
	movementMap.put(VALUE, "Movement");
	List<Map<String, String>> movement = new ArrayList<Map<String, String>>();
	for (int j = 0; j < 5; j++) {
		Map<String, String> curChildMap = new HashMap<String, String>();
		movement.add(curChildMap);
		curChildMap.put(NAME, "Child " + j);
		curChildMap.put(VALUE, "Value");
	}
	childData.add(movement);
	}
	
	
	private void refreshProximity() {
		if (mService == null) {
			return;
		}
		Map<String, String> proximityMap = new HashMap<String, String>();
		groupData.add(proximityMap);
		proximityMap.put(NAME, "Proximity");
		proximityMap.put(VALUE, "Proximity");
		List<Map<String, String>> proximity = new ArrayList<Map<String, String>>();
		for (int j = 0; j < 5; j++) {
			Map<String, String> curChildMap = new HashMap<String, String>();
			proximity.add(curChildMap);
			curChildMap.put(NAME, "Child " + j);
			curChildMap.put(VALUE, "Value");
		}
		childData.add(proximity);
	}
	
	private void refreshWeather() {
		if (mService == null) {
			return;
		}
		Map<String, String> weatherMap = new HashMap<String, String>();
		groupData.add(weatherMap);
		weatherMap.put(NAME, "Weather");
		weatherMap.put(VALUE, "Weather");
		List<Map<String, String>> weather = new ArrayList<Map<String, String>>();
		for (int j = 0; j < 5; j++) {
			Map<String, String> curChildMap = new HashMap<String, String>();
			weather.add(curChildMap);
			curChildMap.put(NAME, "Child " + j);
			curChildMap.put(VALUE, "Value");
		}
		childData.add(weather);
	}
	
	private void refreshSystem() {
		if (mService == null) {
			return;
		}
		Map<String, String> systemMap = new HashMap<String, String>();
		groupData.add(systemMap);
		systemMap.put(NAME, "System");
		systemMap.put(VALUE, "System");
		List<Map<String, String>> system = new ArrayList<Map<String, String>>();
		for (int j = 0; j < 5; j++) {
			Map<String, String> curChildMap = new HashMap<String, String>();
			system.add(curChildMap);
			curChildMap.put(NAME, "Child " + j);
			curChildMap.put(VALUE, "Value");
		}
		childData.add(system);
	}
	
	private void refreshTelephony() {
		if (mService == null) {
			return;
		}
		Map<String, String> telephonyMap = new HashMap<String, String>();
		groupData.add(telephonyMap);
		telephonyMap.put(NAME, "Telephony");
		telephonyMap.put(VALUE, "Telephony");
		List<Map<String, String>> telephony = new ArrayList<Map<String, String>>();
		for (int j = 0; j < 5; j++) {
			Map<String, String> curChildMap = new HashMap<String, String>();
			telephony.add(curChildMap);
			curChildMap.put(NAME, "Child " + j);
			curChildMap.put(VALUE, "Value");
		}
		childData.add(telephony);
	}
	
	private void refreshDerived() {
		if (mService == null) {
			return;
		}
		Map<String, String> derivedMap = new HashMap<String, String>();
		groupData.add(derivedMap);
		derivedMap.put(NAME, "Derived");
		derivedMap.put(VALUE, "Derived");
		List<Map<String, String>> derived = new ArrayList<Map<String, String>>();
		for (int j = 0; j < 5; j++) {
			Map<String, String> curChildMap = new HashMap<String, String>();
			derived.add(curChildMap);
			curChildMap.put(NAME, "Child " + j);
			curChildMap.put(VALUE, "Value");
		}
		childData.add(derived);
	}
	
	
	@Override
	public void onDestroy() {
		MovementMonitor.StopThread();
		running = false;
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, "Refresh").setIcon(R.drawable.add).setAlphabeticShortcut('a');

		menu.add(Menu.NONE, ADD_ID, Menu.NONE, "Add").setIcon(R.drawable.add).setAlphabeticShortcut('a');
		menu.add(Menu.NONE, GEO_ID, Menu.NONE, "Geo Loc").setIcon(R.drawable.add).setAlphabeticShortcut('g');
		menu.add(Menu.NONE, WEATHER_ID, Menu.NONE, "Weather").setIcon(R.drawable.add).setAlphabeticShortcut('w');
		menu.add(Menu.NONE, PHONE_ID, Menu.NONE, "Phone").setIcon(R.drawable.add).setAlphabeticShortcut('p');
		menu.add(Menu.NONE, SMS_ID, Menu.NONE, "SMS").setIcon(R.drawable.add).setAlphabeticShortcut('s');

		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case REFRESH_ID:
			refresh();
			// adapter.notifyDataSetChanged();
			return true;
		case ADD_ID:
			// add();
			return (true);
		case GEO_ID:
			Toast.makeText(getApplicationContext(), "Trying to get geolocation", Toast.LENGTH_SHORT).show();
			String res = LocationMonitor.getZip();
			Toast.makeText(getApplicationContext(), "GeoLocation: " + res, Toast.LENGTH_LONG).show();
			return true;

		case WEATHER_ID:
			Toast.makeText(getApplicationContext(), "Trying to get weather", Toast.LENGTH_SHORT).show();
			String zip = LocationMonitor.getZip();
			String weather = "weather unavailable";
			URL url;

			try {
				// String cityParamString = "32304"; // "Tallahassee,Florida";
				String cityParamString = zip;
				Log.d(TAG, "cityParamString: " + cityParamString);
				String queryString = "http://www.google.com/ig/api?weather=" + cityParamString;
				/* Replace blanks with HTML-Equivalent. */
				url = new URL(queryString.replace(" ", "%20"));
				/* Get a SAXParser from the SAXPArserFactory. */
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				/* Get the XMLReader of the SAXParser we created. */
				XMLReader xr = sp.getXMLReader();
				/* Create a new ContentHandler and apply it to the XML-Reader */
				GoogleWeatherHandler gwh = new GoogleWeatherHandler();
				xr.setContentHandler(gwh);
				/* Parse the xml-data our URL-call returned. */
				xr.parse(new InputSource(url.openStream()));
				/* Our Handler now provides the parsed weather-data to us. */
				WeatherSet ws = gwh.getWeatherSet();
				weather = ws.getWeatherCurrentCondition().getCondition() + " " + ws.getWeatherCurrentCondition().getTempFahrenheit() + " degrees F" + ws.getWeatherCurrentCondition().getHumidity()
						+ " humidity" + ws.getWeatherCurrentCondition().getWindCondition() + " ";

			} catch (Exception e) {
				Log.e(TAG, "WeatherQueryError", e);
			}

			Toast.makeText(getApplicationContext(), "Current Conditions: " + weather, Toast.LENGTH_LONG).show();
			return true;
		case PHONE_ID:
			Toast.makeText(getApplicationContext(), "Trying to get recent phone state", Toast.LENGTH_SHORT).show();
			Toast.makeText(getApplicationContext(), "State: " + TelephonyService.PHONE_STATE + "Updated: " + TelephonyService.PHONE_STATE_UPDATE, Toast.LENGTH_LONG).show();
			return true;
		case SMS_ID:
			Toast.makeText(getApplicationContext(), "Trying to get recent SMS state", Toast.LENGTH_SHORT).show();
			Toast.makeText(getApplicationContext(), "State: " + TelephonyService.SMS_STATE + "Updated: " + TelephonyService.SMS_STATE_UPDATE, Toast.LENGTH_LONG).show();
			return true;

		}

		return (super.onOptionsItemSelected(item));
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, "Delete").setIcon(R.drawable.delete).setAlphabeticShortcut('d');
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			// delete(info.id);
			return true;

		}
		return (super.onOptionsItemSelected(item));
	}

}