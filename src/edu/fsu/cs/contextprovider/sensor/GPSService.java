package edu.fsu.cs.contextprovider.sensor;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import edu.fsu.cs.contextprovider.ContextBrowserActivity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;

import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
//import android.content.ServiceConnection;
//import android.os.Binder;

/**
 * Most objects are declared static because we want only 1 instance.  We could have instead used an iBinder but it is a bit
 * of overkill for this situation.
 * @author meyers
 *
 */
public class GPSService extends Service
{
	private static final String TAG = "GPSService";
	private static final boolean DEBUG_TTS = true;
	private static final String locationType = LocationManager.GPS_PROVIDER;
	private static LocationManager manager;
	private static LocationListener listener;
	@SuppressWarnings("unused")
	private static boolean running = false;
	private static boolean isreliable = false;
	private static Location currentLocation = new Location(locationType);
	private static String currentZip = null;
	private static String currentAddress = null;
	
//	private final IBinder mBinder = new LocalBinder();
//    private static boolean mBound = false;
//    public static GPS RPC = null;
//    public static ServiceConnection mConnection = null;

//	public class LocalBinder extends Binder {
//		GPS getService() {
//			// Return this instance of LocalService so clients can call public methods
//			return GPS.this;
//		}
//	}

	@Override
	public IBinder onBind(Intent intent) {
//		return mBinder;
		return null;
	}

	/** method for clients */
	public static int getLatitude() {
		return (int)currentLocation.getLatitude();
	}

	public static int getLongitude() {
		return (int)currentLocation.getLongitude();
	}
//
//	public static GeoPoint getGeoLocation() {
//		return new GeoPoint((int)currentLocation.getLatitude(), (int)currentLocation.getLongitude());
//	}
	
	public static float getSpeed() {
		return currentLocation.getSpeed();
	}
	
	public static long getTime() {
		return currentLocation.getTime();
	}
	
	public static double getAltitude() {
		return currentLocation.getAltitude();
	}
	
	/* TODO: Maybe convert this to N, S, E, or W */
	public static float getBearing() {
		return currentLocation.getBearing();
	}
	
	public static float proximityTo(String loc) {
		return 0;
	}
	
	public static String getZip() {
		return currentZip;
	}
	
	public static String getAddress() {
		return currentAddress;
	}
	
	/**
	 * Often times the GPS goes in and out of service.  We perform some logic to determine
	 * if the current GPS reading is reliable or not.
	 * 
	 * @return
	 */
	public static boolean isReliable() {
		return isreliable;
	}
	
	private void updateGeoLocation() {
		try{
			Geocoder gcd = new Geocoder(this, Locale.getDefault());
			List<Address> addresses = 
				gcd.getFromLocation(GPSService.getLatitude(), GPSService.getLongitude(),1);
			if (addresses.size() > 0) {
				Address address =  addresses.get(0);
				currentZip = address.getPostalCode();
				currentAddress = address.toString();
			}
		}
		catch(IOException ex){
			Log.i(TAG, "IO: " + ex);
		}
	}
	
	public void onCreate() {
		
		listener = new LocationListener() {
			public void onLocationChanged(Location location) {
				isreliable = true;
//				String result = getFromLocation();
	            // TODO: We should lock the below to make our location update atomic
//	            currentCity = result;
				currentLocation.set(location);
				
				updateGeoLocation();
				Log.i(TAG, "New location found: [" + location.getLongitude() + "," + location.getLatitude() + "] | Speed: [" + location.getSpeed() + "] | Zip: [" + currentZip + "]");
			}

			public void onProviderDisabled(String provider) {
				//manager.removeUpdates(this);
				Log.i(TAG, locationType + ": is no longer reliable");
				if (DEBUG_TTS == true) {
					ContextBrowserActivity.tts.speak("GPS reliability is " + String.valueOf(isreliable), TextToSpeech.QUEUE_FLUSH, null);
				}
				isreliable = false;
			}

			public void onProviderEnabled(String provider) {
				Log.i(TAG, locationType + ": is reliable");
				if (DEBUG_TTS == true) {
					ContextBrowserActivity.tts.speak("GPS reliability is " + String.valueOf(isreliable), TextToSpeech.QUEUE_FLUSH, null);
				}
				isreliable = true;
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				switch (status) {
				case LocationProvider.OUT_OF_SERVICE:
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					if (DEBUG_TTS == true) {
						ContextBrowserActivity.tts.speak("GPS reliability is " + String.valueOf(isreliable), TextToSpeech.QUEUE_FLUSH, null);
					}
					isreliable = false;
					break;
				case LocationProvider.AVAILABLE:
					if (DEBUG_TTS == true) {
						ContextBrowserActivity.tts.speak("GPS reliability is " + String.valueOf(isreliable), TextToSpeech.QUEUE_FLUSH, null);
					}
					isreliable = true;
					break;
				}
				// TODO Check the status here to update isreliable
			}
		};
//		
//		/**
//		 * Makes others lives easier.
//		 * 
//		 * Normally, mService and mConnection would be defined in the class (Main) that wishes
//		 * to call GPS.java functions.  Instead, we declare these variables static and inside GPS.java
//		 * so that the calling function doesn't have to know how a Binder works.  Users wishing to call
//		 * GPS.java functionality call simply call GPS.RPC.xxx().  This model works because we want
//		 * only one instance of GPS but may have multiple Threads bound to it.
//		 * 
//		 */
//		mConnection = new ServiceConnection() {
//
//	        @Override
//	        public void onServiceConnected(ComponentName className,
//	                IBinder service) {
//	            // We've bound to LocalService, cast the IBinder and get LocalService instance
//	            LocalBinder binder = (LocalBinder) service;
//	            RPC = binder.getService();
//	            mBound = true;
//	        }
//
//	        @Override
//	        public void onServiceDisconnected(ComponentName arg0) {
//	            mBound = false;
//	        }
//	    };
		

		manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
		manager.requestLocationUpdates(locationType, 0, 0, listener);


	}

	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(TAG, "Service has been started.");
		running = true;
		isreliable = false;
		return 0;
	}

	public void onDestroy()
	{
		running = false;
		isreliable = false;
		Log.i("GPS", "Stopping the service now.");
		manager.removeUpdates(listener);
		stopSelf();
	}
	

}
