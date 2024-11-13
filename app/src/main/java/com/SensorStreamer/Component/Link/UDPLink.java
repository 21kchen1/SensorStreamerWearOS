package com.SensorStreamer.Component.Link;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

/**
 * 基于 UDP 的 Link
 * @author chen
 * @version 1.1
 * */

public class UDPLink extends Link {
    private final int intNull;
//    发送和接收数据的 socket
    private DatagramSocket sendSocket, receSocket;

    public UDPLink () {
        super();

        intNull = 0;
    }

    /**
     * 注册所有可变成员变量，设置目的地址
     * */
    @Override
    public boolean launch(InetAddress address, int port, int timeout) {
//        0
        if (this.launchFlag)
            return false;

        try {
            this.address = address;
            this.port = port;
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
        return this.launchFlag = true;
    }

    /**
     * 注销所有可变成员变量
     * */
    @Override
    public boolean off() {
//        1
        if (!this.launchFlag)
            return false;

        if (this.sendSocket != null && !this.sendSocket.isClosed())
            this.sendSocket.close();
        this.sendSocket = null;

        if (this.receSocket != null && !this.receSocket.isClosed())
            this.receSocket.close();
        this.receSocket = null;

        this.address = null;
        this.port = this.intNull;

//        0
        this.launchFlag = false;
        return true;
    }

    /**
     * 发送 buf 数据
     * */
    @Override
    public void send(String msg, Charset charset) {
//        1
        if (!this.launchFlag)
            return;

        try {
            byte[] buf = msg.getBytes(charset);
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
    public String rece(Charset charset, int bufSize) {
//        1
        if (!this.launchFlag)
            return null;

        if (bufSize <= this.intNull)
            bufSize = this.bufSize;

        try {
            byte[] buf = new byte[bufSize];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.receSocket.receive(packet);

//            开始自适应
            synchronized (this) {
                this.adaptiveBufSize(packet.getLength());
            }

            return new String(packet.getData(), packet.getOffset(), packet.getLength(), charset);
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
