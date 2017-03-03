package maojian.android.walnut;

/**
 * Created by android on 19/7/16.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

@SuppressLint("NewApi")
public class Me extends Fragment
{
    private MapView mapView;
    private MapboxMap map;

//    private MapView mapView1;
//    private MapboxMap map1;

    private LocationServices locationServices;

    AVUser currentUser;

    //ScrollView scrollView;

    ScrollviewCompat scrollView;

    LinearLayout l1;
    LinearLayout l2;

    private ImageView currentprofileimage;

    TextView viewpager1_postlabel;

    private TextView usernameLabel;
    private TextView historyLabel;
    private TextView fansLabel;
    private TextView followingLabel;

    private Button editprofile_button;

    private TextView me_fansbutton;
    private TextView me_followingbutton;
    private TextView me_historybutton;

    private GridView listView;

    private List<AVObject> PostImageCount;

    private List<AVObject> odometerObjectsArray;

    private List<AVObject> locationObjectsArray;

    private List<AVObject> likePostImageCount;

    private final String[] postUrl = new String[40];

    private final String[] likeUrl = new String[100];

    private ImageAdapter discoverpostadapter;

    View rootView;

    private ViewPagerCompat viewPager;//页卡内容
    private ImageView imageView;// 动画图片
    private TextView textView1,textView2;
    private List<View> views;// Tab页面列表
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
    private View view1,view2;//各个页卡

    private ImageButton order_button;
    private ImageButton discount_button;

    //private ImageView viewpager_cursor;

    private ImageButton multi_post;
    private ImageButton single_post;
    private ImageButton like_post;

    private TextView post_text;
    private TextView mark_text;

    private boolean islike_post;

    private  ArrayList<PointValue> values;

    private  ArrayList<PointValue> existing_usage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_me, container, false);

        PostImageCount = new ArrayList<AVObject>();

        currentprofileimage = (ImageView) rootView.findViewById(R.id.me_profileimage);

        usernameLabel = (TextView) rootView.findViewById(R.id.me_username);
        //historyLabel = (TextView) rootView.findViewById(R.id.me_historylabel);
        fansLabel = (TextView) rootView.findViewById(R.id.me_fanslabel);
        followingLabel = (TextView) rootView.findViewById(R.id.me_followinglabel);

        Typeface face1 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Brown-Regular.otf");
        usernameLabel.setTypeface(face1);
        //historyLabel.setTypeface(face1);
        fansLabel.setTypeface(face1);
        followingLabel.setTypeface(face1);

        islike_post = false;
        //viewpager_cursor = (ImageView) rootView.findViewById(R.id.viewpager_cursor);
        currentUser = AVUser.getCurrentUser();
        queryParseMethod();

        InitImageView();

        InitViewPager();
        InitTextView();

        listView = (GridView) view1.findViewById(R.id.viewpager1_gridView);

        viewpager1_postlabel = (TextView) view1.findViewById(R.id.viewpager1_postlabel);

        Typeface postlabelface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Brown-Regular.otf");
        viewpager1_postlabel.setTypeface(postlabelface);

        multi_post = (ImageButton) view1.findViewById(R.id.multi_postbutton);
        single_post = (ImageButton) view1.findViewById(R.id.single_postbutton);
        like_post = (ImageButton) view1.findViewById(R.id.like_postbutton);

        multi_post.setBackground(getResources().getDrawable(R.drawable.multi_post1));
        single_post.setBackground(getResources().getDrawable(R.drawable.single_post));
        like_post.setBackground(getResources().getDrawable(R.drawable.like_post));

        multi_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setNumColumns(2);
                islike_post = false;

                multi_post.setBackground(getResources().getDrawable(R.drawable.multi_post1));
                single_post.setBackground(getResources().getDrawable(R.drawable.single_post));
                like_post.setBackground(getResources().getDrawable(R.drawable.like_post));

                viewpager1_postlabel.setText("My Post");
                discoverpostadapter.notifyDataSetChanged();
            }
        });

        single_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setNumColumns(1);
                islike_post = false;

                multi_post.setBackground(getResources().getDrawable(R.drawable.multi_post));
                single_post.setBackground(getResources().getDrawable(R.drawable.single_post1));
                like_post.setBackground(getResources().getDrawable(R.drawable.like_post));

                viewpager1_postlabel.setText("My Post");
                discoverpostadapter.notifyDataSetChanged();
            }
        });

        like_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                islike_post = true;
                listView.setNumColumns(1);

                multi_post.setBackground(getResources().getDrawable(R.drawable.multi_post));
                single_post.setBackground(getResources().getDrawable(R.drawable.single_post));
                like_post.setBackground(getResources().getDrawable(R.drawable.like_post1));

                viewpager1_postlabel.setText("Post You've liked");
                discoverpostadapter.notifyDataSetChanged();
            }
        });

        editprofile_button = (Button) rootView.findViewById(R.id.editprofile_button);

        editprofile_button.setTypeface(face1);

        editprofile_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(getActivity(), EditprofileActivity.class);
                        startActivity(intent);

                    }
                }
        );


        post_text = (TextView) rootView.findViewById(R.id.post_tab);
        mark_text = (TextView) rootView.findViewById(R.id.mark_tab);

        post_text.setTextColor(Color.parseColor("#00EAAB"));
        mark_text.setTextColor(Color.parseColor("#A2A2A2"));





        //me_historybutton = (TextView) rootView.findViewById(R.id.me_historybutton);

        me_fansbutton = (TextView) rootView.findViewById(R.id.me_fansbutton);
        me_fansbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(getActivity(), ProfileFollowActivity.class);
                        intent.putExtra("type", "fans");
                        intent.putExtra("ObjectId", AVUser.getCurrentUser().getObjectId());
                        startActivity(intent);

                    }
                }
        );

        me_followingbutton = (TextView) rootView.findViewById(R.id.me_followingbutton);
        me_followingbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ProfileFollowActivity.class);
                        intent.putExtra("type", "following");
                        intent.putExtra("ObjectId", AVUser.getCurrentUser().getObjectId());
                        startActivity(intent);

                    }
                }
        );



        //me_historybutton.setTypeface(face1);
        me_fansbutton.setTypeface(face1);
        me_followingbutton.setTypeface(face1);

//        scrollView = (ScrollView) view2.findViewById(R.id.me_scrollView2);
//        scrollView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                Log.e("map debug", "onTouch scrollView");
//                if(v.getClass().getName().equals("com.mapbox.mapboxsdk.maps.MapView")) {
//
//                    Log.e("map debug","ViewPagerCompat canScroll");
//
//                    scrollView.requestDisallowInterceptTouchEvent(true);
//
//                    return true;
//                }
//
//                return false;
//            }
//        });

        //scrollView.requestDisallowInterceptTouchEvent(false);

        locationServices = LocationServices.getLocationServices(getActivity());
        mapView = (MapView) view2.findViewById(R.id.mapView);


        l1 = (LinearLayout) view2.findViewById(R.id.viewpager_lay2_l1);
        l2 = (LinearLayout) view2.findViewById(R.id.viewpager_lay2_l2);

//        mapView.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                Log.e("map debug", "onTouch mapview");
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    Log.e("map debug", "action_up mapview");
//                    scrollView.requestDisallowInterceptTouchEvent(false);
//                } else {
//                    Log.e("map debug", "action_up else mapview");
//
////                    l1.requestDisallowInterceptTouchEvent(true);
////                    l2.requestDisallowInterceptTouchEvent(true);
////                    viewPager.requestDisallowInterceptTouchEvent(true);
////                    scrollView.requestDisallowInterceptTouchEvent(true);
//                    //mapView.requestDisallowInterceptTouchEvent(false);
//                }
//                return true;
//            }
//        });

//        mapView1 = (MapView) rootView.findViewById(R.id.mapView1);
//        mapView1.onCreate(savedInstanceState);
//        mapView1.getMapAsync(new OnMapReadyCallback() {
//            @Override
//            public void onMapReady(MapboxMap mapboxMap) {
//
//                // Customize map with markers, polylines, etc.
//
//            }
//        });



        //mapView

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                map = mapboxMap;
                enableGps();

                // Enable user tracking to show the padding affect.
                map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
                map.getTrackingSettings().setDismissAllTrackingOnGesture(true);

                // Customize the user location icon using the getMyLocationViewSettings object.
                map.getMyLocationViewSettings().setPadding(0, 500, 0, 0);
                map.getMyLocationViewSettings().setForegroundTintColor(Color.parseColor("#56B881"));
                map.getMyLocationViewSettings().setAccuracyTintColor(Color.parseColor("#FBB03B"));



                AVQuery<AVObject> fansquery = new AVQuery<>("Odometer");
                fansquery.whereEqualTo("type", "location");
                fansquery.whereEqualTo("fromUser", currentUser);


                fansquery.findInBackground(new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> list, AVException e) {

                        if (list != null) {

                            Log.e("location debug","list size:"+list.size());

                            for (int i = 0; i < list.size(); i++) {

                                String latitude = list.get(i).get("Today").toString();
                                String longitude = list.get(i).get("Total").toString();



                                Log.e("location debug", ">>  " + Double.valueOf(latitude) + "   " + Double.valueOf(longitude));




                                map.addMarker(new MarkerOptions()
                                        .position(new LatLng(Double.valueOf(longitude),Double.valueOf(latitude)))
                                        .title("Hello World!")
                                        .snippet("Welcome to my marker."));
//                                map.addMarker(new MarkerOptions()
//                                        .position(new LatLng(48.13863, 11.57603))
//                                        .title("Hello World!")
//                                        .snippet("Welcome to my marker."));

                            }


                        }

                    }
                });



            }
        });




        return rootView;

    }



    public void queryParseMethod (){

        Log.e("main bug", "!!! " + currentUser.get("profileImage").toString());

        if(currentUser.get("profileImage").toString().contains("AVObject")){

            AVObject currentobject = (AVObject) currentUser.get("profileImage");

            AVQuery<AVObject> fansquery = new AVQuery<>("_File");
            fansquery.whereEqualTo("objectId",currentobject.getObjectId());



        }
        else {

            AVFile currentProfile = (AVFile) currentUser.get("profileImage");
            Log.e("main bug", "??? " + currentUser.getUsername());

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .displayer(new CircleBitmapDisplayer(Color.WHITE, 5))
                    .build();

            ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
            ImageLoader.getInstance().displayImage(currentProfile.getUrl(), currentprofileimage, options, animateFirstListener);

        }

        usernameLabel.setText(currentUser.getUsername());

        currentUser.fetchInBackground(new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                Log.e("biodebug", "debugging" + currentUser.get("bio"));

                TextView me_bio_info = (TextView) rootView.findViewById(R.id.profile_bio);
                if (getActivity()!=null&&getActivity().getAssets()!=null) {
                    Typeface face1 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Brown-Regular.otf");
                    me_bio_info.setTypeface(face1);
                }
                if (currentUser.get("bio") != null) {
                    me_bio_info.setText(currentUser.get("bio") + "");
                }
            }
        });



        AVQuery<AVObject> fansquery = new AVQuery<>("Activity");
        fansquery.whereEqualTo("type", "follow");
        fansquery.whereEqualTo("toUser", currentUser);


        fansquery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if (list != null) {
                    Log.e("abc", "1??? " + currentUser.getUsername() + "  " + list.size());
                    Integer x = list.size();
                    fansLabel.setText(x.toString());
                }

            }
        });



        AVQuery<AVObject> followquery = new AVQuery<>("Activity");
        followquery.whereEqualTo("type", "follow");
        followquery.whereEqualTo("fromUser", currentUser);

        followquery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if (list != null) {
                    Log.e("abc", "2??? " + currentUser.getUsername() + "  " + list.size());
                    Integer y = list.size();
                    followingLabel.setText(y.toString());
                }

            }
        });

        AVQuery<AVObject> historyquery = new AVQuery<>("Photo");
        historyquery.whereEqualTo("user", currentUser);
        historyquery.orderByDescending("createdAt");


        historyquery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if (list != null) {
                    Log.e("abc", "3??? " + currentUser.getUsername() + "  " + list.size());
                    Integer z = list.size();
                    //historyLabel.setText(z.toString());

                    PostImageCount = list;
                    for (int i = 0; i < list.size(); i++) {

                        final AVObject post = (AVObject) list.get(i);
                        Log.e("abc", "strange2 " + i);
                        AVFile postImage = post.getAVFile("image");
                        Log.e("abc", "strange3 " + i);

                        postUrl[i] = postImage.getUrl();

                    }
                    if (getActivity()==null) return;
                    discoverpostadapter = new ImageAdapter(getActivity());
                    ((GridView) listView).setAdapter(discoverpostadapter);
                    // discoverpostadapter.notifyDataSetChanged();

                }

            }
        });

        AVQuery<AVObject> likepostquery = new AVQuery<>("Activity");
        likepostquery.whereEqualTo("fromUser", currentUser);
        likepostquery.whereEqualTo("type", "like");
        likepostquery.include("photo");
        likepostquery.orderByDescending("createdAt");

        likepostquery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if (list != null) {
                    Log.e("abc", "3??? " + currentUser.getUsername() + "  " + list.size());
                    //Integer z = list.size();
                    //historyLabel.setText(z.toString());

                    likePostImageCount = list;
                    for (int i = 0; i < list.size(); i++) {

                        final AVObject post = (AVObject) list.get(i).getAVObject("photo");
                        Log.e("abclike", "strange!!! " + i+"  "+list.get(i));

                        Log.e("abclike", "strange3!! " + post);

                        AVFile postImage = post.getAVFile("image");


                        likeUrl[i] = postImage.getUrl();

                    }
//                    discoverpostadapter = new ImageAdapter(getActivity());
//                    ((GridView) listView).setAdapter(discoverpostadapter);
                    // discoverpostadapter.notifyDataSetChanged();

                }

            }
        });


    }

    private void InitViewPager() {
//        MapboxAccountManager.start(getActivity(), "pk.eyJ1IjoiZHJhZ21lcGx6IiwiYSI6ImNpaWp6eDEweTAxOTF0cGtwZmwwaDhmcXMifQ.QMz7SFg6hGGmfo48w6eC8Q");

        viewPager=(ViewPagerCompat) rootView.findViewById(R.id.viewpager);
        views=new ArrayList<View>();
        LayoutInflater inflater=getActivity().getLayoutInflater();
        view1=inflater.inflate(R.layout.viewpager_lay1, null);
        view2=inflater.inflate(R.layout.viewpager_lay2, null);
        views.add(view1);
        views.add(view2);
        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());

//        viewPager.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                Log.e("map debug", "onTouch viewpager");
//
//                return false;
//            }
//        });

        final TextView today_text = (TextView) view2.findViewById(R.id.today_textview);
        final TextView total_text = (TextView) view2.findViewById(R.id.total_textview);

//        LineView lineView = (LineView)view2.findViewById(R.id.line_view);
//        lineView.setDrawDotLine(false); //optional
//        lineView.setShowPopup(LineView.SHOW_POPUPS_NONE); //optional
//
//        ArrayList<String> test = new ArrayList<String>();
//        for (int i=0; i<17; i++){
//            test.add(String.valueOf(i+1));
//        }
//
//        ArrayList<Integer> dataList = new ArrayList<Integer>();
//        int random = (int)(Math.random()*20+1);
//        for (int i=0; i<17; i++){
//            dataList.add((int)(Math.random()*random));
//        }
//
//        lineView.setBottomTextList(test);
//
//
//
//        ArrayList<ArrayList<Integer>> dataLists = new ArrayList<ArrayList<Integer>>();
//        dataLists.add(dataList);
//        lineView.setDataList(dataLists);

        int day_range = 7;

        values= new ArrayList<PointValue>();

//        values.add(new PointValue(0, 2));
//        values.add(new PointValue(1, 4));
//        values.add(new PointValue(2, 3));
//        values.add(new PointValue(3, (float) 2.7));
//        values.add(new PointValue(4, (float) 3.4));
//        values.add(new PointValue(5, (float) 1.4));
//        values.add(new PointValue(6, (float) 2.6));
//        values.add(new PointValue(0, (float)0.0));
//        values.add(new PointValue(1, (float)0.0));
//        values.add(new PointValue(2, (float)0.0));
//        values.add(new PointValue(3, (float)0.0));
//        values.add(new PointValue(4, (float)0.0));
//        values.add(new PointValue(5, (float)0.0));
//        values.add(new PointValue(6, (float)0.0));

        //In most cased you can call data model methods in builder-pattern-like manner.
        Line line = new Line(values).setColor(R.color.lightgray).setCubic(true);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        List<String> date_string_array = new ArrayList<String>();

        Date dNow = new Date();   //当前时间
        Date dBefore = new Date();
        Calendar calendar = Calendar.getInstance(); //得到日历
        calendar.setTime(dNow);//把当前时间赋给日历

        SimpleDateFormat formatter = new SimpleDateFormat("M.d");
        String dateString = formatter.format(dNow);

        List<AxisValue> axisValues = new ArrayList<AxisValue>();

        existing_usage= new ArrayList<PointValue>();

        AVQuery<AVObject> odometerquery = new AVQuery<>("Odometer");

        Log.e("historyusage","debugging "+currentUser.get("username"));
        odometerquery.whereEqualTo("fromUser", currentUser);
        odometerquery.whereEqualTo("type","odometer");
        odometerquery.include("Today");
        odometerquery.include("Total");
        odometerquery.orderByDescending("createdAt");
        odometerquery.limit(7);

        odometerquery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if (list != null) {

                    if (list.size() > 0) {

                        Log.e("historyusage","debugging");
                        odometerObjectsArray = list;

                        Date curDate = new Date(System.currentTimeMillis());

                        long difference = odometerObjectsArray.get(0).getCreatedAt().getTime() - curDate.getTime();

                        if(-difference<=1000 / 60 / 60 / 24) {

                            today_text.setText(((AVObject) list.get(0)).getDouble("Today") + "");
                        }
                        else {
                            //total_text.setText("0.0");
                            today_text.setText("0.0");
                        }
                        total_text.setText(((AVObject) list.get(0)).getDouble("Total") + "");
                        int day_range = 7;

                        Line line = new Line(values).setColor(R.color.lightgray).setCubic(true);
                        List<Line> lines = new ArrayList<Line>();
                        lines.add(line);

                        LineChartData data = new LineChartData();
                        data.setLines(lines);

                        List<String> date_string_array = new ArrayList<String>();

                        Date dNow = new Date();   //当前时间

                        Date dBefore = new Date();
                        Calendar calendar = Calendar.getInstance(); //得到日历

                        calendar.setTime(dNow);//把当前时间赋给日历

                        dNow.setTime(dNow.getTime()-dNow.getTime()%(24*3600*1000)-8*3600*1000);
                        Log.e("historyusa??", "debuggggg " + ((dNow.getTime()-dBefore.getTime())/3600/1000));

                        SimpleDateFormat formatter = new SimpleDateFormat("M.d");
                        String dateString = formatter.format(dNow);

                        List<AxisValue> axisValues = new ArrayList<AxisValue>();

                        for (int i = 0; i < day_range; i++) {

                            dBefore = calendar.getTime();
                            dateString = formatter.format(dBefore);

                            date_string_array.add(dateString + "");
                            calendar.add(Calendar.DAY_OF_MONTH, -(1));  //设置为前一天

                            axisValues.add(new AxisValue(i).setLabel(dateString + ""));

                            Calendar calendar1 = Calendar.getInstance(); //得到日历

                            calendar1.setTime(dNow);
                            Log.e("historyusa??", "debug!! " + i);

                            for (int j = 0; j < 7; j++) {



                                if (odometerObjectsArray!=null) {

                                    if(i<odometerObjectsArray.size()) {

                                        Log.e("historyusa@@","debug "+((float)odometerObjectsArray.get(i).getCreatedAt().getTime() - calendar1.getTime().getTime())/3600/1000/24);


                                        if (odometerObjectsArray.get(i).getCreatedAt().getTime() - calendar1.getTime().getTime() <= 24 * 3600 * 1000
                                                &&odometerObjectsArray.get(i).getCreatedAt().getTime() - calendar1.getTime().getTime()>0) {

//                                            values.set(j,new PointValue(j, (float) odometerObjectsArray.get(i).getNumber("Today")));


                                            //values.add(new PointValue(j, (float) odometerObjectsArray.get(i).getNumber("Today")));
                                            values.add(new PointValue(j, (float)odometerObjectsArray.get(i).getDouble("Today")));

                                            Log.e("historyusa!!", "replace "+odometerObjectsArray.get(i).getDouble("Today"));

//                                            values.remove(j);
//                                            values.add(j, new PointValue(j, (float) odometerObjectsArray.get(i).getNumber("Today")));


                                        }
                                        else {
                                            values.add(new PointValue(j, (float)0.0));
                                            Log.e("historyusa!!", "adding ");
                                        }

                                    }

                                    calendar1.add(Calendar.DAY_OF_MONTH, -(1));


                                }
                            }


                        }

//        data.setAxisXBottom(new Axis(axisValues).setHasLines(true));
//        data.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

                        List<AxisValue> xaxisValues = new ArrayList<AxisValue>();
                        List<PointValue> yvalues = new ArrayList<PointValue>();

                        Log.e("date debug","a???");
                        for(int i=0;i<day_range;i++){
                            Log.e("date debug", "a" + date_string_array.get(day_range - 1 - i));

                            xaxisValues.add(new AxisValue(i).setLabel(date_string_array.get(day_range - 1 - i)));

                            Log.e("date debug", "b" +values.get(day_range - 1 - i));


                            yvalues.add(new PointValue(i, values.get(day_range - 1 - i).getY()));


                        }


                        line = new Line(yvalues).setColor(R.color.lightgray).setCubic(true);
                        line.setStrokeWidth(1);
                        lines = new ArrayList<Line>();
                        lines.add(line);
                        data.setLines(lines);

                        Axis axisX = new Axis(xaxisValues).setHasLines(true); //X轴

                        axisX.setHasTiltedLabels(true);
                        //axisX.setTextSize(9);
                        //axisX.setMaxLabelChars(10);

                        data.setAxisXBottom(axisX);


                        data.setAxisYLeft(new Axis().setName("Range /Km").setHasLines(true).setMaxLabelChars(3));

                        LineChartView chart = (LineChartView) view2.findViewById(R.id.chart);

                        Log.e("history","chart debug"+chart.toString());

                        chart.setViewportCalculationEnabled(true);

                        Viewport viewport = initViewPort();
                        chart.setMaximumViewport(viewport);
                        chart.setCurrentViewport(viewport);

                        chart.setInteractive(false);

                        //chart.setZoomType(ZoomType.HORIZONTAL);

                        chart.setLineChartData(data);


                    }
//                    else {
//                        total_text.setText("0.0");
//                        today_text.setText("0.0");
//                    }
                    else {

                        Log.e("noodometer"," debugging");

                        total_text.setText("0.0");
                        today_text.setText("0.0");

                        values= new ArrayList<PointValue>();
                        values.add(new PointValue(0, (float) 0.0));
                        values.add(new PointValue(1, (float) 0.0));
                        values.add(new PointValue(2, (float) 0.0));
                        values.add(new PointValue(3, (float) 0.0));
                        values.add(new PointValue(4, (float) 0.0));
                        values.add(new PointValue(5, (float) 0.0));
                        values.add(new PointValue(6, (float) 0.0));

                        Line line = new Line(values).setColor(R.color.lightgray).setCubic(true);
                        List<Line> lines = new ArrayList<Line>();
                        lines.add(line);

                        LineChartData data = new LineChartData();
                        data.setLines(lines);

                        Date dNow = new Date();

                        Calendar calendar = Calendar.getInstance(); //得到日历

                        calendar.setTime(dNow);//把当前时间赋给日历
                        List<String> date_string_array = new ArrayList<String>();
                        SimpleDateFormat formatter = new SimpleDateFormat("M.d");

                        String dateString = formatter.format(dNow);

                        List<AxisValue> axisValues = new ArrayList<AxisValue>();
                        List<AxisValue> xaxisValues = new ArrayList<AxisValue>();
                        List<PointValue> yvalues = new ArrayList<PointValue>();
                        Date dBefore = new Date();
                        for (int i = 0; i < 7; i++) {


                            dBefore = calendar.getTime();
                            dateString = formatter.format(dBefore);

                            date_string_array.add(dateString + "");
                            calendar.add(Calendar.DAY_OF_MONTH, -(1));  //设置为前一天

                            axisValues.add(new AxisValue(i).setLabel(dateString + ""));

                        }
                        for (int i = 0; i < 7; i++) {
                            Log.e("date debug", "a" + date_string_array.get(7 - 1 - i));

                            xaxisValues.add(new AxisValue(i).setLabel(date_string_array.get(7 - 1 - i)));

                            Log.e("date debug", "b" + values.get(7 - 1 - i));


                            yvalues.add(new PointValue(i, values.get(7 - 1 - i).getY()));


                        }
                        line = new Line(yvalues).setColor(R.color.lightgray).setCubic(true);
                        line.setStrokeWidth(1);
                        lines = new ArrayList<Line>();
                        lines.add(line);
                        data.setLines(lines);

                        Axis axisX = new Axis(xaxisValues).setHasLines(true); //X轴

                        axisX.setHasTiltedLabels(true);
                        //axisX.setTextSize(9);
                        //axisX.setMaxLabelChars(10);

                        data.setAxisXBottom(axisX);


                        data.setAxisYLeft(new Axis().setName("Range /Km").setHasLines(true).setMaxLabelChars(3));

                        LineChartView chart = (LineChartView) view2.findViewById(R.id.chart);

                        Log.e("history", "chart debug");

                        chart.setViewportCalculationEnabled(true);
//
//                    Viewport viewport = initViewPort();
//                    chart.setMaximumViewport(viewport);
//                    chart.setCurrentViewport(viewport);

                        chart.setInteractive(false);

                        //chart.setZoomType(ZoomType.HORIZONTAL);

                        chart.setLineChartData(data);


                    }
                }



            }

        });











//        for (int i = 0; i < day_range; ++i) {
//
//            calendar.add(Calendar.DAY_OF_MONTH, -(1));  //设置为前一天
//            dBefore = calendar.getTime();
//            dateString = formatter.format(dBefore);
//
//            date_string_array.add(dateString + "");
//            axisValues.add(new AxisValue(i).setLabel(dateString + ""));
//
//            Calendar calendar1 = Calendar.getInstance(); //得到日历
//
//
//        }
////        data.setAxisXBottom(new Axis(axisValues).setHasLines(true));
////        data.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));
//
//        List<AxisValue> xaxisValues = new ArrayList<AxisValue>();
//        List<PointValue> yvalues = new ArrayList<PointValue>();
//
//        //Log.e("date debug","a???");
//        for(int i=0;i<day_range;i++){
//            //Log.e("date debug","a"+date_string_array.get(day_range-1-i));
//            xaxisValues.add(new AxisValue(i).setLabel(date_string_array.get(day_range-1 - i)));
//
//            //Log.e("date debug", "b" + values.get(day_range-1 - i));
//            yvalues.add(new PointValue(i, values.get(day_range-1 - i).getY()));
//
//
//        }
//
//
//        line = new Line(yvalues).setColor(R.color.lightgray).setCubic(true);
//        line.setStrokeWidth(1);
//        lines = new ArrayList<Line>();
//        lines.add(line);
//        data.setLines(lines);
//
//        Axis axisX = new Axis(xaxisValues).setHasLines(true); //X轴
//
//        axisX.setHasTiltedLabels(true);
//        //axisX.setTextSize(9);
//        //axisX.setMaxLabelChars(10);
//
//        data.setAxisXBottom(axisX);
//
//
//        data.setAxisYLeft(new Axis().setName("Range /Km").setHasLines(true).setMaxLabelChars(3));
//
//        LineChartView chart = (LineChartView) view2.findViewById(R.id.chart);
//
//        chart.setViewportCalculationEnabled(true);
//
//        Viewport viewport = initViewPort();
//        chart.setMaximumViewport(viewport);
//        chart.setCurrentViewport(viewport);
//
//        chart.setInteractive(false);
//
//        //chart.setZoomType(ZoomType.HORIZONTAL);
//
//        chart.setLineChartData(data);


        order_button = (ImageButton) view2.findViewById(R.id.order_button);
        discount_button = (ImageButton) view2.findViewById(R.id.discount_button);

        order_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mainIntent = new Intent(getActivity(), OrderActivity.class);
                startActivity(mainIntent);

            }
        });

        discount_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getActivity(), DiscountActivity.class);
                startActivity(mainIntent);
            }
        });


    }

    private Viewport initViewPort() {
        Viewport viewport = new Viewport();
        viewport.top = 100;
        viewport.bottom = 0;
        viewport.left = 100;
        viewport.right = 100;

        return viewport;
    }

    private void InitTextView() {
        textView1 = (TextView) rootView.findViewById(R.id.post_tab);
        textView2 = (TextView) rootView.findViewById(R.id.mark_tab);


        textView1.setOnClickListener(new MyOnClickListener(0));
        textView2.setOnClickListener(new MyOnClickListener(1));

        //textView2.performClick();
    }

    private void InitImageView() {
        imageView= (ImageView) rootView.findViewById(R.id.viewpager_cursor);
        bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.notloading).getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 3 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageView.setImageMatrix(matrix);// 设置动画初始位置
    }

    private class MyOnClickListener implements View.OnClickListener {
        private int index=0;
        public MyOnClickListener(int i){
            index=i;
        }
        public void onClick(View v) {
            Log.e("abc", "text onclick");
            viewPager.setCurrentItem(index);

            if(index == 0){
                imageView.setBackground(getResources().getDrawable(R.drawable.post_cursor));
                post_text.setTextColor(Color.parseColor("#00EAAB"));
                mark_text.setTextColor(Color.parseColor("#A2A2A2"));
            }
            else{
                imageView.setBackground(getResources().getDrawable(R.drawable.mark_cursor));
                post_text.setTextColor(Color.parseColor("#A2A2A2"));
                mark_text.setTextColor(Color.parseColor("#00EAAB"));
            }
        }

    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)   {
            container.removeView(mListViews.get(position));
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return  mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0==arg1;
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int one = 0;// 页卡1 -> 页卡2 偏移量   offset  + bmpW
        //one = 0;
        //int two = one * 2;// 页卡1 -> 页卡3 偏移量
        public void onPageScrollStateChanged(int arg0) {


        }



        public void onPageScrolled(int arg0, float arg1, int arg2) {


        }

        public void onPageSelected(int arg0) {
            /*两种方法，这个是一种，下面还有一种，显然这个比较麻烦
            Animation animation = null;
            switch (arg0) {
            case 0:
                if (currIndex == 1) {
                    animation = new TranslateAnimation(one, 0, 0, 0);
                } else if (currIndex == 2) {
                    animation = new TranslateAnimation(two, 0, 0, 0);
                }
                break;
            case 1:
                if (currIndex == 0) {
                    animation = new TranslateAnimation(offset, one, 0, 0);
                } else if (currIndex == 2) {
                    animation = new TranslateAnimation(two, one, 0, 0);
                }
                break;
            case 2:
                if (currIndex == 0) {
                    animation = new TranslateAnimation(offset, two, 0, 0);
                } else if (currIndex == 1) {
                    animation = new TranslateAnimation(one, two, 0, 0);
                }
                break;

            }
            */
            Animation animation = new TranslateAnimation(one*currIndex, one*arg0, 0, 0);//显然这个比较简洁，只有一行代码。
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            imageView.startAnimation(animation);

            if(currIndex == 0){
                imageView.setBackground(getResources().getDrawable(R.drawable.post_cursor));
                post_text.setTextColor(Color.parseColor("#00EAAB"));
                mark_text.setTextColor(Color.parseColor("#A2A2A2"));
            }
            else{
                imageView.setBackground(getResources().getDrawable(R.drawable.mark_cursor));
                post_text.setTextColor(Color.parseColor("#A2A2A2"));
                mark_text.setTextColor(Color.parseColor("#00EAAB"));
            }

            //viewPager.setCurrentItem(arg0);
            //Toast.makeText(getActivity(), "您选择了" + viewPager.getCurrentItem() + "页卡", Toast.LENGTH_SHORT).show();
        }

    }



    // Image Adapter

    private class ImageAdapter extends BaseAdapter {

        private  String[] IMAGE_URLS = postUrl;



        private LayoutInflater inflater;

        private DisplayImageOptions options;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        ImageAdapter(Context context) {

            if(!islike_post)
                IMAGE_URLS = postUrl;
            else
                IMAGE_URLS = likeUrl;



            inflater = LayoutInflater.from(context);

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        @Override
        public int getCount() {
            if(!islike_post)
                return PostImageCount.size();
            else
                return likePostImageCount.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;

            if(!islike_post)
                IMAGE_URLS = postUrl;
            else
                IMAGE_URLS = likeUrl;


            //Log.e("abclike","  "+IMAGE_URLS.length);

            if (view == null&& inflater != null) {
                view = inflater.inflate(R.layout.me_gridview_item, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.imageView = (ImageView) view.findViewById(R.id.me_historyimage);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.imageView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent intent = new Intent(getActivity(), DetailActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY__TO_FRONT);


                            //Log.e("abc", "testingt " + position + " objectID " + PostImageCount.get(position));

                            if(!islike_post)
                                intent.putExtra("ObjectId", PostImageCount.get(position).getObjectId());
                            else
                                intent.putExtra("ObjectId", likePostImageCount.get(position).getAVObject("photo").getObjectId());

                            startActivity(intent);

                        }
                    }
            );

            ImageLoader.getInstance().displayImage(IMAGE_URLS[position], holder.imageView, options, animateFirstListener);

            return view;
        }
    }

    static class ViewHolder {
        ImageView imageView;
    }


    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    private void enableGps() {
        // Check if user has granted location permission
        if (!locationServices.areLocationPermissionsGranted()) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            enableLocation();
        }
    }


    private void enableLocation() {
        // If we have the last location of the user, we can move the camera to that position.
        Location lastLocation = locationServices.getLastLocation();
        if (lastLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 12));
        }

        locationServices.addLocationListener(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    // Move the map camera to where the user location is and then remove the
                    // listener so the camera isn't constantly updating when the user location
                    // changes. When the user disables and then enables the location again, this
                    // listener is registered again and will adjust the camera once again.
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 12));
                    locationServices.removeLocationListener(this);
                }
            }
        });
        // Enable or disable the location layer on the map
        Log.e("map debug","setMyLocationEnabled");
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {

        super.onResume();
        queryParseMethod();
        Log.e("abc","home resume");

    }
}