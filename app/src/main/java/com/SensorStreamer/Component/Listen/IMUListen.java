package com.SensorStreamer.Component.Listen;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import com.SensorStreamer.Utils.TypeTranDeter;

import java.util.HashMap;

/**
 * 读取 IMU 数据，并基于回调函数处理
 * @author chen
 * @version 1.0
 * */

public class IMUListen extends Listen {
    /**
     * IMU 回调函数类接口
     * */
    public interface IMUCallback extends ListenCallback {
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
//    当采样率为 0 时 启动变化时传输数据
    private int samplingRate;
//    需要监听的 sensor
    private Sensor[] sensors;

    /**
     * 常量初始化
     * */
    public IMUListen(Activity activity) {
        super(activity);

        this.sensorDir = new HashMap<>();
        this.sensorDir.put(Sensor.TYPE_ACCELEROMETER, "accel");
        this.sensorDir.put(Sensor.TYPE_GYROSCOPE, "gyro");
        this.sensorDir.put(Sensor.TYPE_ROTATION_VECTOR, "rotvec");
        this.sensorDir.put(Sensor.TYPE_MAGNETIC_FIELD, "mag");
    
        this.samplingRate = this.intNull = -1;
    }

    /**
     * 启动组件并设置回调函数，适配器
     * @param sensors Sensor 类型
     * @param samplingRate 采样率
     * @param callback 数据处理回调函数
     * */
    public synchronized boolean launch(int[] sensors, int samplingRate, IMUCallback callback) {
//        0 0
        if (!this.canLaunch()) return false;
        
        String[] params = new String[sensors.length + 1];
//        将类型转换为字符串参数
        for (int i = 0; i < sensors.length; i++) {
            params[i] = Integer.toString(sensors[i]);
        }
        params[sensors.length] = Integer.toString(samplingRate);

        return this.launch(params, callback);
    }

    /**
     * 选择 Sensor，并设置成员变量，优先使用重载适配器
     * */
    @Override
    public synchronized boolean launch(String[] params, ListenCallback callback) {
//        0 0
        if (!this.canLaunch()) return false;

        if (params.length < 2 || params.length > this.sensorDir.size() + 1 || !TypeTranDeter.canStr2Num(params[params.length - 1]) || Integer.parseInt(params[params.length - 1]) < 0)
            return false;

        this.samplingRate = Integer.parseInt(params[params.length - 1]);

        this.sensors = new Sensor[params.length - 1];
//        获取当前选择的 Sensor
        for (int i = 0; i < params.length - 1; i++) {
//            是否存在于字典中 判断是否有效
            if (!TypeTranDeter.canStr2Num(params[i]) || !this.sensorDir.containsKey(Integer.parseInt(params[i]))) {
                this.launchFlag = true;
                this.off();
                return false;
            }
            int type = Integer.parseInt(params[i]);
            this.sensors[i] = this.sensorManager.getDefaultSensor(type);
        }

        this.callback = (IMUCallback) callback;

//        1 0
        return this.launchFlag = true;
    }

    /**
     * 注销 IMU 监听组件
     * @implNote 如果在调用 off 前有使用 startRead，必须先使用 stopRead
     * */
    @Override
    public synchronized boolean off() {
//        1 0
        if (!this.canOff()) return false;

        this.samplingRate = this.intNull;
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
    public synchronized void startRead() {
//        1 0
        if (!this.canStartRead()) return;

//        注册对应的监听
        for (Sensor sensor : this.sensors) {
            this.sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

//        1 1
        this.startFlag = true;
    }

    /**
     * 停止处理 IMU 数据
     * @// TODO: 2024/11/7 记得处理按采样率发送数据的程序
     * */
    @Override
    public synchronized void stopRead() {
//        1 1
        if (!this.canStopRead()) return;

//        注销对应的监听
        for (Sensor sensor : this.sensors) {
            this.sensorManager.unregisterListener(this, sensor);
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

        Thread readChangedThread = new Thread(() -> {
            try {
                if (this.callback == null)
                    return;
//                使用字典将 type 转换为 String
                this.callback.dealIMUData(this.sensorDir.get(sensorEvent.sensor.getType()), sensorEvent.values, sensorEvent.timestamp);
            } catch (Exception e) {
                Log.e("IMUListen", "onSensorChanged:Exception", e);
            }
        });
        readChangedThread.start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
