package com.mss.heartrate;

import java.util.ArrayList;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedArrayList {
    Context mContext;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;

    public SharedArrayList(Context context) {
        this.mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mPrefs.edit();
    }
}