package maojian.android.walnut;

/**
 * Created by android on 19/7/16.
 */

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
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
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import maojian.android.walnut.utils.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressLint("NewApi")
public class Home extends Fragment implements ReFlashListView.IReflashListener {
    View view;
    public List<AVObject> postobjectArray;

    public List<Integer> likecountobjectArray;

    //private ListView lv;
    private ReFlashListView lv;
    //ListViewAdapter adapter;
    SimpleAdapter adapter;
    ImageAdapter iadapter;

    ImageButton homeview_camerabutton;

    private ArrayList<Map<String, Object>> myData;

    private final String[] PostImage = new String[200];

    private final String[] ProfileImage = new String[200];

    private final Integer[] likecountArray = new Integer[200];
    private final Integer[] commentcountArray = new Integer[200];

    private ArrayList existinglikeArr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //view = inflater.inflate(R.layout.fragment_home, container, false);
        view = inflater.inflate(R.layout.fragment_home, null);
        postobjectArray = new ArrayList<AVObject>();

        existinglikeArr = new ArrayList();

        likecountobjectArray = new ArrayList<>();

        for (int i = 0; i < 200; i++) {

            likecountArray[i] = 0;
            commentcountArray[i] = 0;
        }

        lv = ((ReFlashListView) view.findViewById(R.id.main_listview));
        lv.setInterface(this);

        getWallImages();


        Log.e("abc", "postobjectArraycreate" + postobjectArray.size());

//        lv = (ListView) view.findViewById(R.id.listView);
//
//        myData = new ArrayList<Map<String, Object>>();
//
//        //myData = getData();
//
//        Log.e("abc", "mydatasize1"+myData.size());
//        adapter = new SimpleAdapter(getActivity(),myData,R.layout.vlist,
//                new String[]{"username","userprofile","img"},
//                new int[]{R.id.userName,R.id.Profile,R.id.Post});
//
//
//
//        lv.setAdapter(adapter);


        homeview_camerabutton = (ImageButton) view.findViewById(R.id.mainview_camerabutton);


        Log.e("a123", "  " + homeview_camerabutton);

        homeview_camerabutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("123", "123");
                        Intent intent = new Intent(getActivity(), CameraActivity.class);
                        startActivity(intent);


//                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                            //具有拍照权限，直接调用相机
//                            //具体调用代码
//                            Log.e("cameradebug","checkSelfPermission");
//                            Intent intent = new Intent(getActivity(), CameraActivity.class);
//                            startActivity(intent);
//                        } else {
//                            //不具有拍照权限，需要进行权限申请
//                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO}, 1);
//                        }
                    }
                }
        );


        //lv.setAdapter(new ArrayAdapter<String>(getActivity(),
        //        android.R.layout.simple_expandable_list_item_1, strs));
        //new MyThread().start();


        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


//        ImageButton homeview_camerabutton = (ImageButton) getActivity().findViewById(R.id.mainview_camerabutton);
//        Log.e("aa123", "123 "+homeview_camerabutton);
//        homeview_camerabutton.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Log.e("123", "123");
//                        Intent intent = new Intent(getActivity(), CameraActivity.class);
//                        startActivity(intent);
//                    }
//                }
//        );

    }

    @Override
    public void onResume() {

        super.onResume();
        //getWallImages();

        new Thread(runnable).start();

        Log.e("abc", "home resume");

        VideoView  videoView = (VideoView) view.findViewById(R.id.videoView);
        Uri uri = Uri.parse("https://s3.amazonaws.com/avos-cloud-tj3tbek3kfqb/K3XTTmPwLABNaMc474hRDdB.mov");
        //holder.videoView.setMediaController(new MediaController(getActivity()));
        videoView.setVideoURI(uri);
        videoView.start();
    }

    @Override
    public void onReflash() {

        AVQuery<AVObject> followquery = new AVQuery<>("Activity");
        followquery.whereEqualTo("type", "follow");
        followquery.whereEqualTo("fromUser", AVUser.getCurrentUser());

        AVQuery<AVObject> photosFromFollowedUsersQuery = new AVQuery<>("Photo");
        photosFromFollowedUsersQuery.whereMatchesKeyInQuery("user", "toUser", followquery);
        photosFromFollowedUsersQuery.whereExists("image");
        photosFromFollowedUsersQuery.include("createdAt");

        AVQuery<AVObject> photosFromCurrentUserQuery = new AVQuery<>("Photo");
        photosFromCurrentUserQuery.whereEqualTo("user", AVUser.getCurrentUser());
        photosFromCurrentUserQuery.whereExists("image");
        photosFromCurrentUserQuery.include("createdAt");


        AVQuery<AVObject> query = AVQuery.or(Arrays.asList(photosFromFollowedUsersQuery, photosFromCurrentUserQuery));
        //query.limit(16);//
        query.include("user");
        query.include("comment");
        query.orderByDescending("createdAt");

        Log.e("abc", "dssbs");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if (list != null) {

                    postobjectArray = list;
                    Log.e("abc", "dssbs" + list);

//                myData = getData();

                    Log.e("abc", "mydatasize " + postobjectArray);

                    iadapter = new ImageAdapter(getActivity());
                    for (int i = 0; i < postobjectArray.size(); i++) {
                        Log.e("abc", "strange " + i);
                        final AVObject post = (AVObject) postobjectArray.get(i);
                        Log.e("abc", "strange2 " + i);
                        AVFile postImage = post.getAVFile("image");
                        Log.e("abc", "strange3 " + i);
                        PostImage[i] = (String) postImage.getUrl();
                        Log.e("abc", "strange4 " + i);
                        Log.e("abc", "av url " + postImage.getUrl());

                        AVUser x = (AVUser) post.get("user");
                        AVFile profileImage = x.getAVFile("profileImage");
                        ProfileImage[i] = (String) profileImage.getUrl();

                    }

                    new Thread(runnable).start();

                    //lv = (ListView) view.findViewById(R.id.listView);

//                lv = ((ReFlashListView) view.findViewById(R.id.main_listview) );
//                lv.setInterface(this);

                    //((ListView) lv).setAdapter(iadapter);

                    lv.setAdapter(iadapter);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //startImagePagerActivity(position);
                        }
                    });

                }

                iadapter.notifyDataSetChanged();
                lv.reflashComplete();
            }

        });


    }

    //
    private class ImageAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        private DisplayImageOptions options;
        private DisplayImageOptions profileoptions;

        private final String[] IMAGE_URLS = PostImage;

        ImageAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .displayer(new SimpleBitmapDisplayer())
                    .build();

            profileoptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .displayer(new CircleBitmapDisplayer(Color.WHITE, 5))
                    .build();

            //IMAGE_URLS = new String[0];

            Log.e("abc", "ssbb" + postobjectArray.size());

        }

        @Override
        public int getCount() {
            return postobjectArray.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {

            View view = convertView;
            final ViewHolder holder;

            if (convertView == null && inflater != null) {
                view = inflater.inflate(R.layout.vlist, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) view.findViewById(R.id.userName);
                Typeface face = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Brown-Regular.otf");
                holder.text.setTypeface(face);

                holder.image = (ImageView) view.findViewById(R.id.Post);
                ViewGroup.LayoutParams para;
                para = holder.image.getLayoutParams();
                WindowManager wm1 = getActivity().getWindowManager();
                int width1 = wm1.getDefaultDisplay().getWidth();
                int height1 = wm1.getDefaultDisplay().getHeight();

                para.height = width1 / 16 * 10;
                para.width = width1;

                holder.image.setLayoutParams(para);
                holder.profile = (ImageView) view.findViewById(R.id.Profile);

                holder.likecount = (TextView) view.findViewById(R.id.mainlikecount);
                //Typeface face1 = Typeface.createFromAsset (getActivity().getAssets() , "fonts/Brown-Light.otf" );
                //holder.likecount.setTypeface (face1);
                holder.commentcount = (TextView) view.findViewById(R.id.maincommentcount);
                //Typeface face2 = Typeface.createFromAsset (getActivity().getAssets() , "fonts/Brown-Light.otf" );
                //holder.commentcount.setTypeface (face1);


                holder.timelabel = (TextView) view.findViewById(R.id.timelabel);
                Typeface face3 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Brown-Light.otf");
                holder.timelabel.setTypeface(face3);
                holder.commentcontent = (TextView) view.findViewById(R.id.main_commentcontent);
                Typeface face4 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Brown-Regular.otf");

                holder.commentcount.setTypeface(face4);
                holder.likecount.setTypeface(face4);
                holder.commentcontent.setTypeface(face4);

                holder.likebutton = (ImageButton) view.findViewById(R.id.mainlikebutton);
                holder.commentbutton = (ImageButton) view.findViewById(R.id.maincommentbutton);
                holder.islike = false;

                holder.videoView = (VideoView) view.findViewById(R.id.videoView);
                holder.video_playbutton = (ImageButton) view.findViewById(R.id.video_playbutton);

                ViewGroup.LayoutParams para_video;
                para_video = holder.videoView.getLayoutParams();
                WindowManager wm2 = getActivity().getWindowManager();
                int width2 = wm2.getDefaultDisplay().getWidth();
                int height2 = wm2.getDefaultDisplay().getHeight();

                para_video.height = width2 / 16 * 10;
                para_video.width = width2;

                holder.videoView.setLayoutParams(para_video);

                holder.player = new MediaPlayer();


                view.setTag(holder);

            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (postobjectArray.size() > 0) {

                if (postobjectArray.get(position).get("comment") != null) {

                    if (postobjectArray.get(position).get("comment").toString().length() > 0) {
                        //holder.commentcontent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        //holder.commentcontent.setHeight(20);
                        holder.commentcontent.setVisibility(View.VISIBLE);
                        holder.commentcontent.setText(postobjectArray.get(position).get("comment").toString());

                        Log.e("abc", "commentcontent " + position);
                    } else {
                        //holder.commentcontent.setHeight(0);
                        //holder.commentcontent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        Log.e("abc", "commentcontent gone " + position);
                        holder.commentcontent.setVisibility(View.GONE);
                        holder.commentcontent.setText(postobjectArray.get(position).get("comment").toString());
                    }

                } else {
                    Log.e("abc", "commentcontent gone " + position);
                    holder.commentcontent.setVisibility(View.GONE);
                    //holder.commentcontent.setText(postobjectArray.get(position).get("comment").toString());
                }

                holder.videoView.setVisibility(View.INVISIBLE);
                holder.image.setVisibility(View.VISIBLE);
                holder.video_playbutton.setVisibility(View.INVISIBLE);

                if (postobjectArray.get(position).get("isVideo") != null) {

                    //holder.videoView.setVisibility(View.VISIBLE);
                    //holder.image.setVisibility(View.INVISIBLE);
                    holder.video_playbutton.setVisibility(View.VISIBLE);

                    AVFile postVideo = postobjectArray.get(position).getAVFile("video");
                    if (postVideo != null && !TextUtils.isEmpty(postVideo.getUrl())) {
                        Uri uri = Uri.parse(postVideo.getUrl());
                        //holder.videoView.setMediaController(new MediaController(getActivity()));
                        holder.videoView.setVideoURI(uri);
                    }
//                    holder.videoView.start();  // Video playing logic
//                    holder.video_playbutton.setVisibility(View.VISIBLE);

                    //holder.video_playbutton.setVisibility(View.INVISIBLE);

                    WindowManager wm = getActivity().getWindowManager();

                    int width = wm.getDefaultDisplay().getWidth();
                    //int height = wm.getDefaultDisplay().getHeight();

                    Log.e("video debug", " " + width);

                    //holder.videoView.getHolder().setFixedSize(2160,2160/16*9);

                    holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            //播放结束后的动作
                            holder.videoView.setVisibility(View.INVISIBLE);
                            holder.video_playbutton.setVisibility(View.VISIBLE);

                            holder.image.setVisibility(View.VISIBLE);

                        }
                    });

                    holder.video_playbutton.setAlpha((float) 0.8);
                    holder.video_playbutton.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    holder.videoView.start();
                                    holder.video_playbutton.setVisibility(View.INVISIBLE);
                                    holder.videoView.setVisibility(View.VISIBLE);

                                    holder.image.setVisibility(View.INVISIBLE);
                                }
                            }
                    );


                    //holder.videoView.setVisibility(View.INVISIBLE);
                    //holder.videoView.requestFocus();


                    //holder.videoView

                }


                final AVUser x = (AVUser) postobjectArray.get(position).get("user");

                holder.profile.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Log.e("abc", "discover: " + position);

                                if (!(x.getUsername().toString().equals(AVUser.getCurrentUser().getUsername().toString()))) {

                                    Intent intent = new Intent(getActivity(), customProfileActivity.class);
                                    //intent.addFlags(Intent.FLAG_ACTIVITY__TO_FRONT);
                                    Log.e("abc", "testingt " + position + " objectID " + x.getObjectId());
                                    intent.putExtra("ObjectId", x.getObjectId());
                                    startActivity(intent);
                                }


                            }
                        }
                );

                holder.text.setText((String) x.getUsername());

                //Log.e("abc", "likiecount " + likecountArray);

                Date curDate = new Date(System.currentTimeMillis());

//                long difference = postobjectArray.get(position).getCreatedAt().getTime() - curDate.getTime();
//                Log.v("Time difference:", String.valueOf(difference));
//
//                String timediff = "today";
//
//                if ((-difference) < 60 * 1000) {
//                    timediff = (-difference / 1000) + " Seconds ago";
//                    Log.e("abc", "timedebug " + timediff + "  " + difference);
//                } else if ((-difference) < 60 * 60 * 1000) {
//                    timediff = (-difference / 1000 / 60) + " Minutes ago";
//
//                } else if ((-difference) < 60 * 60 * 24 * 1000) {
//                    timediff = (-difference / 1000 / 60 / 60) + " Hours ago";
//                } else {
//                    //Log.e("timedebugging","??? "+(int)(-difference/1000/60/60/24)+"difff "+difference);
//                    timediff = (-difference / 1000 / 60 / 60 / 24) + " Days ago";
//                }
//
//                holder.timelabel.setText(timediff);

                holder.timelabel.setText(DateUtils.dateProcess1(postobjectArray.get(position).getCreatedAt().getTime()));

                holder.commentcount.setText(commentcountArray[position].toString());
                holder.likecount.setText(likecountArray[position].toString());

                if (existinglikeArr.contains(position)) {
                    holder.likebutton.setBackground(getResources().getDrawable(R.drawable.mainlikeicon));
                    holder.islike = true;
                } else {
                    holder.likebutton.setBackground(getResources().getDrawable(R.drawable.maindislikeicon));
                    holder.islike = false;
                }

                holder.likebutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!holder.islike) {

                            holder.islike = true;
                            Log.e("abc", "dislike" + position);
                            holder.likebutton.setBackground(getResources().getDrawable(R.drawable.mainlikeicon));
                            Integer a = likecountArray[position] + 1;
                            holder.likecount.setText(a.toString());

                            existinglikeArr.add(position);
                            likecountArray[position] = likecountArray[position] + 1;

                            AVObject likeobject = new AVObject("Activity");
                            likeobject.put("type", "like");
                            likeobject.put("fromUser", AVUser.getCurrentUser());
                            likeobject.put("toUser", (AVUser) postobjectArray.get(position).get("user"));
                            likeobject.put("photo", postobjectArray.get(position));

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
                                        pushQuery.whereEqualTo("owner", (AVUser) postobjectArray.get(position).get("user"));

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


                        } else {
                            holder.islike = false;
                            Log.e("abc", "like!" + position);
                            holder.likebutton.setBackground(getResources().getDrawable(R.drawable.maindislikeicon));
                            Integer a = likecountArray[position] - 1;
                            holder.likecount.setText(a.toString());

                            boolean b = existinglikeArr.remove((Integer) position);
                            likecountArray[position] = likecountArray[position] - 1;

//                        AVObject likeobject = new AVObject("Activity");
//                        likeobject.put("type", "like");
//                        likeobject.put("fromUser", AVUser.getCurrentUser());
//                        likeobject.put("toUser", (AVUser) postobjectArray.get(position).get("user"));
//                        likeobject.put("photo", postobjectArray.get(position));
//
//                        likeobject.deleteEventually();

                            AVQuery existlike = new AVQuery("Activity");
                            existlike.whereEqualTo("photo", postobjectArray.get(position));
                            existlike.whereEqualTo("type", "like");
                            existlike.whereEqualTo("fromUser", AVUser.getCurrentUser());

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


                        }

                    }
                });

                holder.commentbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(getActivity(), CommentActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY__TO_FRONT);
                        Log.e("abc", "testingt " + position + " objectID " + postobjectArray.get(position));
                        intent.putExtra("ObjectId", postobjectArray.get(position).getObjectId());
                        startActivity(intent);


                    }
                });
            }

            //Log.e("abc", "getView" + (String) x.getUsername());

            ImageLoader.getInstance().displayImage(IMAGE_URLS[position], holder.image, options, animateFirstListener);

            ImageLoader.getInstance().displayImage(ProfileImage[position], holder.profile, profileoptions, animateFirstListener);

            return view;


        }


    }

    static class ViewHolder {
        TextView text;
        ImageView profile;
        ImageView image;

        TextView likecount;
        TextView commentcount;

        TextView timelabel;
        TextView commentcontent;

        ImageButton likebutton;
        ImageButton commentbutton;

        Boolean islike;

        VideoView videoView;
        ImageButton video_playbutton;


        SurfaceView videosurfaceView;
        MediaPlayer player;
        Display currDisplay;
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

    //
//    private ArrayList<Map<String, Object>> getData() {
//
//        final ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//
//        Log.e("abc","getdata "+postobjectArray.size());
//        for(int i = 0;i < postobjectArray.size(); i ++){
//
//
//
//            final AVObject post = (AVObject) postobjectArray.get(i);
//            final AVUser user = (AVUser) post.get("user");
//            Log.e("abc", "AVuser user " + post.get("user"));
//
//            final Map<String, Object> map = new HashMap<String, Object>();
//
//
//
//            AVFile userProfile = user.getAVFile("profileImage");
//
//
//            userProfile.getDataInBackground(new GetDataCallback() {
//                @Override
//                public void done(final byte[] bytes, AVException e) {
//
//                    AVFile postImage = post.getAVFile("image");
//
//                    Log.e("aaa","av url"+postImage.getUrl());
//
//                    postImage.getDataInBackground(new GetDataCallback() {
//                        @Override
//                        public void done(byte[] postbytes, AVException e) {
//
//                            adapter = new SimpleAdapter(getActivity(), myData, R.layout.vlist,
//                                    new String[]{"username", "userprofile", "img"},
//                                    new int[]{R.id.userName, R.id.Profile, R.id.Post});
//                            adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
//
//                                @Override
//                                public boolean setViewValue(View view, Object data,
//                                                            String textRepresentation) {
//                                    if ((view instanceof ImageView) & (data instanceof Bitmap)) {
//                                        ImageView iv = (ImageView) view;
//                                        Bitmap bm = (Bitmap) data;
//                                        iv.setImageBitmap(bm);
//                                        return true;
//                                    }
//                                    return false;
//
//                                }
//                            });
//
//                            map.put("username", (String) user.get("username"));
//
//                            Bitmap profile = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                            ByteArrayOutputStream postbos = new ByteArrayOutputStream();
//                            profile.compress(Bitmap.CompressFormat.JPEG, 100, postbos);
//                            byte[] pofbytes = postbos.toByteArray();
//                            profile = BitmapFactory.decodeByteArray(pofbytes, 0, pofbytes.length);
//                            map.put("userprofile", profile);
//
////                            Bitmap post = BitmapFactory.decodeByteArray(postbytes, 0, postbytes.length);
////                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
////                            post.compress(Bitmap.CompressFormat.JPEG, 100, bos);
////                            byte[] pobytes = bos.toByteArray();
////                            post = BitmapFactory.decodeByteArray(pobytes, 0, pobytes.length);
////                            map.put("img", post);
//
//
//                            list.add(map);
//                            lv.setAdapter(adapter);
//                            adapter.notifyDataSetChanged();
//
//                        }
//                    });
//
//
//
//
//                }
//            });

//}}


    public void getWallImages() {

        AVQuery<AVObject> followquery = new AVQuery<>("Activity");
        followquery.whereEqualTo("type", "follow");
        followquery.whereEqualTo("fromUser", AVUser.getCurrentUser());

        AVQuery<AVObject> photosFromFollowedUsersQuery = new AVQuery<>("Photo");
        photosFromFollowedUsersQuery.whereMatchesKeyInQuery("user", "toUser", followquery);
        photosFromFollowedUsersQuery.whereExists("image");
        photosFromFollowedUsersQuery.include("createdAt");

        AVQuery<AVObject> photosFromCurrentUserQuery = new AVQuery<>("Photo");
        photosFromCurrentUserQuery.whereEqualTo("user", AVUser.getCurrentUser());
        photosFromCurrentUserQuery.whereExists("image");
        photosFromCurrentUserQuery.include("createdAt");


        AVQuery<AVObject> query = AVQuery.or(Arrays.asList(photosFromFollowedUsersQuery, photosFromCurrentUserQuery));
        //query.limit(16);//
        query.include("user");
        query.include("comment");
        query.orderByDescending("createdAt");

        Log.e("abc", "dssbs");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if (list != null) {

                    postobjectArray = list;
                    Log.e("abc", "dssbs" + list);

//                myData = getData();

                    Log.e("abc", "mydatasize " + postobjectArray);

                    iadapter = new ImageAdapter(getActivity());
                    for (int i = 0; i < postobjectArray.size(); i++) {
                        Log.e("abc", "strange " + i);
                        final AVObject post = (AVObject) postobjectArray.get(i);
                        Log.e("abc", "strange2 " + i);
                        AVFile postImage = post.getAVFile("image");
                        Log.e("abc", "strange3 " + i);
                        PostImage[i] = (String) postImage.getUrl();
                        Log.e("abc", "strange4 " + i);
                        Log.e("abc", "av url " + postImage.getUrl());

                        AVUser x = (AVUser) post.get("user");
                        AVFile profileImage = x.getAVFile("profileImage");
                        ProfileImage[i] = (String) profileImage.getUrl();

                    }

                    new Thread(runnable).start();

                    //lv = (ListView) view.findViewById(R.id.listView);

//                lv = ((ReFlashListView) view.findViewById(R.id.main_listview) );
//                lv.setInterface(this);

                    //((ListView) lv).setAdapter(iadapter);

                    lv.setAdapter(iadapter);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //startImagePagerActivity(position);
                        }
                    });

                }
            }

        });

        Log.e("abc", "ds" + postobjectArray.size());
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            iadapter.notifyDataSetChanged();

        }
    };


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO: http request.
            if (postobjectArray.size() > 0) {

                for (int i = 0; i < postobjectArray.size(); i++) {

                    AVQuery<AVObject> likequery = new AVQuery<>("Activity");
                    likequery.whereEqualTo("type", "like");
                    likequery.whereEqualTo("photo", postobjectArray.get(i));

                    try {
                        List<AVObject> ab = likequery.find();
                        // Log.e("abc","thread "+ab.size());
                        likecountArray[i] = ab.size();
                        //iadapter.notifyDataSetChanged();
                    } catch (AVException e) {
                        e.printStackTrace();
                    }

                    likequery.whereEqualTo("fromUser", AVUser.getCurrentUser());

                    try {
                        List<AVObject> ab = likequery.find();
                        if (ab.size() > 0) {
                            existinglikeArr.add(i);

                        }

                        //iadapter.notifyDataSetChanged();
                    } catch (AVException e) {
                        e.printStackTrace();
                    }


                    AVQuery<AVObject> commentquery = new AVQuery<>("Activity");
                    commentquery.whereEqualTo("type", "comment");
                    commentquery.whereEqualTo("photo", postobjectArray.get(i));

                    try {
                        List<AVObject> ab = commentquery.find();
                        //Log.e("abc","thread "+ab.size());
                        commentcountArray[i] = ab.size();
                        //iadapter.notifyDataSetChanged();
                    } catch (AVException e) {
                        e.printStackTrace();
                    }

                }
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("value", "请求结果");
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }
    };

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1) {
//            if (grantResults.length >= 1) {
//                int cameraResult = grantResults[0];//相机权限
//                boolean cameraGranted = cameraResult == PackageManager.PERMISSION_GRANTED;//拍照权限
//                if (cameraGranted) {
//                    //具有拍照权限，调用相机
//                    Log.e("cameradebug1","checkSelfPermission");
//                    Intent intent = new Intent(getActivity(), CameraActivity.class);
//                    startActivity(intent);
//                } else {
//                    //不具有相关权限，给予用户提醒，比如Toast或者对话框，让用户去系统设置-应用管理里把相关权限开启
//                }
//            }
//        }
//    }

}


