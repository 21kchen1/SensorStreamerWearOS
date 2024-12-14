package com.SensorStreamer.Services.Command.ListenCommand;

import com.SensorStreamer.Component.Listen.Listen;
import com.SensorStreamer.Services.Command.Command;
import com.SensorStreamer.Services.Command.CommandManger;

public class ListenCommandManger extends CommandManger {

    public ListenCommandManger() {
        super();
    }

    /**
     * 根据参数启动 Launch 命令
     * @param key 命令键
     * @param params 参数
     * @param callback 回调函数
     */
    public void executeLaunchCommand(String key, String[] params, Listen.ListenCallback callback) {
        ListenCommand listenCommand = (ListenCommand) this.commandMap.get(key);
        if (listenCommand == null)
            return;
        listenCommand.launch(params, callback);
    }

    /**
     * 根据参数启动 Off 命令
     * @param key 命令键
     */
    public void executeOffCommand(String key) {
        ListenCommand listenCommand = (ListenCommand) this.commandMap.get(key);
        if (listenCommand == null)
            return;
        listenCommand.off();
    }

    /**
     * 启动所有 Off 指令
     * */
    public void executeAllOffCommand() {
        for (Command command : this.commandMap.values())
            ((ListenCommand) command).off();
    }
}
