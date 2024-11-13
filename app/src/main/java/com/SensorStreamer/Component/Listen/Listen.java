package com.SensorStreamer.Component.Listen;

import android.app.Activity;

import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Listen 抽象类
 * @author chen
 * @version 2.0
 * */

public abstract class Listen implements SensorEventListener {
    /**
     * 通用回调函数接口
     * */
    public interface ListenCallback {};

    protected final SensorManager sensorManager;
    protected boolean launchFlag, startFlag;

    /**
     * 设置启动和开始标志
     * 0 0 launch
     * 1 0 off, start
     * 1 1 stop
     * 0 1 error
     * */
    public Listen(Activity activity) {
        this.sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        this.launchFlag = this.startFlag = false;
    }

    /**
     * 启动组件
     * @param params 参数列表
     * @param callback 回调函数
     * @return 是否启动成功
     * */
    public abstract boolean launch(String[] params, ListenCallback callback);
    /**
     * 注销组件
     * @return 是否注销成功
     * */
    public abstract boolean off();
    /**
     * 持续读取数据并处理
     * */
    public abstract void startRead();
    /**
     * 结束持续读取数据
     * */
    public abstract void stopRead();
}
