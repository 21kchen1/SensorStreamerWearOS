package com.SensorStreamer.Component.Listen;

import android.hardware.SensorEventListener;

/**
 * Listen 抽象类
 * @author chen
 * @version 1.0
 * */

public abstract class Listen implements SensorEventListener {
    protected boolean launchFlag, startFlag;

    /**
     * 设置启动和开始标志
     * 0 0 launch
     * 1 0 off, start
     * 1 1 stop
     * 0 1 error
     * */
    public Listen() {
        this.launchFlag = this.startFlag = false;
    }

    /**
     * 启动组件
     * @param params 参数列表
     * @return 是否启动成功
     * */
    public abstract boolean launch(String[] params);
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
