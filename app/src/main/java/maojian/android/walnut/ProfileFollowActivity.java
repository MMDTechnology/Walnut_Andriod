package maojian.android.walnut;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by android on 1/8/16.
 */
public class ProfileFollowActivity extends Activity {

    private  String objectID;

    AVUser currentUser;

    private  String type;

    private final String[] profileUrl = new String[40];

    public List<AVObject> postobjectArray;

    public List<AVObject> userfollowobjectArray;

    private  ListView lv;
    private userlistAdapter iadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_profilefollow);

        postobjectArray = new ArrayList<AVObject>();

        ImageButton backButton = (ImageButton) findViewById(R.id.profilefollow_backButton);

        backButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        ProfileFollowActivity.this.finish();
                    }
                }
        );

        Intent intent = getIntent();
        objectID = intent.getStringExtra("ObjectId");
        type = intent.getStringExtra("type");

        final AVQuery commentquery = new AVQuery("_User");
        commentquery.whereEqualTo("objectId", objectID);
        commentquery.include("profileImage");

        commentquery.findInBackground(
                new FindCallback() {
                    @Override
                    public void done(List list, AVException e) {

                        if(list!=null){
                            currentUser = (AVUser) list.get(0);

                        }

                        getUserlist();

                    }
                }

        );

        TextView follow_textview = (TextView) findViewById(R.id.profilefollow_textview);

        if(type.equals("fans")){

            follow_textview.setText("Fans");

        }
        else {

            follow_textview.setText("Following");

        }

//        getUserlist();



    }

    private class userlistAdapter extends BaseAdapter {

        private  String[] IMAGE_URLSS = profileUrl;



        private LayoutInflater inflater;

        private DisplayImageOptions options;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        userlistAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .displayer(new CircleBitmapDisplayer(Color.WHITE, 5))
                    .build();
        }

        @Override
        public int getCount() {
            return postobjectArray.size();
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
            final userListHolder holder;
            View view = convertView;
            if (view == null&& inflater != null) {
                view = inflater.inflate(R.layout.discoveruserlist, parent, false);
                holder = new userListHolder();
                assert view != null;
                holder.profileimageView = (ImageView) view.findViewById(R.id.discover_profile);
                holder.username = (TextView) view.findViewById(R.id.discover_username);
                holder.followbutton = (TextView)  view.findViewById(R.id.discover_button);
                holder.isfollowing = false;

                Typeface face1 = Typeface.createFromAsset(ProfileFollowActivity.this.getAssets(), "fonts/Brown-Regular.otf");

                holder.followbutton.setTypeface(face1);

                view.setTag(holder);
            } else {
                holder = (userListHolder) view.getTag();
            }

            if(postobjectArray.size()>0){

                AVUser x = new AVUser();

                if(type.equals("fans")){

                    x = (AVUser)(postobjectArray.get(position).get("fromUser"));

                   if(userfollowobjectArray!=null) {
                       //Log.e("abc","user follow count: "+userfollowobjectArray.size());
                       for (int i = 0; i < userfollowobjectArray.size(); i++) {

                          AVUser y = (AVUser)(userfollowobjectArray.get(i).get("toUser"));

                           Log.e("abc", "user follow debug: " + x.getUsername() + "??  " + y.getUsername());

                           if(x.getUsername().equals(y.getUsername())){
                               Log.e("abc","abcfollowing");
                               holder.followbutton.setText("Following");
                               holder.isfollowing = true;
                               break;
                           }
                           else{
                               Log.e("abc","abcfollow");
                               holder.followbutton.setText("Follow");
                               holder.isfollowing = false;
                           }
                           //holder.followbutton.setText("????");

                       }
                   }
                }
                else{
                    x = (AVUser)(postobjectArray.get(position).get("toUser"));
                    holder.followbutton.setText("Following");
                    holder.isfollowing = true;
                }
                holder.username.setText((String) x.getUsername());


            }

            holder.followbutton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(holder.followbutton.getText().equals("Follow")){

                                AVObject likeobject = new AVObject("Activity");
                                likeobject.put("type", "follow");
                                likeobject.put("fromUser", AVUser.getCurrentUser());

                                final AVQuery pushQuery = AVInstallation.getQuery();
                                pushQuery.whereEqualTo("owner", currentUser);

                                if(type.equals("fans")){
                                    likeobject.put("toUser", (AVUser)(postobjectArray.get(position).get("fromUser")));
                                    pushQuery.whereEqualTo("owner", (AVUser) (postobjectArray.get(position).get("fromUser")));
                                }
                                else {
                                    likeobject.put("toUser", (AVUser)(postobjectArray.get(position).get("toUser")));
                                    pushQuery.whereEqualTo("owner", (AVUser) (postobjectArray.get(position).get("fromUser")));
                                }




                                AVACL likeACL = new AVACL();
                                likeACL.setPublicReadAccess(true);// 设置公开的「读」权限，任何人都可阅读
                                likeACL.setWriteAccess(AVUser.getCurrentUser(), true);//为当前用户赋予「写」权限

                                likeobject.setACL(likeACL);

                                likeobject.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        if (e == null) {
                                            // 存储成功
//                                            AVQuery pushQuery = AVInstallation.getQuery();
//                                            pushQuery.whereEqualTo("owner", currentUser);

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

                                holder.followbutton.setText("Following");

                            }
                            else{

                                AVQuery existlike = new AVQuery("Activity");
                                existlike.whereEqualTo("type", "follow");
                                existlike.whereEqualTo("fromUser", AVUser.getCurrentUser());
                                if(type.equals("fans")){
                                    existlike.whereEqualTo("toUser", (AVUser) (postobjectArray.get(position).get("fromUser")));
                                }
                                else {
                                    existlike.whereEqualTo("toUser", (AVUser) (postobjectArray.get(position).get("toUser")));
                                }

                                //existlike.whereEqualTo("toUser", currentUser);

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

                                holder.followbutton.setText("Follow");
                            }

                        }
                    }
            );

            holder.profileimageView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }
            );

            ImageLoader.getInstance().displayImage(IMAGE_URLSS[position], holder.profileimageView, options, new SimpleImageLoadingListener());

            return view;
        }
    }

    static class userListHolder {
        ImageView profileimageView;
        TextView username;

        TextView followbutton;

        Boolean isfollowing;
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

    public void getUserlist() {


        final AVQuery<AVObject> discoverpostquery = new AVQuery<>("Activity");

        discoverpostquery.whereEqualTo("type","follow");

        if(type.equals("fans")) {
            discoverpostquery.whereEqualTo("toUser",currentUser);
            discoverpostquery.include("fromUser");
        }
        else {
            discoverpostquery.whereEqualTo("fromUser",currentUser);
            discoverpostquery.include("toUser");
        }

        discoverpostquery.findInBackground(
                new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> list, AVException e) {

                        if(list!=null) {

                            postobjectArray = list;


                            lv = (ListView) findViewById(R.id.profilefollow_listview);
                            iadapter = new userlistAdapter(ProfileFollowActivity.this);
                            lv.setAdapter(iadapter);

                            Log.e("abc", "profilefollow: " + postobjectArray.size() + " type: " + type);
                            for (int i = 0; i < list.size(); i++) {

                                //Log.e("abc","debugging: "+list.get(i));

                                AVUser post = new AVUser();
                                if (type.equals("fans")) {
                                    post = (AVUser) (list.get(i).get("fromUser"));
                                } else {
                                    post = (AVUser) (list.get(i).get("toUser"));
                                }

                                final AVQuery<AVObject> discoverfansquery = new AVQuery<>("Activity");
                                discoverfansquery.whereEqualTo("type","follow");
                                discoverfansquery.whereEqualTo("fromUser",AVUser.getCurrentUser());
                                discoverfansquery.include("toUser");

                                discoverfansquery.findInBackground(
                                        new FindCallback<AVObject>() {
                                            @Override
                                            public void done(List<AVObject> list, AVException e) {
                                                if(list!=null){
                                                    userfollowobjectArray = list;

                                                    iadapter.notifyDataSetChanged();

                                                }
                                            }
                                        }
                                );



                                AVFile postImage = post.getAVFile("profileImage");

                                //PostImageurl.add(postImage.getUrl());

                                //Log.e("abc", "loglogdede " + i + "  " + postImage.getUrl());

                                profileUrl[i] = postImage.getUrl();

                            }
                        }

                    }
                }
        );




    }

}
