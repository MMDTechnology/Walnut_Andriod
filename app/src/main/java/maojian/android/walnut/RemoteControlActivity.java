package maojian.android.walnut;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by android on 4/8/16.
 */
public class RemoteControlActivity extends Activity {

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    ImageView remote_home;

    private Handler mHandler;

    ProgressBar battery_progress;
    TextView remain_battery;

    RoundProgressBar leftprogressbar;
    RoundProgressBar rightprogressbar;

    ImageButton back_button;
    ImageButton forward_button;

    DeviceActivity device;

    private  int batteryremain;

    private boolean isRide;

    private Boolean isBack;

    private  int forward_progress = 0;
    private  int backward_progress = 0;

    private  double remote_ride_radius=5;
    private boolean remote_ride_forward_flag;


    private int v_left;

    private int v_right;

    private OrientationEventListener mOrientationListener; // screen orientation listener
    private boolean mScreenProtrait = true;
    private boolean mCurrentOrient = false;

    private double rotation_angle;

    private SensorManager sm;

    private boolean forward_longClick = false;
    private boolean back_longClick = false;

    private BluetoothLeService mBluetoothLeService;

    private BluetoothGattCharacteristic speedmodeChara = null ;

    private int current_mode = 1;

    private int exam_value = 0;

    private TextView exam_label;

    private int startmode = 3;

    private boolean lockmode = false;

    private LinearLayout riding_remote;
    private ImageView remote_ride_forward;
    private ImageView remote_ride_backward;

    private ImageView remote_ride_forward_circle;

    int count = -1;

    private ImageButton remote_mode_switch;

    private ProgressBar remote_ride_battery_progress;
    private TextView remote_ride_battery_label;

    public static final String action = "jason.broadcast.action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_remotecontrol);

        remain_battery = (TextView) findViewById(R.id.remote_remainbattery);
        battery_progress = (ProgressBar) findViewById(R.id.battery_progress);

        battery_progress.setProgress(100);

        device = new DeviceActivity();

        isBack = false;

        exam_label = (TextView) findViewById(R.id.exam_value);

        remote_ride_battery_progress = (ProgressBar) findViewById(R.id.remote_ride_battery_progress);
        remote_ride_battery_label = (TextView) findViewById(R.id.remote_ride_battery_label);

        SharedPreferences pref = getSharedPreferences("myActivityName", 0);
        current_mode = pref.getInt("current_mode", 1);

        //startOrientationChangeListener();

        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //选取加速度感应器
        int sensorType = Sensor.TYPE_ACCELEROMETER;
        sm.registerListener(myAccelerometerListener,sm.getDefaultSensor(sensorType),SensorManager.SENSOR_DELAY_NORMAL);

        riding_remote = (LinearLayout) findViewById(R.id.riding_remote);

        remote_ride_forward = (ImageView) findViewById(R.id.remote_ride_forward);
        remote_ride_backward = (ImageView) findViewById(R.id.remote_ride_backward);

//        riding_remote.setOnTouchListener(
//                new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//
//
//
//
//                        return true;
//                    }
//                }
//        );

        //riding_remote.setVisibility(View.GONE);

        remote_ride_forward_circle = (ImageView) findViewById(R.id.remote_ride_forward_circle);

        remote_ride_forward.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN) {
                            int x = (int) event.getX();
                            int y = (int) event.getY();
                            startmode = 3;
                            //remote_ride_forward.setBackground(getResources().getDrawable(R.drawable.remote_ride_forward1));
                            remote_ride_forward_flag = true;

                            remote_ride_forward_circle.setImageDrawable(getResources().getDrawable(R.drawable.circle_remote_ride));
                            remote_ride_forward_circle.setScaleX((float)0);
                            remote_ride_forward_circle.setScaleY((float)0);
//                            remote_ride_forward.setImageDrawable(getResources().getDrawable(R.drawable.circle));//remote_ride_forward1  //circle

                            new Thread(ride_forward_runnable).start();

                            Log.e("riding debug front", "x: " + x + " y: " + y + "  " + riding_remote.getWidth() + " " + riding_remote.getHeight());

                            if(isRide) {
                                v_left = (byte) (15);
                                v_right = (byte) (15);
                            }

                            remote_ride_radius = 5;
                            remote_ride_backward.setEnabled(false);

//                            mHandler = new Handler();
//
//                            mHandler.post(ride_forward_runnable); // hide the navigation bar
//                            if(x<riding_remote.getWidth()/2){
//                                v_left = (byte) (50);
//                                v_right = (byte)(50);
//                            }
//                            else{
//                                v_left = (byte) (-128+50);
//                                v_right = (byte)(-128+50);
//                            }
                        }

                        //Log.e("riding","  "+event.getAction());

                        if(event.getAction() == MotionEvent.ACTION_UP){
                            Log.e("riding! debug", "  " + riding_remote.getWidth() + " " + riding_remote.getHeight());

                            remote_ride_backward.setBackground(getResources().getDrawable(R.drawable.remote_ride_backward));
                            remote_ride_forward.setBackground(getResources().getDrawable(R.drawable.remote_ride_forward));

                            remote_ride_forward.setImageDrawable(null);

                            remote_ride_forward_flag = false;


                            remote_ride_backward.setEnabled(true);
                            remote_ride_forward.setEnabled(true);

                            v_left = 0;
                            v_right = 0;
                        }
                        return true;
                    }
                }
        );

        remote_ride_backward.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN) {
                            int x = (int) event.getX();
                            int y = (int) event.getY();
                            startmode = 3;
                            remote_ride_backward.setBackground(getResources().getDrawable(R.drawable.remote_ride_backward1));

                            Log.e("riding debug back", "x: " + x + " y: " + y + "  " + riding_remote.getWidth() + " " + riding_remote.getHeight());

                            if(isRide) {
                                v_left = (byte) (-128 + 5);
                                v_right = (byte) (-128 + 5);
                            }

                            remote_ride_forward.setEnabled(false);
//                            if(x<riding_remote.getWidth()/2){
//                                v_left = (byte) (50);
//                                v_right = (byte)(50);
//                            }
//                            else{
//                                v_left = (byte) (-128+50);
//                                v_right = (byte)(-128+50);
//                            }
                        }

                        //Log.e("riding","  "+event.getAction());

                        if(event.getAction() == MotionEvent.ACTION_UP){
                            Log.e("riding! debug","  "+riding_remote.getWidth()+" "+riding_remote.getHeight());

                            remote_ride_backward.setBackground(getResources().getDrawable(R.drawable.remote_ride_backward));
                            remote_ride_forward.setBackground(getResources().getDrawable(R.drawable.remote_ride_forward));

                            remote_ride_forward.setImageDrawable(null);

                            v_left = 0;
                            v_right = 0;

                            remote_ride_backward.setEnabled(true);
                            remote_ride_forward.setEnabled(true);
                        }
                        return true;
                    }
                }
        );


        remote_home = (ImageView) findViewById(R.id.remote_homebutton);
        remote_home.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

//                        current_mode = 01;
//                        startmode = 3;
//                        lockmode = false;

                        //unregisterReceiver(mGattUpdateReceiver);
                        isBack = true;

                        Intent intent_ui = getIntent();
                        Bundle bundle = intent_ui.getExtras();
                        bundle.putBoolean("isDisconnect", false);//添加要返回给页面1的数据
                        intent_ui.putExtras(bundle);

                        RemoteControlActivity.this.setResult(Activity.RESULT_OK, intent_ui);
                        Log.e("remote", "sendbackData123");
                        RemoteControlActivity.this.finish();
                        //RemoteControlActivity.this.finish();
                    }
                }
        );


        leftprogressbar = (RoundProgressBar) findViewById(R.id.progressBar_left);

        leftprogressbar.setCricleProgressColor(Color.parseColor("#00EAAB"));


        //leftprogressbar.setProgress(70);

        rightprogressbar = (RoundProgressBar) findViewById(R.id.progressBar_right);

        rightprogressbar.setCricleProgressColor(Color.parseColor("#00EAAB"));
        //rightprogressbar.setProgress(30);

        back_button = (ImageButton) findViewById(R.id.left_button);
        forward_button = (ImageButton) findViewById(R.id.right_button);

        back_button.setLongClickable(true);
        forward_button.setLongClickable(true);

        forward_button.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        startmode = 3;
                        forward_longClick = true;
                        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                        backward_progress = 1;

                        new Thread(forward_runnable).start();

                        Log.e("videodebug", "longpressing");

                        return false;
                    }
                }
        );

        forward_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    forward_longClick = false;
                    new Thread(forward_runnable).start();
                }

                return false;
            }
        });


        back_button.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        startmode = 3;
                        back_longClick = true;
                        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                        forward_progress = 1;

                        new Thread(back_runnable).start();

                        Log.e("videodebug", "longpressing");

                        return false;
                    }
                }
        );

        back_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    back_longClick = false;
                    new Thread(back_runnable).start();
                }

                return false;
            }
        });

        remote_mode_switch = (ImageButton) findViewById(R.id.remote_mode_switch);
        remote_mode_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                riding_remote.setVisibility(View.GONE);
            }
        });


        Log.e("str debug", " remote oncreate");

        Intent gattServiceIntent = new Intent(RemoteControlActivity.this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


        new Thread(battery_runnable).start();

//        if(mBluetoothLeService!=null) {
//            for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
//                String uuid = null;
//                String unknownServiceString = getResources().getString(R.string.unknown_service);
//                String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
//                HashMap<String, String> currentServiceData = new HashMap<String, String>();
//                uuid = gattService.getUuid().toString();
//                currentServiceData.put(
//                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
//                currentServiceData.put(LIST_UUID, uuid);
//                //gattServiceData.add(currentServiceData);
//
//                ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
//                        new ArrayList<HashMap<String, String>>();
//                List<BluetoothGattCharacteristic> gattCharacteristics =
//                        gattService.getCharacteristics();
//                ArrayList<BluetoothGattCharacteristic> charas =
//                        new ArrayList<BluetoothGattCharacteristic>();
//
//                // Loops through available Characteristics.
//                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                    Log.e("Device debug", " " + gattCharacteristic.getUuid());
//                    if (gattCharacteristic.getUuid().toString().equals("0000ffe1-0000-1000-8000-00805f9b34fb")) {
//                        Log.e("Device done", " " + gattCharacteristic.getUuid());
//                        speedmodeChara = gattCharacteristic;
//                    }
//                }
//            }
//        }
//        byte array[] = {0,04,(byte)backward_progress,(byte)backward_progress,0};
//        Log.e("speedmodech", " " + speedmodeChara);
//        if(speedmodeChara!=null){
//            speedmodeChara.setValue(array);
//            mBluetoothLeService.writeCharacteristic(speedmodeChara);
//        }




    }

    Runnable ride_forward_runnable = new Runnable() {
        @Override
        public void run() {

//            ImageView ride_forward_circle = new ImageView(getBaseContext());
//            ride_forward_circle.setLayoutParams(remote_ride_forward.getLayoutParams());

            while(remote_ride_forward_flag) {


                try {
                    Thread.sleep(50);
                    remote_ride_radius = remote_ride_radius+2.0;

                    if(remote_ride_radius>=24.0){
                        remote_ride_radius = 24.0;
                        //break;
                    }

                    Log.e("circle","d"+remote_ride_radius);

                    remote_ride_forward_circle.setScaleX((float)remote_ride_radius);
                    remote_ride_forward_circle.setScaleY((float)remote_ride_radius);

                } catch (InterruptedException e) {
                        e.printStackTrace();
                }



            }
            while(!remote_ride_forward_flag){

                try {
                    Thread.sleep(25);
                    remote_ride_radius = remote_ride_radius-2.0;

                    if(remote_ride_radius<=1.0){
                        remote_ride_radius = 0;
                        //break;
                    }

                    Log.e("circle", "d" + remote_ride_radius);

                    remote_ride_forward_circle.setScaleX((float) remote_ride_radius);
                    remote_ride_forward_circle.setScaleY((float)remote_ride_radius);

                    if(remote_ride_radius==0.0)
                        break;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

        }
    };

    Runnable forward_runnable = new Runnable() {
        @Override
        public void run() {

            //int progress = 0;
            //Log.e("remotedebug", "rotation " + rotation_angle);

            while (forward_longClick) {
                try {
                    Thread.sleep(50);
                    forward_progress++;


                    rightprogressbar.setProgress(forward_progress);
                    if(forward_progress>=100) {
                        forward_progress = 100;
                        rightprogressbar.setProgress(100);
                        //break;
                    }
                } catch (InterruptedException e) {

                }


                //current_mode = 04;



                v_left = (byte) (forward_progress+forward_progress* Math.sin(rotation_angle*Math.PI/180));
                v_right = (byte) (forward_progress-forward_progress* Math.sin(rotation_angle*Math.PI/180));


                if(v_left>100) v_left = 100;
                if(v_right>100) v_right = 100;

                if(v_left<-5) v_left = 100;
                if(v_right<-5) v_right = 100;

                Log.e("!@!","debuggingggg "+forward_progress+"   "+v_left+"   "+v_right+"  "+Math.sin(rotation_angle*Math.PI/180));


            }

            while(!forward_longClick){
                try {
                    Thread.sleep(50);
                    if(forward_progress<=2) {
                        forward_progress = 0;
                        rightprogressbar.setProgress(forward_progress);
                        break;
                    }
                    forward_progress=forward_progress-2;
                    rightprogressbar.setProgress(forward_progress);

                } catch (InterruptedException e) {

                }


                //current_mode = 04;

                v_left = (byte) (forward_progress+forward_progress* Math.sin(rotation_angle*Math.PI/180));
                v_right = (byte) (forward_progress-forward_progress* Math.sin(rotation_angle*Math.PI/180));

                if(v_left<4) v_left = 0;
                if(v_right<4) v_right = 0;


            }





        }
    };

    Runnable back_runnable = new Runnable() {
        @Override
        public void run() {


            //Log.e("remotedebug", "rotation " + rotation_angle);
            while (back_longClick) {
                try {
                    Thread.sleep(50);
                    backward_progress++;
                    leftprogressbar.setProgress(backward_progress);
                    if(backward_progress>=100) {
                        backward_progress = 100;
                        leftprogressbar.setProgress(100);
                        //break;
                    }
                } catch (InterruptedException e) {

                }


                //current_mode = 05;

                v_left = (byte) (-128+(backward_progress+backward_progress* Math.sin(rotation_angle*Math.PI/180)));
                v_right = (byte)(-128+(backward_progress-backward_progress* Math.sin(rotation_angle*Math.PI/180)));

                if(v_left>-28) v_left = -28;
                if(v_right>-28) v_right = -28;

                Log.e("!@!","debuggingggg "+forward_progress+"   "+v_left+"   "+v_right+"  "+Math.sin(rotation_angle*Math.PI/180));


            }
            while (!back_longClick){
                try {
                    Thread.sleep(50);
                    if(backward_progress<=2) {
                        backward_progress = 0;
                        leftprogressbar.setProgress(backward_progress);
                        break;
                    }
                    backward_progress=backward_progress-2;
                    leftprogressbar.setProgress(backward_progress);
                } catch (InterruptedException e) {

                }

                //current_mode = 05;



                v_left = (byte) (-128+(backward_progress+backward_progress* Math.sin(rotation_angle*Math.PI/180)));
                v_right = (byte)(-128+(backward_progress-backward_progress* Math.sin(rotation_angle*Math.PI/180)));

                if(v_left<=-124) v_left = -128;
                if(v_right<=-124) v_right = -128;

                Log.e("!@!","debuggingg#g "+forward_progress+"   "+v_left+"   "+v_right+"  "+Math.sin(rotation_angle*Math.PI/180));

            }



        }
    };

    final SensorEventListener myAccelerometerListener = new SensorEventListener(){

        //复写onSensorChanged方法
        public void onSensorChanged(SensorEvent sensorEvent){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                //Log.e("sensor debug","onSensorChanged");

                //图解中已经解释三个值的含义
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];
//                Log.e("sensor debug"," heading "+X_lateral);
//                Log.e("sensor debug"," pitch "+Y_longitudinal);
//                Log.e("sensor debug"," roll "+Z_vertical);

                double angle = Math.acos(Z_vertical/Math.sqrt(Y_longitudinal*Y_longitudinal+Z_vertical*Z_vertical));

                if(Y_longitudinal<0) angle = 2*Math.PI - angle;

                //Log.e("sensor debug","onSensorChanged "+ 180*angle/Math.PI);

                rotation_angle = 180*angle/Math.PI;
            }
        }
        //复写onAccuracyChanged方法
        public void onAccuracyChanged(Sensor sensor , int accuracy){
            Log.e("sensor debug", "onAccuracyChanged");
        }
    };

//    private final void startOrientationChangeListener() {
//        mOrientationListener = new OrientationEventListener(this) {
//            @Override
//            public void onOrientationChanged(int rotation) {
//
//                rotation_angle = rotation;
//
//                Log.e("remotedebug", "rotation " + rotation_angle);
//
//                if (((rotation >= 0) && (rotation <= 45)) || (rotation >= 315)||((rotation>=135)&&(rotation<=225))) {//portrait
//                    mCurrentOrient = true;
//                    if(mCurrentOrient!=mScreenProtrait)
//                    {
//                        mScreenProtrait = mCurrentOrient;
//                        //OrientationChanged(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//
//                        Log.e("remote debug", "Screen orientation changed from Landscape to Portrait!");
//                    }
//                }
//                else if (((rotation > 45) && (rotation < 135))||((rotation>225)&&(rotation<315))) {//landscape
//                    mCurrentOrient = false;
//                    if(mCurrentOrient!=mScreenProtrait)
//                    {
//                        mScreenProtrait = mCurrentOrient;
//                        //OrientationChanged(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//
//                        Log.e("remote debug", "Screen orientation changed from Portrait to Landscape!");
//                    }
//                }
//            }
//        };
//        mOrientationListener.enable();
//    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e("dialog debug","???");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("Device debug", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);

            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());



        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("debugging","onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //mConnected = true;
                //updateConnectionState(true);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //mConnected = false;
                //updateConnectionState(false);
                invalidateOptionsMenu();
                Log.e("remotedebugging", "ACTION_GATT_DISCONNECTED");

//                Intent intent_ui = new Intent(action);
//                intent_ui.putExtra("data", "yes i am data");
//                sendBroadcast(intent_ui);

                sendBackData();
                //***



                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void sendBackData(){
        Intent intent_ui = getIntent();
        Bundle bundle = intent_ui.getExtras();
        bundle.putBoolean("isDisconnect", true);//添加要返回给页面1的数据
        intent_ui.putExtras(bundle);

        Log.e("remote", "sendbackData");
        //setResult(1001, intent_ui);

        RemoteControlActivity.this.setResult(Activity.RESULT_OK, intent_ui);
        RemoteControlActivity.this.finish();
        //RemoteControlActivity.this.finish();
    }

    private void displayData(String data) {

        if (data != null) {

            String real_data = data.substring(data.indexOf("22"),data.length()-1);

            //Log.e("what data", " remote! "+real_data);
            //Log.e("what data", " remote! "+real_data.length());


            hexStringToByte(real_data);

            byte sendBytes[] = {0x12,0x55,1,1,0,0,0,0,0,0,0,0};

            //try {
                //byte[] sendBytes= real_data.getBytes("UTF8");//data.getBytes("UTF8")


                remain_battery.setText(+batteryremain+" %");

                battery_progress.setProgress(batteryremain);

                remote_ride_battery_label.setText(+batteryremain+" %");

                remote_ride_battery_progress.setProgress(batteryremain);


                //
                if(mBluetoothLeService!=null) {
                    for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
                        String uuid = null;
                        String unknownServiceString = getResources().getString(R.string.unknown_service);
                        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
                        HashMap<String, String> currentServiceData = new HashMap<String, String>();
                        uuid = gattService.getUuid().toString();
                        currentServiceData.put(
                                LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
                        currentServiceData.put(LIST_UUID, uuid);
                        //gattServiceData.add(currentServiceData);

                        ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                                new ArrayList<HashMap<String, String>>();
                        List<BluetoothGattCharacteristic> gattCharacteristics =
                                gattService.getCharacteristics();
                        ArrayList<BluetoothGattCharacteristic> charas =
                                new ArrayList<BluetoothGattCharacteristic>();

                        // Loops through available Characteristics.
                        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                            //Log.e("Device debug", " " + gattCharacteristic.getUuid());
                            if (gattCharacteristic.getUuid().toString().equals("0000ffe9-0000-1000-8000-00805f9b34fb")) {
                                Log.e("Device done", " " + gattCharacteristic.getUuid());
                                speedmodeChara = gattCharacteristic;
                            }
                        }
                    }
                }

                if(isRide)
                    riding_remote.setVisibility(View.VISIBLE);
//                else
//                    riding_remote.setVisibility(View.GONE);

                if(exam_value>0){

                    //exam_label.setText(exam_value);
                    exam_label.setVisibility(View.VISIBLE);

                    switch (exam_value){

                        case 1:
                        case 2:
                            exam_label.setText("Abnormality detected ("+ exam_value +").");
                            break;

                        case 4:
                        case 8:
                            exam_label.setText("Abnormality detected ("+ exam_value +").");
                            break;
                        case 16:
                            exam_label.setText("Abnormality detected ("+ exam_value +").");
                            break;
                        case 32:
                            exam_label.setText("Abnormality detected ("+ exam_value +").");
                            break;
                        case 64:
                            exam_label.setText("Abnormality detected ("+ exam_value +").");
                            break;

                    }

                }
                else
                    exam_label.setVisibility(View.GONE);



                //byte array[] = {0,03,0,0,0};
                sendBytes[0] = 0x12;
                sendBytes[1] = 0x55;

                sendBytes[2] = (byte) startmode;


                Log.e("what data", " remote!! "+startmode+"   "+v_right);

                sendBytes[3] = (byte) current_mode;//current_mode

                if(lockmode)
                    sendBytes[4] = 1;
                else
                    sendBytes[4] = 0;

                //sendBytes[3] = (byte) current_mode;

                //sendBytes[7] = (byte) current_mode;


                sendBytes[5] = (byte) v_left;

                sendBytes[6] = (byte) v_right;

                //sendBytes[3] = (byte) (sendBytes[4] + 1);
                Log.e("speedmodech", " " + speedmodeChara);
                if(speedmodeChara!=null){
                    speedmodeChara.setValue(sendBytes);
                    mBluetoothLeService.writeCharacteristic(speedmodeChara);

                    //mBluetoothLeService.readCharacteristic(speedmodeChara);
                }

//                for(int i=0;i<sendBytes.length;i++){
//
//                    //Log.e("what",""+sendBytes[i]);
//                }

//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }



            //mDataField.setText(data);
        }
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    public void hexStringToByte(String hex) {
        int len = (hex.length() / 3);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 3;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
            //Log.e("byte debyg","  "+result[i]);
        }

        batteryremain = result[5];

        exam_value = result[4];

        if(result[7]>0) isRide = true;
        else  isRide=false;

        //odometer_range = result[6];

        //return result;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    Runnable battery_runnable = new Runnable() {
        @Override
        public void run() {

            //while (mConnected){

            try {
                Thread.sleep(200);

                //beginnerText.performClick();
                if (mBluetoothLeService != null) {
                    for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
                        String uuid = null;
                        String unknownServiceString = getResources().getString(R.string.unknown_service);
                        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
                        HashMap<String, String> currentServiceData = new HashMap<String, String>();
                        uuid = gattService.getUuid().toString();
                        currentServiceData.put(
                                LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
                        currentServiceData.put(LIST_UUID, uuid);
                        //gattServiceData.add(currentServiceData);

                        ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                                new ArrayList<HashMap<String, String>>();
                        List<BluetoothGattCharacteristic> gattCharacteristics =
                                gattService.getCharacteristics();
                        ArrayList<BluetoothGattCharacteristic> charas =
                                new ArrayList<BluetoothGattCharacteristic>();

                        // Loops through available Characteristics.     //0000ffe1-0000-1000-8000-00805f9b34fb
                        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                            //Log.e("Device debug", " " + gattCharacteristic.getUuid());
                            if (gattCharacteristic.getUuid().toString().equals("0000ffe9-0000-1000-8000-00805f9b34fb")) {//0000ffe9-0000-1000-8000-00805f9b34fb
                                Log.e("Remote done", " " + gattCharacteristic.getUuid().toString());
                                speedmodeChara = gattCharacteristic;
                            }
                        }
                    }
                }

                // start mode = remote

                byte array[] = {0x12,0x55,3,2,0,0,0,0,0,0,0,0};  // mode 4

                //current_mode = 01;
                startmode = 3;
                lockmode = false;

                array[3] = (byte) current_mode;

                Log.e("???speedmodech", " " + speedmodeChara);
                if (speedmodeChara != null) {
                    speedmodeChara.setValue(array);
                    mBluetoothLeService.writeCharacteristic(speedmodeChara);

                    //mBluetoothLeService.readCharacteristic(speedmodeChara);
                }
            }
            catch (InterruptedException e) {

            }

        }

        //}

    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("str debug", " remote onresume");

        startmode = 3;


        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {

        super.onPause();

        Log.e("str debug", " remote onpause");

        v_left = 0;
        v_right = 0;

        startmode = 2;

        //RemoteControlActivity.this.finish();
        if(isBack)
            unregisterReceiver(mGattUpdateReceiver);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.e("str debug", " remote ondestroy");

        //unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action = event.getAction();

//        if (action ==KeyEvent.ACTION_DOWN) {
//            count++;
//            Log.e("volume key debug","ACTION_DOWN "+count);
//
//            return true;
//        }

        if(riding_remote.getVisibility()==View.VISIBLE) {

            if (action == KeyEvent.ACTION_UP) {
                v_left = (byte) (0);
                v_right = (byte) (0);
                remote_ride_backward.setEnabled(true);
                remote_ride_forward.setEnabled(true);
                count--;
                Log.e("volume key debug", "ACTION_UP " + count);

                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {

//    int action = event.getAction();
//    if (action== KeyEvent.ACTION_UP) {
//            count--;
//            Log.e("volume key debug","ACTION_UP "+count);
//
//            return true;
//        }

    if (riding_remote.getVisibility() == View.VISIBLE){
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Log.e("remotebackpress!!!","debug");
                isBack = true;

                Log.e("remotebackpress","debug");
                Intent intent_ui = getIntent();
                Bundle bundle = intent_ui.getExtras();
                bundle.putBoolean("isDisconnect", false);//添加要返回给页面1的数据
                intent_ui.putExtras(bundle);

                RemoteControlActivity.this.setResult(Activity.RESULT_OK, intent_ui);
                Log.e("remote", "sendbackData123");
                finish();
                return  true;

            case KeyEvent.KEYCODE_VOLUME_DOWN:
                startmode = 3;
                v_left = (byte) (-128 + 50);
                v_right = (byte) (-128 + 50);
                remote_ride_forward.setEnabled(false);
                count--;
                Log.e("volume key debug", "KEYCODE_VOLUME_DOWN " + count);
                return true;

            case KeyEvent.KEYCODE_VOLUME_UP:
                startmode = 3;
                v_left = (byte) (50);
                v_right = (byte) (50);
                remote_ride_backward.setEnabled(false);
                count++;
                Log.e("volume key debug", "KEYCODE_VOLUME_UP " + count);
                return true;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                return true;
        }
}



    return super.onKeyDown(keyCode, event);
}

    @Override  // override backpressed
    public void onBackPressed() {
        isBack = true;

        Log.e("remotebackpress","debug");
        Intent intent_ui = getIntent();
        Bundle bundle = intent_ui.getExtras();
        bundle.putBoolean("isDisconnect", false);//添加要返回给页面1的数据
        intent_ui.putExtras(bundle);

        RemoteControlActivity.this.setResult(Activity.RESULT_OK, intent_ui);
        Log.e("remote", "sendbackData123");
        finish();
    }



}
