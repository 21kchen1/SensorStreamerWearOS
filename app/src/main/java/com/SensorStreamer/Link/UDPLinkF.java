package com.SensorStreamer.Link;

/**
 * UDPLink 工厂
 * @author chen
 * @version 1.0
 * */

public class UDPLinkF extends LinkF {
    @Override
    public Link create() {
        return new UDPLink();
    }
}
