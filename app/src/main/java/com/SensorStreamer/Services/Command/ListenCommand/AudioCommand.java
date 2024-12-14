package com.SensorStreamer.Services.Command.ListenCommand;

import com.SensorStreamer.Component.Listen.AudioListen;
import com.SensorStreamer.Component.Listen.Listen;
import com.SensorStreamer.Utils.TypeTranDeter;

public class AudioCommand implements ListenCommand {
    private final AudioListen audioListen;

    /**
     * 仅支持一个音频
     * */
    public AudioCommand(AudioListen audioListen) {
        this.audioListen = audioListen;
    }

    /**
     * 启动音频
     * @param params 第一个值为采样率
     * @param callback AudioCallback 回调函数
     * */
    @Override
    public void launch(String[] params, Listen.ListenCallback callback) {
        if (params == null || params.length == 0 || !TypeTranDeter.canStr2Num(params[0]))
            return;
        this.audioListen.launch(params, callback);
        this.audioListen.startRead();
    }

    /**
     * 关闭音频
     * */
    @Override
    public void off() {
        this.audioListen.stopRead();
        this.audioListen.off();
    }
}
