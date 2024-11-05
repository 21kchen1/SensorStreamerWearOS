package com.SensorStreamer.Link;

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

public class UDPLink extends Link {
    private DatagramSocket socket;

    UDPLink () {
    }

    /**
     * 启动 socket
     * @return 是否成功
     * */
    @Override
    public boolean star() {
        if (socket != null && !socket.isClosed()) {
            return false;
        }

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            Log.d("UDPLinker", "SocketException", e);
            return false;
        }
        return true;
    }

    /**
     * 关闭 socket
     * @return 是否成功
     * */
    @Override
    public boolean off() {
        if (socket == null || socket.isClosed()) {
            return false;
        }
        socket.close();
        return true;
    }

    /**
     * 基于 UDP 发送数据
     * @param address 目的地址
     * @param port 目的端口
     * @param buf 二进制缓存
     * */
    @Override
    public void send(InetAddress address, int port, byte[] buf) {
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            Log.e("UDPLinker.send", "IOException", e);
        }
    }

    /**
     * 基于 UDP 接收数据
     * @param address 目的地址
     * @param port 目的端口
     * @param buf 二进制缓存
     * */
    @Override
    public void rece(InetAddress address, int port, byte[] buf) {
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            socket.receive(packet);
        } catch (IOException e) {
            Log.e("UDPLinker.rece", "IOException", e);
        }
    }
}
