package com.SensorStreamer.Model;

/**
 * 通信数据单元与变量字典
 * @author chen
 * @version 1.0
 * */

public class RemotePDU {
    public final static String TYPE_CONTROL = "type_control";
    public final static String TYPE_SYN = "type_syn";
    public final static String TYPE_MSG = "type_msg";


    public final static String DATA_CONTROL_SWITCHON = "data_control_switchOn";
    public final static String DATA_CONTROL_SWITCHOFF = "data_control_switchOff";


    public String type;
    public String time;
    public String[] data;

    public RemotePDU(String type, String time, String[] data) {
        this.type = type;
        this.time = time;
        this.data = data;
    }
}
