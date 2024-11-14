package com.SensorStreamer.Component.Link;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    public synchronized boolean launch(InetAddress address, int port, int timeout) {
//        0
        if (!this.canLaunch()) return false;

        try {
            this.address = address;
            this.port = port;
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(address, port), timeout);
        } catch (Exception e) {
            Log.d("TCPLink", "launch:Exception", e);
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
    public synchronized boolean off() {
//        1
        if (!this.canOff()) return false;

        try {
            if (this.socket != null && !this.socket.isClosed())
                this.socket.close();
            this.socket = null;

            this.address = null;
            this.port = this.intNull;
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
    public void send(String msg, Charset charset) {
//        1
        if (!this.canSend())
            return;

        try {
            OutputStreamWriter writer = new OutputStreamWriter(this.socket.getOutputStream(), charset);
            PrintWriter out = new PrintWriter(writer, true);
            out.println(msg);
        } catch (Exception e) {
            Log.e("TCPLink", "send:Exception", e);
            this.off();
        }
    }

    /**
     * 接收并将数据存储在 buf
     * */
    @Override
    public String rece(Charset charset, int bufSize) {
//        1
        if (!this.canRece())
            return null;

        try {
            InputStreamReader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(reader);
            return in.readLine();
        } catch (IOException e) {
            Log.e("TCPLink", "rece:Exception", e);
            this.off();
            return null;
        }
    }

    @Override
    protected synchronized void adaptiveBufSize(int packetSize) {

    }
}
