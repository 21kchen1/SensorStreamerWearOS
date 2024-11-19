package com.SensorStreamer.Component.Switch;

import android.util.Log;

import com.SensorStreamer.Component.Link.Link;
import com.SensorStreamer.Model.RemotePDU;
import com.SensorStreamer.Utils.TypeTranDeter;

import java.nio.charset.Charset;

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
//    监听线程
    private Thread listenThread;

    /**
     * 常量初始化
     * */
    public RemoteSwitch() {
        super();
    }

    /**
     * 注册，不负责组件的 launch
     * */
    @Override
    public synchronized boolean launch(Link link, SwitchCallback callback) {
//        0 0
        if (!this.canLaunch()) return false;

        if (!link.canSend() || !link.canRece()) return false;

        this.link = link;
        this.callback = (RemoteCallback) callback;

//        1 0
        return this.launchFlag = true;
    }

    /**
     * 注销，不负责组件的 off
     * */
    @Override
    public synchronized boolean off() {
//        1 0
        if (!this.canOff())
            return false;

        this.link = null;
        this.callback = null;

//        0 0
        this.launchFlag = false;
        return true;
    }

    /**
     * 开始监听事件，并基于事件类型选择对应的回调函数
     * */
    @Override
    public synchronized void startListen(int bufSize) {
//        1 0
        if (!this.canStartListen())
            return;

        this.listenThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (!this.link.canRece())
                        break;
                    String msg = this.link.rece(bufSize, Link.INTNULL);
//                    信息格式确认
                    if (!TypeTranDeter.canStr2JsonData(msg, RemotePDU.class))
                        continue;
                    RemotePDU remotePDU = this.gson.fromJson(msg, RemotePDU.class);
//                    信息类型校准
                    if (!remotePDU.type.equals(RemotePDU.TYPE_CONTROL))
                        continue;
//                    根据类型执行回调函数
                    switch (remotePDU.control) {
                        case RemotePDU.CONTROL_SWITCHON: {
                            this.callback.switchOn();
                            break;
                        }
                        case RemotePDU.CONTROL_SWITCHOFF: {
                            this.callback.switchOff();
                            break;
                        }
                    }
                }
            }
            catch (Exception e) {
                Log.e("RemoteSwitch", "startListen.listenThread:Exception", e);
            } finally {
                this.stopListen();
            }
        });

//        1 1
        this.startFlag = true;
        this.listenThread.start();
    }

    /**
     * 停止监听事件
     * */
    @Override
    public synchronized void stopListen() {
//        1 1
        if (!this.canStopListen())
            return;

        if (this.listenThread != null)
            this.listenThread.interrupt();

//        1 0
        this.startFlag = false;
    }
}
