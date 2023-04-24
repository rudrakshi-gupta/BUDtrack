package com.example.budtrack;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.budtrack.Model.User;
import com.example.budtrack.Utils.Common;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Arrays;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    DatabaseReference user_information;
    private  static final int MY_REQUEST_CODE=7117;
    List<AuthUI.IdpConfig> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Paper.init(this);

        //firebase
        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);

        //provider
        providers= Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        //request permission location
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener(){
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {//report.areAllPermissionsGranted()
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            boolean Islogin = prefs.getBoolean("Islogin", false);
                            showSignInOptions();
//                            setupUI();
                        } else {
                            Toast.makeText(MainActivity.this,"You must accept permission to use app",Toast.LENGTH_SHORT).show();
//                            Log.d("Permissions", "Not all permissions granted: " + report.getDeniedPermissionResponses());
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private void showSignInOptions() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(),MY_REQUEST_CODE);
//        Intent signInIntent = AuthUI.getInstance()
//                .createSignInIntentBuilder()
//                .setAvailableProviders(providers)
//                .build();
//        signInLauncher.launch(signInIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                user_information.orderByKey()
                        .equalTo(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null) //User is not present
                                {
                                    if (!dataSnapshot.child(firebaseUser.getUid()).exists()) {
                                        Common.loggedUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());

                                        //Adding to the database
                                        user_information.child(Common.loggedUser.getUid()).setValue(Common.loggedUser);
                                        setupUI();
                                    }
                                } else {
                                    Common.loggedUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                                }

                                //Save the Uid to storage and update the location from background
                                Paper.book().write(Common.USER_UID_SAVE_KEY, Common.loggedUser.getUid());
                                UpdateToken(firebaseUser);
                                setupUI();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        }
    }

    private void setupUI() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean islogin = true;
        prefs.edit().putBoolean("Islogin", islogin).apply(); // islogin is a boolean value of your login status

        startActivity(new Intent(MainActivity.this, HomeActivity.class));
//        Log.d("Display", "Not working: ");
        finish();
    }

    private void UpdateToken(FirebaseUser firebaseUser) {
        final DatabaseReference tokens = FirebaseDatabase.getInstance()
                .getReference(Common.TOKENS);

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        tokens.child(firebaseUser.getUid()).setValue(token);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}