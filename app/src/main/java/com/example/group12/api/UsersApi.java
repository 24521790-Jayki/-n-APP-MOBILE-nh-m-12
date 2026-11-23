package com.example.group12.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface UsersApi {

    @GET("users/{id}")
    Call<UserResponse> getUserById(
            @Header("Authorization") String token,
            @Path("id") int userId
    );
}
