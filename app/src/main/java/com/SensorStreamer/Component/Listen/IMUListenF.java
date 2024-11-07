package com.SensorStreamer.Component.Listen;

import android.hardware.SensorManager;

/**
 * IMUListen 工厂
 * @author chen
 * @version 1.0
 * */

public class IMUListenF extends ListenF {
    @Override
    public Listen create() {
        return new IMUListen();
    }
}
