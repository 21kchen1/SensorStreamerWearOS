package com.SensorStreamer.Listen;

public class AudioListenF extends ListenF {
    @Override
    public Listen create() {
        return new AudioListen();
    }
}
