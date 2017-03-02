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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.VideoView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by android on 17/11/16.
 */
public class TutVideoActivity extends Activity {

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    VideoView videoview;

    MediaPlayer player;

    public List<AVObject> postobjectArray;

    ImageButton replayButton;
    ImageButton trainButton;
    ImageButton continueButton;

    ImageButton startButton;
    ImageButton skipButton;

    private int tut_number;

    private int train_number;

    private Boolean train_status;

    private int current_mode;

    private int start_mode;

    private BluetoothLeService mBluetoothLeService;

    private BluetoothGattCharacteristic speedmodeChara = null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tutvideo);

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("myActivityName", 0);

        Log.e("tut debug", ">>>>>");

        tut_number = 0;  //pref.getInt("isFirstIn",0);
        current_mode = 1;

        train_status = false;

        videoview = (VideoView)findViewById(R.id.tut_videoview);
        ViewGroup.LayoutParams para_video;
        para_video = videoview.getLayoutParams();
        WindowManager wm2 = this.getWindowManager();
        int width2 = wm2.getDefaultDisplay().getWidth();
        int height2 = wm2.getDefaultDisplay().getHeight();

        para_video.height = width2/16*10;
        para_video.width = width2;

        videoview.setLayoutParams(para_video);

        AVQuery<AVObject> followquery = new AVQuery<>("Tutorial_Video");

        followquery.orderByDescending("createdAt");

        Log.e("tut debug", ">>>>>1");

        Intent gattServiceIntent = new Intent(TutVideoActivity.this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


        new Thread(battery_runnable).start();

       followquery.findInBackground(new FindCallback<AVObject>() {
           @Override
           public void done(List<AVObject> list, AVException e) {

               Log.e("tut debug","???? "+"22 "+e);
               Log.e("tut debug","???? "+list);

               if (list != null) {

                       postobjectArray = list;

                       AVFile postVideo = postobjectArray.get(0).getAVFile("Video");
                       Uri uri = Uri.parse(postVideo.getUrl());
                       //holder.videoView.setMediaController(new MediaController(getActivity()));

                       Log.e("tut debug","uri "+uri.toString()+" count: "+postobjectArray.size());

                       videoview.setVideoURI(uri);

                       replayButton.setVisibility(View.GONE);
                       trainButton.setVisibility(View.GONE);
                       continueButton.setVisibility(View.GONE);

                       startButton.setVisibility(View.GONE);
                       skipButton.setVisibility(View.GONE);

                       videoview.start();

               }

           }
       });




//        AVFile postVideo = postobjectArray.get(pref.getInt("isFirstIn",0)).getAVFile("video");
//        Uri uri = Uri.parse(postVideo.getUrl());
//        //holder.videoView.setMediaController(new MediaController(getActivity()));
//
//        videoview.setVideoURI(uri);

       videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
           @Override
           public void onCompletion (MediaPlayer mp){

               train_status = false;

               if(tut_number==0){

                   startButton.setVisibility(View.VISIBLE);
                   skipButton.setVisibility(View.VISIBLE);


               }
               else {
                   replayButton.setVisibility(View.VISIBLE);
                   trainButton.setVisibility(View.VISIBLE);
                   continueButton.setVisibility(View.VISIBLE);

                   continueButton.setBackground(getResources().getDrawable(R.drawable.tut_continuebutton1));

                   trainButton.setBackground(getResources().getDrawable(R.drawable.tut_trainbutton));

               }

           }
       }

       );

       replayButton=(ImageButton) findViewById(R.id.replayButton);

       trainButton=(ImageButton) findViewById(R.id.trainButton);

       continueButton=(ImageButton) findViewById(R.id.continueButton);

       startButton = (ImageButton) findViewById(R.id.startButton);

       skipButton = (ImageButton) findViewById(R.id.skipButton);

       start_mode = 1;

       startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tut_number = 1;
                //current_mode = 4;
                start_mode = 4;
                AVFile postVideo = postobjectArray.get(tut_number).getAVFile("Video");
                Uri uri = Uri.parse(postVideo.getUrl());
                //holder.videoView.setMediaController(new MediaController(getActivity()));

                videoview.setVideoURI(uri);

                startButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.GONE);
                //continueButton.setVisibility(View.GONE);

                videoview.start();// start train mode
            }
        });

       skipButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("isFirstIn", 4);
                editor.commit();
                TutVideoActivity.this.finish();
           }
       });

       replayButton.setOnClickListener(
               new View.OnClickListener() {
           @Override
           public void onClick (View v){

           videoview.start();

           replayButton.setVisibility(View.GONE);
           trainButton.setVisibility(View.GONE);
           continueButton.setVisibility(View.GONE);

       }
       }

       );

       trainButton.setOnClickListener(
               new View.OnClickListener() {
           @Override
           public void onClick (View v){

               train_status = false;
               train_number = tut_number;

               trainButton.setBackground(getResources().getDrawable(R.drawable.tut_trainbutton1));
       }
       }

       );

       continueButton.setOnClickListener(
               new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {

                       if (train_status) {

                           if (tut_number < 4) {
                               tut_number++;

                               AVFile postVideo = postobjectArray.get(tut_number).getAVFile("Video");
                               Uri uri = Uri.parse(postVideo.getUrl());
                               //holder.videoView.setMediaController(new MediaController(getActivity()));

                               videoview.setVideoURI(uri);

                               replayButton.setVisibility(View.GONE);
                               trainButton.setVisibility(View.GONE);
                               continueButton.setVisibility(View.GONE);

                               videoview.start();

                           } else {
                               //tut_number = 0;
                               start_mode = 1;
                               TutVideoActivity.this.finish();

                               // resume normal mode
                           }

                           SharedPreferences.Editor editor = pref.edit();
                           editor.putInt("isFirstIn", tut_number);
                           editor.commit();


                       }
                   }
               }

       );


   }

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
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void displayData(String data) {

        if (data != null) {

            String real_data = data.substring(data.indexOf("22"),data.length()-1);

            //Log.e("what data", " remote! "+real_data);
            //Log.e("what data", " remote! "+real_data.length());
            //Log.e("what data", " remote! "+v_left+"   "+v_right);

            hexStringToByte(real_data);





            //try {
            //byte[] sendBytes= real_data.getBytes("UTF8");//data.getBytes("UTF8")


            //remain_battery.setText(+batteryremain+" %");

            //battery_progress.setProgress(batteryremain);


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
            byte sendBytes[] = {0x12,0x55,1,1,0,0,0,0,0,0,0,0};

            //byte array[] = {0,03,0,0,0};
            sendBytes[0] = 0x12;
            sendBytes[1] = 0x55;

            sendBytes[2] = (byte) start_mode;


            sendBytes[3] = (byte) 1;//current_mode

            sendBytes[7] = (byte) train_number;//tut_number

            Log.e("tutvideo debug"," tutvideo "+sendBytes[7]+"  "+sendBytes[2]+" "+start_mode);

            //sendBytes[3] = (byte) current_mode;

            //sendBytes[7] = (byte) current_mode;

            sendBytes[5] = (byte) 0;

            sendBytes[6] = (byte) 0;

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

        if(result[7]==1){

            train_status = true;
            continueButton.setBackground(getResources().getDrawable(R.drawable.tut_continuebutton0));

            trainButton.setBackground(getResources().getDrawable(R.drawable.tut_trainbutton2));

            train_number = 0;
        }
        //batteryremain = result[5];

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

                byte array[] = {0x12,0x55,1,1,0,0,0,0,0,0,0,0};  // mode 4

                //current_mode = 01;


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

//    @Override  // override backpressed
//    public void onBackPressed() {
//
//
//    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {

        super.onPause();

        //unbindService(mServiceConnection);
        Log.e("tut debug", " tut onpause");

        unregisterReceiver(mGattUpdateReceiver);

        //unbindService(mServiceConnection);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.e("str debug", " remote ondestroy");

        unbindService(mServiceConnection);
    }


}
