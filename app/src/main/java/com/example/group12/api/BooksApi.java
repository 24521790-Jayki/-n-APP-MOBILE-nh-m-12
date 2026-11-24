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

    // Stream download file epub tu server
    @retrofit2.http.Streaming
    @GET("books/{id}/download")
    Call<okhttp3.ResponseBody> downloadEpubFile(
            @Path("id") int bookId
    );

    // Lay tien do doc tu server
    @GET("users/me/progress/{bookId}")
    Call<ProgressResponse> getReadingProgress(
            @Path("id") int bookId
    );

    // Cap nhat tien do doc len server (Last-Write-Wins)
    @PUT("users/me/progress/{bookId}")
    Call<ProgressResponse> updateReadingProgress(
            @Path("id") int bookId,
            @Body com.example.group12.db.entity.ReadingProgress progress
    );

    // Upload file epub len server
    @retrofit2.http.Multipart
    @POST("books/upload")
    Call<CreateBookResponse> uploadEpubFile(
            @retrofit2.http.Part okhttp3.MultipartBody.Part file,
            @retrofit2.http.Part("title") okhttp3.RequestBody title,
            @retrofit2.http.Part("author") okhttp3.RequestBody author
    );
}
