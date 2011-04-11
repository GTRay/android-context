package edu.fsu.cs.contextprovider.services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.os.IBinder;
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
	private static LocationManager manager;
	private static LocationListener listener;
	private static boolean running = false;
	private static boolean isreliable = false;
	private static Location currentLocation = null;
	
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
	
	/**
	 * Often times the GPS goes in and out of service.  We perform some logic to determine
	 * if the current GPS reading is reliable or not.
	 * 
	 * @return
	 */
	public static boolean isReliable() {
		return isreliable;
	}
	
	public void onCreate () {
		Log.i(TAG, "Created()");
		listener = new LocationListener() {
			public void onLocationChanged(Location location) {
				Log.i(TAG, "New location found: [" + location.getLongitude() + "," + location.getLatitude() + "] | Speed: [" + location.getSpeed() + "]");
				currentLocation.set(location);
			}

			public void onProviderDisabled(String provider) {
				//manager.removeUpdates(this);
				//isreliable = false;
			}

			public void onProviderEnabled(String provider) {
				//isreliable = true;
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
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
		
		currentLocation = new Location(LocationManager.GPS_PROVIDER);
		manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);

	}

	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(TAG, "Service has been started.");
		running = true;
		isreliable = true;
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