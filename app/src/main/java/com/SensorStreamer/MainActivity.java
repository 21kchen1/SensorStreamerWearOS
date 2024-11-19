package com.SensorStreamer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.SensorStreamer.Component.Link.HTCPLink;
import com.SensorStreamer.Component.Link.Link;
import com.SensorStreamer.Component.Link.LinkF;
import com.SensorStreamer.Component.Link.TCPLinkF;
import com.SensorStreamer.Component.Link.UDPLinkF;
import com.SensorStreamer.Component.Listen.AudioListen;
import com.SensorStreamer.Component.Listen.AudioListenF;
import com.SensorStreamer.Component.Listen.IMUListen;
import com.SensorStreamer.Component.Listen.IMUListenF;
import com.SensorStreamer.Component.Switch.RemoteSwitch;
import com.SensorStreamer.Component.Switch.RemoteSwitchF;
import com.SensorStreamer.Component.Switch.Switch;
import com.SensorStreamer.Component.Switch.SwitchF;
import com.SensorStreamer.Model.AudioData;
import com.SensorStreamer.Model.IMUData;
import com.SensorStreamer.Model.RemotePDU;
import com.SensorStreamer.databinding.ActivityMainBinding;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @// TODO: 2024/11/15 后续将ui的text改成表示当前系统状态的说明
 * */
public class MainActivity extends WearableActivity {

    private static final String LOG_TAG = "log tag";

    private TextView ipAddr;
//    留着后续更新 ui
    private TextView unixTimeText;
    private AtomicBoolean mIsRecording = new AtomicBoolean(false);
    private final int udpPort = 5005;
    private final int tcpPort = 5006;
    private final Gson gson = new Gson();

    // audio stuff
    int audioSamplingRate = 16000;

//    服务器连接用
    private Link udpLink;
    private Link tcpLink;

    private HTCPLink htcpLink;

//    远程控制开关
    private Switch tcpRemoteSwitch;

    private AudioListen audioListen;
    private IMUListen imuListen;

    private PowerManager.WakeLock mWakeLock;
    private PowerManager powerManager;

    private final static int REQUEST_CODE_ANDROID = 1001;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
    };

    private ScheduledExecutorService refreshUIService;

//    创建通知服务
    private Intent intent;

    private static boolean hasPermissions(Context context, String... permissions) {
        // check Android hardware permissions
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // nullify back button when recording starts
        if (!mIsRecording.get()) {
            super.onBackPressed();
            MainActivity.this.offSensor();
            wakeLockRelease();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button startButton = findViewById(R.id.StartStreaming);
        Button stopButton = findViewById(R.id.StopStreaming);

        ipAddr = findViewById(R.id.ipAddr);

        unixTimeText = findViewById(R.id.unixTime);

        startButton.setOnClickListener(startListener);
        stopButton.setOnClickListener(stopListener);

        // get required permissions
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_ANDROID);
        }

        // acquire wakelock
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLockAcquire();
        setAmbientEnabled();

        //    创建 Link 类
        LinkF udpLinkF = new UDPLinkF();
        udpLink = udpLinkF.create();
        LinkF tcpLinkF = new TCPLinkF();
        tcpLink = tcpLinkF.create();

//        创建监听器
        AudioListenF audioListenF = new AudioListenF();
        audioListen = (AudioListen) audioListenF.create(this);
        IMUListenF imuListenF = new IMUListenF();
        imuListen = (IMUListen) imuListenF.create(this);

//        创建开关
        SwitchF remoteSwitchF = new RemoteSwitchF();
        tcpRemoteSwitch = remoteSwitchF.create();
//        心跳 TCP
        htcpLink = new HTCPLink();

        intent = new Intent(MainActivity.this, SensorService.class);
    }

    /**
     * 音频回调函数
     * */
    private final AudioListen.AudioCallback audioCallback = new AudioListen.AudioCallback () {
        @Override
        public void dealAudioData(byte[] data) {
            AudioData audioData = new AudioData(System.currentTimeMillis(), data);
            String json = gson.toJson(audioData);
//            发送数据
            udpLink.send(json);
        }
    };

    /**
     * IMU 回调函数
     * */
    private final IMUListen.IMUCallback imuCallback = new IMUListen.IMUCallback() {
        @Override
        public void dealIMUData(String type, float[] data, long sensorTimestamp) {
            IMUData imuData = new IMUData(System.currentTimeMillis(), sensorTimestamp, type, data);
            String json = gson.toJson(imuData);
//            发送数据
            udpLink.send(json);
        }
    };

    /**
     * 更新ui
     * */
    private void refreshUI() {
//        Stuff that updates the UI
        runOnUiThread(() -> {
            // 更新 UI 的代码
            unixTimeText.setText(String.format(Locale.CHINA, "%d", System.currentTimeMillis()));
        });
    }

    /**
     * 启动传感器
     * */
    public void launchSensor() {
//                这里后面变成远程设置
        int[] sensors = new int[] {
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_ROTATION_VECTOR,
                Sensor.TYPE_MAGNETIC_FIELD
        };
//            启动相关组件
        imuListen.launch(sensors,0 ,this.imuCallback);
        audioListen.launch(audioSamplingRate, this.audioCallback);
//            开始读取数据
        imuListen.startRead();
        audioListen.startRead();
    }

    /**
     * 关闭传感器
     * */
    public void offSensor() {
        imuListen.stopRead();
        audioListen.stopRead();
        imuListen.off();
        audioListen.off();
    }

    public void test() {
        Thread testTread = new Thread(() -> {
            tcpLink.send("114514");
            String msg = tcpLink.rece(1024, 5000);
            RemotePDU a = gson.fromJson(msg, RemotePDU.class);
            System.out.println(Arrays.toString(a.data));
        });
        testTread.start();
    }

    /**
     * 远程开关回调函数
     * 负责开启数据的传输
     * */
    private final RemoteSwitch.RemoteCallback remoteCallback = new RemoteSwitch.RemoteCallback() {
        @Override
        public void switchOn() {
            MainActivity.this.launchSensor();
//            ui更新
            if (refreshUIService != null && !refreshUIService.isShutdown())
                return;
            refreshUIService = Executors.newSingleThreadScheduledExecutor();
            refreshUIService.scheduleWithFixedDelay(MainActivity.this::refreshUI, 0, 500, TimeUnit.MILLISECONDS);
        }

        @Override
        public void switchOff() {
            MainActivity.this.offSensor();
            if (refreshUIService == null)
                return;
            refreshUIService.shutdown();
        }
    };

    private final View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            new Thread(() -> {
                try {
                    String SERVER = ipAddr.getText().toString();
//                    if (!tcpLink.launch(InetAddress.getByName(SERVER), tcpPort, 2000, StandardCharsets.UTF_8))
//                        return;
                    if (!htcpLink.launch(InetAddress.getByName(SERVER), tcpPort, 100, StandardCharsets.UTF_8))
                        return;
                    htcpLink.startHeartbeat(2000, 2000);
                    if (!udpLink.launch(InetAddress.getByName(SERVER), udpPort, 0, StandardCharsets.UTF_8))
                        return;

//                    启动远程开关
//                    tcpRemoteSwitch.launch(tcpLink, MainActivity.this.remoteCallback);
                    tcpRemoteSwitch.launch(htcpLink, MainActivity.this.remoteCallback);
                    tcpRemoteSwitch.startListen(1024);


//                    startService(MainActivity.this.intent);
                } catch (UnknownHostException e) {
                    Log.e("VS", "UnknownHostException", e);
                }
            }).start();
        }
    };

    private final View.OnClickListener stopListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {

            MainActivity.this.offSensor();

            tcpRemoteSwitch.stopListen();
            tcpRemoteSwitch.off();

            htcpLink.off();

//            关闭连接，允许更新地址
            if(!udpLink.off() || !tcpLink.off())
                Log.e("MainActivity", "Link off error");
            if (refreshUIService == null)
                return;
            refreshUIService.shutdown();

            stopService(MainActivity.this.intent);
        }
    };

    private void wakeLockAcquire(){

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mWakeLock != null){
                    Log.e(LOG_TAG, "WakeLock already acquired!");
                    return;
                }
                mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sensors_data_logger:wakelock");
                mWakeLock.acquire(600*60*1000L /*10 hours*/);
                Log.e(LOG_TAG, "WakeLock acquired!");
            }
        };
        timer.schedule(task, 2000);
    }

    private void wakeLockRelease(){
        if (mWakeLock != null && mWakeLock.isHeld()){
            mWakeLock.release();
            Log.e(LOG_TAG, "WakeLock released!");
            mWakeLock = null;
        }
        else{
            Log.e(LOG_TAG, "No wakeLock acquired!");
        }
    }

    public void getSamplingRates(){
        for (int rate : new int[] {125, 250, 500, 1000, 2000, 4000, 8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                Log.d("Sampling Rates", rate + " Hz is allowed!, buff size " + bufferSize);

            }
        }
    }
}
