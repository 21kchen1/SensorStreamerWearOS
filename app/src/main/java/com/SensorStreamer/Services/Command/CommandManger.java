package com.SensorStreamer.Services.Command;

import com.SensorStreamer.Services.Command.ListenCommand.ListenCommand;

import java.util.HashMap;

/**
 * CommandManger 抽象类
 * @author chen
 * @version 1.0
 * */

public abstract class CommandManger {
    protected final HashMap<String, Command> commandMap;

    /**
     * 初始化字典
     * */
    protected CommandManger() {
        this.commandMap = new HashMap<>();
    }

    /**
     * 添加命令键与对应的命令
     * @param key 命令键
     * @param command 命令
     * @return 是否添加成功
     * */
    public boolean addCommand(String key, Command command) {
        if (this.commandMap.containsKey(key))
            return false;

        this.commandMap.put(key, command);
        return true;
    }

    /**
     * 批量添加命令键与对应的命令
     * @param commands 命令列表
     * @return 是否添加成功
     * */
    public boolean addCommands(HashMap<String, Command> commands) {
        for (String key : commands.keySet()) {
            if (this.addCommand(key, commands.get(key)))
                continue;
            return false;
        }
        return true;
    }
}
