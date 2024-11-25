package com.SensorStreamer.Model.Sensor;

/**
 * 传感器控制数据结构
 * @author chen
 * @version 1.0
 * */

public class SensorControl {
    public int sampling;
    public int[] sensors;

    /**
     * @param sampling 采样率
     * @param sensors 传感器
     * */
    public SensorControl(int sampling, int[] sensors) {
        this.sampling = sampling;
        this.sensors = sensors;
    }
}
