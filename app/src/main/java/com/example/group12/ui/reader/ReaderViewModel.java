package com.example.group12.ui.reader;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.group12.db.entity.ReadingProgress;
import com.example.group12.model.BookContent;
import com.example.group12.repository.BookRepository;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ViewModel cung cap du lieu cho ReaderActivity
// Quan ly BookContent (Single Source of Truth) va ReadingProgress
public class ReaderViewModel extends AndroidViewModel {

    private final BookRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<BookContent> bookContentLive = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ReaderViewModel(@NonNull Application application) {
        super(application);
        repository = new BookRepository(application);
        executor = Executors.newSingleThreadExecutor();
    }

    // Lay BookContent LiveData
    public LiveData<BookContent> getBookContentLive() {
        return bookContentLive;
    }

    // Lay trang thai loading
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Lay thong bao loi
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Lay tien do doc LiveData
    public LiveData<ReadingProgress> getProgressLive(int bookId) {
        return repository.getProgressLive(bookId);
    }

    // Tai hoac download va parse epub
    public void loadOrDownloadBook(int bookId) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                if (repository.isEpubDownloaded(bookId)) {
                    parseAndPostContent(bookId);
                } else {
                    repository.downloadEpub(bookId, new BookRepository.DownloadCallback() {
                        @Override
                        public void onSuccess(File file) {
                            parseAndPostContent(bookId);
                        }

                        @Override
                        public void onError(String message) {
                            errorMessage.postValue(message);
                            isLoading.postValue(false);
                        }
                    });
                }
            } catch (Exception e) {
                errorMessage.postValue("Loi: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    // Parse epub file va post ket qua
    private void parseAndPostContent(int bookId) {
        try {
            BookContent content = repository.parseEpub(bookId);
            if (content != null && content.getChapters() != null && !content.getChapters().isEmpty()) {
                bookContentLive.postValue(content);
            } else {
                errorMessage.postValue("Khong the phan tich noi dung file epub");
            }
        } catch (Exception e) {
            errorMessage.postValue("Loi phan tich file: " + e.getMessage());
        } finally {
            isLoading.postValue(false);
        }
    }

    // Luu tien do doc
    public void saveProgress(int bookId, int chapterIdx, int paraIdx,
                             int sentIdx, float ttsSpeed) {
        ReadingProgress progress = new ReadingProgress();
        progress.setBookId(bookId);
        progress.setChapterIndex(chapterIdx);
        progress.setParagraphIndex(paraIdx);
        progress.setSentenceIndex(sentIdx);
        progress.setTtsSpeed(ttsSpeed);
        repository.saveProgress(progress);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
