package com.SensorStreamer;

import android.Manifest;
import android.content.Context;
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

import com.SensorStreamer.Component.Link.Link;
import com.SensorStreamer.Component.Link.LinkF;
import com.SensorStreamer.Component.Link.TCPLinkF;
import com.SensorStreamer.Component.Link.UDPLinkF;
import com.SensorStreamer.Component.Listen.AudioListen;
import com.SensorStreamer.Component.Listen.AudioListenF;
import com.SensorStreamer.Component.Listen.IMUListen;
import com.SensorStreamer.Component.Listen.IMUListenF;
import com.SensorStreamer.Model.AudioData;
import com.SensorStreamer.Model.IMUData;
import com.SensorStreamer.databinding.ActivityMainBinding;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @// TODO: 2024/11/13 需要注意tcp连接的额外线程
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

    private Link udpLink;
    private Link tcpLink;

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
            udpLink.send(json, StandardCharsets.UTF_8);
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
            udpLink.send(json, StandardCharsets.UTF_8);
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
        udpLink.off();
    }

    public void test() {
        Thread testTread = new Thread(() -> {
            String msg = "Hello!";
            tcpLink.send(msg, StandardCharsets.UTF_8);
            msg = tcpLink.rece(StandardCharsets.UTF_8, 0);
            System.out.println(msg);
        });
        testTread.start();
    }

    private final View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            new Thread(() -> {
                try {
                    String SERVER = ipAddr.getText().toString();
                    udpLink.launch(InetAddress.getByName(SERVER), udpPort, 0);
                    tcpLink.launch(InetAddress.getByName(SERVER), tcpPort, 2000);
    //
                    MainActivity.this.launchSensor();
                    MainActivity.this.test();

    //                ui 更新
                    if (refreshUIService != null && !refreshUIService.isShutdown())
                        return;
                    refreshUIService = Executors.newSingleThreadScheduledExecutor();
                    refreshUIService.scheduleWithFixedDelay(MainActivity.this::refreshUI, 0, 500, TimeUnit.MILLISECONDS);
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

            udpLink.off();
            tcpLink.off();
            if (refreshUIService == null)
                return;
            refreshUIService.shutdown();
            Log.d("VS","Recorder released");
        }
    };

    private void wakeLockAcquire(){
        if (mWakeLock != null){
            Log.e(LOG_TAG, "WakeLock already acquired!");
            return;
        }
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sensors_data_logger:wakelocktag");
        mWakeLock.acquire(600*60*1000L /*10 hours*/);
        Log.e(LOG_TAG, "WakeLock acquired!");
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
