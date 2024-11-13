package com.SensorStreamer.Component.Listen;

import android.app.Activity;

/**
 * IMUListen 工厂
 * @author chen
 * @version 1.0
 * */

public class IMUListenF extends ListenF {
    @Override
    public Listen create(Activity activity) {
        return new IMUListen(activity);
    }
}
