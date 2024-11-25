package com.SensorStreamer.Model.Data;

/**
 * 音频数据结构
 * @author chen
 * @version 1.0
 * */

public class AudioData extends TypeData {
    public static final String TYPE = "AudioData";
    public byte[] audioData;

    /**
     * @param unixTimestamp 时间戳
     * @param audioData 音频数据
     * */
    public AudioData(long unixTimestamp, byte[] audioData) {
        super(AudioData.TYPE, unixTimestamp);
        this.audioData = audioData;
    }
}