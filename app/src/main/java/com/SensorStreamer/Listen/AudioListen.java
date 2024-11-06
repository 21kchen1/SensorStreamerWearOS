package com.SensorStreamer.Listen;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * 读取音频数据 并基于回调函数处理
 * @author chen
 * @version 1.0
 * */

public class AudioListen extends Listen {
    private AudioRecord audioRecord;
    private int minBufSize;
    private Thread readThread;

    /**
     * 启动组件 适配器
     * @param samplingRate 采样率
     * @param callback 数据处理回调函数
     * */
    public void launch(int samplingRate, AudioCallback callback) {
        int[] params = new int[1];
        params[0] = samplingRate;
        this.launch(params, callback);
    }

    /**
     * 注册监听音频所需要的成员变量
     * */
    @Override
    public void launch(int[] params, AudioCallback callback) {
        if (params.length != 1 || params[0] <= 0)
            return;
        int samplingRate = params[0];
        this.minBufSize = AudioRecord.getMinBufferSize(samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufSize * 10);

        this.callback = callback;
    }

    /**
     * 注销监听音频所需要的成员变量
     * */
    @Override
    public void off() {
        if (this.audioRecord == null)
            return;
        this.audioRecord.stop();
        this.audioRecord.release();
        this.audioRecord = null;

        if (this.callback == null)
            return;
        this.callback = null;
        this.minBufSize = 0;
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
