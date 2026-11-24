package com.example.group12.util;

import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Tro giup chon engine TTS va ngon ngu doc
public class TtsEngineHelper {

    private static final String TAG = "TtsEngineHelper";
    private static final String GOOGLE_TTS_PACKAGE = "com.google.android.tts";

    // Kiem tra va tra ve ten package engine TTS tot nhat
    public static String getBestEnginePackage(TextToSpeech tts) {
        if (tts == null) return GOOGLE_TTS_PACKAGE;
        List<TextToSpeech.EngineInfo> engines = tts.getEngines();
        if (engines == null || engines.isEmpty()) {
            return tts.getDefaultEngine();
        }

        for (TextToSpeech.EngineInfo engine : engines) {
            if (GOOGLE_TTS_PACKAGE.equals(engine.name)) {
                Log.d(TAG, "Tim thay Google TTS engine");
                return GOOGLE_TTS_PACKAGE;
            }
        }

        Log.d(TAG, "Khong tim thay Google TTS, su dung engine mac dinh: " + tts.getDefaultEngine());
        return tts.getDefaultEngine();
    }

    // Lay danh sach giong doc tieng Viet (vi-VN)
    public static List<Voice> getVietnameseVoices(TextToSpeech tts) {
        List<Voice> vietnameseVoices = new ArrayList<>();
        if (tts == null) return vietnameseVoices;

        Set<Voice> allVoices = tts.getVoices();
        if (allVoices == null) return vietnameseVoices;

        for (Voice voice : allVoices) {
            Locale locale = voice.getLocale();
            if (locale != null && "vi".equalsIgnoreCase(locale.getLanguage())) {
                vietnameseVoices.add(voice);
            }
        }

        Log.d(TAG, "Tim thay " + vietnameseVoices.size() + " giong tieng Viet");
        return vietnameseVoices;
    }

    // Kiem tra thiet bi co ho tro tieng Viet khong
    public static boolean isVietnameseSupported(TextToSpeech tts) {
        if (tts == null) return false;
        Locale vietnamese = new Locale("vi", "VN");
        int result = tts.isLanguageAvailable(vietnamese);
        return result >= TextToSpeech.LANG_AVAILABLE;
    }

    // Dat ngon ngu tieng Viet cho TTS (kem fallback an toan)
    public static boolean setVietnameseLanguage(TextToSpeech tts) {
        if (tts == null) return false;

        Locale vietnamese = new Locale("vi", "VN");
        int result = tts.setLanguage(vietnamese);

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Thiet bi khong ho tro vi-VN, thu voi Locale('vi')");
            result = tts.setLanguage(new Locale("vi"));

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Thiet bi khong ho tro tieng Viet, chuyen sang ngon ngu mac dinh thiet bi");
                tts.setLanguage(Locale.getDefault());
                return false;
            }
        }
        return true;
    }

    // Lay danh sach tat ca engine TTS co san
    public static List<TextToSpeech.EngineInfo> getAvailableEngines(TextToSpeech tts) {
        if (tts == null) return new ArrayList<>();
        return tts.getEngines();
    }
}
