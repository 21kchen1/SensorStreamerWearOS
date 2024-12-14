package com.SensorStreamer.Services.Command.ListenCommand;

import com.SensorStreamer.Component.Listen.Listen;
import com.SensorStreamer.Services.Command.Command;

/**
 * Listen 抽象命令
 * @author chen
 * @version 1.0
 * */

public interface ListenCommand extends Command {
    /**
     * 启动行为
     * @param params 参数列表
     * @param callback 回调函数
     * */
    void launch(String[] params, Listen.ListenCallback callback);

    /**
     * 关闭行为
     * */
    void off();
}
