package com.SensorStreamer.Model.Listen.Control;

import com.SensorStreamer.Resource.String.ListenString;

/**
 * 传感器控制数据结构
 * @author chen
 * @version 1.0
 * */

public class SensorControl extends TypeControl {
    public static final String TYPE = ListenString.TYPE_SENSOR;
//    传感器控制类型
    public int[] sensors;

    /**
     * @param sampling 采样率
     * @param sensors 传感器
     * */
    public SensorControl(int sampling, int[] sensors) {
        super(SensorControl.TYPE, sampling);
        this.sensors = sensors;
    }
}
