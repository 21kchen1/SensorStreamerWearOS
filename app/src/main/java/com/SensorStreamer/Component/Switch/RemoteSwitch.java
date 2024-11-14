package com.SensorStreamer.Component.Switch;

import com.SensorStreamer.Component.Link.Link;

/**
 * 远程开关，并基于回调函数处理
 * @author chen
 * @version 1.0
 * */

public class RemoteSwitch extends Switch {
    /**
     * Remote 回调函数类接口
     * */
    public interface RemoteCallback extends SwitchCallback {
        void switchOn();
        void switchOff();
    }

//    回调函数
    private RemoteCallback callback;
//    连接
    private Link link;

    /**
     * 常量初始化
     * */
    public RemoteSwitch() {
        super();
    }

    @Override
    public boolean launch(Link link, SwitchCallback callback) {
//        0 0
        if (this.launchFlag || this.startFlag)
            return false;



        this.callback = (RemoteCallback) callback;

//        1 0
        return this.launchFlag = true;
    }

    @Override
    public boolean off() {
//        1 0
        if (!this.launchFlag || this.startFlag)
            return false;

//        0 0
        this.launchFlag = false;
        return true;
    }

    @Override
    public void startListen() {
//        1 0
        if (!this.launchFlag || this.startFlag)
            return;

//        1 1
        this.startFlag = true;
    }

    @Override
    public void stopListen() {
//        1 1
        if (!this.launchFlag || !this.startFlag)
            return;

//        1 0
        this.startFlag = false;
    }
}
