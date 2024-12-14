package com.SensorStreamer.Model.Listen.Data;

import android.hardware.Sensor;

import com.SensorStreamer.Resource.String.ListenString;

import java.util.HashMap;

/**
 * Sensor 数据结构
 * @author chen
 * @version 1.0
 * */

public class SensorData extends TypeData {
    public static final String TYPE = ListenString.TYPE_SENSOR;
//    数据类型字典
    private final static HashMap<Integer, String> sensorTypeDir = new HashMap<Integer, String>(){{
        put(Sensor.TYPE_ACCELEROMETER, ListenString.TYPE_ACCELEROMETER);
        put(Sensor.TYPE_GYROSCOPE, ListenString.TYPE_GYROSCOPE);
        put(Sensor.TYPE_ROTATION_VECTOR, ListenString.TYPE_ROTATION_VECTOR);
        put(Sensor.TYPE_MAGNETIC_FIELD, ListenString.TYPE_MAGNETIC_FIELD);
        put(Sensor.TYPE_LIGHT, ListenString.TYPE_LIGHT);
        put(Sensor.TYPE_PRESSURE, ListenString.TYPE_PRESSURE);
        put(Sensor.TYPE_GRAVITY, ListenString.TYPE_GRAVITY);
        put(Sensor.TYPE_LINEAR_ACCELERATION, ListenString.TYPE_LINEAR_ACCELERATION);
        put(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, ListenString.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        put(Sensor.TYPE_GAME_ROTATION_VECTOR, ListenString.TYPE_GAME_ROTATION_VECTOR);
        put(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, ListenString.TYPE_GYROSCOPE_UNCALIBRATED);
        put(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT, ListenString.TYPE_LOW_LATENCY_OFFBODY_DETECT);
        put(Sensor.TYPE_PROXIMITY, ListenString.TYPE_PROXIMITY);
        put(Sensor.TYPE_RELATIVE_HUMIDITY, ListenString.TYPE_RELATIVE_HUMIDITY);
        put(Sensor.TYPE_AMBIENT_TEMPERATURE, ListenString.TYPE_AMBIENT_TEMPERATURE);
        put(Sensor.TYPE_POSE_6DOF, ListenString.TYPE_POSE_6DOF);
        put(Sensor.TYPE_STATIONARY_DETECT, ListenString.TYPE_STATIONARY_DETECT);
        put(Sensor.TYPE_MOTION_DETECT, ListenString.TYPE_MOTION_DETECT);
        put(Sensor.TYPE_HEART_BEAT, ListenString.TYPE_HEART_BEAT);
        put(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, ListenString.TYPE_ACCELEROMETER_UNCALIBRATED);
        put(69682, ListenString.TYPE_HEART_BEAT);
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
