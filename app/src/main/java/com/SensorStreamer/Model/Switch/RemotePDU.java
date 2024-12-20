package com.SensorStreamer.Model.Switch;

import com.SensorStreamer.Resource.String.SwitchString;

/**
 * 通信数据单元与变量字典
 * @author chen
 * @version 1.0
 * */


public class RemotePDU {
    /**
     * 控制报文：
     * time 发送方时间戳，
     * control 控制信息，
     * data 子控制信息（如控制要使用的传感器类型）
     * */
    public final static String TYPE_CONTROL = SwitchString.TYPE_CONTROL;
    /**
     * 同步报文：
     * time 发送方时间戳，
     * control null，
     * data null
     * */
    public final static String TYPE_SYN = SwitchString.TYPE_SYN;
    public final static String TYPE_MSG = SwitchString.TYPE_MSG;

    public final static String CONTROL_SWITCHON = SwitchString.CONTROL_SWITCH_ON;
    public final static String CONTROL_SWITCHOFF = SwitchString.CONTROL_SWITCH_OFF;

//    信息类型
    public String type;
//    时间
    public long time;
//    命令
    public String control;
//    数据
    public String[] data;

    public RemotePDU(String type, long time, String control, String[] data) {
        this.type = type;
        this.time = time;
        this.control = control;
        this.data = data;
    }
}
