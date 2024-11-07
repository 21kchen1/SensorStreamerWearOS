package com.SensorStreamer.Component.Listen;

/**
 * AudioListen 工厂
 * @author chen
 * @version 1.0
 * */

public class AudioListenF extends ListenF {
    @Override
    public Listen create() {
        return new AudioListen();
    }
}
