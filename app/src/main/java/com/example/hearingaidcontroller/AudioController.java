package com.example.hearingaidcontroller;

import android.content.Context;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Virtualizer;
import android.media.MediaPlayer;
import android.util.Log;

public class AudioController {
    private static final String TAG = "AudioController";
    private AudioManager audioManager;
    private Equalizer equalizer;
    private MediaPlayer mediaPlayer;
    private int sessionId;
    private short numberOfBands;
    private short[] bandLevelRange;

    public AudioController(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.silent);
            if (mediaPlayer == null) {
                Log.e(TAG, "Failed to create MediaPlayer: silent.mp3 missing?");
                return;
            }
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0f, 0f); // ensure silent
            sessionId = mediaPlayer.getAudioSessionId();

            equalizer = new Equalizer(0, sessionId);
            equalizer.setEnabled(true);

            numberOfBands = equalizer.getNumberOfBands();
            bandLevelRange = equalizer.getBandLevelRange();

            Log.d(TAG, "Equalizer initialized: bands=" + numberOfBands
                    + ", min=" + bandLevelRange[0] + " max=" + bandLevelRange[1]);

            // Printing center frequencies for debugging
            for (short i = 0; i < numberOfBands; i++) {
                int freq = equalizer.getCenterFreq(i);
                Log.d(TAG, "Band " + i + " center freq: " + freq + " Hz");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing equalizer", e);
            equalizer = null;
        }
    }

    public void startAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public void setVolume(int level) { // 0-100
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int index = level * max / 100;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
    }

    public int getVolume() {
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return current * 100 / max;
    }

    public void setBass(int level) { // 0-100
        if (equalizer == null || numberOfBands < 2) {
            Log.w(TAG, "Equalizer not available or insufficient bands");
            return;
        }
        try {
            // Map bass to first 2 bands (low frequencies)
            int minLevel = bandLevelRange[0];
            int maxLevel = bandLevelRange[1];
            int levelValue = minLevel + (maxLevel - minLevel) * level / 100;
            equalizer.setBandLevel((short) 0, (short) levelValue);
            if (numberOfBands > 1) {
                equalizer.setBandLevel((short) 1, (short) levelValue);
            }
            Log.d(TAG, "Bass set to " + level + " -> " + levelValue);
        } catch (Exception e) {
            Log.e(TAG, "Error setting bass", e);
        }
    }

    public void setTreble(int level) { // 0-100
        if (equalizer == null || numberOfBands < 2) {
            Log.w(TAG, "Equalizer not available or insufficient bands");
            return;
        }
        try {
            int minLevel = bandLevelRange[0];
            int maxLevel = bandLevelRange[1];
            int levelValue = minLevel + (maxLevel - minLevel) * level / 100;
            equalizer.setBandLevel((short) (numberOfBands - 1), (short) levelValue);
            if (numberOfBands > 2) {
                equalizer.setBandLevel((short) (numberOfBands - 2), (short) levelValue);
            }
            Log.d(TAG, "Treble set to " + level + " -> " + levelValue);
        } catch (Exception e) {
            Log.e(TAG, "Error setting treble", e);
        }
    }

    public int getBass() {
        return 50; // placeholder
    }

    public int getTreble() {
        return 50;
    }
}