package com.example.group12.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.group12.api.ApiClient;
import com.example.group12.api.BooksApi;
import com.example.group12.api.ProgressResponse;
import com.example.group12.db.AppDatabase;
import com.example.group12.db.dao.ReadingProgressDao;
import com.example.group12.db.entity.ReadingProgress;
import com.example.group12.model.BookContent;
import com.example.group12.parser.EpubContentParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

// Repository trung gian giua UI va du lieu (Room + Retrofit + File)
// Cung cap cac phuong thuc doc/ghi tien do, tai va parse epub
public class BookRepository {

    private static final String TAG = "BookRepository";
    private final ReadingProgressDao progressDao;
    private final Context context;
    private final ExecutorService executor;
    private final BooksApi booksApi;

    public interface DownloadCallback {
        void onSuccess(File file);
        void onError(String message);
    }

    public BookRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(this.context);
        this.progressDao = db.readingProgressDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.booksApi = ApiClient.getClient().create(BooksApi.class);
    }

    // === Tien do doc ===

    // Lay tien do doc (LiveData - tu dong cap nhat UI)
    public LiveData<ReadingProgress> getProgressLive(int bookId) {
        return progressDao.getProgressLive(bookId);
    }

    // Luu tien do doc va dong bo voi server (background thread)
    public void saveProgress(ReadingProgress progress) {
        progress.setUpdatedAt(System.currentTimeMillis());
        executor.execute(() -> {
            progressDao.upsert(progress);
            syncProgressToServer(progress);
        });
    }

    // Dong bo tien do doc len server API
    private void syncProgressToServer(ReadingProgress progress) {
        try {
            Call<ProgressResponse> call = booksApi.updateReadingProgress(progress.getBookId(), progress);
            Response<ProgressResponse> response = call.execute();
            if (response.isSuccessful()) {
                Log.d(TAG, "Da dong bo tien do doc len server thanh cong cho book: " + progress.getBookId());
            } else {
                Log.w(TAG, "Khong the dong bo tien do doc len server, status: " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Loi khi ket noi dong bo tien do doc: " + e.getMessage());
        }
    }

    // === Quan ly va download file epub ===

    // Kiem tra file epub da duoc tai xuong chua
    public boolean isEpubDownloaded(int bookId) {
        File file = getEpubFile(bookId);
        return file.exists() && file.length() > 0;
    }

    // Lay duong dan file epub local
    public File getEpubFile(int bookId) {
        File dir = new File(context.getFilesDir(), "epubs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "book_" + bookId + ".epub");
    }

    // Tai file epub tu server qua Retrofit @Streaming
    public void downloadEpub(int bookId, DownloadCallback callback) {
        executor.execute(() -> {
            try {
                Call<ResponseBody> call = booksApi.downloadEpubFile(bookId);
                Response<ResponseBody> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    InputStream is = response.body().byteStream();
                    File savedFile = saveEpubFromStream(bookId, is);
                    callback.onSuccess(savedFile);
                } else {
                    callback.onError("Khong the tai file epub tu server. Http code: " + response.code());
                }
            } catch (Exception e) {
                Log.e(TAG, "Loi khi download file epub: " + e.getMessage(), e);
                callback.onError("Loi ket noi tai file epub: " + e.getMessage());
            }
        });
    }

    // Luu file epub tu InputStream (download tu server)
    public File saveEpubFromStream(int bookId, InputStream inputStream)
            throws IOException {
        File epubFile = getEpubFile(bookId);
        FileOutputStream fos = new FileOutputStream(epubFile);

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
        fos.flush();
        fos.close();
        inputStream.close();

        Log.d(TAG, "Da luu file epub: " + epubFile.getAbsolutePath()
                + " (" + epubFile.length() + " bytes)");
        return epubFile;
    }

    // Parse file epub thanh BookContent (Single Source of Truth)
    public BookContent parseEpub(int bookId) {
        File epubFile = getEpubFile(bookId);
        if (!epubFile.exists()) {
            Log.w(TAG, "File epub chua duoc tai: book_" + bookId);
            return null;
        }
        return EpubContentParser.parse(epubFile, bookId);
    }

    // Xoa file epub local
    public void deleteEpub(int bookId) {
        File file = getEpubFile(bookId);
        if (file.exists()) {
            file.delete();
            Log.d(TAG, "Da xoa file epub: book_" + bookId);
        }
    }
}
