package com.SensorStreamer.Component.Link;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * 基于 TCP 的 Link
 * @author chen
 * @version 1.0
 * */

public class TCPLink extends Link {
    private final int intNull;
    //    发送和接收数据的 socket
    private Socket send_socket, rece_socket;

    public TCPLink () {
        super();

        this.intNull = 0;
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
            this.send_socket = new Socket(this.address, this.port);

            this.rece_socket = new Socket(this.address, this.port);
        } catch (IOException e) {
            Log.d("TCPLink", "launch:IOException", e);
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

        try {
            this.send_socket.close();
            this.send_socket = null;

            this.rece_socket.close();
            this.rece_socket = null;

            this.address = null;
            this.port = this.intNull;
        } catch (IOException e) {
            Log.d("TCPLink", "off:IOException", e);
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
    public void send(String msg, Charset charset) {
//        1
        if (!this.launchFlag)
            return;
    }

    /**
     * 接收并将数据存储在 buf
     * */
    @Override
    public String rece(Charset charset, int bufSize) {
//        1
        if (!this.launchFlag)
            return null;
        return "";
    }
}
