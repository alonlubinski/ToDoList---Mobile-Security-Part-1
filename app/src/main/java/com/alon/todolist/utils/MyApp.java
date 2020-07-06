package com.alon.todolist.utils;

import android.app.Application;

import com.alon.todolist.utils.MySP;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MySP.initHelper(this);
    }
}
