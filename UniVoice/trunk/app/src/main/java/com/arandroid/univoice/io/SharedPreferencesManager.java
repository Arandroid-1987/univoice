package com.arandroid.univoice.io;

import android.content.Context;
import android.content.SharedPreferences;

import com.arandroid.univoice.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class SharedPreferencesManager {
    private SharedPreferences preferences;
    private static SharedPreferencesManager instance;
    private Gson gson = new GsonBuilder().create();

    private SharedPreferencesManager(Context context) {
        preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public static SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context);
        }
        return instance;
    }

    public <T> T read(String key, Type type) {
        return gson.fromJson(preferences.getString(key, ""), type);
    }

    public <T> void write(String key, T object) {
        String json = gson.toJson(object);
        preferences.edit().putString(key, json).apply();
    }
}
