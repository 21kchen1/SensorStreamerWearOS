package com.SensorStreamer.Model.Audio;

/**
 * 音频控制数据结构
 * @author chen
 * @version 1.0
 * */

public class AudioControl {
    public int sampling;

    /**
     * @param sampling 采样率
     * */
    public AudioControl(int sampling, int[] sensors) {
        this.sampling = sampling;
    }
}
