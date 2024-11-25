package com.SensorStreamer.Model.Data;

/**
 * Sensor 数据结构
 * @author chen
 * @version 1.0
 * */

public class SensorData extends TypeData {
    public static final String TYPE = "SensorData";
    public long sensorTimestamp;
    public String sensorType;
    public float[] values;

    /**
     * @param unixTimestamp 系统时间戳
     * @param sensorTimestamp 传感器时间戳
     * @param sensorType 传感器类型
     * @param values 传感器数据
     * */
    public SensorData(long unixTimestamp, long sensorTimestamp, String sensorType, float[] values) {
        super(SensorData.TYPE, unixTimestamp);
        this.sensorTimestamp = sensorTimestamp;
        this.sensorType = sensorType;
        this.values = values;
    }
}
