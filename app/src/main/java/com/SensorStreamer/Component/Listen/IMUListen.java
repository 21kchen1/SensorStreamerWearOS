package com.SensorStreamer.Component.Listen;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.SensorStreamer.Utils.TypeTranDeter;

import java.util.HashMap;

/**
 * 读取 IMU 数据，并基于回调函数处理
 * @author chen
 * @version 1.0
 * */

public class IMUListen extends Listen {
    /**
     * Audio 回调函数类接口
     * */
    public interface IMUCallback {
        /**
         * 回调函数 用于处理数据
         * @param type Sensor 类型
         * @param data 传入 Sensor 数据
         * @param sensorTimestamp 与硬件绑定的时间戳
         * */
        void dealIMUData(String type, float[] data, long sensorTimestamp);
    }

    private final HashMap<Integer, String> sensorDir;
    private final int intNull;
//    回调函数
    private IMUCallback callback;
//    IMU 管理者
    private SensorManager imuManager;
//    当采样率为 0 时 启动变化时传输数据
    private int samplingRate;
//    需要监听的 sensor
    private Sensor[] sensors;

    /**
     * 常量初始化
     * */
    public IMUListen() {
        super();

        this.sensorDir = new HashMap<>();
        this.sensorDir.put(Sensor.TYPE_ACCELEROMETER, "accel");
        this.sensorDir.put(Sensor.TYPE_GYROSCOPE, "gyro");
        this.sensorDir.put(Sensor.TYPE_ROTATION_VECTOR, "rotvec");
        this.sensorDir.put(Sensor.TYPE_MAGNETIC_FIELD, "mag");

        this.samplingRate = this.intNull = -1;
    }

    /**
     * 启动组件并设置回调函数，适配器
     * @param imuManager 传入 SensorManager 类
     * @param sensors Sensor 类型
     * @param samplingRate 采样率
     * @param callback 数据处理回调函数
     * */
    public boolean launch(SensorManager imuManager, int[] sensors, int samplingRate, IMUCallback callback) {
//        0 0
        if (this.launchFlag || this.startFlag)
            return false;

        String[] params = new String[sensors.length + 1];
//        将类型转换为字符串参数
        for (int i = 0; i < sensors.length; i++) {
            params[i] = Integer.toString(sensors[i]);
        }
        params[sensors.length] = Integer.toString(samplingRate);

        this.setIMUManager(imuManager);
        this.setCallback(callback);

        return this.launch(params);
    }

    /**
     * 设置回调函数
     * @param callback 回调函数
     * */
    public void setCallback(IMUCallback callback) {
        this.callback = callback;
    }

    /**
     * 设置回调函数
     * @param imuManager IMU 管理
     * */
    public void setIMUManager(SensorManager imuManager) {
        this.imuManager = imuManager;
    }

    /**
     * 选择 Sensor，并设置成员变量，优先使用重载适配器
     * */
    @Override
    public boolean launch(String[] params) {
//        0 0
        if (this.launchFlag || this.startFlag)
            return false;

        if (this.imuManager == null || params.length < 2 || params.length > 5 || !TypeTranDeter.isStr2Num(params[params.length - 1]) || Integer.parseInt(params[params.length - 1]) < 0)
            return false;

        this.samplingRate = Integer.parseInt(params[params.length - 1]);

        this.sensors = new Sensor[params.length - 1];
//        获取当前选择的 Sensor
        for (int i = 0; i < params.length - 1; i++) {
//            是否存在于字典中 判断是否有效
            if (!TypeTranDeter.isStr2Num(params[i]) || !this.sensorDir.containsKey(Integer.parseInt(params[i]))) {
                this.launchFlag = true;
                this.off();
                return false;
            }
            int type = Integer.parseInt(params[i]);
            this.sensors[i] = this.imuManager.getDefaultSensor(type);
        }

//        1 0
        return this.launchFlag = true;
    }

    /**
     * 注销 IMU 监听组件
     * @implNote 如果在调用 off 前有使用 startRead，必须先使用 stopRead
     * */
    @Override
    public boolean off() {
//        1 0
        if (!this.launchFlag || this.startFlag)
            return false;

        this.samplingRate = this.intNull;
        this.imuManager = null;
        this.sensors = null;
        this.callback = null;

//        0 0
        this.launchFlag = false;
        return true;
    }

    /**
     * 读取并使用回调函数处理 IMU 数据
     * 仅当采样率为零时启动 onSensorChanged
     * @// TODO: 2024/11/7 还需要添加按采样率发送数据的程序，需要设置buf和大小
     * */
    @Override
    public void startRead() {
//        1 0
        if (!this.launchFlag || this.startFlag)
            return;

//        注册对应的监听
        for (Sensor sensor : this.sensors) {
            this.imuManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

//        1 1
        this.startFlag = true;
    }

    /**
     * 停止处理 IMU 数据
     * @// TODO: 2024/11/7 记得处理按采样率发送数据的程序
     * */
    @Override
    public void stopRead() {
//        1 1
        if (!this.launchFlag || !this.startFlag)
            return;

//        注销对应的监听
        for (Sensor sensor : this.sensors) {
            this.imuManager.unregisterListener(this, sensor);
        }

//        1 0
        this.startFlag = false;
    }

    /**
     * 当 IMU 数据变化时执行回调函数，仅当采样率为零时生效
     * */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        1 1
        if (this.samplingRate > 0 || !this.startFlag)
            return;

        if (callback == null)
            return;
//        使用字典将 type 转换为 String
        callback.dealIMUData(this.sensorDir.get(sensorEvent.sensor.getType()), sensorEvent.values, sensorEvent.timestamp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
