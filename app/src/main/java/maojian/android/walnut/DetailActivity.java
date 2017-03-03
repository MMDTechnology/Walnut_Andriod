package maojian.android.walnut;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

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

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by android on 29/7/16.
 */
public class DetailActivity extends FragmentActivity {


    private  String objectID;

    private AVObject detailObject;

    private boolean islike;

    private int likecount;
    private int commentcount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_detail);

        islike = false;

        Intent intent = getIntent();
        objectID=intent.getStringExtra("ObjectId");

        Log.e("abc", "object: " + objectID);
        detailObject = AVObject.createWithoutData("Photo", objectID);

        final DisplayImageOptions postoptions;
        postoptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        final DisplayImageOptions profileptions;
        profileptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer(Color.WHITE, 5))
                .build();

        final ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        final TextView detailusername = (TextView) findViewById(R.id.detailuserName);

        Typeface face1 = Typeface.createFromAsset(this.getAssets(), "fonts/Brown-Regular.otf");
        detailusername.setTypeface(face1);

        final AVQuery commentquery = new AVQuery("Photo");
        commentquery.whereEqualTo("objectId", objectID);
        commentquery.include("user");

        Log.e("detaildebug","  "+objectID);

        final ImageView detailPost = (ImageView) findViewById(R.id.detailPost);
        final ImageView detailProfile = (ImageView) findViewById(R.id.detailProfile);

        commentquery.findInBackground(
                new FindCallback() {
                    @Override
                    public void done(List list, AVException e) {
                        if (list != null) {
                            detailObject = (AVObject) list.get(0);

                            detailusername.setText(((AVUser) detailObject.get("user")).getUsername());

                            AVFile userProfile = ((AVUser) detailObject.get("user")).getAVFile("profileImage");

                            ImageLoader.getInstance().displayImage(userProfile.getUrl(), detailProfile, profileptions, animateFirstListener);

                            Log.e("detaildebug", "?? " + detailObject.getAVFile("image").getUrl());




                            ViewGroup.LayoutParams para;
                            para = detailPost.getLayoutParams();
                            WindowManager wm1 = getWindowManager();
                            int width1 = wm1.getDefaultDisplay().getWidth();
                            int height1 = wm1.getDefaultDisplay().getHeight();

                            para.height = width1/16*10;
                            para.width = width1;

                            detailPost.setLayoutParams(para);



                            ImageLoader.getInstance().displayImage(detailObject.getAVFile("image").getUrl(), detailPost, postoptions, animateFirstListener);

                            final VideoView detailVideo = (VideoView) findViewById(R.id.detail_videoView);
                            final ImageButton detailPlay = (ImageButton) findViewById(R.id.detail_videoplay);

                            ViewGroup.LayoutParams para_video;
                            para_video = detailVideo.getLayoutParams();
                            WindowManager wm2 = getWindowManager();
                            int width2 = wm2.getDefaultDisplay().getWidth();
                            int height2 = wm2.getDefaultDisplay().getHeight();

                            para_video.height = width2/16*10;
                            para_video.width = width2;

                            detailVideo.setLayoutParams(para_video);

                            detailPost.setVisibility(View.VISIBLE);
                            detailVideo.setVisibility(View.INVISIBLE);
                            detailPlay.setVisibility(View.INVISIBLE);

                            getWallImages();


                            if(detailObject.get("isVideo")!=null){
                                //detailPost.setVisibility(View.INVISIBLE);
                                //detailVideo.setVisibility(View.VISIBLE);
                                detailPlay.setVisibility(View.VISIBLE);

                                AVFile postVideo = detailObject.getAVFile("video");
                                Uri uri = Uri.parse(postVideo.getUrl());
                                //holder.videoView.setMediaController(new MediaController(getActivity()));
                                detailVideo.setVideoURI(uri);

                                detailVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        //播放结束后的动作

                                        detailPlay.setVisibility(View.VISIBLE);
                                        detailVideo.setVisibility(View.INVISIBLE);

                                        detailPost.setVisibility(View.VISIBLE);

                                    }
                                });

                                detailPlay.setAlpha((float) 0.8);
                                detailPlay.setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                detailVideo.start();
                                                detailPlay.setVisibility(View.INVISIBLE);
                                                detailVideo.setVisibility(View.VISIBLE);

                                                detailPost.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                );

                            }

                        }
                    }
                }
        );



        ImageButton backButton = (ImageButton) findViewById(R.id.detailbackButton);

        backButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        DetailActivity.this.finish();
                    }
                }
        );



        detailProfile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {



                        Intent intent = new Intent(DetailActivity.this, customProfileActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY__TO_FRONT);

                        Log.e("jump debug", "" + ((AVUser) detailObject.get("user")).getObjectId());

                        intent.putExtra("ObjectId", ((AVUser) detailObject.get("user")).getObjectId());

                        startActivity(intent);

                    }
                }
        );


    }

//    static class ViewHolder {
//        TextView text;
//        ImageView profile;
//        ImageView image;
//
//        TextView likecount;
//        TextView commentcount;
//
//        TextView timelabel;
//
//        ImageButton likebutton;
//        ImageButton commentbutton;
//
//        Boolean islike;
//    }

    public void getWallImages() {

        final TextView detaillikeCount = (TextView) findViewById(R.id.detaillikecount);
        final TextView detailcommentCount = (TextView) findViewById(R.id.detailcommentcount);
        final TextView detailtimelabel = (TextView) findViewById(R.id.detailtimelabel);

        final TextView detailusercomment = (TextView) findViewById(R.id.detail_usercomment);

        Typeface face1 = Typeface.createFromAsset(this.getAssets(), "fonts/Brown-Regular.otf");
        Typeface face2 = Typeface.createFromAsset(this.getAssets(), "fonts/Brown-Light.otf");

        detaillikeCount.setTypeface(face1);
        detailcommentCount.setTypeface(face1);
        detailtimelabel.setTypeface(face2);
        detailusercomment.setTypeface(face1);

        Log.e("detaildebug", "111");

        if(detailObject.get("comment")!=null)
            detailusercomment.setText(detailObject.get("comment").toString());

        final ImageButton detail_likebutton = (ImageButton) findViewById(R.id.detaillikebutton);
        final ImageButton detail_commentbutton = (ImageButton) findViewById(R.id.detailcommentbutton);


        final AVQuery<AVObject> likequery = new AVQuery<>("Activity");
        likequery.whereEqualTo("type", "like");
        likequery.whereEqualTo("photo", detailObject);
        likequery.findInBackground(
                new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> list, AVException e) {

                        Integer x = list.size();

                        likecount = list.size();

                        detaillikeCount.setText(x.toString());

                        likequery.whereEqualTo("fromUser", AVUser.getCurrentUser());

                        likequery.findInBackground(
                                new FindCallback<AVObject>() {
                                    @Override
                                    public void done(List<AVObject> list, AVException e) {
                                        if(list.size()>0){
                                            detail_likebutton.setBackground(getResources().getDrawable(R.drawable.mainlikeicon));
                                            islike = true;
                                        }
                                    }
                                }
                        );

                    }
                }
        );

        AVQuery<AVObject> commentquery = new AVQuery<>("Activity");
        commentquery.whereEqualTo("type", "comment");
        commentquery.whereEqualTo("photo", detailObject);
        commentquery.findInBackground(
                new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> list, AVException e) {

                        Integer x = list.size();

                        commentcount = list.size();

                        detailcommentCount.setText(x.toString());

                    }
                }
        );
        Date curDate = new Date(System.currentTimeMillis());

        long difference = detailObject.getCreatedAt().getTime() - curDate.getTime();
        Log.v("Time difference:", String.valueOf(difference));

        String timediff = "today";
        if (-difference<60*1000)
            timediff = (int)(-difference/1000)+" Seconds ago";
        else if (-difference<60*60*1000){
            timediff = (int)(-difference/1000/60)+" Minutes ago";

        }
        else if (-difference<60*60*24*1000){
            timediff = (int)(-difference/1000/60/60)+" Hours ago";
        }
        else {
            //Log.e("timedebugging","??? "+(int)(-difference/1000/60/60/24)+"difff "+difference);
            timediff = (int)(-difference/1000/60/60/24)+" Days ago";
        }

        detailtimelabel.setText(timediff);

        detail_commentbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(DetailActivity.this, CommentActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY__TO_FRONT);
                        intent.putExtra("ObjectId", detailObject.getObjectId());
                        startActivity(intent);

                    }
                }
        );

        detail_likebutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(!islike){

                            islike = true;

                            detail_likebutton.setBackground(getResources().getDrawable(R.drawable.mainlikeicon));
                            Integer a = likecount+1;
                            likecount = likecount+1;
                            detaillikeCount.setText(a.toString());


                            AVObject likeobject = new AVObject("Activity");
                            likeobject.put("type", "like");
                            likeobject.put("fromUser",AVUser.getCurrentUser());
                            likeobject.put("toUser", (AVUser) detailObject.get("user"));
                            likeobject.put("photo", detailObject);

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
                                        pushQuery.whereEqualTo("owner", (AVUser) detailObject.get("user"));

                                        AVPush.sendMessageInBackground(AVUser.getCurrentUser().getUsername() + " liked your post", pushQuery, new SendCallback() {
                                            @Override
                                            public void done(AVException e) {
                                                Log.e("abc","like push done");
                                            }
                                        });

                                    } else {
                                        // 失败的话，请检查网络环境以及 SDK 配置是否正确
                                    }
                                }
                            });


                        }
                        else{
                            islike = false;

                            detail_likebutton.setBackground(getResources().getDrawable(R.drawable.maindislikeicon));
                            Integer a = likecount-1;
                            likecount = likecount -1;
                            detaillikeCount.setText(a.toString());

                            AVQuery existlike = new AVQuery("Activity");
                            existlike.whereEqualTo("photo",detailObject);
                            existlike.whereEqualTo("type","like");
                            existlike.whereEqualTo("fromUser",AVUser.getCurrentUser());

                            existlike.findInBackground(new FindCallback() {
                                                           @Override
                                                           public void done(List list, AVException e) {

                                                               for(int i=0;i<list.size();i++){

                                                                   AVObject a =(AVObject) list.get(i);
                                                                   a.deleteEventually();
                                                               }
                                                           }
                                                       }
                            );


                        }

                    }
                }
        );


    }

//    @Override
//    public void onResume() {
//
//        super.onResume();
//        getWallImages();
//        Log.e("abc","detail resume");
//
//    }

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
