package com.example.myapplication;

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;


public class MyTwitterApiClient extends TwitterApiClient {
    public MyTwitterApiClient(TwitterSession session) {
        super(session);
    }

    public PostFriendshipsCreateAPICustomService postCustomService() {
        return getService(PostFriendshipsCreateAPICustomService.class);
    }
}

interface PostFriendshipsCreateAPICustomService {
    @POST("/1.1/friendships/create.json")
    Call<User> create(@Query("user_id") long userId);
}