package com.SensorStreamer.Model.Listen.Control;

import com.SensorStreamer.Resource.String.ListenString;

/**
 * 音频控制数据结构
 * @author chen
 * @version 1.0
 * */

public class AudioControl extends TypeControl {
    public static final String TYPE = ListenString.TYPE_AUDIO;

    /**
     * @param sampling 采样率
     * */
    public AudioControl(int sampling) {
        super(AudioControl.TYPE, sampling);
    }
}
