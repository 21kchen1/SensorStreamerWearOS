package com.SensorStreamer.Component.Link;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 带心跳机制的 TCP 的 Link
 * @author chen
 * @version 2.0
 */

public class HTCPLink extends TCPLink {
    //    心跳标记
    protected final static String HEARTBEAT = "heartbeat";
    //    主阻塞队列
    protected final LinkedBlockingQueue<String> receQueue, heartbeatQueue;
    //    信号
    protected int receNum;
    //    socket 收信线程
    protected Thread socketThread;
    //    往返时间，毫米单位
    protected double RTT;
    //    同步锁
    protected final Object socketLock, receNumLock, RTTLock;
    //    心跳任务
    protected ScheduledExecutorService heartbeatService;

    public HTCPLink() {
        super();

        receQueue = new LinkedBlockingQueue<>();
        heartbeatQueue = new LinkedBlockingQueue<>();
        socketLock = new Object();
        receNumLock = new Object();
        RTTLock = new Object();

        this.RTT = Link.INTNULL;
        this.receNum = Link.INTNULL;
    }

    /**
     * 注册所有可变成员变量，设置目的地址
     * */
    @Override
    public synchronized boolean launch(InetAddress address, int port, int timeout, Charset charset) {
//        0
        if (!this.canLaunch())
            return false;

        try {
            this.address = address;
            this.port = port;
            this.charset = charset;
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(address, port), timeout);

            this.socketSend = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream(), this.charset), true);
            this.socketRece = new BufferedReader(new InputStreamReader(socket.getInputStream(), this.charset));
//            允许接收
            this.startSocketRece();
        } catch (Exception e) {
            Log.d("TCPLink", "launch:Exception", e);
            this.launchFlag = true;
            this.off();
            return false;
        }

//        1
        this.launchFlag = true;
        return true;
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

            if (this.socketThread != null)
                this.socketThread.interrupt();
            this.socketThread = null;

            this.RTT = Link.INTNULL;
            this.receNum = Link.INTNULL;

        } catch (Exception e) {
            Log.d("HTCPLink", "off:Exception", e);
            return false;
        }

//        0
        this.launchFlag = false;

        return true;
    }

    /**
     * 重新启动连接
     * @param timeLimit 重启时间限制
     */
    public synchronized boolean reLaunch(int timeLimit) {
//        非启动状态不考虑重启
        if (!this.canRece())
            return false;

        this.off();
        return this.launch(this.address, this.port, timeLimit, this.charset);
    }

    /**
     * 使用 socket 收信，并将信息存入对应队列
     * 若队列已满，则删除最先入队的元素
     */
    protected void startSocketRece() {
        this.socketThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (this.socketLock) {
                        while (true) {
//                            获取数字锁
                            synchronized (this.receNumLock) {
                                Log.i("HTCPLink", "receNum = " + this.receNum);
                                if (this.receNum > Link.INTNULL)
                                    break;
                            }
                            Log.i("HTCPLink", "socketLock = lock on");
                            this.socketLock.wait();
                            Log.i("HTCPLink", "socketLock = lock off");
                        }
                    }
//                    执行任务
                    String msg = this.socketRece.readLine();
                    Log.i("HTCPLink", "socketRece = " + msg);
//                    如果是心跳信号
                    if (HTCPLink.HEARTBEAT.equals(msg)) {
                        if (this.heartbeatQueue.remainingCapacity() == 0)
                            this.heartbeatQueue.take();
                        this.heartbeatQueue.put(msg);
                        continue;
                    }
//                    如果是普通信号
                    if (this.receQueue.remainingCapacity() == 0)
                        this.receQueue.take();
                    this.receQueue.put(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.e("HTCPLink", "startSocketRece:InterruptedException", e);
                    break;
                } catch (Exception e) {
                    Log.e("HTCPLink", "startSocketRece:Exception", e);
                    break;
                }
            }
        });

        this.socketThread.start();
    }

    /**
     * 从心跳队列获取信息
     */
    protected String heartbeatRece(int timeLimit) {
        Log.i("HTCPLink", "Using heartbeatRece");
//        1
        if (!this.canRece())
            return null;

        try {
            String msg;
            synchronized (this.receNumLock) {
                this.receNum++;
            }
            if (!this.heartbeatQueue.isEmpty())
                return this.heartbeatQueue.take();

            synchronized (this.socketLock) {
                this.socketLock.notify();
            }
            if (timeLimit == Link.INTNULL)
                msg = this.heartbeatQueue.take();
            else msg = this.heartbeatQueue.poll(timeLimit, TimeUnit.MILLISECONDS);

            return msg;
        } catch (Exception e) {
            Log.e("HTCPLink", "heartbeatRece:Exception", e);
            return null;
        } finally {
            synchronized (this.receNumLock) {
                if (this.receNum > Link.INTNULL)
                    this.receNum--;
            }
        }
    }

    /**
     * 接收并将数据存储在 buf
     */
    @Override
    public String rece(int bufSize, int timeLimit) {
        Log.i("HTCPLink", "Using rece");
//        1
        if (!this.canRece()) {
            return null;
        }

        try {
            String msg;
            synchronized (this.receNumLock) {
                this.receNum++;
            }
            if (!this.receQueue.isEmpty())
                return this.receQueue.take();

            synchronized (this.socketLock) {
                this.socketLock.notify();
            }
            if (timeLimit == Link.INTNULL)
                msg = this.receQueue.take();
            else msg = this.receQueue.poll(timeLimit, TimeUnit.MILLISECONDS);

            return msg;
        } catch (Exception e) {
            Log.e("HTCPLink", "rece:Exception", e);
            return null;
        } finally {
            synchronized (this.receNumLock) {
                if (this.receNum > Link.INTNULL)
                    this.receNum--;
            }
        }
    }

    /**
     * 心跳
     * @param timeLimit 心跳最长响应时间，毫秒单位
     * @param interTime 心跳间隔，毫秒单位
     */
    public synchronized void startHeartbeat(int timeLimit, int interTime) {
//        1
        if (!this.canRece() || this.heartbeatService != null)
            return;

        this.heartbeatService = Executors.newSingleThreadScheduledExecutor();
        this.heartbeatService.scheduleWithFixedDelay(
                () -> {
//                   发送心跳信息
                    this.send(HTCPLink.HEARTBEAT);
                    long startTime = System.currentTimeMillis();
                    String msg = this.heartbeatRece(timeLimit);
                    long stopTime = System.currentTimeMillis();

//                    心跳超时则尝试重连
                    if (!HTCPLink.HEARTBEAT.equals(msg)) {
                        boolean result = this.reLaunch(timeLimit);
                        if (!result) return;
                        this.startHeartbeat(timeLimit, interTime);
                        Log.e("HTCPLink", "startHeartbeat:reLaunch");
                        return;
                    }
//                    心跳正常
                    synchronized (this.RTTLock) {
                        this.RTT = this.RTT * 0.3 + (stopTime - startTime) * 0.7;
                    }
                    Log.i("HTCPLink", "Heartbeat: RTT = " + this.RTT);

                }, 0, interTime, TimeUnit.MILLISECONDS
        );
    }

    /**
     * 获取 RTT
     * */
    public double getRTT() {
//        1
        if (!this.canRece() || this.heartbeatService == null)
            return Link.INTNULL;

        synchronized (this.RTTLock) {
            return this.RTT;
        }
    }

    /**
     * 中断心跳线程
     */
    public synchronized void stopHeartbeat() {
//        1
        if (!this.canRece())
            return;

//        关闭心跳线程
        if (this.heartbeatService != null)
            this.heartbeatService.shutdown();
        this.heartbeatService = null;
        this.heartbeatQueue.clear();
    }

    @Override
    public boolean canRece() {
        return (this.launchFlag && this.socketThread != null);
    }
}
