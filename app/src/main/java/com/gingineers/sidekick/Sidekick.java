package com.gingineers.sidekick;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class Sidekick extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
