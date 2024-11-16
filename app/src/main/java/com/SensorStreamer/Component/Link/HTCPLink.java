package com.SensorStreamer.Component.Link;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 带心跳机制的 TCP 的 Link
 * @author chen
 * @version 1.0
 */
public class HTCPLink extends TCPLink {

    //    主阻塞队列
    protected final LinkedBlockingQueue<String> receQueue, heartbeatQueue;
    //    心跳任务
    private ScheduledExecutorService heartbeatService;
    //    发送和接收数据的 socket
    private Socket socket;
    //    往返时间，毫米单位
    protected long RTT;

    public HTCPLink() {
        super();

        receQueue = new LinkedBlockingQueue<>();
        heartbeatQueue = new LinkedBlockingQueue<>();

        this.RTT = 0;
    }

    /**
     * 注销所有可变成员变量
     */
    @Override
    public synchronized boolean off() {
//        1
        if (!this.canOff())
            return false;

        try {
            this.stopHeartbeat();
//            清空队列信息
            this.receQueue.clear();

            if (this.socket != null && !this.socket.isClosed())
                this.socket.close();
            this.socket = null;

        } catch (Exception e) {
            Log.d("TCPLink", "off:Exception", e);
            return false;
        }

//        0
        this.launchFlag = false;
        return true;
    }

    /**
     * 重新启动连接
     *
     * @param timeLimit 重启时间限制
     */
    private synchronized boolean reLaunch(int timeLimit) {
//        非启动状态不考虑重启
        if (!this.canRece())
            return false;

        this.off();
        return this.launch(this.address, this.port, timeLimit, this.charset);
    }

    /**
     * 使用 socket 收信，并将信息存入对应队列
     */
    protected void socketRece() {
//        1
        if (!canRece())
            return;

        Thread rece = new Thread(() -> {
            try {
                InputStreamReader reader = new InputStreamReader(socket.getInputStream(), this.charset);
                BufferedReader in = new BufferedReader(reader);
                String msg = in.readLine();
//        如果是心跳信号
                if (msg.equals(Link.HEARTBEAT)) {
                    this.heartbeatQueue.put(msg);
                    return;
                }
//        如果是普通信号
                this.receQueue.put(msg);
            } catch (Exception e) {
                Log.e("TCPLink", "socketRece:Exception", e);
                this.off();
            }
        });
        rece.start();
    }

    /**
     * 从心跳队列获取信息
     */
    protected synchronized String heartbeatRece(int timeLimit) {
//        1
        if (!this.canRece())
            return null;

        try {
//            只希望接收到实时的信息
            this.heartbeatQueue.clear();
            this.socketRece();
            if (timeLimit == Link.INTNULL)
                return this.heartbeatQueue.take();
            return this.heartbeatQueue.poll(timeLimit, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e("TCPLink", "rece:InterruptedException", e);
            return null;
        }
    }

    /**
     * 接收并将数据存储在 buf
     */
    @Override
    public synchronized String rece(int bufSize, int timeLimit) {
//        1
        if (!this.canRece())
            return null;

        try {
//            只希望接收到实时的信息
            this.receQueue.clear();
            this.socketRece();
            if (timeLimit == Link.INTNULL)
                return this.receQueue.take();
            return this.receQueue.poll(timeLimit, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e("TCPLink", "rece:InterruptedException", e);
            return null;
        }
    }

    /**
     * 心跳
     *
     * @param timeLimit 心跳最长响应时间，毫秒单位
     * @param interTime 心跳间隔，毫秒单位
     */
    public void startHeartbeat(int timeLimit, int interTime) {
//        1
        if (!this.canRece() || this.heartbeatService != null)
            return;

        this.heartbeatService = Executors.newSingleThreadScheduledExecutor();
        this.heartbeatService.scheduleWithFixedDelay(
                () -> {
//                   发送心跳信息
                    this.send(HEARTBEAT);
                    long startTime = System.currentTimeMillis();
                    String msg = this.heartbeatRece(timeLimit);
                    long stopTime = System.currentTimeMillis();

//                    心跳超时则尝试重连
                    if (!HEARTBEAT.equals(msg)) {
                        boolean result = this.reLaunch(timeLimit);
                        if (result)
                            this.startHeartbeat(timeLimit, interTime);
                        return;
                    }
//                心跳正常
                    this.RTT = stopTime - startTime;
                }, 0, interTime, TimeUnit.MILLISECONDS
        );
    }

    /**
     * 中断心跳线程
     */
    public void stopHeartbeat() {
//        1
        if (!this.canRece())
            return;

//        关闭心跳线程
        if (this.heartbeatService != null)
            this.heartbeatService.shutdown();
        this.heartbeatService = null;
        this.heartbeatQueue.clear();
    }

}
