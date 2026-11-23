package com.example.group12.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;

public interface BooksApi {

    // READ: Lay danh sach sach
    @GET("books")
    Call<BooksResponse> getBooks(
            @Header("Authorization") String token
    );

    // CREATE: Them sach moi (Admin)
    @POST("books")
    Call<CreateBookResponse> createBook(
            @Header("Authorization") String token,
            @Body CreateBookRequest request
    );

    // UPDATE: Cap nhat sach (Admin)
    @PUT("books/{id}")
    Call<CreateBookResponse> updateBook(
            @Header("Authorization") String token,
            @Path("id") int bookId,
            @Body CreateBookRequest request
    );

    @DELETE("books/{id}")
    Call<Void> deleteBook(
            @Header("Authorization") String token,
            @Path("id") int bookId
    );
}
