package com.SensorStreamer.Model;

/**
 * IMU 数据结构
 * @author chen
 * @version 1.0
 * */

public class IMUData {
    long unixTimestamp;
    long sensorTimestamp;
    String sensorType;
    float[] values;

    /**
     * @param unixTimestamp 系统时间戳
     * @param sensorTimestamp 传感器时间戳
     * @param sensorType 传感器类型
     * @param values 传感器数据
     * */
    public IMUData(long unixTimestamp, long sensorTimestamp, String sensorType, float[] values) {
        this.unixTimestamp = unixTimestamp;
        this.sensorTimestamp = sensorTimestamp;
        this.sensorType = sensorType;
        this.values = values;
    }
}
