package com.SensorStreamer.Services.Command.ListenCommand;

import com.SensorStreamer.Component.Listen.Listen;
import com.SensorStreamer.Component.Listen.SensorListen.SensorListen;

/**
 * SensorListen 命令
 * @author chen
 * @version 1.0
 * */

public class SensorCommand implements ListenCommand {
    private final SensorListen[] sensorListens;

    /**
     * 支持多个传感器组合，仅限同种数据类型的传感器
     * */
    public SensorCommand(SensorListen... sensorListens) {
        this.sensorListens = sensorListens;
    }

    /**
     * 启动各个传感器
     * @param params 无须参数
     * @param callback SensorCallback 回调函数
     * */
    @Override
    public void launch(String[] params, Listen.ListenCallback callback) {
        for (SensorListen sensorListen : this.sensorListens) {
            sensorListen.launch(null, callback);
            sensorListen.startRead();
        }
    }

    /**
     * 关闭各个传感器
     * */
    @Override
    public void off() {
        for (SensorListen sensorListen : this.sensorListens) {
            sensorListen.stopRead();
            sensorListen.off();
        }
    }
}
