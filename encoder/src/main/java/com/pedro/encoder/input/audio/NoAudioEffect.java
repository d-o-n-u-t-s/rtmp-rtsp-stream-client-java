package com.pedro.encoder.input.audio;

public class NoAudioEffect extends CustomAudioEffect {
  
  @Override
  public byte[] process(byte[] pcmBuffer) {
    return pcmBuffer;
  }
  
  @Override
  public byte[] process(byte[] pcmBuffer, byte[] internalPcmBuffer) {
    return pcmBuffer;
  }
}
