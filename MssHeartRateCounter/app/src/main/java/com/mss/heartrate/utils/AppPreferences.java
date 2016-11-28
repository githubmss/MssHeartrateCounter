package com.mss.heartrate.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AppPreferences {
	SharedPreferences			pref;
	Editor						editor;
	public static Context		mContext;
	int							PRIVATE_MODE	= 0;
	private static final String	PREF_NAME		= "AndroidMSSNative_Prefrence";

	@SuppressLint("CommitPrefEdits")
	public AppPreferences(Context context) {
		this.mContext = context;
		pref = AppPreferences.mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	public static void init(Context context) {
		AppPreferences.mContext = context;

	}

	public static String getLastModified() {
		return PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext).getString("lastModified", "");
	}

	public static void setLastModified(String timeStamp) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext).edit();
		editor.putString("lastModified", timeStamp);
		editor.commit();
	}

	public static void setPreferenceRelaod(String key, int value) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static int getPreferenceRelaod(String key, int defaultValue) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
		return preferences.getInt(key, defaultValue);
	}

	public static boolean getPrefrenceBoolean(String keyName) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
		return preferences.getBoolean(keyName, false);
	}

	public static void setPrefrenceBoolean(String keyName, Boolean booleanValue) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(keyName, booleanValue);
		editor.commit();
	}

	public String getPrefrenceString(String keyName) {
		return pref.getString(keyName, "");
	}

	public void setPrefrenceString(String keyName, String stringValue) {
		editor.putString(keyName, stringValue);
		editor.commit();
	}

	public int getPrefrenceInt(String keyName) {
		return pref.getInt(keyName, 0);
	}

	public void setPrefrenceInt(String keyName, int intValue) {
		editor.putInt(keyName, intValue);
		editor.commit();
	}

	public long getPrefrenceLong(String keyName) {
		return pref.getLong(keyName, 0);
	}

	public void setPrefrenceLong(String keyName, Long intValue) {
		editor.putLong(keyName, intValue);
		editor.commit();
	}

	public void setPreferenceNotificationCount(String key, int value) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public int getPreferenceNotificationCount(String key, int defaultValue) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
		return preferences.getInt(key, defaultValue);
	}
	///////////////////////////////////////////////////////////
	public static void saveArray(String key, ArrayList<Integer> array) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
		SharedPreferences.Editor editor = preferences.edit();
		JSONArray jArray = new JSONArray(array);
		editor.remove(key);
		editor.putString(key, jArray.toString());
		editor.commit();
	}

	public static ArrayList<Integer> getArray(String key) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
		ArrayList<Integer> array = new ArrayList<Integer>();
		String jArrayString = preferences.getString(key, "NOPREFSAVED");
		if (jArrayString.matches("NOPREFSAVED"))
			return getDefaultArray();
		else {
			try {
				JSONArray jArray = new JSONArray(jArrayString);
				for (int i = 0; i < jArray.length(); i++) {
					array.add(jArray.getInt(i));
				}
				return array;
			} catch (JSONException e) {
				return getDefaultArray();
			}
		}
	}
	private static ArrayList<Integer> getDefaultArray() {
		ArrayList<Integer> array = new ArrayList<Integer>();
//		array.add("Example 1");
//		array.add("Example 2");
//		array.add("Example 3");
		return array;
	}
	
}
