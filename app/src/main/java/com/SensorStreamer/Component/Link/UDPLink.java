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
    private DatagramSocket send_socket, rece_socket;

    public UDPLink () {
        super();

        intNull = 0;
    }

    /**
     * 注册所有可变成员变量，设置目的地址
     * */
    @Override
    public boolean launch(InetAddress address, int port) {
//        0
        if (this.launchFlag)
            return false;

        try {
            this.address = address;
            this.port = port;
//            发送用初始化
            this.send_socket = new DatagramSocket();
//            接收用初始化 固定接收对应地址端口的信息
            this.rece_socket = new DatagramSocket(this.port);
        } catch (SocketException e) {
            Log.d("UDPLink", "launch:SocketException", e);
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

        this.send_socket.close();
        this.send_socket = null;

        this.rece_socket.close();
        this.rece_socket = null;

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
            this.send_socket.send(packet);
        } catch (IOException e) {
            Log.e("UDPLink", "send:IOException", e);
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

        try {
            byte[] buf = new byte[bufSize];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.rece_socket.receive(packet);

            return new String(buf, charset);
        } catch (IOException e) {
            Log.e("UDPLink", "rece:IOException", e);
            return null;
        }
    }
}
