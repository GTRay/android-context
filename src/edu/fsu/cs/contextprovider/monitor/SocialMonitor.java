package edu.fsu.cs.contextprovider.monitor;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import net.smart_entity.EntityManager;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;

import android.util.Log;


public class SocialMonitor extends TimerTask {
	private static final String TAG = "SocialMonitor";
	private static final boolean DEBUG = true;
	private static final boolean DEBUG_TTS = false;

	private static Timer timer = new Timer();
	private static SocialMonitor twitterObj = new SocialMonitor();
	private static boolean running = false;
	private static Twitter twitter = null;
	private static String currentTwitterStatus = new String();
	
	public static String contact = "Contact Name";
	public static String communication = "CommType";
	public static String message = "Message";
	public static Long lastIn = (long) 0.0;
	public static Long lastOut = (long) 0.0;
	
	Long now = Long.valueOf(System.currentTimeMillis());	
	
	EntityManager entityManager;


	/**
	 * Create a timer/thread to continuous run and keep the getMovement() state up to date
	 * 
	 * @param interval rate at which to run the thread, in seconds
	 */
	public static void StartThread(int interval) {
		if (running == true) {
			return;
		}
		Log.i(TAG, "Start()");
		timer.schedule(twitterObj, 100, interval*1000);
		running = true;
		if (twitter == null) {
			twitter = new Twitter("crm04d@fsu.edu","android");
		}
	}

	/**
	 * Stop the thread/timer that keeps the movement state up to date
	 */
	public static void StopThread() {
		Log.i(TAG, "Stop()");
		timer.purge();
		twitterObj = new SocialMonitor();
		running = false;
	}

	@Override
	public void run() {
		String tmp = null;
		if (twitter != null) {
			try { 
				tmp = twitter.getStatus("crm04d").toString();
			} catch(TwitterException x) { 
				Log.e(TAG, "Twitter failed [" + x + "]");
			}
			if (tmp != null) {
				setCurrentTwitterStatus(tmp);
			}
			if (DEBUG == true) Log.i(TAG, "Twitter Status: " + tmp);
		} else {
			if (DEBUG == true) Log.i(TAG, "Twitter is null");
		}
	}


	public static void setCurrentTwitterStatus(String currentTwitterStatus) {
		SocialMonitor.currentTwitterStatus = currentTwitterStatus;
	}

	public static String getCurrentTwitterStatus() {
		return currentTwitterStatus;
	}

	public static String getContact() {
		return contact;
	}
	
	public static String getCommunication() {
		return communication;
	}

	public static String getMessage() {
		return message;
	}

	public static Long getLastOut() {
		return lastOut;
	}

	public static Long getLastIn() {
		return lastIn;
	}
	
	public static Date getLastInDate() {
		return new Date(lastIn);
	}
	
	public static Date getLastOutDate() {
		return new Date(lastOut);
	}
}
