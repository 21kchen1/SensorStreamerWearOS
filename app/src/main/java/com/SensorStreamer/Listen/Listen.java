package com.SensorStreamer.Listen;

import android.hardware.SensorEventListener;

public abstract class Listen implements SensorEventListener {
    /**
     * 回调函数类接口
     * */
    public interface AudioCallback {
        /**
         * 回调函数 用于处理数据
         * @param data 传入音频数据
         * */
        void dealAudioData(byte[] data);
    }
    protected AudioCallback callback;

    /**
     * 启动组件
     * @param params 参数列表
     * @param callback 回调函数
     * */
    public abstract void launch(int[] params, AudioCallback callback);
    /**
     * 关闭组件
     * */
    public abstract void off();
    /**
     * 持续读取数据并处理
     * */
    public abstract void startRead();
    /**
     * 结束持续读取数据
     * */
    public abstract void stopRead();
}
