package com.example.budtrack.Remote;

import com.example.budtrack.Model.MyResponse;
import com.example.budtrack.Model.Request;

import java.util.Observable;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAALsF8t4Q:APA91bGX1BT7aIEdrMuuwkCkHr1aQK362jSvR2egy9DyQ3w1EemZrPFIxpoSkow0fo01BbZHfjuCKG7nuRze7H8OL8638L8pakL1VUjrtab8m_zk1ws6VaFyV3UPkVRvJWuqk0-5gAR_"

    })

    @POST("fcm/send")
        //Observable<MyResponse> sendFriendRequestToUser(@Body Request body);
    io.reactivex.Observable<MyResponse> sendFriendRequestToUser(@Body Request body);

}
