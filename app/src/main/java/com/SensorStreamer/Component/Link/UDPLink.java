package com.SensorStreamer.Component.Link;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;

/**
 * 基于 UDP 的 Link
 * @author chen
 * @version 1.1
 * */

public class UDPLink extends Link {
//    发送和接收数据的 socket
    protected DatagramSocket sendSocket, receSocket;

    public UDPLink () {
        super();
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
//            发送用初始化
            this.sendSocket = new DatagramSocket();
//            接收用初始化 固定接收对应地址端口的信息
            this.receSocket = new DatagramSocket(this.port);
        } catch (Exception e) {
            Log.d("UDPLink", "launch:Exception", e);
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
            if (this.sendSocket != null && !this.sendSocket.isClosed())
                this.sendSocket.close();
            this.sendSocket = null;

            if (this.receSocket != null && !this.receSocket.isClosed())
                this.receSocket.close();
            this.receSocket = null;

            this.address = null;
            this.port = Link.INTNULL;
            this.charset = null;
        } catch (Exception e) {
            Log.d("UDPLink", "off:Exception", e);
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
            byte[] buf = msg.getBytes(this.charset);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
            this.sendSocket.send(packet);
        } catch (Exception e) {
            Log.e("UDPLink", "send:Exception", e);
            this.off();
        }
    }

    /**
     * 接收并将数据存储在 buf
     * */
    @Override
    public String rece(int bufSize, int timeLimit) {
//        1
        if (!this.canRece())
            return null;

        if (bufSize < this.minBufSize)
            bufSize = this.bufSize;

        try {
            byte[] buf = new byte[bufSize];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

//            设置时间限制
            if (timeLimit != Link.INTNULL)
                this.receSocket.setSoTimeout(timeLimit);
            this.receSocket.receive(packet);
//            开始自适应
            synchronized (this) {
                this.adaptiveBufSize(packet.getLength());
            }

            return new String(packet.getData(), packet.getOffset(), packet.getLength(), this.charset);
        } catch (Exception e) {
            Log.e("UDPLink", "rece:Exception", e);
            this.off();
            return null;
        }
    }

    /**
     * 自适应缓冲大小
     * */
    @Override
    protected synchronized void adaptiveBufSize(int packetSize) {
        if (packetSize > this.bufSize) {
            this.bufSize = Math.min(this.maxBufSize, (this.bufSize << 1));
            return;
        }
        if (packetSize < (this.bufSize >> 2))
            this.bufSize = Math.max(this.minBufSize, (this.bufSize >> 1));
    }
}
