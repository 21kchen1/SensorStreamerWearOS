package com.SensorStreamer.Model.Control;

/**
 * 传感器控制数据结构
 * @author chen
 * @version 1.0
 * */

public class SensorControl {
    public String type;
    public int sampling;
    public int[] sensors;

    /**
     * @param sampling 采样率
     * @param sensors 传感器
     * */
    public SensorControl(String type, int sampling, int[] sensors) {
        this.type = type;
        this.sampling = sampling;
        this.sensors = sensors;
    }
}
