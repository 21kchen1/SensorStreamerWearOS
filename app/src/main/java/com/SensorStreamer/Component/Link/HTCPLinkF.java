package com.SensorStreamer.Component.Link;

/**
 * HTCPLink 工厂
 * @author chen
 * @version 1.0
 * */

public class HTCPLinkF extends LinkF {
    @Override
    public Link create() {
        return new HTCPLink();
    }
}
