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
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 基于 TCP 的 Link
 * @author chen
 * @version 1.0
 * */

public class TCPLink extends Link {
    private final int intNull;
    //    主阻塞队列
    protected final LinkedBlockingQueue<String> receQueue, heartbeatQueue;
    //    发送和接收数据的 socket
    private Socket socket;
    //    往返时间
    protected long RTT;

    public TCPLink () {
        super();

        receQueue = new LinkedBlockingQueue<>();
        heartbeatQueue = new LinkedBlockingQueue<>();
        this.intNull = 0;

        this.RTT = 0;
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
     * */
    @Override
    public synchronized boolean off() {
//        1
        if (!this.canOff())
            return false;

        try {
            this.stopHeartbeat();
//            清空队列信息
            this.heartbeatQueue.clear();
            this.receQueue.clear();

            if (this.socket != null && !this.socket.isClosed())
                this.socket.close();
            this.socket = null;

            this.address = null;
            this.port = this.intNull;
            this.charset = null;
        } catch (Exception e) {
            Log.d("TCPLink", "off:Exception", e);
            return false;
        }

//        0
        this.launchFlag = false;
        return true;
    }

    /**
     * 发送 buf 数据
     * */
    @Override
    public void send(String msg) {
//        1
        if (!this.canSend())
            return;

        try {
            OutputStreamWriter writer = new OutputStreamWriter(this.socket.getOutputStream(), this.charset);
            PrintWriter out = new PrintWriter(writer, true);
            out.println(msg);
        } catch (Exception e) {
            Log.e("TCPLink", "send:Exception", e);
            this.off();
        }
    }

    /**
     * 使用 socket 收信，并将信息存入对应队列
     * */
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
     * */
    protected String heartbeatRece() {
//        1
        if (!canRece())
            return null;

        this.socketRece();
        return this.heartbeatQueue.poll();
    }

    /**
     * 心跳
     * @param timeLimit 心跳最长间隔
     * */
    public void startHeartbeat(long timeLimit) {

    }

    /**
     * 中断心跳线程
     * */
    public void stopHeartbeat() {

    }

    /**
     * 接收并将数据存储在 buf
     * */
    public String rece(int bufSize) {
//        1
        if (!this.canRece())
            return null;

        this.socketRece();
        return this.receQueue.poll();
    }

    @Override
    protected synchronized void adaptiveBufSize(int packetSize) {

    }
}
