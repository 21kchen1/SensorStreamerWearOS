package com.SensorStreamer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @// TODO: 2024/11/8 变量 unixTimeText 用于后续更新 ui
 * @// TODO: 2024/11/9 记得发送的sock和接收的sock要分开，防止塞车，发送的可以多整几个
 * */
public class MainActivity extends WearableActivity implements AudioListen.AudioCallback, IMUListen.IMUCallback {

    private static final String LOG_TAG = "log tag";
    private Button startButton,stopButton;

    private TextView ipAddr;
//    留着后续更新 ui
    private TextView unixTimeText;
    private ActivityMainBinding binding;
    private AtomicBoolean mIsRecording = new AtomicBoolean(false);
    private int port = 5005;
    private final Gson gson = new Gson();

    // audio stuff
    int audioSamplingRate = 16000;
    // random
    private String SERVER = "192.168.1.101"; // tplink5g

//    创建 Link 类
    UDPLinkF udpLinkF = new UDPLinkF();
    final private Link link = udpLinkF.create();

    AudioListenF audioListenF = new AudioListenF();
    final private AudioListen audioListen = (AudioListen) audioListenF.create();

    IMUListenF imuListenF = new IMUListenF();
    final private IMUListen imuListen = (IMUListen) imuListenF.create();

    private PowerManager.WakeLock mWakeLock;
    private PowerManager powerManager;
    private SensorManager mSensorManager;
//    private HashMap<String, Sensor> mSensors = new HashMap<>();


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

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startButton = findViewById (R.id.StartStreaming);
        stopButton = findViewById (R.id.StopStreaming);

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

        // sensor stuff
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
    }

    private final View.OnClickListener stopListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
//            停止获取 IMU 数据
            imuListen.stopRead();
//            停止获取音频数据
            audioListen.stopRead();
//            注销 IMU 监听
            imuListen.off();
//            注销音频监听
            audioListen.off();
//            关闭数据传输
            link.off();
            Log.d("VS","Recorder released");
        }

    };

    private final View.OnClickListener startListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            MainActivity.this.launchSensor();
//            MainActivity.this.test();
        }
    };

    public void launchSensor() {
        SERVER = ipAddr.getText().toString();
        try {
            link.launch(InetAddress.getByName(SERVER), port);

//                这里后面变成远程设置
            int[] sensors = new int[] {
                    Sensor.TYPE_ACCELEROMETER,
                    Sensor.TYPE_GYROSCOPE,
                    Sensor.TYPE_ROTATION_VECTOR,
                    Sensor.TYPE_MAGNETIC_FIELD
            };
//                启动相关组件
            imuListen.launch(mSensorManager, sensors,0 ,MainActivity.this);
            audioListen.launch(audioSamplingRate, MainActivity.this);
        } catch (UnknownHostException e) {
            Log.e("VS", "UnknownHostException", e);
            return;
        }
//            开始读取数据
        imuListen.startRead();
        audioListen.startRead();
    }

    public void test() {
        Thread testTread = new Thread(() -> {
            try {
                byte[] buf = new byte[10240];
                link.launch(InetAddress.getByName(SERVER), this.port);
                link.rece(buf);
                System.out.println(Arrays.toString(buf));
            } catch (UnknownHostException e) {
                Log.e("VS", "UnknownHostException", e);
                return;
            }
        });
        testTread.start();
    }

    /**
     * 音频回调函数
     * */
    @Override
    public void dealAudioData(byte[] data) {
        AudioData audioData = new AudioData(System.currentTimeMillis(), data);
        String json = gson.toJson(audioData);
        byte[] buf = json.getBytes(StandardCharsets.UTF_8);
        // 发送数据
        link.send(buf);
    }

    /**
     * IMU 回调函数
     * */
    public void dealIMUData(String type, float[] data, long sensorTimestamp) {
        Thread streamThread = new Thread(() -> {
            long unixTimestamp = System.currentTimeMillis();
            IMUData imuData = new IMUData(unixTimestamp, sensorTimestamp, type, data);
            String json = gson.toJson(imuData);
            byte []buf = json.getBytes(StandardCharsets.UTF_8);

            link.send(buf);
        });
        streamThread.start();
    }

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

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // Stuff that updates the UI
//                        unixTimeText.setText(unixString);
//
//                    }
//                });


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
