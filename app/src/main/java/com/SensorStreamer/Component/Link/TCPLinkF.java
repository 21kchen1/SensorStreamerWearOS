package com.SensorStreamer.Component.Link;

/**
 * TCPLink 工厂
 * @author chen
 * @version 1.0
 * */

public class TCPLinkF extends LinkF {
    @Override
    public Link create() {
        return new TCPLink();
    }
}