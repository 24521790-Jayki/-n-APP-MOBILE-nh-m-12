
package com.example.group12.api;

import com.example.group12.model.BookChaptersResponse;
import com.example.group12.model.ChapterContentResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("books/{id}/chapters")
    Call<BookChaptersResponse> getBookChapters(
            @Path("id") int bookId
    );

    @GET("books/{id}/chapters/{chapterNumber}")
    Call<ChapterContentResponse> getChapterContent(
            @Path("id") int bookId,
            @Path("chapterNumber") int chapterNumber
    );
}
