package com.example.budtrack.Interface;

import java.util.List;

public interface IFirebaseLoadDone {
    void onFirebaseLoadUserNameDone(List<String> lstEmail);

    void onFirebaseLoadFailed(String message);
}
