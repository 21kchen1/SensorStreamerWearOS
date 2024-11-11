package com.SensorStreamer.Component.Link;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 基于 TCP 的 Link
 * @author chen
 * @version 1.0
 * */

public class TCPLink extends Link {
    private final int intNull;
    //    发送和接收数据的 socket
    private Socket socket;

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
            this.socket = new Socket(this.address, this.port);
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
            this.socket.close();
            this.socket = null;

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

        try {
            OutputStreamWriter writer = new OutputStreamWriter(this.socket.getOutputStream(), charset);
            PrintWriter out = new PrintWriter(writer, true);
            out.println(msg);
        } catch (IOException e) {
            Log.e("TCPLink", "send:IOException", e);
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
            InputStreamReader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(reader);
            return in.readLine();
        } catch (IOException e) {
            Log.e("TCPLink", "rece:IOException", e);
            return null;
        }
    }

    /**
     * 自适应缓冲大小
     * */
    @Override
    protected synchronized void adaptiveBufSize(int packetSize) {

    }
}
