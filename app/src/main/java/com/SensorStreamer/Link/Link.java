package com.SensorStreamer.Link;

import java.net.InetAddress;

/**
 * Link 抽象类
 * @author chen
 * @version 1.0
 * */

public abstract class Link {
    public abstract boolean star();
    public abstract boolean off();
    public abstract void send(InetAddress address, int port, byte[] buf);
    public abstract void rece(InetAddress address, int port, byte[] buf);
}
