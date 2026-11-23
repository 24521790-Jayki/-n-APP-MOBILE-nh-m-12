
package com.example.group12.api;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface CategoriesApi {

    @GET("categories")
    Call<CategoriesResponse> getCategories(
            @Header("Authorization") String token
    );
}

