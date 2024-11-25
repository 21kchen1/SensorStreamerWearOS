package com.SensorStreamer.Model.Control;

/**
 * 音频控制数据结构
 * @author chen
 * @version 1.0
 * */

public class AudioControl extends TypeControl {
    public static final String TYPE = "AudioControl";

    /**
     * @param sampling 采样率
     * */
    public AudioControl(int sampling) {
        super(AudioControl.TYPE, sampling);
    }
}
