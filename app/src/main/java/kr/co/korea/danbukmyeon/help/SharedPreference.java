package kr.co.korea.danbukmyeon.help;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {

	private static Context mContext;

	public SharedPreference(Context c) {
		mContext = c;
	}

	public String getPreferences(String key) {
		SharedPreferences pref = mContext.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
		return pref.getString(key, null);
	}

	public long getIntPreferences(String key) {
		SharedPreferences pref = mContext.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
		return pref.getInt(key, 0);
	}

	public long getLongPreferences(String key) {
		SharedPreferences pref = mContext.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
		return pref.getLong(key, 0);
	}

	public boolean getBoolPreferences(String key) {
		SharedPreferences pref = mContext.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
		return pref.getBoolean(key, false);
	}

	public void setPreferences(String key, String value) {
		SharedPreferences pref = mContext.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void setPreferences(String key, int value) {
		SharedPreferences pref = mContext.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public void setPreferences(String key, long value) {
		SharedPreferences pref = mContext.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putLong(key, value);
		editor.commit();
	}

	public void setPreferences(String key, boolean value) {
		SharedPreferences pref = mContext.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public void removePreferences(String key) {
		SharedPreferences pref = mContext.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.remove(key);
		editor.commit();
	}

	public void removeAllPreferences() {
		SharedPreferences pref = mContext.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.clear();
		editor.commit();
	}
}
