package com.SensorStreamer.Component.Listen;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.SensorStreamer.Utils.TypeTranDeter;

/**
 * 读取音频数据 并基于回调函数处理
 * @author chen
 * @version 1.0
 * */

public class AudioListen extends Listen {
    /**
     * Audio 回调函数类接口
     * */
    public interface AudioCallback {
        /**
         * 回调函数 用于处理数据
         * @param data 传入音频数据
         * */
        void dealAudioData(byte[] data);
    }

    private AudioCallback callback;
    private AudioRecord audioRecord;
    private int minBufSize;
    private Thread readThread;

    /**
     * 启动组件并设置回调函数 适配器
     * @param samplingRate 采样率
     * @param callback 数据处理回调函数
     * */
    public boolean launch(int samplingRate, AudioCallback callback) {
        String[] params = new String[1];
        params[0] = Integer.toString(samplingRate);
        this.setCallback(callback);
        return this.launch(params);
    }

    /**
     * 设置回调函数
     * @param callback 回调函数
     * */
    public void setCallback(AudioCallback callback) {
        this.callback = callback;
    }

    /**
     * 注册监听音频所需要的成员变量 优先使用重载适配器
     * */
    @Override
    public boolean launch(String[] params) {
        if (audioRecord != null || minBufSize != 0)
            return false;

        if (params.length != 1 || !TypeTranDeter.isStr2Num(params[0]) || Integer.parseInt(params[0]) <= 0)
            return false;

        int samplingRate = Integer.parseInt(params[0]);
        this.minBufSize = AudioRecord.getMinBufferSize(samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufSize * 10);

        return true;
    }

    /**
     * 注销监听音频所需要的成员变量
     * */
    @Override
    public boolean off() {
        if (this.audioRecord == null)
            return false;
        this.audioRecord.stop();
        this.audioRecord.release();
        this.audioRecord = null;

        this.callback = null;
        this.minBufSize = 0;
        return true;
    }

    /**
     * 持续读取音频数据
     * */
    private void readAudio() {
        this.audioRecord.startRecording();
        byte[] buf = new byte[this.minBufSize];
        while (!Thread.currentThread().isInterrupted()) {
            int n_read = this.audioRecord.read(buf, 0, buf.length);
            if (n_read <= 0 || this.callback == null)
                continue;
            this.callback.dealAudioData(buf);
        }
    }

    /**
     * 启动线程 处理音频数据
     * */
    @Override
    public void startRead() {
        if (audioRecord == null || readThread != null)
            return;
        readThread = new Thread(this::readAudio);
        readThread.start();
    }

    /**
     * 结束线程 停止处理音频数据
     * */
    @Override
    public void stopRead() {
        if (readThread == null)
            return;
        readThread.interrupt();
        readThread = null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
