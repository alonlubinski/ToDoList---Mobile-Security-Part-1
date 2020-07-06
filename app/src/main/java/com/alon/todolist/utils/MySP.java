package com.alon.todolist.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class MySP {

    private static MySP instance;
    private SharedPreferences sharedPreferences;

    public static MySP getInstance(){
        return instance;
    }

    private MySP(Context context){
        sharedPreferences = context.getApplicationContext().getSharedPreferences("sp", Context.MODE_PRIVATE);
    }

    public static MySP initHelper(Context context){
        if(instance == null){
            instance = new MySP(context);
        }
        return instance;
    }

    public void putString(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean putStringSync(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public String getString(String key, String defValue){
        return sharedPreferences.getString(key, defValue);
    }

    public void putBoolean(String key, Boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBoolean(String key, Boolean defValue){
        return sharedPreferences.getBoolean(key, defValue);
    }
}
