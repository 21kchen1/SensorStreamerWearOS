package com.SensorStreamer.Component.Link;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * 基于 UDP 的 Link
 * @author chen
 * @version 1.0
 * */

/**
 * @// TODO: 2024/11/8 rece_socket 开发，实现接收数据1
 * */
public class UDPLink extends Link {
//    发送和接收数据的 socket
    private DatagramSocket send_socket, rece_socket;
    private InetAddress address;
    private int port;

    UDPLink () {
        super();
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
            this.send_socket = new DatagramSocket();
        } catch (SocketException e) {
            Log.d("UDPLinker", "SocketException", e);
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

//        0
        this.launchFlag = false;
        return true;
    }

    /**
     * 发送 buf 数据
     * */
    @Override
    public void send(byte[] buf) {
//        1
        if (!this.launchFlag)
            return;

        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
            this.send_socket.send(packet);
        } catch (IOException e) {
            Log.e("UDPLinker.send", "IOException", e);
        }
    }

    /**
     * 接收并将数据存储在 buf
     * */
    @Override
    public void rece(byte[] buf) {
//        1
        if (!this.launchFlag)
            return;

        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.send_socket.receive(packet);
        } catch (IOException e) {
            Log.e("UDPLinker.rece", "IOException", e);
        }
    }
}
