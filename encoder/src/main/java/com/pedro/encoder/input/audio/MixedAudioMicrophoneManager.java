package com.pedro.encoder.input.audio;

import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.pedro.encoder.Frame;

import java.nio.ByteBuffer;

/**
 * MicrophoneManager implementation for mixed audio (microphone + internal audio)
 * Follows the same pattern as the original MicrophoneManager for consistency
 */
public class MixedAudioMicrophoneManager extends MicrophoneManager {
    
    private static final String TAG = "MixedAudioMicrophoneManager";
    // Internal audio capture components
    private AudioRecord internalAudioRecord;
    private ByteBuffer internalPcmBuffer;
    private AudioPostProcessEffect internalAudioPostProcessEffect;
    private boolean internalMuted = false;
    
    public MixedAudioMicrophoneManager(GetMicrophoneData getMicrophoneData) {
        super(getMicrophoneData);
    }
    
    
    /**
     * Create mixed audio microphone with both microphone and internal audio
     */
    public boolean createMixedAudioMicrophone(AudioPlaybackCaptureConfiguration internalConfig,
                                              int sampleRate, boolean isStereo,
                                              boolean echoCanceler, boolean noiseSuppressor) {
        // Create regular microphone first
        Log.d(TAG, "Creating regular microphone...");
        boolean micCreated = super.createMicrophone(
            MediaRecorder.AudioSource.MIC, sampleRate, isStereo, echoCanceler, noiseSuppressor);
        
        if (!micCreated) {
            Log.e(TAG, "Failed to create microphone");
            return false;
        }
        Log.d(TAG, "Regular microphone created successfully");
        
        // Create internal audio capture
        Log.d(TAG, "Creating internal audio record...");
        boolean internalCreated = createInternalAudioRecord(internalConfig, sampleRate, isStereo, echoCanceler, noiseSuppressor);
        created = micCreated && internalCreated;
        
        if (!internalCreated) {
            Log.e(TAG, "Failed to create internal audio record");
            return false;
        }
        Log.d(TAG, "Internal audio record created successfully");
        Log.i(TAG, "Mixed audio microphone created successfully");
        return true;
    }
    
    /**
     * Create AudioRecord for internal audio capture
     */
    private boolean createInternalAudioRecord(AudioPlaybackCaptureConfiguration config,
                                              int sampleRate, boolean isStereo,
                                              boolean echoCanceler, boolean noiseSuppressor) {
        try {
            Log.d(TAG, "Android version check: " + Build.VERSION.SDK_INT + " >= " + Build.VERSION_CODES.Q);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                int channel = isStereo ? AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_IN_MONO;
                int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, AudioFormat.ENCODING_PCM_16BIT);
                Log.d(TAG, "Buffer size calculated: " + bufferSize);
                
                Log.d(TAG, "Building AudioRecord...");
                internalAudioRecord = new AudioRecord.Builder()
                    .setAudioPlaybackCaptureConfig(config)
                    .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channel)
                        .build())
                    .setBufferSizeInBytes(bufferSize)
                    .build();
                
                Log.d(TAG, "AudioRecord built, checking state...");
                if (internalAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord state: " + internalAudioRecord.getState());
                    throw new IllegalArgumentException("Internal audio record parameters are not valid");
                }
                
                internalPcmBuffer = ByteBuffer.allocateDirect(bufferSize);
                
                internalAudioPostProcessEffect = new AudioPostProcessEffect(internalAudioRecord.getAudioSessionId());
                if (echoCanceler) internalAudioPostProcessEffect.enableEchoCanceler();
                if (noiseSuppressor) internalAudioPostProcessEffect.enableNoiseSuppressor();
                
                String chl = isStereo ? "Stereo" : "Mono";
                Log.i(TAG, "Internal audio record created, " + sampleRate + "hz, " + chl);
                return true;
            } else {
                Log.e(TAG, "Android version too old for internal audio capture");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating internal audio record", e);
        }
        return false;
    }
    
    /**
     * Override start() to handle both microphone and internal audio
     */
    @Override
    public synchronized void start() {
        // Initialize both audio sources
        initMixedAudio();
        
        // Follow the same pattern as MicrophoneManager.start()
        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(() -> {
            while (running) {
                Frame frame = readMixedAudio();
                if (frame != null) {
                    getMicrophoneData.inputPCMData(frame);
                }
            }
        });
    }
    
    /**
     * Initialize both audio sources (following MicrophoneManager.init() pattern)
     */
    private void initMixedAudio() {
        if (audioRecord != null && internalAudioRecord != null) {
            audioRecord.startRecording();
            internalAudioRecord.startRecording();
            running = true;
            Log.i(TAG, "Mixed audio started");
        } else {
            Log.e(TAG, "Error starting mixed audio, use createMixedAudioMicrophone() before start()");
        }
    }
    
    /**
     * Read and mix audio from both sources (following MicrophoneManager.read() pattern)
     */
    private Frame readMixedAudio() {
        try {
            // Read from microphone
            pcmBuffer.rewind();
            int micSize = audioRecord.read(pcmBuffer, pcmBuffer.remaining());
            if (micSize < 0) return null;
            
            // Read from internal audio
            internalPcmBuffer.rewind();
            int internalSize = internalAudioRecord.read(internalPcmBuffer, internalPcmBuffer.remaining());
            if (internalSize < 0) return null;
            
            // Ensure both have the same size
            int frameSize = Math.min(micSize, internalSize);
            
            // Process mixed audio directly from buffers without unnecessary copies
            byte[] mixedData;
            if (customAudioEffect != null) {
                // Limit buffers to the actual read size
                pcmBuffer.limit(frameSize);
                internalPcmBuffer.limit(frameSize);
                
                // Create byte arrays from the limited buffers
                byte[] micData = new byte[frameSize];
                byte[] internalData = new byte[frameSize];
                pcmBuffer.get(micData);
                internalPcmBuffer.get(internalData);
                
                // Apply muting if needed
                if (muted) {
                    micData = pcmBufferMuted;
                }
                
                if (internalMuted) {
                    internalData = pcmBufferMuted;
                }
                
                // Process both audio streams
                mixedData = customAudioEffect.process(micData, internalData);
            } else {
                // If no custom effect, just return microphone data
                mixedData = new byte[frameSize];
                pcmBuffer.get(mixedData);
            }
            
            return new Frame(mixedData, 0, mixedData.length);
            
        } catch (Exception e) {
            Log.e(TAG, "Error reading mixed audio", e);
            return null;
        }
    }
    
    public void internalMute() {
        internalMuted = true;
    }
    
    public void internalUnMute() {
        internalMuted = false;
    }
    
    public boolean isInternalMuted() {
        return internalMuted;
    }
    
    /**
     * Override stop() to handle both audio sources
     */
    @Override
    public synchronized void stop() {
        running = false;
        created = false;
        
        // Stop handler thread (same as MicrophoneManager)
        if (handlerThread != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                handlerThread.quitSafely();
            } else {
                handlerThread.quit();
            }
        }
        
        // Stop and release microphone
        if (audioRecord != null) {
            audioRecord.setRecordPositionUpdateListener(null);
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        
        // Stop and release internal audio
        if (internalAudioRecord != null) {
            internalAudioRecord.stop();
            internalAudioRecord.release();
            internalAudioRecord = null;
        }
        
        // Release audio effects
        if (audioPostProcessEffect != null) {
            audioPostProcessEffect.releaseEchoCanceler();
            audioPostProcessEffect.releaseNoiseSuppressor();
        }
        
        if (internalAudioPostProcessEffect != null) {
            internalAudioPostProcessEffect.releaseEchoCanceler();
            internalAudioPostProcessEffect.releaseNoiseSuppressor();
        }
        
        Log.i(TAG, "Mixed audio stopped");
    }
    
    
    @Override
    public int getMaxInputSize() {
        if (internalAudioRecord != null) {
            // Return the larger of the two buffer sizes
            return Math.max(super.getMaxInputSize(), internalPcmBuffer.capacity());
        }
        return super.getMaxInputSize();
    }
}