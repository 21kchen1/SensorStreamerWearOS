package com.SensorStreamer.Component.Link;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Link 抽象类
 * @author chen
 * @version 1.2
 * @// TODO: 2024/11/15 记得加timeout，超时就告诉普通rece，取消从队列获取数据
 * */

public abstract class Link {
//    心跳标记
    protected final static String HEARTBEAT = "heartbeat";
    public final static int INTNULL = 0;

//    缓存最值
    protected final int maxBufSize, minBufSize;

    protected boolean launchFlag;
    protected InetAddress address;
    protected int port;
//    自适应缓存大小
    protected int bufSize;
//    编码集
    protected Charset charset;

    /**
     * 设置启动标志
     * 0 launch
     * 1 off
     * */
    public Link() {
        this.launchFlag = false;

        this.maxBufSize = 65536;
        this.minBufSize = 128;
        this.bufSize = 1024;

    }

    /**
     * 注册组件
     * @param address 目的地址
     * @param port 目的端口
     * @param timeout 连接超时
     * @param charset 编码集
     * @return 是否注册成功
     * */
    public abstract boolean launch(InetAddress address, int port, int timeout, Charset charset);

    /**
     * 注销组件
     * @return 是否注销成功
     * */
    public abstract boolean off();

    /**
     * 发信
     * @param msg 数据
     * */
    public abstract void send(String msg);

    /**
     * 收息
     * @param bufSize 缓存大小
     * @param timeLimit 接收时间限制，毫秒为单位
     * @return 数据
     * */
    public abstract String rece(int bufSize, int timeLimit);


    /**
     * 自适应缓冲大小设置
     * @param packetSize 当前包大小
     * */
    protected abstract void adaptiveBufSize(int packetSize);

    /**
     * 能否注册
     * */
    public boolean canLaunch() {
        return !this.launchFlag;
    }

    /**
     * 能否注销
     * */
    public boolean canOff() {
        return this.launchFlag;
    }

    /**
     * 能否发送
     * */
    public boolean canSend() {
        return this.canOff();
    }

    /**
     * 能否接收
     * */
    public boolean canRece() {
        return this.canOff();
    }
}
