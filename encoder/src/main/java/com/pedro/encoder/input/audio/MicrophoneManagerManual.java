package com.pedro.encoder.input.audio;

import android.os.HandlerThread;
import android.util.Log;

import com.pedro.encoder.Frame;
import com.pedro.encoder.GetFrame;

import java.nio.ByteBuffer;

/**
 * Similar to MicrophoneManager but samples are not read automatically.
 * The owner must manually call read(...) as often as samples are needed.
 */
public class MicrophoneManagerManual extends MicrophoneManager implements GetFrame {

  private final String TAG = "MicMM";

  public MicrophoneManagerManual() {
    super(null);
  }

  /**
   * Start record and get data
   */
  @Override
  public synchronized void start() {
    init();
  }

  private void init() {
    if (audioRecord != null) {
      audioRecord.startRecording();
      running = true;
      Log.i(TAG, "Microphone started");
    } else {
      Log.e(TAG, "Error starting, microphone was stopped or not created, "
          + "use createMicrophone() before start()");
    }
    if (audioInternalRecord != null) {
      audioInternalRecord.startRecording();
      running = true;
      Log.i(TAG, "Microphone started");
    } else {
      Log.e(TAG, "Error starting, microphone was stopped or not created, "
          + "use createMicrophone() before start()");
    }
  }

  /**
   * Call when you need mic samples.
   * This method will block until numBytes worth of samples are ready.
   */
  public int read(ByteBuffer directBuffer, int numBytes) {
    directBuffer.rewind();
    // write to the buffer and return number of bytes written.
    return audioRecord.read(directBuffer, numBytes);
  }

  /**
   * Stop and release microphone
   */
  public synchronized void stop() {
    // handlerThread must not be null, else the stop impl will throw
    handlerThread = new HandlerThread("nothing");
    super.stop();
  }

  public GetFrame getGetFrame() {
    return this;
  }

  @Override
  public Frame getInputFrame() {
    pcmBuffer.rewind();
    internalPcmBuffer.rewind();
    int size = audioRecord.read(pcmBuffer, pcmBuffer.remaining());
    int internalSize = audioInternalRecord.read(internalPcmBuffer, internalPcmBuffer.remaining());
    Log.d(TAG, "size: " + size);
    Log.d(TAG, "internalSize: " + internalSize);
    
    if (size <= 0 && internalSize <= 0) {
      return null;
    }
    if (size > 0 && internalSize <= 0) {
      return new Frame(muted ? pcmBufferMuted : customAudioEffect.process(pcmBuffer.array()),
          muted ? 0 : pcmBuffer.arrayOffset(), size);
    }
    if (size <= 0 && internalSize > 0) {
      return new Frame(muted ? pcmBufferMuted : customAudioEffect.process(internalPcmBuffer.array()),
          muted ? 0 : internalPcmBuffer.arrayOffset(), internalSize);
    }
    
    return new Frame(muted ? pcmBufferMuted : customAudioEffect.process(pcmBuffer.array(), internalPcmBuffer.array()),
        muted ? 0 : pcmBuffer.arrayOffset(), size);
    
    //    short[] array = new short[size / 2];
    //    for (int i = 0; i < size / 2; i++) {
    //      array[i] = pcmBuffer.getShort();
    //    }
    //    short[] internalArray = new short[internalSize / 2];
    //    for (int i = 0; i < internalSize / 2; i++) {
    //      internalArray[i] = internalPcmBuffer.getShort();
    //    }
    //    int shortArraySize = Math.min(array.length, internalArray.length);
    //
    //    for (int i = 0; i < shortArraySize; i++) {
    //      Log.d(TAG, "array[i]: " + array[i] + ", internalArray[i]: " + internalArray[i]);
    //      int sum = (int)array[i] + (int)(internalArray[i]);
    //      array[i] = (short)Math.min((int)Short.MAX_VALUE, sum);
    //      Log.d(TAG, "sum: " + sum + ", array[i]: " + array[i]);
    //    }
    //    ByteBuffer buffer = ByteBuffer.allocate(array.length * 2);
    //    for (short s : array) {
    //      buffer.putShort(s);
    //    }
    //
    //    return new Frame(muted ? pcmBufferMuted : customAudioEffect.process(buffer.array()),
    //        muted ? 0 : buffer.arrayOffset(), size);
  }
}
