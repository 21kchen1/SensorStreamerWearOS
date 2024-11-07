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

public class UDPLink extends Link {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    UDPLink () {
    }

    @Override
    public boolean launch(InetAddress address, int port) {
        if (this.socket != null && !this.socket.isClosed()) {
            return false;
        }

        try {
            this.address = address;
            this.port = port;
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            Log.d("UDPLinker", "SocketException", e);
            this.off();
            return false;
        }
        return true;
    }

    @Override
    public boolean off() {
        if (this.socket == null || this.socket.isClosed()) {
            return false;
        }
        this.socket.close();
        return true;
    }

    @Override
    public void send(byte[] buf) {
        if (this.socket == null)
            return;

        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            this.socket.send(packet);
        } catch (IOException e) {
            Log.e("UDPLinker.send", "IOException", e);
        }
    }

    @Override
    public void rece(byte[] buf) {
        if (this.socket == null)
            return;

        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            this.socket.receive(packet);
        } catch (IOException e) {
            Log.e("UDPLinker.rece", "IOException", e);
        }
    }
}
