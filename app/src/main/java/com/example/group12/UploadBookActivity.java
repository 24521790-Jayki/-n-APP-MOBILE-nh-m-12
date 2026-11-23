package com.example.group12;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import androidx.appcompat.app.AlertDialog;

import com.example.group12.api.ApiClient;
import com.example.group12.api.BooksApi;
import com.example.group12.api.CategoriesApi;
import com.example.group12.api.CategoriesResponse;
import com.example.group12.api.Category;
import com.example.group12.api.CreateBookRequest;
import com.example.group12.api.CreateBookResponse;
import com.example.group12.api.SessionManager;
import com.example.group12.databinding.ActivityUploadBookBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadBookActivity extends AppCompatActivity {

    private ActivityUploadBookBinding binding;
    private SessionManager sessionManager;

    private final List<Category> categoryList = new ArrayList<>();

    private boolean isEditMode = false;
    private int bookId = -1;
    private int selectedCategoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUploadBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // Chi Admin moi duoc them sach
        if (!sessionManager.isLoggedIn()
                || !"admin".equalsIgnoreCase(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "Chỉ Admin mới có quyền thêm sách!",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());

        // Kiểm tra có đang ở chế độ cập nhật sách hay không
        bookId = getIntent().getIntExtra("bookId", -1);
        isEditMode = bookId != -1;

        if (isEditMode) {
            // Điền thông tin sách cũ vào form
            binding.etBookTitle.setText(
                    getIntent().getStringExtra("bookTitle")
            );

            binding.etAuthor.setText(
                    getIntent().getStringExtra("bookAuthor")
            );

            binding.etDescription.setText(
                    getIntent().getStringExtra("bookDescription")
            );

            selectedCategoryId = getIntent().getIntExtra(
                    "categoryId", -1
            );

            binding.btnSubmitReview.setText("Cập nhật sách");

            binding.btnDeleteBook.setVisibility(View.VISIBLE);
        } else {
            binding.btnSubmitReview.setText("Thêm sách");

            binding.btnDeleteBook.setVisibility(View.GONE);
        }

        // Tai danh sach the loai
        loadCategories();

        // Them sach
        binding.btnSubmitReview.setText("Thêm sách");
        binding.btnSubmitReview.setOnClickListener(v -> saveBook());
        binding.btnDeleteBook.setOnClickListener(v -> confirmDeleteBook());
    }

    // Lay danh sach the loai tu Backend
    private void loadCategories() {

        String token = "Bearer " + sessionManager.getToken();

        CategoriesApi api = ApiClient.getClient()
                .create(CategoriesApi.class);

        api.getCategories(token).enqueue(new Callback<CategoriesResponse>() {
            @Override
            public void onResponse(
                    Call<CategoriesResponse> call,
                    Response<CategoriesResponse> response
            ) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isSuccess()) {

                    categoryList.clear();
                    categoryList.addAll(response.body().getData());

                    ArrayAdapter<Category> adapter =
                            new ArrayAdapter<>(
                                    UploadBookActivity.this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    categoryList
                            );

                    binding.actvCategory.setAdapter(adapter);
                    binding.actvCategory.setThreshold(0);

                } else {
                    Toast.makeText(
                            UploadBookActivity.this,
                            "Không thể tải danh sách thể loại!",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                if (isEditMode && selectedCategoryId > 0) {
                    for (Category category : categoryList) {
                        if (category.getCategoryID() == selectedCategoryId) {
                            binding.actvCategory.setText(
                                    category.toString(), false
                            );
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(
                    Call<CategoriesResponse> call,
                    Throwable t
            ) {
                Toast.makeText(
                        UploadBookActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // Tim CategoryID theo ten the loai da chon
    private Integer getSelectedCategoryId() {

        String categoryName =
                binding.actvCategory.getText().toString().trim();

        if (categoryName.isEmpty()) {
            return null;
        }

        for (Category category : categoryList) {
            if (category.getCategoryName().equals(categoryName)) {
                return category.getCategoryID();
            }
        }

        return -1;
    }

    private void confirmDeleteBook() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa sách")
                .setMessage(
                        "Bạn có chắc chắn muốn xóa sách này không? "
                                + "Thao tác này không thể hoàn tác."
                )
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteBook();
                })
                .show();
    }

    private void deleteBook() {
        if (bookId <= 0) {
            Toast.makeText(
                    this,
                    "Mã sách không hợp lệ",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String token = "Bearer " + sessionManager.getToken();

        binding.btnDeleteBook.setEnabled(false);

        BooksApi api = ApiClient.getClient()
                .create(BooksApi.class);

        api.deleteBook(token, bookId).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {
                binding.btnDeleteBook.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(
                            UploadBookActivity.this,
                            "Xóa sách thành công",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                    return;
                }

                String message;

                if (response.code() == 401) {
                    message = "Phiên đăng nhập hết hạn";
                } else if (response.code() == 403) {
                    message = "Bạn không có quyền xóa sách";
                } else if (response.code() == 404) {
                    message = "Sách không tồn tại";
                } else {
                    message = "Không thể xóa sách";
                }

                Toast.makeText(
                        UploadBookActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {
                binding.btnDeleteBook.setEnabled(true);

                Toast.makeText(
                        UploadBookActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // Gui thong tin sach len Backend
    private void saveBook() {
        String title = binding.etBookTitle.getText().toString().trim();
        String author = binding.etAuthor.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        Integer categoryId = getSelectedCategoryId();

        if (title.isEmpty()) {
            binding.etBookTitle.setError("Vui lòng nhập tên sách");
            return;
        }

        if (title.length() > 250) {
            binding.etBookTitle.setError("Tên sách tối đa 250 ký tự");
            return;
        }

        if (author.length() > 200) {
            binding.etAuthor.setError("Tên tác giả tối đa 200 ký tự");
            return;
        }

        if (categoryId == null || categoryId == -1) {
            Toast.makeText(
                    this,
                    "Vui lòng chọn thể loại hợp lệ",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        CreateBookRequest request = new CreateBookRequest(
                title,
                author.isEmpty() ? null : author,
                description.isEmpty() ? null : description,
                categoryId,
                null
        );

        String token = "Bearer " + sessionManager.getToken();

        binding.btnSubmitReview.setEnabled(false);

        BooksApi api = ApiClient.getClient().create(BooksApi.class);

        Call<CreateBookResponse> call;

        if (isEditMode) {
            call = api.updateBook(token, bookId, request);
        } else {
            call = api.createBook(token, request);
        }

        call.enqueue(new Callback<CreateBookResponse>() {
            @Override
            public void onResponse(
                    Call<CreateBookResponse> call,
                    Response<CreateBookResponse> response
            ) {
                binding.btnSubmitReview.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(
                            UploadBookActivity.this,
                            isEditMode
                                    ? "Cập nhật sách thành công"
                                    : "Thêm sách thành công",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                    return;
                }

                if (response.code() == 401) {
                    Toast.makeText(
                            UploadBookActivity.this,
                            "Phiên đăng nhập hết hạn",
                            Toast.LENGTH_SHORT
                    ).show();
                } else if (response.code() == 403) {
                    Toast.makeText(
                            UploadBookActivity.this,
                            "Bạn không có quyền thực hiện thao tác này",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            UploadBookActivity.this,
                            isEditMode
                                    ? "Không thể cập nhật sách"
                                    : "Không thể thêm sách",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<CreateBookResponse> call,
                    Throwable t
            ) {
                binding.btnSubmitReview.setEnabled(true);

                Toast.makeText(
                        UploadBookActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
    }

