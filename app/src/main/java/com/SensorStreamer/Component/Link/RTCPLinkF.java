package com.SensorStreamer.Component.Link;

/**
 * RTCPLink 工厂
 * @author chen
 * @version 1.0
 * */

public class RTCPLinkF extends LinkF {
    @Override
    public Link create() {
        return new RTCPLink();
    }
}
