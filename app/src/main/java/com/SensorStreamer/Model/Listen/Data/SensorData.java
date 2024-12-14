package com.SensorStreamer.Model.Listen.Data;

import android.hardware.Sensor;

import java.util.HashMap;

/**
 * Sensor 数据结构
 * @author chen
 * @version 1.0
 * */

public class SensorData extends TypeData {
    public static final String TYPE = "SENSOR";
//    数据类型字典
    private final static HashMap<Integer, String> sensorTypeDir = new HashMap<Integer, String>(){{
        put(Sensor.TYPE_ACCELEROMETER, "ACCELEROMETER");
        put(Sensor.TYPE_GYROSCOPE, "GYROSCOPE");
        put(Sensor.TYPE_ROTATION_VECTOR, "ROTATION_VECTOR");
        put(Sensor.TYPE_MAGNETIC_FIELD, "MAGNETIC_FIELD");
        put(Sensor.TYPE_LIGHT, "LIGHT");
        put(Sensor.TYPE_PRESSURE, "PRESSURE");
        put(Sensor.TYPE_GRAVITY, "GRAVITY");
        put(Sensor.TYPE_LINEAR_ACCELERATION, "LINEAR_ACCELERATION");
        put(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, "MAGNETIC_FIELD_UNCALIBRATED");
        put(Sensor.TYPE_GAME_ROTATION_VECTOR, "GAME_ROTATION_VECTOR");
        put(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "GYROSCOPE_UNCALIBRATED");
        put(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT, "LOW_LATENCY_OFFBODY_DETECT");
        put(Sensor.TYPE_PROXIMITY, "PROXIMITY");
        put(Sensor.TYPE_RELATIVE_HUMIDITY, "RELATIVE_HUMIDITY");
        put(Sensor.TYPE_AMBIENT_TEMPERATURE, "AMBIENT_TEMPERATURE");
        put(Sensor.TYPE_POSE_6DOF, "POSE_6DOF");
        put(Sensor.TYPE_STATIONARY_DETECT, "STATIONARY_DETECT");
        put(Sensor.TYPE_MOTION_DETECT, "MOTION_DETECT");
        put(Sensor.TYPE_HEART_BEAT, "HEART_BEAT");
        put(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, "ACCELEROMETER_UNCALIBRATED");
        put(69682, "HEART_BEAT");
    }};

    public long sensorTimestamp;
    public float[] values;

    /**
     * @param sensorType 传感器类型
     * @param unixTimestamp 系统时间戳
     * @param sensorTimestamp 传感器时间戳
     * @param values 传感器数据
     * */
    public SensorData(int sensorType, long unixTimestamp, long sensorTimestamp, float[] values) {
        super(SensorData.sensorTypeDir.get(sensorType) , unixTimestamp);
        this.sensorTimestamp = sensorTimestamp;
        this.values = values;
    }
}
