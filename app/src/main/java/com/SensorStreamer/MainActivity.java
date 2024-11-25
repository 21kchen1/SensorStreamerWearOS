package com.SensorStreamer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.SensorStreamer.Component.Link.Link;
import com.SensorStreamer.Component.Link.LinkF;
import com.SensorStreamer.Component.Link.RTCPLink;
import com.SensorStreamer.Component.Link.RTCPLinkExpand.HeartBeat;
import com.SensorStreamer.Component.Link.UDPLinkF;
import com.SensorStreamer.Component.Listen.AudioListen;
import com.SensorStreamer.Component.Listen.AudioListenF;
import com.SensorStreamer.Component.Listen.SensorListen;
import com.SensorStreamer.Component.Listen.SensorListenF;
import com.SensorStreamer.Component.Switch.RemoteSwitch;
import com.SensorStreamer.Component.Switch.RemoteSwitchF;
import com.SensorStreamer.Component.Switch.Switch;
import com.SensorStreamer.Component.Switch.SwitchF;
import com.SensorStreamer.Component.Time.ReferenceTimeF;
import com.SensorStreamer.Component.Time.Time;
import com.SensorStreamer.Component.Time.TimeF;
import com.SensorStreamer.Model.Control.AudioControl;
import com.SensorStreamer.Model.Data.AudioData;
import com.SensorStreamer.Model.Control.SensorControl;
import com.SensorStreamer.Model.Data.SensorData;
import com.SensorStreamer.Model.Switch.RemotePDU;
import com.SensorStreamer.Utils.TypeTranDeter;
import com.SensorStreamer.databinding.ActivityMainBinding;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends WearableActivity {

    private static final String LOG_TAG = "MainActivity";
    private TextView ipText;
    private TextView infoText;
    private final int udpPort = 5005;
    private final int tcpPort = 5006;
//    用于向主线程发送信息，可用于更新 UI
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Gson gson = new Gson();
//    服务器连接用
    private Link udpLink;
    private RTCPLink rtcpLink;
//    rtcp 拓展功能
    private HeartBeat rtcpHeartBeat;
//    监视器
    private AudioListen audioListen;
    private SensorListen sensorListen;
//    获取基准时间
    private Time referenceTime;
//    远程控制开关
    private Switch tcpRemoteSwitch;
//    系统锁
    private PowerManager.WakeLock mWakeLock;
    private PowerManager powerManager;
//    通知服务
    private Intent sensorServiceIntent;

//    动态获取服务权限
    private final static int REQUEST_CODE_ANDROID = 1001;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
    };

    /**
     * 检测权限是否已经获取
     * */
    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 软件启动
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        获取ui各个组件
        Button connectButton = findViewById(R.id.Connect);
        Button disconnectButton = findViewById(R.id.Disconnect);
//        绑定处理方法
        connectButton.setOnClickListener(connectCallback);
        disconnectButton.setOnClickListener(disconnectCallback);
        ipText = findViewById(R.id.ip);
        infoText = findViewById(R.id.info);

//        请求权限
        if (!hasPermissions(this, REQUIRED_PERMISSIONS))
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_ANDROID);
//        请求锁
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLockAcquire();
        setAmbientEnabled();

//        创建 Link 类
        LinkF linkF = new UDPLinkF();
        udpLink = linkF.create();
        rtcpLink = new RTCPLink();
//        创建 rtcp 心跳服务，并设置回调函数
        rtcpHeartBeat = new HeartBeat(rtcpLink, heartBeatCallback);
//        创建监听器
        AudioListenF audioListenF = new AudioListenF();
        audioListen = (AudioListen) audioListenF.create(this);
        SensorListenF sensorListenF = new SensorListenF();
        sensorListen = (SensorListen) sensorListenF.create(this);
//        创建远程开关
        SwitchF remoteSwitchF = new RemoteSwitchF();
        tcpRemoteSwitch = remoteSwitchF.create();
        TimeF timeF = new ReferenceTimeF();
        referenceTime = timeF.create();
//        设置服务
        sensorServiceIntent = new Intent(MainActivity.this, SensorService.class);
    }

    /**
     * 软件退出
     * */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainActivity.this.disconnectClick();
        MainActivity.this.wakeLockRelease();
    }

    /**
     * 软件销毁
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivity.this.disconnectClick();
        MainActivity.this.wakeLockRelease();
    }

    /**
     * 音频回调函数
     * */
    private final AudioListen.AudioCallback audioCallback = new AudioListen.AudioCallback () {
        @Override
        public void dealAudioData(byte[] data) {
            AudioData audioData = new AudioData(referenceTime.getTime(), data);
            String json = gson.toJson(audioData);
//            发送数据
            try {
                udpLink.send(json);
            } catch (Exception e) {
                Log.e(LOG_TAG, "dealAudioData:Exception", e);
            }
        }
    };

    /**
     * Sensor 回调函数
     * */
    private final SensorListen.SensorCallback sensorCallback = new SensorListen.SensorCallback() {
        @Override
        public void dealSensorData(String type, float[] data, long sensorTimestamp) {
            SensorData sensorData = new SensorData(referenceTime.getTime(), sensorTimestamp, type, data);
            String json = gson.toJson(sensorData);
//            发送数据
            try {
                udpLink.send(json);
            } catch (Exception e) {
                Log.e(LOG_TAG, "dealSensorData:Exception", e);
            }
        }
    };

    /**
     * 启动传感器
     * */
    public void launchSensor(String[] dataList) {
        AudioControl audioControl = null;
        SensorControl sensorControl = null;

        int[] defaultSensors = new int[] {
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_ROTATION_VECTOR,
                Sensor.TYPE_MAGNETIC_FIELD,
        };
        int audioDefaultSampling = 16000;

        for (String data : dataList) {
            if (TypeTranDeter.canStr2JsonData(data, AudioControl.class) && AudioControl.TYPE.equals(gson.fromJson(data, AudioControl.class).type))
                audioControl = gson.fromJson(data, AudioControl.class);
            if (TypeTranDeter.canStr2JsonData(data, SensorControl.class) && SensorControl.TYPE.equals(gson.fromJson(data, SensorControl.class).type))
                sensorControl = gson.fromJson(data, SensorControl.class);
        }

//            启动相关组件
        if (sensorControl == null || sensorControl.sensors == null)
            sensorListen.launch(defaultSensors,0 ,this.sensorCallback);
        else
            sensorListen.launch(sensorControl.sensors, sensorControl.sampling,this.sensorCallback);

        if (audioControl == null)
            audioListen.launch(audioDefaultSampling, this.audioCallback);
        else
            audioListen.launch(audioControl.sampling, this.audioCallback);

//            开始读取数据
        sensorListen.startRead();
        audioListen.startRead();
    }

    /**
     * 关闭传感器
     * */
    public void offSensor() {
        sensorListen.stopRead();
        audioListen.stopRead();
        sensorListen.off();
        audioListen.off();
    }

    /**
     * 远程开关回调函数
     * 负责开启数据的传输
     * */
    private final RemoteSwitch.RemoteCallback remoteCallback = new RemoteSwitch.RemoteCallback() {
        @Override
        public void switchOn(RemotePDU remotePDU) {
            MainActivity.this.referenceTime.setBase(remotePDU.time, System.currentTimeMillis(), (long) rtcpHeartBeat.getRTT());
            MainActivity.this.launchSensor(remotePDU.data);
            updateInfoText(R.string.text_info_streaming);
        }

        @Override
        public void switchOff(RemotePDU remotePDU) {
            MainActivity.this.offSensor();
            updateInfoText(R.string.text_info_connected);
        }
    };

    /**
     * 利用 handle 更新 ui
     * */
    private void updateInfoText(int RString) {
        MainActivity.this.mainHandler.post(() -> infoText.setText(RString));
    }

    /**
     * 点击 connect 后执行
     * */
    private void connectClick() {
//        启动通知
        new Thread(() -> {
            try {
//                获取目标 IP
                InetAddress aimIP = InetAddress.getByName(ipText.getText().toString());
                udpLink.launch(aimIP, udpPort, 0, StandardCharsets.UTF_8);
                rtcpLink.launch(aimIP, tcpPort, 100, StandardCharsets.UTF_8);
//                添加服务
                rtcpLink.addReuseName(RemoteSwitch.LOG_TAG);
                tcpRemoteSwitch.launch(rtcpLink, MainActivity.this.remoteCallback);
//                添加服务
                rtcpLink.addReuseName(HeartBeat.LOG_TAG);
                rtcpHeartBeat.startHeartbeat(2000, 3, 2000);
//                启动远程开关
                tcpRemoteSwitch.startListen(1024);
                startForegroundService(MainActivity.this.sensorServiceIntent.setAction(SensorService.ACTION_START_FORE));
            } catch (Exception e) {
                Log.e(LOG_TAG, "connectClick:Exception", e);
                updateInfoText(R.string.text_info_fail);
                disconnectClick();
            }
        }).start();
    }

    /**
     * 点击 disconnect 或 connect 异常时执行
     * */
    private void disconnectClick() {
        MainActivity.this.offSensor();
        tcpRemoteSwitch.stopListen();
        tcpRemoteSwitch.off();

        rtcpHeartBeat.stopHeartbeat();
//        关闭连接，允许更新地址
        if(!udpLink.off() || !rtcpLink.off())
            Log.e("MainActivity", "disconnectClick:Link off error");
        startService(MainActivity.this.sensorServiceIntent.setAction(SensorService.ACTION_STOP_FORE));
    }

    /**
     * 心跳异常回调函数
     * */
    private final HeartBeat.HeartBeatCallback heartBeatCallback = () -> {
        disconnectClick();
//        Connect Break
        updateInfoText(R.string.text_info_break);
    };

    /**
     * 点击 connect 时的回调函数
     * */
    private final View.OnClickListener connectCallback = (arg0) -> {
        connectClick();
//        Connected
        updateInfoText(R.string.text_info_connected);
    };

    /**
     * 点击 disconnect 时的回调函数
     * */
    private final View.OnClickListener disconnectCallback = (arg0) -> {
        disconnectClick();
//        Be Ready
        updateInfoText(R.string.text_info_default);
    };

    /**
     * 获取唤醒锁，防止软件被杀死
     * */
    private void wakeLockAcquire(){
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mWakeLock != null){
                    Log.e(LOG_TAG, "wakeLockAcquire:WakeLock already acquired!");
                    return;
                }
                mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sensors_data_logger:wakelock");
                mWakeLock.acquire(600*60*1000L /*10 hours*/);
                Log.i(LOG_TAG, "wakeLockAcquire:WakeLock acquired!");
            }
        };
        timer.schedule(task, 2000);
    }

    /**
     * 解除唤醒锁
     * */
    private void wakeLockRelease(){
        if (mWakeLock != null && mWakeLock.isHeld()){
            mWakeLock.release();
            Log.i(LOG_TAG, "wakeLockRelease:WakeLock released!");
            mWakeLock = null;
        }
        else{
            Log.e(LOG_TAG, "wakeLockRelease:No wakeLock acquired!");
        }
    }

    public void testSamplingRates(){
        for (int rate : new int[] {125, 250, 500, 1000, 2000, 4000, 8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                Log.d(LOG_TAG, "testSamplingRates: " + rate + " Hz is allowed!, buff size " + bufferSize);
            }
        }
    }

}
