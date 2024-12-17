package com.SensorStreamer.Model.Listen.Data;

import android.hardware.Sensor;

import com.SensorStreamer.Resource.String.DataString;

import java.util.HashMap;

/**
 * Sensor 数据结构
 * @author chen
 * @version 1.0
 * */

public class SensorData extends TypeData {
    public static final String TYPE = DataString.TYPE_SENSOR;
//    数据类型字典
    private final static HashMap<Integer, String> sensorTypeDir = new HashMap<Integer, String>(){{
        put(Sensor.TYPE_ACCELEROMETER, DataString.TYPE_ACCELEROMETER);
        put(Sensor.TYPE_GYROSCOPE, DataString.TYPE_GYROSCOPE);
        put(Sensor.TYPE_ROTATION_VECTOR, DataString.TYPE_ROTATION_VECTOR);
        put(Sensor.TYPE_MAGNETIC_FIELD, DataString.TYPE_MAGNETIC_FIELD);
        put(Sensor.TYPE_LIGHT, DataString.TYPE_LIGHT);
        put(Sensor.TYPE_PRESSURE, DataString.TYPE_PRESSURE);
        put(Sensor.TYPE_GRAVITY, DataString.TYPE_GRAVITY);
        put(Sensor.TYPE_LINEAR_ACCELERATION, DataString.TYPE_LINEAR_ACCELERATION);
        put(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, DataString.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        put(Sensor.TYPE_GAME_ROTATION_VECTOR, DataString.TYPE_GAME_ROTATION_VECTOR);
        put(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, DataString.TYPE_GYROSCOPE_UNCALIBRATED);
        put(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT, DataString.TYPE_LOW_LATENCY_OFFBODY_DETECT);
        put(Sensor.TYPE_PROXIMITY, DataString.TYPE_PROXIMITY);
        put(Sensor.TYPE_RELATIVE_HUMIDITY, DataString.TYPE_RELATIVE_HUMIDITY);
        put(Sensor.TYPE_AMBIENT_TEMPERATURE, DataString.TYPE_AMBIENT_TEMPERATURE);
        put(Sensor.TYPE_POSE_6DOF, DataString.TYPE_POSE_6DOF);
        put(Sensor.TYPE_STATIONARY_DETECT, DataString.TYPE_STATIONARY_DETECT);
        put(Sensor.TYPE_MOTION_DETECT, DataString.TYPE_MOTION_DETECT);
        put(Sensor.TYPE_HEART_BEAT, DataString.TYPE_HEART_BEAT);
        put(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, DataString.TYPE_ACCELEROMETER_UNCALIBRATED);
        put(69682, DataString.TYPE_HEART_BEAT);
    }};

    public long sensorTimestamp;
    public float[] values;

    /**
     * @param sensorType 传感器类型
     * @param unixTimestamp 系统时间戳
     * @param sensorTimestamp 传感器时间戳
     * @param values 传感器数据
     * */
    public SensorData(String sensorType, long unixTimestamp, long sensorTimestamp, float[] values) {
        super(sensorType, unixTimestamp);
        this.sensorTimestamp = sensorTimestamp;
        this.values = values;
    }
}
