package com.SensorStreamer.Component.Link;

import java.net.InetAddress;

/**
 * Link 抽象类
 * @author chen
 * @version 1.0
 * */

public abstract class Link {
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
     * @param buf 数据缓存
     * */
    public abstract void send(byte[] buf);
    /**
     * 收信
     * @param buf 数据缓存
     * */
    public abstract void rece(byte[] buf);
}
