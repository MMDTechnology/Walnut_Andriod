package maojian.android.walnut;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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

import java.util.Date;
import java.util.List;

/**
 * Created by android on 20/9/16.
 */
public class FunspotActivity extends Activity {

    ImageView funspot_imageview;
    ImageView detailProfile;

    DisplayImageOptions avatarstyle;

    String objectID;

    AVObject target_Photo;
    private int likecount;
    private boolean islike;


    ImageButton returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_funspot);

        funspot_imageview = (ImageView) findViewById(R.id.funspot_imageview);

        detailProfile = (ImageView) findViewById(R.id.funspotprofile);

        avatarstyle= new DisplayImageOptions.Builder()
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

        Intent intent = getIntent();
        objectID = intent.getStringExtra("ObjectId");

        final AVQuery commentquery = new AVQuery("FunSpot");
        commentquery.whereEqualTo("objectId", objectID);
        commentquery.include("user");
        commentquery.findInBackground(
                new FindCallback() {
                    @Override
                    public void done(List list, AVException e) {
                        if (list != null) {
                            AVObject a = (AVObject) list.get(0);

                            ImageLoader.getInstance().displayImage(a.getAVFile("image").getUrl(), funspot_imageview, avatarstyle);

                            AVUser detailUser = (AVUser) ((AVObject) list.get(0)).get("user");

                            TextView username = (TextView) findViewById(R.id.funspot_username);

                            username.setText(detailUser.getUsername());

                            TextView funspot_location = (TextView) findViewById(R.id.funspot_location);

                            funspot_location.setText(a.getString("comment")+" "+a.get("State"));

                            Log.e("funspotdebug"," "+detailUser.getUsername());

                            AVFile userProfile = ((AVUser) detailUser).getAVFile("profileImage");

                            ImageLoader.getInstance().displayImage(userProfile.getUrl(), detailProfile, profileptions);

                            final AVQuery photoquery = new AVQuery("Photo");
                            photoquery.whereEqualTo("objectId", ((AVObject) a.get("Related_Photo")).getObjectId());
                            photoquery.findInBackground(
                                    new FindCallback() {
                                        @Override
                                        public void done(List list, AVException e) {

                                            if (list != null) {
                                                target_Photo = (AVObject) list.get(0);

                                                getWallImages();
                                            }
                                        }

                                    }

                                        );


                                    }
                        }
                    }

                    );


                    returnButton=(ImageButton)findViewById(R.id.funspot_returnbutton);

                    returnButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick (View view){
                        FunspotActivity.this.finish();
                        }
                    }

                    );


                }


    public void getWallImages() {

        final TextView detaillikeCount = (TextView) findViewById(R.id.funspotllikecount);
        final TextView detailcommentCount = (TextView) findViewById(R.id.funspotcommentcount);
        final TextView detailtimelabel = (TextView) findViewById(R.id.funspottimelabel);

        final TextView detailusercomment = (TextView) findViewById(R.id.funspot_usercomment);

        Typeface face1 = Typeface.createFromAsset(this.getAssets(), "fonts/Brown-Regular.otf");
        Typeface face2 = Typeface.createFromAsset(this.getAssets(), "fonts/Brown-Light.otf");

        detaillikeCount.setTypeface(face1);
        detailcommentCount.setTypeface(face1);
        detailtimelabel.setTypeface(face2);
        detailusercomment.setTypeface(face1);

        detailusercomment.setText(target_Photo.get("comment").toString());

        final ImageButton detail_likebutton = (ImageButton) findViewById(R.id.funspotlikebutton);
        final ImageButton detail_commentbutton = (ImageButton) findViewById(R.id.funspotcommentbutton);


        final AVQuery<AVObject> likequery = new AVQuery<>("Activity");
        likequery.whereEqualTo("type", "like");
        likequery.whereEqualTo("photo", target_Photo);
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
        commentquery.whereEqualTo("photo", target_Photo);
        commentquery.findInBackground(
                new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> list, AVException e) {

                        Integer x = list.size();
                        detailcommentCount.setText(x.toString());

                    }
                }
        );
        Date curDate = new Date(System.currentTimeMillis());

        long difference = target_Photo.getCreatedAt().getTime() - curDate.getTime();
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

        //detailtimelabel.setText(timediff);

        detail_commentbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FunspotActivity.this, CommentActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY__TO_FRONT);
                        intent.putExtra("ObjectId", target_Photo.getObjectId());
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
                            likeobject.put("toUser", (AVUser) target_Photo.get("user"));
                            likeobject.put("photo", target_Photo);

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
                                        pushQuery.whereEqualTo("owner", (AVUser) target_Photo.get("user"));

                                        AVPush.sendMessageInBackground(AVUser.getCurrentUser().getUsername() + " liked your post", pushQuery, new SendCallback() {
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


                        }
                        else{
                            islike = false;

                            detail_likebutton.setBackground(getResources().getDrawable(R.drawable.maindislikeicon));
                            Integer a = likecount-1;
                            likecount = likecount -1;
                            detaillikeCount.setText(a.toString());

                            AVQuery existlike = new AVQuery("Activity");
                            existlike.whereEqualTo("photo",target_Photo);
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


}
