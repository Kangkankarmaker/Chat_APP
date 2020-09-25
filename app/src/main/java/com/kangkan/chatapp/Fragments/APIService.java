package com.kangkan.chatapp.Fragments;

import com.kangkan.chatapp.Notifications.MyResponse;
import com.kangkan.chatapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAPE0ZwW8:APA91bEau6Ps0PsxALodwPFI-Stypp05GegY6HpZECg0p9RzfzsvSgPtvgw7yGcNH0XtLTY-CIfYJu625Cz8pr6v5PUXj0f7TDzQDIuWdMNyrq-o6e3iqwCp8RpH17MqnixmLxvNMo_5"
            }
    )

    @POST("fcm/send")
    Call<MyResponse>sentNotification(@Body Sender body);
}
