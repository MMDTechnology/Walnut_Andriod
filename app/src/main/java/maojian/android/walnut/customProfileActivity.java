package maojian.android.walnut;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVACL;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVPush;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SendCallback;
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
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by android on 30/7/16.
 */
public class customProfileActivity extends FragmentActivity {


    private  String objectID;

    AVUser currentUser;

    private ImageView currentprofileimage;

    TextView viewpager1_postlabel;

    private ViewPager viewPager;//页卡内容
    private ImageView imageView;// 动画图片
    private TextView textView1,textView2;
    private List<View> views;// Tab页面列表
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
    private View view1,view2;//各个页卡

    private ImageButton multi_post;
    private ImageButton single_post;
    private ImageButton like_post;

    private TextView post_text;
    private TextView mark_text;

    private TextView usernameLabel;
    private TextView historyLabel;
    private TextView fansLabel;
    private TextView followingLabel;

    private TextView me_fansbutton;
    private TextView me_followingbutton;
    private TextView me_historybutton;

    private GridView listView;

    private Button custom_followbutton;

    private List<AVObject> PostImageCount;

    private List<AVObject> likePostImageCount;

    private final String[] postUrl = new String[40];

    private final String[] likeUrl = new String[100];

    private ImageAdapter discoverpostadapter;


    private List<AVObject> odometerObjectsArray;

    private boolean islike_post;

    private  ArrayList<PointValue> values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_customprofile);

        Intent intent = getIntent();
        objectID = intent.getStringExtra("ObjectId");

        Log.e("abc", "customProfile: " + objectID);

        ImageButton backbutton = (ImageButton) findViewById(R.id.custom_backButton);
        backbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customProfileActivity.this.finish();
                    }
                }
        );




        InitImageView();
        InitTextView();

        InitViewPager();




        PostImageCount = new ArrayList<AVObject>();

        currentprofileimage = (ImageView) findViewById(R.id.custom_profileimage);

        usernameLabel = (TextView) findViewById(R.id.custom_username);
        //historyLabel = (TextView) findViewById(R.id.custom_historylabel);
        fansLabel = (TextView) findViewById(R.id.custom_fanslabel);
        followingLabel = (TextView) findViewById(R.id.custom_followinglabel);

        Typeface face1 = Typeface.createFromAsset(customProfileActivity.this.getAssets(), "fonts/Brown-Regular.otf");
        usernameLabel.setTypeface(face1);
        //historyLabel.setTypeface(face1);
        fansLabel.setTypeface(face1);
        followingLabel.setTypeface(face1);

        custom_followbutton = (Button) findViewById(R.id.custom_followbutton);
        custom_followbutton.setTypeface(face1);

        listView = (GridView) view2.findViewById(R.id.viewpager1_gridView);

        viewpager1_postlabel = (TextView) view2.findViewById(R.id.viewpager1_postlabel);

        multi_post = (ImageButton) view2.findViewById(R.id.multi_postbutton);
        single_post = (ImageButton) view2.findViewById(R.id.single_postbutton);
        like_post = (ImageButton) view2.findViewById(R.id.like_postbutton);

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


        final AVQuery commentquery = new AVQuery("_User");
        commentquery.whereEqualTo("objectId", objectID);
        commentquery.include("profileImage");

        commentquery.include("bio");

        commentquery.findInBackground(
                new FindCallback() {
                    @Override
                    public void done(List list, AVException e) {

                        if (list != null) {
                            currentUser = (AVUser) list.get(0);

                            TextView me_bio_info = (TextView) findViewById(R.id.profile_bio_info);

                            if (currentUser.get("bio") != null) {

                                me_bio_info.setText(currentUser.get("bio") + "");
                            }

//                            currentUser.fetchInBackground(new GetCallback<AVObject>() {
//                                @Override
//                                public void done(AVObject avObject, AVException e) {
//                                    Log.e("biodebug", "debugging" + currentUser.get("bio"));
//
//                                    TextView me_bio_info = (TextView) findViewById(R.id.profile_bio_info);
//
//                                    if(currentUser.get("bio")!=null){
//
//                                        me_bio_info.setText(currentUser.get("bio") + "");
//                                    }
//                                }
//                            });
                            queryParseMethod();
                        }
                        //InitViewPager();


                    }
                }

        );

    }

    public void queryParseMethod (){

        final AVFile currentProfile = currentUser.getAVFile("profileImage");

        DisplayImageOptions options =  new DisplayImageOptions.Builder()
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

        usernameLabel.setText(currentUser.getUsername());



        AVQuery<AVObject> fansquery = new AVQuery<>("Activity");
        fansquery.whereEqualTo("type", "follow");
        fansquery.whereEqualTo("toUser", currentUser);
        fansquery.include("fromUser");

        Log.e("abc", "0??? " + currentUser.getUsername());
        fansquery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if (list != null) {

                    Integer x = list.size();
                    fansLabel.setText(x.toString());


                    Log.e("bugabc", "1??? " + x);

                    custom_followbutton.setText("Follow");

                    for (int i = 0; i < x; i++) {


                        Log.e("bugabc", "0for " + i + " " + ((AVUser) list.get(i).get("fromUser")).getUsername());

                        if (((AVUser) list.get(i).get("fromUser")).getUsername().equals(AVUser.getCurrentUser().getUsername())) {

                            Log.e("bugabc", "customfollowing debug");
                            custom_followbutton.setText("Following");

                        }
//                        else {
//                            custom_followbutton.setText("Follow");
//                            Log.e("bugabc", "customfollow debug");
//                        }

                        Log.e("xx", "forcurrent " + i + "  " + AVUser.getCurrentUser().getUsername());
                    }

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
                    discoverpostadapter = new ImageAdapter(customProfileActivity.this);
                    ((GridView) listView).setAdapter(discoverpostadapter);

                    discoverpostadapter.notifyDataSetChanged();

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
                        Log.e("abc", "strange2 " + i);

                        if(post!=null) {
                            AVFile postImage = post.getAVFile("image");
                            Log.e("abc", "strange3 " + i);

                            likeUrl[i] = postImage.getUrl();
                        }

                    }
//                    discoverpostadapter = new ImageAdapter(getActivity());
//                    ((GridView) listView).setAdapter(discoverpostadapter);
                    // discoverpostadapter.notifyDataSetChanged();

                }

            }
        });


        //me_historybutton = (TextView) findViewById(R.id.custom_historybutton);


        me_fansbutton = (TextView) findViewById(R.id.custom_fansbutton);
        me_fansbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(customProfileActivity.this, ProfileFollowActivity.class);
                        intent.putExtra("type", "fans");
                        intent.putExtra("ObjectId", currentUser.getObjectId());
                        startActivity(intent);

                    }
                }
        );

        me_followingbutton = (TextView) findViewById(R.id.custom_followingbutton);
        me_followingbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(customProfileActivity.this, ProfileFollowActivity.class);
                        intent.putExtra("type", "following");
                        intent.putExtra("ObjectId", currentUser.getObjectId());
                        startActivity(intent);

                    }
                }
        );

        Typeface face1 = Typeface.createFromAsset(customProfileActivity.this.getAssets(), "fonts/Brown-Regular.otf");
        //me_historybutton.setTypeface(face1);
        me_fansbutton.setTypeface(face1);
        me_followingbutton.setTypeface(face1);


        custom_followbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (custom_followbutton.getText().equals("Follow")) {

                            AVObject likeobject = new AVObject("Activity");
                            likeobject.put("type", "follow");
                            likeobject.put("fromUser", AVUser.getCurrentUser());
                            likeobject.put("toUser", currentUser);


                            AVACL likeACL = new AVACL();
                            likeACL.setPublicReadAccess(true);// 设置公开的「读」权限，任何人都可阅读
                            likeACL.setWriteAccess(AVUser.getCurrentUser(), true);//为当前用户赋予「写」权限

                            likeobject.setACL(likeACL);

                            likeobject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        // 存储成功
                                        AVQuery pushQuery = AVInstallation.getQuery();
                                        pushQuery.whereEqualTo("owner", currentUser);

                                        AVPush.sendMessageInBackground(AVUser.getCurrentUser().getUsername() + " started following you", pushQuery, new SendCallback() {
                                            @Override
                                            public void done(AVException e) {
                                                Log.e("abc", "like push done");
                                            }
                                        });

                                    } else {
                                        // 失败的话，请检查网络环境以及 SDK 配置是否正确
                                    }
                                }
                            });

                            custom_followbutton.setText("Following");

                        } else {

                            AVQuery existlike = new AVQuery("Activity");
                            existlike.whereEqualTo("type", "follow");
                            existlike.whereEqualTo("fromUser", AVUser.getCurrentUser());
                            existlike.whereEqualTo("toUser", currentUser);

                            existlike.findInBackground(new FindCallback() {
                                                           @Override
                                                           public void done(List list, AVException e) {

                                                               for (int i = 0; i < list.size(); i++) {

                                                                   AVObject a = (AVObject) list.get(i);
                                                                   a.deleteEventually();
                                                               }
                                                           }
                                                       }
                            );

                            custom_followbutton.setText("Follow");
                        }

                    }
                }
        );



        final TextView today_text = (TextView) view1.findViewById(R.id.today_textview);
        final TextView total_text = (TextView) view1.findViewById(R.id.total_textview);

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

        AVQuery<AVObject> odometerquery = new AVQuery<>("Odometer");
        odometerquery.whereEqualTo("fromUser", currentUser);

        //Log.e("cuhistoryusage", "debugging " + currentUser.get("username"));

        odometerquery.include("Today");
        odometerquery.include("Total");
        odometerquery.orderByDescending("createdAt");
        odometerquery.limit(7);

        values= new ArrayList<PointValue>();

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

                        LineChartView chart = (LineChartView) view1.findViewById(R.id.chart);

                        Log.e("history","chart debug"+chart.toString());

                        chart.setViewportCalculationEnabled(true);

//                        Viewport viewport = initViewPort();
//                        chart.setMaximumViewport(viewport);
//                        chart.setCurrentViewport(viewport);

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

                        LineChartView chart = (LineChartView) view1.findViewById(R.id.chart);

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

    private void InitTextView() {
        textView1 = (TextView) findViewById(R.id.mark_tab_custom);
        textView2 = (TextView) findViewById(R.id.post_tab_custom);

        post_text = (TextView) findViewById(R.id.mark_tab_custom);
        mark_text = (TextView) findViewById(R.id.post_tab_custom);

        post_text.setTextColor(Color.parseColor("#00EAAB"));
        mark_text.setTextColor(Color.parseColor("#A2A2A2"));


        textView1.setOnClickListener(new MyOnClickListener(0));
        textView2.setOnClickListener(new MyOnClickListener(1));
    }

    private void InitImageView() {
        imageView= (ImageView) findViewById(R.id.viewpager_cursor_custom);
        bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.notloading).getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 3 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageView.setImageMatrix(matrix);// 设置动画初始位置
    }

    private void InitViewPager() {
//        MapboxAccountManager.start(getActivity(), "pk.eyJ1IjoiZHJhZ21lcGx6IiwiYSI6ImNpaWp6eDEweTAxOTF0cGtwZmwwaDhmcXMifQ.QMz7SFg6hGGmfo48w6eC8Q");

        viewPager=(ViewPager) findViewById(R.id.custom_viewpager);
        views=new ArrayList<View>();
        LayoutInflater inflater=this.getLayoutInflater();
        view1=inflater.inflate(R.layout.viewpager_lay2_custom, null);
        view2=inflater.inflate(R.layout.viewpager_lay1, null);
        views.add(view1);
        views.add(view2);
        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());

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
                post_text.setTextColor(Color.parseColor("#A2A2A2"));
                mark_text.setTextColor(Color.parseColor("#00EAAB"));
            }
            else{
                imageView.setBackground(getResources().getDrawable(R.drawable.mark_cursor));
                post_text.setTextColor(Color.parseColor("#00EAAB"));
                mark_text.setTextColor(Color.parseColor("#A2A2A2"));
            }

            //viewPager.setCurrentItem(arg0);
            //Toast.makeText(getActivity(), "您选择了" + viewPager.getCurrentItem() + "页卡", Toast.LENGTH_SHORT).show();
        }

    }





    private class ImageAdapter extends BaseAdapter {

        private  String[] IMAGE_URLS = postUrl;



        private LayoutInflater inflater;

        private DisplayImageOptions options;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        ImageAdapter(Context context) {
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

                            Intent intent = new Intent(customProfileActivity.this, DetailActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY__TO_FRONT);
                            Log.e("abc", "testingt " + position + " objectID " + PostImageCount.get(position));

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



}
