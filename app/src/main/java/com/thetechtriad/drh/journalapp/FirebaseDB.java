package com.thetechtriad.drh.journalapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDB extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
