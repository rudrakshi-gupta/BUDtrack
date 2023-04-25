package com.example.budtrack.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.example.budtrack.Utils.Common;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.paperdb.Paper;

public class MyLocationReciever extends BroadcastReceiver {
    public static final String ACTION = "com.example.budtrack.UPDATE_LOCATION";

    DatabaseReference publicLocation;
    String uid;

    public MyLocationReciever() {
        publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Paper.init(context);
        uid = Paper.book().read(Common.USER_UID_SAVE_KEY);

        if (intent != null) {

            final String action = intent.getAction();
            if (action.equals(ACTION)) {
                LocationResult result = LocationResult.extractResult(intent);

                if (result != null) {
                    Location location = result.getLastLocation();
                    if (Common.loggedUser != null) //App in foreground
                    {
                        Log.i("Location", "Location");
                        publicLocation.child(Common.loggedUser.getUid()).setValue(location);
                    } else //App is killed
                    {
                        publicLocation.child(uid).setValue(location);
                    }
                }
            }
        }

    }
}

