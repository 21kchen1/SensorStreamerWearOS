package com.SensorStreamer.Component.Switch;

/**
 * Switch 抽象类
 * @author chen
 * @version 1.0
 * */

public abstract class Switch {
    protected boolean launchFlag, startFlag;

    /**
     * 设置启动和开始标志
     * 0 0 launch
     * 1 0 off, start
     * 1 1 stop
     * 0 1 error
     * */
    public Switch() {
        this.launchFlag = this.startFlag = false;
    }
}
