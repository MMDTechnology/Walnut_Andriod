package maojian.android.walnut;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by WalnutTech_Developer01 on 3/8/16.
 */
public class DeviceActivity extends Activity {

    private LocationManager locationManager;
    private String locationProvider;

    private  double longitude;
    private  double latitude;

    private int today_usage;
    int odometer_range;

    ImageButton returnButton;

    ImageButton device_connectbutton;
    ImageButton device_product;

    ImageButton lockstatus;

    ImageView start_mode_button;

    private Boolean lockmode;

    private boolean lock_signal = false;

    private byte[] passwordArray;

    private int startmode;// true: stand to go    false: slide to go

    SeekBar speedMode;

    TextView device_remind;

    TextView beginnerText;
    TextView sportText;
    TextView proText;

    ImageButton remotecontrol_button;

    TextView battery_percent_text;
    TextView battery_time_text;
    TextView locking_text;

    AlwaysMarqueeTextView rounding_text;

    AlertDialog connecting_dialog;

    private  int batteryremain;

    private int testdata1;

    private int testdata2;

    private boolean pause_connect;

    private  AlertDialog selfcheck_dialog;

    private int sensorValue0,sensorValue1,sensorValue2,sensorValue3,sensorValue4;


    // Bluetooth Setting

    // private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private Handler batteryHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private String mDeviceAddress;

    private BluetoothLeService mBluetoothLeService;

    private boolean mConnected = false;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private BluetoothGattCharacteristic speedmodeChara = null ;


    private int current_mode = 0;

    private int examvalue = 0;

    private  View dialog_view_selfcheck;

    private ImageView selfcheck_leftmotor;
    private ImageView selfcheck_rightmotor;
    private ImageView selfcheck_battery;
    private ImageView selfcheck_system;

    private ImageView sensor_deck1;
    private ImageView sensor_deck2;
    private ImageView sensor_deck3;
    private ImageView sensor_deck4;
    private ImageView sensor_deck5;


    private Button nextTrain;

    private int trainmode;

    private LinearLayout device_bottomlayout;
    private LinearLayout diconnnected_layout;
    private RelativeLayout device_middlelayout;

    private int navigationBarHeight;

    Handler sHandler;




    // Data Recording
    public List<String> record_array;

    private ServiceConnection mServiceConnection;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_device);


        record_array = new ArrayList<String>();

        lockmode = true;

        startmode = 1;

        trainmode = 0;

        returnButton = (ImageButton) findViewById(R.id.device_returnbutton);

        returnButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //mBluetoothLeService.disconnect();
                        pause_connect = true;

                        DeviceActivity.this.finish();
                        overridePendingTransition(R.anim.bottom_in,
                                R.anim.bottom_out);

                    }
                }
        );

        if (ContextCompat.checkSelfPermission(DeviceActivity.this, Manifest.permission.INSTALL_LOCATION_PROVIDER) == PackageManager.PERMISSION_GRANTED) {
            //具有拍照权限，直接调用相机
            //具体调用代码
            Log.e("cameradebug","checkSelfPermission");

        } else {
            //不具有拍照权限，需要进行权限申请
            ActivityCompat.requestPermissions(DeviceActivity.this, new String[]{Manifest.permission.INSTALL_LOCATION_PROVIDER}, 1);
        }

        device_bottomlayout = (LinearLayout) findViewById(R.id.device_bottomlayout);
        diconnnected_layout = (LinearLayout) findViewById(R.id.diconnnected_layout);
        device_middlelayout = (RelativeLayout) findViewById(R.id.device_middlelayout);

        battery_percent_text = (TextView) findViewById(R.id.batterypercent_text);
        battery_time_text = (TextView) findViewById(R.id.batterytime_text);
        locking_text = (TextView) findViewById(R.id.locking_text);
        Typeface face = Typeface.createFromAsset(DeviceActivity.this.getAssets(), "fonts/Brown-Light.otf");
        battery_percent_text.setTypeface(face);
        battery_time_text.setTypeface(face);
        locking_text.setTypeface(face);

        rounding_text = (AlwaysMarqueeTextView) findViewById(R.id.device_remind);
        Typeface face1 = Typeface.createFromAsset(DeviceActivity.this.getAssets(), "fonts/Brown-Light.otf");
        rounding_text.setTypeface(face1);


        device_connectbutton = (ImageButton) findViewById(R.id.device_connectbutton);
        device_connectbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Log.e("strange", "setdisConnected_Layout>>>>");


                        if (!mConnected) {

//                            Intent tutintent = new Intent(DeviceActivity.this, TutVideoActivity.class);
//                            startActivity(tutintent);

                            AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
                            LayoutInflater inflater = getLayoutInflater();
                            View dialog_view = inflater.inflate(R.layout.dialog_searchingdevice, null);

                            //dialog_view.setBackgroundColor(Color.parseColor("#00EAAB"));
                            builder.setView(dialog_view);

                            connecting_dialog = builder.create();
                            connecting_dialog.show();

                            mHandler = new Handler();

                            // Use this check to determine whether BLE is supported on the device.  Then you can
                            // selectively disable BLE-related features.
                            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                                Toast.makeText(DeviceActivity.this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
                            // BluetoothAdapter through BluetoothManager.
                            final BluetoothManager bluetoothManager =
                                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                            mBluetoothAdapter = bluetoothManager.getAdapter();

                            Log.e("strange", "setdisConnected_Layou.....   " + bluetoothManager);

                            // Checks if Bluetooth is supported on the device.
                            if (mBluetoothAdapter == null) {
                                Toast.makeText(DeviceActivity.this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }

                            if (!mBluetoothAdapter.isEnabled()) {
                                if (!mBluetoothAdapter.isEnabled()) {
                                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                }
                            }

                            scanLeDevice(true);


                        } else {

//                            if(mConnected){
//                                unregisterReceiver(displayGattServices);
//
//                                unbindService(mServiceConnection);
//                            }

                            mBluetoothLeService.disconnect();
                            mConnected = false;
                            device_connectbutton.setBackground(getResources().getDrawable(R.drawable.decive_disconnected));


                        }


//                        Dialog dialog = new Dialog(DeviceActivity.this);
//                        dialog.setContentView(R.layout.dialog_searchingdevice);
//
//                        //dialog.setTitle("Custom Dialog");
//                        dialog.show();

                    }
                }
        );

        device_product = (ImageButton) findViewById(R.id.device_product);
        lockstatus = (ImageButton) findViewById(R.id.lockstatus_button);

        start_mode_button = (ImageView) findViewById(R.id.start_mode_button);

        start_mode_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        lock_signal = false;
                        if(startmode == 1){
                            start_mode_button.setBackground(getResources().getDrawable(R.drawable.device_slidemode));
                            startmode = 2;
                        }
                        else {
                            start_mode_button.setBackground(getResources().getDrawable(R.drawable.device_standmode));
                            startmode = 1;

                        }

                        if(mConnected) {

                        }



                    }
                }
        );


        lockstatus.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(mConnected) {

                            if(AVUser.getCurrentUser()!=null) {
                                lock_signal = true;
                                Log.e("password setup","lock signal true");
                            }

                            // 收到返回值改变状态更好
                            if (lockmode) {

                                lockstatus.setBackground(getResources().getDrawable(R.drawable.unlockstatus));
                                device_product.setBackground(getResources().getDrawable(R.drawable.device_product));

                                lockmode = false;

                                rounding_text.setText("Tap the SPECTRA for self-test");

                            } else {
                                lockstatus.setBackground(getResources().getDrawable(R.drawable.lockstatus));
                                device_product.setBackground(getResources().getDrawable(R.drawable.device_product_lock));

                                lockmode = true;

                                rounding_text.setText("Tap the SPECTRA to unlock");

                            }

                        }
                    }
                }
        );


        nextTrain = (Button) findViewById(R.id.nextTrain);
        nextTrain.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        trainmode++;

                        if(trainmode>4) trainmode = 0;

                        nextTrain.setText(""+trainmode);

                    }
                }
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
//                        LayoutInflater inflater = getLayoutInflater();
        LayoutInflater inflater = getLayoutInflater();

        dialog_view_selfcheck = inflater.inflate(R.layout.dialog_sensorchecking, null);

        builder.setView(dialog_view_selfcheck);
        selfcheck_dialog = builder.create();

        sensor_deck1 = (ImageView) dialog_view_selfcheck.findViewById(R.id.sensor_deck1);
        sensor_deck2 = (ImageView) dialog_view_selfcheck.findViewById(R.id.sensor_deck2);
        sensor_deck3 = (ImageView) dialog_view_selfcheck.findViewById(R.id.sensor_deck3);
        sensor_deck4 = (ImageView) dialog_view_selfcheck.findViewById(R.id.sensor_deck4);
        sensor_deck5 = (ImageView) dialog_view_selfcheck.findViewById(R.id.sensor_deck5);

        //sensor_deck1.setImageDrawable(getResources().getDrawable(R.drawable.sensor_deck1_s));


        device_product.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


//                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
////                        LayoutInflater inflater = getLayoutInflater();
////                        LayoutInflater inflater = getLayoutInflater();
////
////                        dialog_view_selfcheck = inflater.inflate(R.layout.dialog_sensorchecking, null);
////
////                        builder.setView(dialog_view_selfcheck);
//
//                        selfcheck_dialog = builder.create();

                        sensor_deck1.setBackground(getResources().getDrawable(R.drawable.sensor_deck1));
                        sensor_deck2.setBackground(getResources().getDrawable(R.drawable.sensor_deck5));
                        sensor_deck3.setBackground(getResources().getDrawable(R.drawable.sensor_deck2));
                        sensor_deck4.setBackground(getResources().getDrawable(R.drawable.sensor_deck4));
                        sensor_deck5.setBackground(getResources().getDrawable(R.drawable.sensor_deck3));


//                        dialog_view_selfcheck = inflater.inflate(R.layout.dialog_selfchecking, null);
//                        selfcheck_leftmotor = (ImageView) dialog_view_selfcheck.findViewById(R.id.selfcheck_leftmotor);
//                        selfcheck_rightmotor = (ImageView) dialog_view_selfcheck.findViewById(R.id.selfcheck_rightmotor);
//                        selfcheck_battery = (ImageView) dialog_view_selfcheck.findViewById(R.id.selfcheck_battery);
//                        selfcheck_system = (ImageView) dialog_view_selfcheck.findViewById(R.id.selfcheck_system);
//
//
//                        //dialog_view.setBackgroundColor(Color.parseColor("#00EAAB"));
//                        builder.setView(dialog_view_selfcheck);
//
//                        AlertDialog dialog = builder.create();

                        if(mConnected) {

                            if (!lockmode) {
                                selfcheck_dialog.show();

                            // Sensor_checking


                            // sensor_value & 1  sensor_deck1.setBackground(getResources().getDrawable(R.drawable.sensor_deck1_s));



//                        selfcheck_leftmotor = (ImageView) dialog_view_selfcheck.findViewById(R.id.selfcheck_leftmotor);
//                        selfcheck_rightmotor = (ImageView) dialog_view_selfcheck.findViewById(R.id.selfcheck_rightmotor);
//                        selfcheck_battery = (ImageView) dialog_view_selfcheck.findViewById(R.id.selfcheck_battery);
//                        selfcheck_system = (ImageView) dialog_view_selfcheck.findViewById(R.id.selfcheck_system);

//                                new Handler().postDelayed(new Runnable() {
//                                    public void run() {
//                                        selfcheck_leftmotor.setBackground(getResources().getDrawable(R.drawable.device_selfchecking_ok));
//                                        selfcheck_rightmotor.setBackground(getResources().getDrawable(R.drawable.device_selfchecking_ok));
//                                        //execute the task
//                                    }
//                                }, 1500);
//
//                                new Handler().postDelayed(new Runnable() {
//                                    public void run() {
//                                        selfcheck_battery.setBackground(getResources().getDrawable(R.drawable.device_selfchecking_ok));
//                                        selfcheck_system.setBackground(getResources().getDrawable(R.drawable.device_selfchecking_ok));
//                                        //execute the task
//                                    }
//                                }, 1500);
//
//
//
//                            //new Thread(self_exam_thread).start();
//
                            ImageButton contact = (ImageButton) dialog_view_selfcheck.findViewById(R.id.sensor_contactbutton);

                            contact.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(DeviceActivity.this, FeedbackActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                            );

                        }else{
                                lockstatus.setBackground(getResources().getDrawable(R.drawable.unlockstatus));
                                device_product.setBackground(getResources().getDrawable(R.drawable.device_product));

                                lockmode = false;

                                rounding_text.setText("Tap the SPECTRA for self-test");

                            }

                        }
                    }


                }
        );

       //final BluetoothGattCharacteristic speedmodeChara = null ;



        speedMode = (SeekBar) findViewById(R.id.device_speedmode);
        speedMode.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {


                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                        int progress = seekBar.getProgress();

                        Typeface face = Typeface.createFromAsset(DeviceActivity.this.getAssets(), "fonts/Brown-Regular.otf");
                        beginnerText.setTypeface(face);
                        sportText.setTypeface(face);
                        proText.setTypeface(face);

                        lock_signal = false;

                        if (progress >= 0 && progress < 33) {

                            seekBar.setProgress(10);

                            beginnerText.setTextSize(15);
                            sportText.setTextSize(10);
                            proText.setTextSize(10);

                            beginnerText.setTextColor(Color.parseColor("#00EAAB"));
                            sportText.setTextColor(Color.parseColor("#727171"));
                            proText.setTextColor(Color.parseColor("#727171"));

                            byte array[] = {0, 01, 0, 0, 0};
                            Log.e("speedmodech", " " + speedmodeChara);

                            current_mode = 01;


                        } else if (progress >= 33 && progress < 66) {

                            seekBar.setProgress(50);

                            beginnerText.setTextSize(10);
                            sportText.setTextSize(15);
                            proText.setTextSize(10);

                            beginnerText.setTextColor(Color.parseColor("#727171"));
                            sportText.setTextColor(Color.parseColor("#00EAAB"));
                            proText.setTextColor(Color.parseColor("#727171"));

                            byte array[] = {0, 02, 0, 0, 0};

                            Log.e("speedmodech", " " + speedmodeChara);

                            current_mode = 02;

                        } else if (progress >= 66 && progress <= 100) {

                            seekBar.setProgress(90);

                            beginnerText.setTextSize(10);
                            sportText.setTextSize(10);
                            proText.setTextSize(15);

                            beginnerText.setTextColor(Color.parseColor("#727171"));
                            sportText.setTextColor(Color.parseColor("#727171"));
                            proText.setTextColor(Color.parseColor("#00EAAB"));

                            byte array[] = {0, 03, 0, 0, 0};
                            Log.e("speedmodech", " " + speedmodeChara);

                            current_mode = 03;

                        }
//                        Log.e("remain battery debug", "" + speedmodeChara.getValue());
//                        Log.e("remain battery debug", "" + speedmodeChara.getValue()[0]+" "+speedmodeChara.getValue()[1]+" "+speedmodeChara.getValue()[2]);

                    }
                }
        );

        remotecontrol_button = (ImageButton) findViewById(R.id.device_remotecontrol);
        remotecontrol_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mConnected) {

                            //current_mode = 01;
                            //startmode = 3;
                            lockmode = false;

                            //DeviceActivity.this.finish();

                            //Intent gattServiceIntent = new Intent(DeviceActivity.this, BluetoothLeService.class);

                            //unbindService(mServiceConnection);
                            pause_connect = true;

                            //stopService(gattServiceIntent);
                            final SharedPreferences pref = getApplicationContext().getSharedPreferences("myActivityName", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putInt("current_mode", current_mode);
                            editor.commit();

                            Intent intent = new Intent(DeviceActivity.this, RemoteControlActivity.class);

                            Bundle bundle = new Bundle();
                            intent.putExtras(bundle);
//                            startActivity(intent);
                            startActivityForResult(intent, 0);

//                            IntentFilter filter = new IntentFilter(RemoteControlActivity.action);
//                            registerReceiver(broadcastReceiver, filter);

//                            unbindService(mServiceConnection);
                        }

                    }
                }
        );

        device_remind = (TextView) findViewById(R.id.device_remind);
        device_remind.setFocusable(true);



//        Log.e("password setup", "current user: "+AVUser.getCurrentUser().getObjectId());
//
//        hexStringToPassword(AVUser.getCurrentUser().getObjectId());





        setdisConnected_Layout();

        beginnerText = (TextView) findViewById(R.id.device_speedmode_beginner);
        sportText = (TextView) findViewById(R.id.device_speedmode_sport);
        proText = (TextView) findViewById(R.id.device_speedmode_pro);

        beginnerText.setTextColor(Color.parseColor("#00EAAB"));
        beginnerText.setTextSize(15);

        beginnerText.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lock_signal = false;
                        speedMode.setProgress(10);

                        beginnerText.setTextSize(15);
                        sportText.setTextSize(10);
                        proText.setTextSize(10);

                        beginnerText.setTextColor(Color.parseColor("#00EAAB"));
                        sportText.setTextColor(Color.parseColor("#727171"));
                        proText.setTextColor(Color.parseColor("#727171"));


                        current_mode = 01;
                    }
                }
        );
        sportText.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lock_signal = false;
                        speedMode.setProgress(50);

                        beginnerText.setTextSize(10);
                        sportText.setTextSize(15);
                        proText.setTextSize(10);

                        beginnerText.setTextColor(Color.parseColor("#727171"));
                        sportText.setTextColor(Color.parseColor("#00EAAB"));
                        proText.setTextColor(Color.parseColor("#727171"));

                        current_mode = 02;
                    }
                }
        );
        proText.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lock_signal = false;
                        speedMode.setProgress(90);

                        beginnerText.setTextSize(10);
                        sportText.setTextSize(10);
                        proText.setTextSize(15);

                        beginnerText.setTextColor(Color.parseColor("#727171"));
                        sportText.setTextColor(Color.parseColor("#727171"));
                        proText.setTextColor(Color.parseColor("#00EAAB"));

                        current_mode = 03;
                    }
                }
        );


        // location manager

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if(providers.contains(LocationManager.GPS_PROVIDER)){
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
        }else if(providers.contains(LocationManager.NETWORK_PROVIDER)){
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }else{
            Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
            return ;
        }


        //获取Location
//        if (DeviceActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
//                PackageManager.PERMISSION_GRANTED || this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
//                PackageManager.PERMISSION_GRANTED) {



            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


            if (location != null) {
                //不为空,显示地理位置经纬度
                showLocation(location);
            }
            //监视地理位置变化
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            Log.e("locationdebug", "permissiongranted " + locationProvider.toString());
//        }





    }

//    Runnable self_exam_thread = new Runnable() {
//        @Override
//        public void run() {
//
//            //while (mConnected){
//
//
//
//            try {
//                Thread.sleep(1200);
//
//                selfcheck_leftmotor.setBackground(getResources().getDrawable(R.drawable.device_selfchecking_ok));
//                selfcheck_rightmotor.setBackground(getResources().getDrawable(R.drawable.device_selfchecking_ok));
//
//                Thread.sleep(1200);
//
//                selfcheck_battery.setBackground(getResources().getDrawable(R.drawable.device_selfchecking_ok));
//                selfcheck_system.setBackground(getResources().getDrawable(R.drawable.device_selfchecking_ok));
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    };
    Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
        int flags;
        int curApiVersion = Build.VERSION.SDK_INT;
        // This work only for android 4.4+
        if(curApiVersion >= Build.VERSION_CODES.KITKAT){
            // This work only for android 4.4+
            // hide navigation bar permanently in android activity
            // touch the screen, the navigation bar will not show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;

        }else{
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // must be executed in main thread :)
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }
};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("device", "sendbackData "+requestCode);

        if(resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();

            Log.e("device", "sendbackData");

            if (bundle.getBoolean("isDisconnect")){
                Log.e("device", "sendbackData strange");
                device_connectbutton.setBackground(getResources().getDrawable(R.drawable.decive_disconnected));
                setdisConnected_Layout();
            }
        }
    }

//    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // TODO Auto-generated method stub
//            setdisConnected_Layout();
//
//        }
//    };


    @Override
    protected void onPause() {

        super.onPause();

        Log.e("device debug", " device onpause");

        if(mConnected&&pause_connect){
            unregisterReceiver(mGattUpdateReceiver);
            //unregisterReceiver(broadcastReceiver);
        }
    }

//    @Override
//    protected void onDestroy(){
//        super.onDestroy();
//
//
//        //unregisterReceiver(mGattUpdateReceiver);
//        if(mConnected){
//            Log.e("str debug", " device ondestroy");
//            //unregisterReceiver(mGattUpdateReceiver);
//
//            unbindService(mServiceConnection);
//        }
//
////        unbindService(mServiceConnection);
//    }

    @Override
    protected void onResume() {

        super.onResume();

//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
        pause_connect = false;

        sHandler = new Handler();

        sHandler.post(mHideRunnable); // hide the navigation bar

        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
        {
            @Override
            public void onSystemUiVisibilityChange(int visibility)
            {
                sHandler.post(mHideRunnable); // hide the navigation bar
            }
        });

        if(mConnected) {
            //current_mode = 01;
            //startmode = 1;
            lockmode = false;

            Log.e("debuggg", "deviceresume");
//


            Intent gattServiceIntent = new Intent(DeviceActivity.this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());



            new Thread(battery_runnable).start();
        }

    }


    LocationListener locationListener =  new LocationListener() {

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("locationdebug", "disable");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("locationdebug", "enable");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("locationdebug", "status");
        }

        @Override
        public void onLocationChanged(Location location) {
            //如果位置发生变化,重新显示
            showLocation(location);

        }
    };

    private void showLocation(Location location){
        String locationStr = "维度：" + location.getLatitude() +"\n"
                + "经度：" + location.getLongitude();

        Log.e("locationdebug",""+locationStr);

        longitude = location.getLongitude();
        latitude = location.getLatitude();




    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    if(mBluetoothAdapter!=null)
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Log.e("device info", " " + device.getName() + " " + device.getAddress());

                            if (device.getName() != null) {
                                Log.e("dedebug new name","devicetype"+device.getType());
                                if (device.getName().toString().equals("SPECTRA")) {//BabyBluetoothStubOnOSX //HC-08 //Tv221u-169EAFDD //SPECTRA

                                    mDeviceAddress = device.getAddress();

                                    // Code to manage Service lifecycle.
                                    mServiceConnection = new ServiceConnection() {

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

                                            if (mBluetoothLeService != null) {
                                                final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                                                Log.e("device debug", "!@!Connect request result= " + result);

                                                mConnected = true; //
                                                //connecting_dialog.dismiss();

                                                //new Thread(battery_runnable).start();
                                            }



                                        }

                                        @Override
                                        public void onServiceDisconnected(ComponentName componentName) {
                                            Log.e("debugging","onServiceDisconnected");
                                            mBluetoothLeService = null;
                                        }
                                    };

                                    Intent gattServiceIntent = new Intent(DeviceActivity.this, BluetoothLeService.class);
                                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);



                                    if (mBluetoothLeService != null) {
                                        Log.e("@@device bugbug", " " + device.getName() + " " + device.getAddress());
                                        mBluetoothLeService.connect(mDeviceAddress);


//                                        device_connectbutton.setBackground(getResources().getDrawable(R.drawable.device_connectedbu));

//                                        connecting_dialog.dismiss();
//                                        mConnected = true;

                                        mScanning = false;
                                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                    }
                                }
                            }
                        }
                    });
                }
            };

//    // Code to manage Service lifecycle.
//    private ServiceConnection mServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            Log.e("dialog debug","???");
//            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//            if (!mBluetoothLeService.initialize()) {
//                Log.e("Device debug", "Unable to initialize Bluetooth");
//                finish();
//            }
//            // Automatically connects to the device upon successful start-up initialization.
//            //mBluetoothLeService.connect(mDeviceAddress);
//
//            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//            if (mBluetoothLeService != null) {
//                final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//                Log.e("device debug", "!@!Connect request result=" + result);
//
//                mConnected = true; //
//                //connecting_dialog.dismiss();
//
//                //new Thread(battery_runnable).start();
//            }
//
//
//
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            Log.e("debugging","onServiceDisconnected");
//            mBluetoothLeService = null;
//        }
//    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.e("deviceGattUpReceiver", "connected");

                mConnected = true;
                connecting_dialog.dismiss();
                device_connectbutton.setBackground(getResources().getDrawable(R.drawable.device_connectedbu));

                setConnected_Layout();

                SharedPreferences pref = context.getSharedPreferences("myActivityName", 0);

                if ((pref.getInt("isFirstIn", 0) < 0)) { //<4  <8

                    Intent tutintent = new Intent(DeviceActivity.this, TutVideoActivity.class);
                    startActivity(tutintent);

                }

                if (AVUser.getCurrentUser() != null) {

                    // upload location
                    AVObject locationobject = new AVObject("Odometer");
                    locationobject.put("fromUser", AVUser.getCurrentUser());
                    locationobject.put("type", "location");
                    locationobject.put("Today", longitude);
                    locationobject.put("Total", latitude);

                    locationobject.saveInBackground();
                }

                updateConnectionState(true);
                invalidateOptionsMenu();
            }
                else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {  // Lost Connection Testing

                mConnected = false;
                updateConnectionState(false);
                invalidateOptionsMenu();
                Log.e("deviceGattUpReceiver", "disconnected");

                device_connectbutton.setBackground(getResources().getDrawable(R.drawable.decive_disconnected));

                setdisConnected_Layout();

//                scanLeDevice(false);
//
//                unregisterReceiver(mGattUpdateReceiver);
//
//                //unbindService(mServiceConnection);
//
//                mBluetoothAdapter = null;
//
//                mServiceConnection = null;

                mBluetoothLeService.close();

//                onCreate(null);

                Log.e("record debug","  "+record_array);

                String filePath = "/sdcard/Test/";
                String fileName = "log.txt";

                writeTxtToFile(record_array.toString(), filePath, fileName);

                // Upload Odometer

                if(AVUser.getCurrentUser()!=null&&today_usage>0) {

                    AVQuery<AVObject> followquery = new AVQuery<>("Odometer");
                    followquery.whereEqualTo("type", "odometer");
                    followquery.whereEqualTo("fromUser", AVUser.getCurrentUser());


                    followquery.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list, AVException e) {

                            if (list != null) {
                                Log.e("odometer debug", "  " + list.size());
                                if (list.size() != 0) {
                                    AVObject todayRecord = list.get(0);

                                    Date dNow = new Date();
                                    Calendar calendar = Calendar.getInstance(); //得到日历
                                    calendar.setTime(dNow);//把当前时间赋给日历

                                    if (todayRecord.getCreatedAt().getTime() - calendar.getTime().getTime() < 24 * 3600 * 1000) {

                                        Log.e("odometer debug", "today save " + todayRecord.get("Today"));


                                        int today_use = (int) todayRecord.get("Today") + today_usage;
                                        todayRecord.put("Today", today_use);

                                        todayRecord.put("Total", (int) todayRecord.get("Total") + today_usage);

                                        todayRecord.saveInBackground();

                                    } else {

                                        AVObject todayRecord_new = new AVObject("Odometer");

                                        todayRecord_new.put("type", "odometer");
                                        todayRecord_new.put("fromUser", AVUser.getCurrentUser());

                                        Log.e("odometer debug", "today new");
                                        todayRecord_new.put("Today", today_usage);

                                        todayRecord_new.put("Total", (int) todayRecord.get("Total") + today_usage);

                                        todayRecord_new.saveInBackground();


                                    }
                                } else {
                                    Log.e("odometer debug", "total new");

                                    AVObject todayRecord = new AVObject("Odometer");
                                    todayRecord.put("fromUser", AVUser.getCurrentUser());
                                    todayRecord.put("type", "odometer");

                                    todayRecord.put("Today", today_usage);

                                    todayRecord.put("Total", today_usage);

                                    todayRecord.saveInBackground();
                                }
                            }
                        }

                    });

                }



                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.e("xiaomidebug","ACTION_GATT_SERVICES_DISCOVERED "+mBluetoothLeService.getSupportedGattServices());

                if(mBluetoothLeService!=null) {
                    Log.e("xiaomidebug1","ACTION_GATT_SERVICES_DISCOVERED "+mBluetoothLeService.getSupportedGattServices());
                    for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
                        Log.e("speedmodechconnected!!", " " + gattService);
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
                            Log.e("Device getUuid", " " + gattCharacteristic.getUuid());
                            if (gattCharacteristic.getUuid().toString().equals("0000ffe9-0000-1000-8000-00805f9b34fb")) {
                                //Log.e("Device done connected", " " + gattCharacteristic.getUuid().toString());
                                speedmodeChara = gattCharacteristic;
                            }
                        }
                    }
                }

                // lockmode; startmode


                byte array[] = {0x12,0x55,1,1,0,0,0,0,0,0,0,0};
                Log.e("speedmodech connected!", " " + speedmodeChara);

                if(speedmodeChara!=null){
                    speedmodeChara.setValue(array);
                    mBluetoothLeService.writeCharacteristic(speedmodeChara);

                    //mBluetoothLeService.readCharacteristic(mNotifyCharacteristic);
                }

                current_mode = 01;
                startmode = 1;
                lockmode = true;

                //displayGattServices(mBluetoothLeService.getSupportedGattServices());

                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

                short strength = intent.getExtras().getShort(
                        BluetoothDevice.EXTRA_RSSI);
            }
        }
    };

    private void updateConnectionState(final boolean i) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(i == true){


//                    if(mBluetoothLeService!=null) {
//                        Log.e("xiaomidebug1","ACTION_GATT_SERVICES_DISCOVERED "+mBluetoothLeService.getSupportedGattServices());
//                        for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
//                            Log.e("speedmodechconnected!!", " " + gattService);
//                            String uuid = null;
//                            String unknownServiceString = getResources().getString(R.string.unknown_service);
//                            String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
//                            HashMap<String, String> currentServiceData = new HashMap<String, String>();
//                            uuid = gattService.getUuid().toString();
//                            currentServiceData.put(
//                                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
//                            currentServiceData.put(LIST_UUID, uuid);
//                            //gattServiceData.add(currentServiceData);
//
//                            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
//                                    new ArrayList<HashMap<String, String>>();
//                            List<BluetoothGattCharacteristic> gattCharacteristics =
//                                    gattService.getCharacteristics();
//                            ArrayList<BluetoothGattCharacteristic> charas =
//                                    new ArrayList<BluetoothGattCharacteristic>();
//
//
//                            // Loops through available Characteristics.
//                            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                                Log.e("Device getUuid", " " + gattCharacteristic.getUuid());
//                                if (gattCharacteristic.getUuid().toString().equals("0000ffe9-0000-1000-8000-00805f9b34fb")) {
//                                    //Log.e("Device done connected", " " + gattCharacteristic.getUuid().toString());
//                                    speedmodeChara = gattCharacteristic;
//                                }
//                            }
//                        }
//                    }
//
//                   // lockmode; startmode
//
//
//                    byte array[] = {0x12,0x55,1,1,0,0,0,0,0,0,0,0};
//                    Log.e("speedmodech connected!", " " + speedmodeChara);
//
//                   if(speedmodeChara!=null){
//                        speedmodeChara.setValue(array);
//                        mBluetoothLeService.writeCharacteristic(speedmodeChara);
//
//                        //mBluetoothLeService.readCharacteristic(mNotifyCharacteristic);
//                    }
//
//                current_mode = 01;
//                startmode = 1;
//                lockmode = true;
//
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());

                }

                else {
                    device_connectbutton.setBackground(getResources().getDrawable(R.drawable.decive_disconnected));
                    mConnected = false;

                    setdisConnected_Layout();
                }
            }
        });
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            Log.e("xiaomidebug2","service "+gattService.getUuid());

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

                //Log.e("char scan", "character " + gattCharacteristic.getUuid());


                final int charaProp = gattCharacteristic.getProperties();

                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
//                    if (mNotifyCharacteristic != null) {
//                        mBluetoothLeService.setCharacteristicNotification(
//                                mNotifyCharacteristic, false);
//                        mNotifyCharacteristic = null;
//                    }

                    //Log.e("Device done", " " + gattCharacteristic.getValue()+" char "+ gattCharacteristic.getUuid());


                    //mBluetoothLeService.readCharacteristic(gattCharacteristic);



                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {

                    mNotifyCharacteristic = gattCharacteristic;
                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                }

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }




    }

    private void displayData(String data) {

        if (data != null) {
            //Log.e("what data", ""+data);

            //String real_data = data.substring(data.length()-13,data.length()-1);

            String real_data = data.substring(13,data.length()-1);

            //Log.e("what data000","  "+real_data);


            //try {

            //byte[] sendBytes= real_data.getBytes();//data.getBytes("UTF8")

            hexStringToByte(real_data);

            byte sendBytes[] = {0x12,0x55,1,1,0,0,0,0,0,0,0,0};


            if(selfcheck_dialog.isShowing()){

                sendBytes[0] = 0x13;
                sendBytes[1] = 0x04;
                sendBytes[2] = 0;
                sendBytes[3] = 0;

            }
            else{

                sendBytes[0] = 0x12;
                sendBytes[1] = 0x55;

                sendBytes[2] = (byte) startmode;
                sendBytes[3] = (byte) current_mode;

            }

            Log.e("what data111"," device "+real_data.substring(15, 17));

            //Log.e("what data", ""+real_data);

            //Log.e("what data", "" + sendBytes.length + "mode " + startmode + " ss " + current_mode);

//            for(int u=0;u<real_data.length();u++)
//                Log.e("what data333","  "+sendB[u]);

                battery_percent_text.setText(testdata1+"");
                battery_time_text.setText(testdata2+"");

//                battery_percent_text.setText(+batteryremain+" %");
//
//                battery_time_text.setText((int)(batteryremain*72/100)+" Min");

                if(examvalue>0){
                    Log.e("exam","examvalue = "+examvalue);
                    //rounding_text.setText("Abnormality detected ("+ examvalue +"). Tap for support.");
                    device_product.setBackground(getResources().getDrawable(R.drawable.device_product_abnormality));

                    switch (examvalue){

                        case 1:
                        case 2:
                            rounding_text.setText("Sensor abnormality detected ("+ examvalue +"). Tap for support.");
                            break;

                        case 4:
                        case 8:
                            rounding_text.setText("Motor abnormality detected ("+ examvalue +"). Tap for support.");
                            break;
                        case 16:
                            rounding_text.setText("Low battery level detected ("+ examvalue +"). Tap for support.");
                            break;
                        case 32:
                            rounding_text.setText("Battery abnormality detected ("+ examvalue +"). Tap for support.");
                            break;
                        case 64:
                            rounding_text.setText("IMU abnormality detected ("+ examvalue +"). Tap for support.");
                            break;

                    }


                }
//                else {
//                    lockstatus.setBackground(getResources().getDrawable(R.drawable.unlockstatus));
//                    device_product.setBackground(getResources().getDrawable(R.drawable.device_product));
//
//                    lockmode = false;
//
//                    rounding_text.setText("Tap the SPECTRA for self-test");
//                }

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
                                Log.e("Device write done", " " + gattCharacteristic.getUuid());
                                speedmodeChara = gattCharacteristic;
                            }
                        }
                    }
                }

//                sendBytes[0] = 0x12;
//                sendBytes[1] = 0x55;
//
//                sendBytes[2] = (byte) startmode;
//                sendBytes[3] = (byte) current_mode;

                Log.e("what data111"," tutvideo111 "+sendBytes[3]+"  "+sendBytes[2]);
                //sendBytes[8] = (byte) trainmode;

                // Text Debug
                //rounding_text.setText("device mode "+sendBytes[2]+", "+sendBytes[3]);

//                rounding_text.setText("Abnormality detected. Tap for support.");
//                device_product.setBackground(getResources().getDrawable(R.drawable.device_product_abnormality));

//                sensor_value

                if(lock_signal) {

                    if (lockmode){

                        sendBytes[0] = 0x11;
                        sendBytes[1] = 0x02;

                        Log.e("password setup", "current user: "+AVUser.getCurrentUser().getObjectId());
                        hexStringToPassword(AVUser.getCurrentUser().getObjectId());

                        for(int i=2;i<12;i++){

                            sendBytes[i] = passwordArray[i-2];

                        }

                    }
                    else{
                        sendBytes[0] = 0x11;
                        sendBytes[1] = 0x03;

                        Log.e("password setup", "current user: "+AVUser.getCurrentUser().getObjectId());
                        hexStringToPassword(AVUser.getCurrentUser().getObjectId());

                        for(int i=2;i<12;i++){

                            sendBytes[i] = passwordArray[i-2];

                        }

                    }
                    //byte array[] = {0,03,0,0,0};
                }

                // lockmode; startmode

                today_usage = odometer_range;// obtain today_usage;

                //sendBytes[3] = (byte) (sendBytes[3] + 1);
                //Log.e("speedmodech", " " + speedmodeChara);

                if(speedmodeChara!=null){
                    speedmodeChara.setValue(sendBytes);
                    mBluetoothLeService.writeCharacteristic(speedmodeChara);

                    //mBluetoothLeService.readCharacteristic(mNotifyCharacteristic);
                }




//                for(int i=0;i<sendBytes.length;i++){
//
//                    //Log.e("what",""+sendBytes[i]);
//                }





            //mDataField.setText(data);
        }
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    public void hexStringToPassword(String hex) {


        int len = 10;
        passwordArray = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            passwordArray[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
            Log.e("password setup", "result: "+passwordArray[i]);
        }
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



        if(result[0]==0x23){
            sensorValue0 = result[2];
            sensorValue1 = result[3];
            sensorValue2 = result[4];
            sensorValue3 = result[5];
            sensorValue4 = result[6];
        }
        else {

            sensorValue0 = 49;
            sensorValue1 = 49;
            sensorValue2 = 49;
            sensorValue3 = 49;
            sensorValue4 = 49;
        }

        Log.e("spectra_test",+result[0]+"  "+result[1]+"  "+result[2]+"  "+result[3]+"  "+result[4]+"  "+result[5]+"  "+result[6]+"  "
                +result[7]+"  "+result[8]);

        if(sensorValue0>100){
            Log.e("spectra_test","sensor1 change");
            sensor_deck1.setBackground(getResources().getDrawable(R.drawable.sensor_deck1_s));
        }
        else {
            //sensor_deck1.setBackground(getResources().getDrawable(R.drawable.sensor_deck1));
        }

        if(sensorValue1>100){
            sensor_deck2.setBackground(getResources().getDrawable(R.drawable.sensor_deck5_s));
        }
        else {
            //sensor_deck2.setBackground(getResources().getDrawable(R.drawable.sensor_deck2));
        }

        if(sensorValue2>100){
            sensor_deck3.setBackground(getResources().getDrawable(R.drawable.sensor_deck2_s));
        }
        else {
            //sensor_deck3.setBackground(getResources().getDrawable(R.drawable.sensor_deck3));
        }

        if(sensorValue3>100){
            sensor_deck4.setBackground(getResources().getDrawable(R.drawable.sensor_deck4_s));
        }
        else {
            //sensor_deck4.setBackground(getResources().getDrawable(R.drawable.sensor_deck4));
        }


        if(sensorValue4>100){
            sensor_deck5.setBackground(getResources().getDrawable(R.drawable.sensor_deck3_s));
        }
        else {
            //sensor_deck5.setBackground(getResources().getDrawable(R.drawable.sensor_deck5));
        }

        batteryremain = result[5];

        odometer_range = result[6];

        examvalue = result[4];


        testdata1 = result[5];
        testdata2 = result[6];

        record_array.add(+testdata1+"||"+testdata2);

        if(result[0]==0x21){

            lock_signal = false;
            Log.e("password setup","lock signal false");

            if(result[2]==0x01){
                Log.e("password setup","lock/unlock true");

            }
            else {
                Log.e("password setup","lock/unlock false");

            }

        }

        //odometer_range = 100;

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

//    public void sendspeed(int left,int right){
//
//        for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
//            String uuid = null;
//            String unknownServiceString = getResources().getString(R.string.unknown_service);
//            String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
//            HashMap<String, String> currentServiceData = new HashMap<String, String>();
//            uuid = gattService.getUuid().toString();
//            currentServiceData.put(
//                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
//            currentServiceData.put(LIST_UUID, uuid);
//            //gattServiceData.add(currentServiceData);
//
//            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
//                    new ArrayList<HashMap<String, String>>();
//            List<BluetoothGattCharacteristic> gattCharacteristics =
//                    gattService.getCharacteristics();
//            ArrayList<BluetoothGattCharacteristic> charas =
//                    new ArrayList<BluetoothGattCharacteristic>();
//
//            // Loops through available Characteristics.
//            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                Log.e("Device debugChar"," "+gattCharacteristic.getUuid());
//                if(gattCharacteristic.getUuid().toString().equals("0000ffe1-0000-1000-8000-00805f9b34fb")){
//                    Log.e("Device done"," "+gattCharacteristic.getUuid());
//                    speedmodeChara = gattCharacteristic;
//                }
//            }
//        }
//
//        byte array[] = {0,04,(byte)left,(byte)right,0};
//
//        if(speedmodeChara!=null){
//            speedmodeChara.setValue(array);
//            mBluetoothLeService.writeCharacteristic(speedmodeChara);
//        }
//
//    }


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

                            // Loops through available Characteristics.
                            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                                //Log.e("Device debug", " " + gattCharacteristic.getUuid());
                                if (gattCharacteristic.getUuid().toString().equals("0000ffe9-0000-1000-8000-00805f9b34fb")) {
                                    Log.e("???Device done", " " + gattCharacteristic.getUuid().toString());
                                    speedmodeChara = gattCharacteristic;
                                }
                            }
                        }
                    }

                    byte array[] = {0x12,0x55,1,1,0,0,0,0,0,0,0,0};  // mode 4

                    array[2] = (byte)startmode;

                    array[3] = (byte)current_mode;
//                    current_mode = 01;
//                    startmode = 1;
                    lockmode = false;


                    Log.e("???speedmodech", " " + speedmodeChara);
                    if (speedmodeChara != null) {
                        speedmodeChara.setValue(array);
                        mBluetoothLeService.writeCharacteristic(speedmodeChara);

                        mBluetoothLeService.readCharacteristic(speedmodeChara);
                    }
                }
                catch (InterruptedException e) {

                }

            }

         //}

    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length >= 1) {
                int cameraResult = grantResults[0];//相机权限
                boolean cameraGranted = cameraResult == PackageManager.PERMISSION_GRANTED;//拍照权限
                if (cameraGranted) {

                    Log.e("cameradebug1","checkSelfPermission");

                } else {
                    //不具有相关权限，给予用户提醒，比如Toast或者对话框，让用户去系统设置-应用管理里把相关权限开启
                }
            }
        }
    }


    private void setConnected_Layout(){
        device_remind.setVisibility(View.VISIBLE);
        device_middlelayout.setVisibility(View.VISIBLE);
        device_bottomlayout.setVisibility(View.VISIBLE);

        lockstatus.setVisibility(View.VISIBLE);

        diconnnected_layout.setVisibility(View.GONE);

    }

    private void setdisConnected_Layout(){
        Log.e("strange","setdisConnected_Layout??");

        device_remind.setVisibility(View.GONE);
        device_middlelayout.setVisibility(View.GONE);
        device_bottomlayout.setVisibility(View.INVISIBLE);

        lockstatus.setVisibility(View.GONE);

        diconnnected_layout.setVisibility(View.VISIBLE);

        Log.e("strange", "setdisConnected_Layout!!");

    }



//    // 判断设备是否有返回键、菜单键来确定是否有 NavigationBar
//    public static boolean hasNavigationBar(Context context) {
//        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
//        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
//        if (!hasMenuKey && !hasBackKey) {
//            return true;
//        }
//        return false;
//    }
//
//    // 获取 NavigationBar 的高度
//    public static int getNavigationBarHeight(Activity activity) {
//        Resources resources = activity.getResources();
//        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
//        return resources.getDimensionPixelSize(resourceId);
//    }

    // 将字符串写入到文本文件中
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath+fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    // 生成文件
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }

    }


    @Override  // override backpressed
    public void onBackPressed() {

    }

}


// 0000ffe0-0000-1000-8000-00805f9b34fb      0000ffe4-0000-1000-8000-00805f9b34fb

// 0000ffe5-0000-1000-8000-00805f9b34fb      0000ffe9-0000-1000-8000-00805f9b34fb