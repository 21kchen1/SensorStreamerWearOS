package com.SensorStreamer.Component.Listen.SensorListen;

import android.app.Activity;
import android.hardware.Sensor;

import com.SensorStreamer.Resource.String.DataString;

/**
 * GyroscopeListen 允许自定义数据处理
 * @author chen
 * @version 1.0
 * */

public class GyroscopeListen extends SensorListen {

    public GyroscopeListen(Activity activity) {
        super(activity);
    }

    @Override
    protected Sensor getSensor() {
        return this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    protected String getType() {
        return DataString.TYPE_GYROSCOPE;
    }
}
