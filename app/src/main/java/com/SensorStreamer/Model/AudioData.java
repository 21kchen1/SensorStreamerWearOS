package com.SensorStreamer.Model;

/**
 * 音频数据结构
 * @author chen
 * @version 1.0
 * */

public class AudioData {
    public long unixTimestamp;
    public byte[] audioData;

    /**
     * @param unixTimestamp 时间戳
     * @param audioData 音频数据
     * */
    public AudioData(long unixTimestamp, byte[] audioData) {
        this.unixTimestamp = unixTimestamp;
        this.audioData = audioData;
    }
}
