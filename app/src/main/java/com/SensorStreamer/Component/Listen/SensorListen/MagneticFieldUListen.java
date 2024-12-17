package com.SensorStreamer.Component.Listen.SensorListen;

import android.app.Activity;
import android.hardware.Sensor;

import com.SensorStreamer.Resource.String.DataString;

/**
 * MagneticFieldUListen 未校准的 允许自定义数据处理
 * @author chen
 * @version 1.0
 * */

public class MagneticFieldUListen extends SensorListen {

    public MagneticFieldUListen(Activity activity) {
        super(activity);
    }

    @Override
    protected Sensor getSensor() {
        return this.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
    }

    @Override
    protected String getType() {
        return DataString.TYPE_MAGNETIC_FIELD_UNCALIBRATED;
    }
}
