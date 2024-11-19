package com.SensorStreamer.Component.Listen;

import android.app.Activity;

/**
 * SensorListen 工厂
 * @author chen
 * @version 1.0
 * */

public class SensorListenF extends ListenF {
    @Override
    public Listen create(Activity activity) {
        return new SensorListen(activity);
    }
}
