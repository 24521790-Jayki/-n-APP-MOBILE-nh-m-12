package com.example.group12;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityAudioPlayerBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AudioPlayerActivity extends AppCompatActivity {

    private boolean isPlaying = true; // Mặc định đang phát (hiện icon pause)
    private boolean isFavorite = false; // Trạng thái yêu thích

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAudioPlayerBinding binding = ActivityAudioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Yêu thích (Tim)
        binding.btnFavorite.setOnClickListener(v -> {
            if (isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                Toast.makeText(AudioPlayerActivity.this, "Đã bỏ khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                isFavorite = false;
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite);
                Toast.makeText(AudioPlayerActivity.this, "Đã thêm vào danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                isFavorite = true;
            }
        });

        // Xử lý sự kiện bấm nút Phát / Dừng (Play/Pause)
        binding.btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                // Đang phát -> bấm để dừng (chuyển sang icon Play)
                binding.btnPlayPause.setImageResource(R.drawable.ic_play);
                isPlaying = false;
            } else {
                // Đang dừng -> bấm để phát (chuyển sang icon Pause)
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause);
                isPlaying = true;
            }
        });

        // Nút Hẹn giờ tắt -> Hiển thị BottomSheetDialog các mốc thời gian hẹn giờ
        binding.btnTimer.setOnClickListener(v -> {
            BottomSheetDialog sheetDialog = new BottomSheetDialog(AudioPlayerActivity.this);
            View view = LayoutInflater.from(AudioPlayerActivity.this).inflate(R.layout.dialog_sleep_timer, null);

            view.findViewById(R.id.timerOff).setOnClickListener(thiz -> {
                Toast.makeText(AudioPlayerActivity.this, "Đã tắt hẹn giờ", Toast.LENGTH_SHORT).show();
                sheetDialog.dismiss();
            });
            view.findViewById(R.id.timer15).setOnClickListener(thiz -> {
                Toast.makeText(AudioPlayerActivity.this, "Đã hẹn giờ tắt sau 15 phút", Toast.LENGTH_SHORT).show();
                sheetDialog.dismiss();
            });
            view.findViewById(R.id.timer30).setOnClickListener(thiz -> {
                Toast.makeText(AudioPlayerActivity.this, "Đã hẹn giờ tắt sau 30 phút", Toast.LENGTH_SHORT).show();
                sheetDialog.dismiss();
            });
            view.findViewById(R.id.timer45).setOnClickListener(thiz -> {
                Toast.makeText(AudioPlayerActivity.this, "Đã hẹn giờ tắt sau 45 phút", Toast.LENGTH_SHORT).show();
                sheetDialog.dismiss();
            });
            view.findViewById(R.id.timer60).setOnClickListener(thiz -> {
                Toast.makeText(AudioPlayerActivity.this, "Đã hẹn giờ tắt sau 60 phút", Toast.LENGTH_SHORT).show();
                sheetDialog.dismiss();
            });
            view.findViewById(R.id.timerEndChapter).setOnClickListener(thiz -> {
                Toast.makeText(AudioPlayerActivity.this, "Đã hẹn giờ tắt khi hết chương hiện tại", Toast.LENGTH_SHORT).show();
                sheetDialog.dismiss();
            });

            sheetDialog.setContentView(view);
            sheetDialog.show();
        });

        // Nút Playlist / Danh sách chương -> Hiển thị BottomSheetDialog danh sách chương
        binding.btnPlaylist.setOnClickListener(v -> {
            BottomSheetDialog sheetDialog = new BottomSheetDialog(AudioPlayerActivity.this);
            View view = LayoutInflater.from(AudioPlayerActivity.this).inflate(R.layout.dialog_chapters, null);
            sheetDialog.setContentView(view);
            sheetDialog.show();
        });
    }
}
