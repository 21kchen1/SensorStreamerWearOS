package com.SensorStreamer.Component.Link;

import java.net.InetAddress;
import java.nio.charset.Charset;

/**
 * Link 抽象类
 * @author chen
 * @version 1.2
 * @implNote 注意多线程安全，避免在执行函数中修改公共变量
 * */

public abstract class Link {
    protected boolean launchFlag;
    protected InetAddress address;
    protected int port;

    /**
     * 设置启动标志
     * 0 launch
     * 1 off
     * */
    public Link() {
        this.launchFlag = false;
    }
    /**
     * 注册并启动组件
     * @param address 目的地址
     * @param port 目的端口
     * @return 是否创建成功
     * */
    public abstract boolean launch(InetAddress address, int port);
    /**
     * 注销并关闭组件
     * @return 是否注销成功
     * */
    public abstract boolean off();
    /**
     * 发信
     * @param msg 数据
     * @param charset 编码集
     * */
    public abstract void send(String msg, Charset charset);
    /**
     * 收信
     * @param charset 编码集
     * @param bufSize 缓存大小
     * @return 数据
     * */
    public abstract String rece(Charset charset, int bufSize);
}
