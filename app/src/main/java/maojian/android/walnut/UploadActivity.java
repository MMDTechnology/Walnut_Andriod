package maojian.android.walnut;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVACL;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
//import com.twitter.sdk.android.core.TwitterAuthConfig;
//import com.twitter.sdk.android.core.TwitterCore;
//import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//import io.fabric.sdk.android.Fabric;

/**
 * Created by android on 5/8/16.
 */
public class UploadActivity extends Activity {

    private LayoutInflater inflater;

    private DisplayImageOptions options;

    private String objectID;
    private String objectPath;
    private ImageView upload_imageview;

    private ImageButton upload_share;
    private EditText upload_edittext;

    private Boolean isVideo = false;
    private String videoPath;

    CallbackManager callbackManager;
    ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_upload);

        TextView tagpeople_button = (TextView) findViewById(R.id.tagpeople_button);
        TextView addlocation_button = (TextView) findViewById(R.id.addlocation_button);

        Typeface face1 = Typeface.createFromAsset(UploadActivity.this.getAssets(), "fonts/Brown-Regular.otf");
        tagpeople_button.setTypeface(face1);
        addlocation_button.setTypeface(face1);

        Intent intent = getIntent();

//        TwitterAuthConfig authConfig =  new TwitterAuthConfig("consumerKey", "consumerSecret");
//        Fabric.with(this, new TwitterCore(authConfig), new TweetComposer());

        FacebookSdk.sdkInitialize(this);

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        // this part is optional
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {


            @Override
            public void onSuccess(Sharer.Result result) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });


        isVideo = intent.getBooleanExtra("isVideo",false);
        videoPath = intent.getStringExtra("VideoPath");

        objectID="file://"+intent.getStringExtra("filepath");
        objectPath = intent.getStringExtra("filepath");
        Log.e("abc", "upload debug" + objectID);

        upload_imageview = (ImageView) findViewById(R.id.upload_imageview);

        WindowManager wm = this.getWindowManager();

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        ImageSize mImageSize = new ImageSize(width/2, width/2/16*9);

        ImageLoader.getInstance().loadImage(objectID, mImageSize, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view,
                                          Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                upload_imageview.setImageBitmap(loadedImage);
            }

        });


        upload_edittext = (EditText) findViewById(R.id.upload_edittext);

        upload_edittext.setTypeface(face1);



        upload_share = (ImageButton) findViewById(R.id.upload_sharebutton);
        upload_share.setEnabled(true);
        upload_share.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        upload_share.setEnabled(false);
                        try {
                            final AVFile file = AVFile.withAbsoluteLocalPath("img", objectPath);
                            file.saveInBackground(
                                    new SaveCallback() {
                                        @Override
                                        public void done(AVException e) {
                                            final AVObject photo = new AVObject("Photo");
                                            photo.put("user", AVUser.getCurrentUser());
                                            photo.put("image", file);
                                            photo.put("comment", upload_edittext.getText().toString());

                                            if (!isVideo) {
                                                photo.put("isVideo", false);


                                                AVACL likeACL = new AVACL();
                                                likeACL.setPublicReadAccess(true);// 设置公开的「读」权限，任何人都可阅读
                                                photo.setACL(likeACL);

                                                photo.saveInBackground(
                                                        new SaveCallback() {
                                                            @Override
                                                            public void done(AVException e) {

                                                                if (upload_edittext.getText().length() > 0) {
                                                                    AVObject likeobject = new AVObject("Activity");
                                                                    likeobject.put("type", "comment");
                                                                    likeobject.put("content", upload_edittext.getText().toString());
                                                                    likeobject.put("fromUser", AVUser.getCurrentUser());
                                                                    likeobject.put("toUser", AVUser.getCurrentUser());
                                                                    likeobject.put("photo", photo);


                                                                    AVACL likeACL = new AVACL();
                                                                    likeACL.setPublicReadAccess(true);// 设置公开的「读」权限，任何人都可阅读
                                                                    //likeACL.setWriteAccess(AVUser.getCurrentUser(), true);//为当前用户赋予「写」权限

                                                                    likeobject.setACL(likeACL);

                                                                    likeobject.saveInBackground(new SaveCallback() {
                                                                        @Override
                                                                        public void done(AVException e) {
                                                                            if (e == null) {

                                                                                Log.e("abc", "save successfully");
                                                                                // 存储成功
                                                                            } else {
                                                                                Log.e("abc", "error" + e);
                                                                                // 失败的话，请检查网络环境以及 SDK 配置是否正确
                                                                            }
                                                                        }
                                                                    });
                                                                }

                                                                Intent intent = new Intent(UploadActivity.this, MainActivity.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                startActivity(intent);

                                                            }
                                                        }
                                                );


                                            } else {
                                                photo.put("isVideo", true);

                                                Log.e("abc", "uploading video");

                                                try {
                                                    Log.e("uploadingvideo", "path: " + videoPath);
                                                    final AVFile videofile = AVFile.withAbsoluteLocalPath("video.mp4", videoPath);
                                                    videofile.saveInBackground(
                                                            new SaveCallback() {
                                                                @Override
                                                                public void done(AVException e) {

                                                                    photo.put("video", videofile);


                                                                    AVACL likeACL = new AVACL();
                                                                    likeACL.setPublicReadAccess(true);// 设置公开的「读」权限，任何人都可阅读
                                                                    photo.setACL(likeACL);

                                                                    photo.saveInBackground(
                                                                            new SaveCallback() {
                                                                                @Override
                                                                                public void done(AVException e) {

                                                                                    if (upload_edittext.getText().length() > 0) {
                                                                                        AVObject likeobject = new AVObject("Activity");
                                                                                        likeobject.put("type", "comment");
                                                                                        likeobject.put("content", upload_edittext.getText().toString());
                                                                                        likeobject.put("fromUser", AVUser.getCurrentUser());
                                                                                        likeobject.put("toUser", AVUser.getCurrentUser());
                                                                                        likeobject.put("photo", photo);


                                                                                        AVACL likeACL = new AVACL();
                                                                                        likeACL.setPublicReadAccess(true);// 设置公开的「读」权限，任何人都可阅读
                                                                                        //likeACL.setWriteAccess(AVUser.getCurrentUser(), true);//为当前用户赋予「写」权限

                                                                                        likeobject.setACL(likeACL);

                                                                                        likeobject.saveInBackground(new SaveCallback() {
                                                                                            @Override
                                                                                            public void done(AVException e) {
                                                                                                if (e == null) {

                                                                                                    Log.e("abc", "save successfully");
                                                                                                    // 存储成功
                                                                                                } else {
                                                                                                    Log.e("abc", "error" + e);
                                                                                                    // 失败的话，请检查网络环境以及 SDK 配置是否正确
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }

                                                                                    Intent intent = new Intent(UploadActivity.this, MainActivity.class);
                                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                    startActivity(intent);

                                                                                }
                                                                            }
                                                                    );


                                                                }
                                                            }
                                                    );
                                                } catch (IOException e1) {
                                                    e1.printStackTrace();
                                                }


                                            }


                                        }
                                    }
                            );


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
        );


        ImageButton facebookshare = (ImageButton) findViewById(R.id.upload_facebookshare);
        facebookshare.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.e("abc","facebook share click");

                        if(!isVideo) {
                            Log.e("abc","facebook photo share click");
                            Bitmap image = BitmapFactory.decodeFile(objectPath);
                            //Bitmap image = ...
                            SharePhoto photo = new SharePhoto.Builder()
                                    .setBitmap(image)
                                    .build();
                            SharePhotoContent content = new SharePhotoContent.Builder()
                                    .addPhoto(photo)
                                    .build();

                            shareDialog.show(content);


                        }
                        else {
                            Log.e("abc","facebook video share click");
                            Uri videoFileUri = Uri.parse(videoPath);
                            ShareVideo video = new ShareVideo.Builder()
                                    .setLocalUrl(videoFileUri)
                                    .build();
                            ShareVideoContent content = new ShareVideoContent.Builder()
                                    .setVideo(video)
                                    .build();

                            shareDialog.show(content);
                        }
                    }
                }
        );


        ImageButton twittershare = (ImageButton) findViewById(R.id.upload_twittershare);
        twittershare.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

//                        if(!isVideo) {
//                            Log.e("abc", "facebook photo share click");
//
//
//                            File myImageFile = new File(objectPath);
//                            Uri myImageUri = Uri.fromFile(myImageFile);
//                            TweetComposer.Builder builder = new TweetComposer.Builder(UploadActivity.this)
//                                    .text("")
//                                    .image(myImageUri);
//                            builder.show();
//                        }
//                        else{
//                            File myImageFile = new File(videoPath);
//                            Uri myImageUri = Uri.fromFile(myImageFile);
//                            TweetComposer.Builder builder = new TweetComposer.Builder(UploadActivity.this)
//                                    .text("")
//                                    .image(myImageUri);
//                            builder.show();
//                        }
                    }
                }
        );

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
