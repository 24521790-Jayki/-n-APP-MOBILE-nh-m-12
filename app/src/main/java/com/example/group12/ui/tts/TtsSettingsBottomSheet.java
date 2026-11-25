package com.example.group12.ui.tts;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.group12.R;
import com.example.group12.util.TtsEngineHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// BottomSheet cai dat nhanh TTS tren man hinh doc sach
// Cho phep chinh toc do, chon giong theo engine, chuyen engine, mo system TTS settings
public class TtsSettingsBottomSheet extends BottomSheetDialogFragment {

    // Interface callback de thong bao thay doi cai dat
    public interface TtsSettingsListener {
        void onSpeedChanged(float speed);
        void onVoiceChanged(Voice voice);
        void onEngineChanged(String enginePackage);
    }

    private TtsSettingsListener listener;
    private TextToSpeech tts;
    private float currentSpeed = 1.0f;
    private List<Voice> currentViVoices = new ArrayList<>();
    private List<TextToSpeech.EngineInfo> availableEngines = new ArrayList<>();

    private boolean isInitialEngineSelection = true;
    private boolean isInitialVoiceSelection = true;

    public void setTts(TextToSpeech tts) {
        this.tts = tts;
    }

    public void setListener(TtsSettingsListener listener) {
        this.listener = listener;
    }

    public void setCurrentSpeed(float speed) {
        this.currentSpeed = speed;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_tts_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView txtSpeedValue = view.findViewById(R.id.txtSpeedValue);
        SeekBar seekBarSpeed = view.findViewById(R.id.seekBarSpeed);
        Spinner spinnerVoice = view.findViewById(R.id.spinnerVoice);
        Spinner spinnerEngine = view.findViewById(R.id.spinnerEngine);
        View btnOpenTtsSettings = view.findViewById(R.id.btnOpenTtsSettings);

        // === Toc do doc (0.5x - 2.0x) ===
        int initialProgress = Math.round(currentSpeed * 10) - 5;
        seekBarSpeed.setProgress(Math.max(0, Math.min(15, initialProgress)));
        txtSpeedValue.setText(String.format(Locale.US, "%.1fx", currentSpeed));

        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = (progress + 5) / 10.0f;
                txtSpeedValue.setText(String.format(Locale.US, "%.1fx", speed));
                if (fromUser && listener != null) {
                    listener.onSpeedChanged(speed);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // === Thiet lap Engine va Giong doc theo Engine ===
        if (tts != null) {
            availableEngines = TtsEngineHelper.getAvailableEngines(tts);
            List<String> engineNames = new ArrayList<>();
            int selectedEngineIdx = 0;

            String currentDefaultEngine = tts.getDefaultEngine();

            for (int i = 0; i < availableEngines.size(); i++) {
                TextToSpeech.EngineInfo engine = availableEngines.get(i);
                engineNames.add(engine.label);
                if (engine.name.equals(currentDefaultEngine)) {
                    selectedEngineIdx = i;
                }
            }

            if (engineNames.isEmpty()) {
                engineNames.add("Engine mac dinh thiet bi");
            }

            ArrayAdapter<String> engineAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    engineNames);
            engineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEngine.setAdapter(engineAdapter);
            spinnerEngine.setSelection(selectedEngineIdx);

            // Cap nhat danh sach giong noi ban dau
            updateVoicesForCurrentEngine(spinnerVoice);

            // Bat su kien chon Engine -> Ignored initially to prevent resetting reading position!
            spinnerEngine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                    if (isInitialEngineSelection) {
                        isInitialEngineSelection = false;
                        return;
                    }

                    if (position >= 0 && position < availableEngines.size()) {
                        TextToSpeech.EngineInfo selectedEngine = availableEngines.get(position);
                        if (listener != null) {
                            listener.onEngineChanged(selectedEngine.name);
                        }
                        isInitialVoiceSelection = true;
                        updateVoicesForCurrentEngine(spinnerVoice);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // Bat su kien chon Giong doc -> Ignored initially to prevent resetting voice!
            spinnerVoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                    if (isInitialVoiceSelection) {
                        isInitialVoiceSelection = false;
                        return;
                    }

                    if (position >= 0 && position < currentViVoices.size()) {
                        Voice selectedVoice = currentViVoices.get(position);
                        if (listener != null) {
                            listener.onVoiceChanged(selectedVoice);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // === Nut mo cai dat TTS he thong ===
        btnOpenTtsSettings.setOnClickListener(v -> {
            Intent intent = new Intent("com.android.settings.TTS_SETTINGS");
            startActivity(intent);
        });
    }

    private void updateVoicesForCurrentEngine(Spinner spinnerVoice) {
        if (tts == null) return;

        currentViVoices = TtsEngineHelper.getVietnameseVoices(tts);
        List<String> voiceDisplayNames = new ArrayList<>();
        int selectedVoiceIdx = 0;

        Voice activeVoice = null;
        try {
            activeVoice = tts.getVoice();
        } catch (Exception ignored) {}

        for (int i = 0; i < currentViVoices.size(); i++) {
            Voice v = currentViVoices.get(i);
            String name = v.getName();

            if (activeVoice != null && activeVoice.getName().equals(name)) {
                selectedVoiceIdx = i;
            }

            if (name.contains("vi-vn") || name.contains("vietnam")) {
                voiceDisplayNames.add("Giong tieng Viet #" + (i + 1) + " (" + (v.isNetworkConnectionRequired() ? "Online" : "Offline") + ")");
            } else {
                voiceDisplayNames.add("Giong #" + (i + 1) + ": " + name);
            }
        }

        if (voiceDisplayNames.isEmpty()) {
            voiceDisplayNames.add("Giong tieng Viet mac dinh");
        }

        ArrayAdapter<String> voiceAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                voiceDisplayNames);
        voiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVoice.setAdapter(voiceAdapter);
        spinnerVoice.setSelection(selectedVoiceIdx);
    }
}
