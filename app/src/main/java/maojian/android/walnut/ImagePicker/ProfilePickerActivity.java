package maojian.android.walnut.ImagePicker;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import maojian.android.walnut.ImagePicker.imageselector_view.ProfileClipImageLayout;
import maojian.android.walnut.R;

/**
 * Created by android on 6/8/16.
 */
public class ProfilePickerActivity extends Activity {

    private ArrayList<Floder> mDirPaths = new ArrayList<Floder>();

    private Floder imageAll, currentImageFolder;

    private GridView listView;

    private List<AVObject> PostImageCount;

    private final String[] postUrl = new String[500];

    private ImageAdapter discoverpostadapter;

    private ImageView imagepicker_imageview;

    private ImageButton imagepicker_next;
    private ImageButton imagepicker_return;

    private ProfileClipImageLayout mClipImageLayout;
    private View contview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_profilepicker);

        initViews();

        listView = (GridView) findViewById(R.id.profileimagepicker_gridView);
        discoverpostadapter  = new ImageAdapter(ProfilePickerActivity.this);
        ((GridView) listView).setAdapter(discoverpostadapter);

        imagepicker_return= (ImageButton) findViewById(R.id.profileimagepicker_returnbutton);
        imagepicker_return.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProfilePickerActivity.this.finish();
                    }
                }
        );

        imagepicker_next = (ImageButton) findViewById(R.id.profileimagepicker_nextbutton);
        imagepicker_next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //mClipImageLayout.clip();
                        imagepicker_next.setEnabled(false);
                        Log.e("abc","click debug");

                        File pictureFile = getOutputMediaFile();
                        if (pictureFile == null){
                            Log.d("abc", "Error creating media file, check storage permissions: ");
                            return;
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        mClipImageLayout.clip().compress(Bitmap.CompressFormat.PNG, 100, baos);

                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(baos.toByteArray());
                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.d("abc", "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d("abc", "Error accessing file: " + e.getMessage());
                        }


                        final AVFile profileImage;

                        try {
                            profileImage = AVFile.withAbsoluteLocalPath("img", pictureFile.toString());
                            Log.e("profilepicker","debug!  "+pictureFile.toString());

                            profileImage.saveInBackground(
                                    new SaveCallback() {
                                        @Override
                                        public void done(AVException e) {
                                            Log.e("profilepicker","debug");
                                            AVUser cUser = AVUser.getCurrentUser();
                                            cUser.put("profileImage",profileImage);
                                            cUser.saveInBackground(
                                                    new SaveCallback() {
                                                        @Override
                                                        public void done(AVException e) {

                                                            Log.e("profilepicker","debug111");

                                                            //ProfilePickerActivity.this.finish();
                                                        }
                                                    }
                                            );

                                        }
                                    }
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }




//                        Intent intent = new Intent(ProfilePickerActivity.this, UploadActivity.class);
//                        intent.putExtra("filepath", pictureFile.toString());
//                        startActivity(intent);

                    }
                }
        );


    }

    private File getOutputMediaFile(){
        //get the mobile Pictures directory
        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        //get the current time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(picDir.getPath() + File.separator + "IMAGE_"+ timeStamp + ".jpg");
    }

    private void initViews() {

        imageAll = new Floder();
        imageAll.setDir("/所有图片");
        currentImageFolder = imageAll;
        mDirPaths.add(imageAll);

        getThumbnail();
    }

    /**
     * 得到缩略图
     */
    private void getThumbnail() {
        /**
         * 临时的辅助类，用于防止同一个文件夹的多次扫描
         */
        HashMap<String, Integer> tmpDir = new HashMap<String, Integer>();

        Cursor mCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.ImageColumns.DATA}, "", null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC");
        Log.e("TAG0", mCursor.getCount() + "");
        if (mCursor.moveToFirst()) {
            do {
                // 获取图片的路径

                String path = mCursor.getString(0);

                if (path != null) {
                    Log.e("TAG1", path);

                    path = "file://"+path;

                    imageAll.images.add(new ImageItem(path));
                    // 获取该图片的父路径名
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null) {
                        continue;
                    }
                    Floder imageFloder = null;
                    String dirPath = parentFile.getAbsolutePath();
                    if (!tmpDir.containsKey(dirPath)) {
                        // 初始化imageFloder
                        imageFloder = new Floder();
                        imageFloder.setDir(dirPath);
                        imageFloder.setFirstImagePath(path);
                        mDirPaths.add(imageFloder);
                        Log.d("zyh", dirPath + "," + path);
                        tmpDir.put(dirPath, mDirPaths.indexOf(imageFloder));
                    } else {
                        imageFloder = mDirPaths.get(tmpDir.get(dirPath));
                    }
                    imageFloder.images.add(new ImageItem(path));

                }
            }
            while (mCursor.moveToNext());
        }


        mCursor.close();

        for (int i = 0; i < mDirPaths.size(); i++) {
            Floder f = mDirPaths.get(i);
            Log.d("zyh", i + "-----" + f.getName() + "---" + f.images.size());
        }
        tmpDir = null;
    }

    class Floder {


        /**
         * 图片的文件夹路径
         */
        private String dir;
        /**
         * 第一张图片的路径
         */
        private String firstImagePath;

        /**
         * 文件夹的名称
         */
        private String name;
        public List<ImageItem> images = new ArrayList<ImageItem>();

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
            int lastIndexOf = this.dir.lastIndexOf("/");
            this.name = this.dir.substring(lastIndexOf);
        }

        public String getFirstImagePath() {
            return firstImagePath;
        }

        public void setFirstImagePath(String firstImagePath) {
            this.firstImagePath = firstImagePath;
        }

        public String getName() {
            return name;
        }

    }

    class ImageItem {

        String path;

        public ImageItem(String p) {
            this.path = p;
        }

    }

//    public void chip(View view) {
//        Bitmap bitmap = mClipImageLayout.clip();
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] datas = baos.toByteArray();
//
//        imageView.setImageBitmap(BitmapFactory.decodeByteArray(datas, 0, datas.length));
//        dialog.show();
//    }

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

//            imagepicker_imageview = (ImageView) findViewById(R.id.imagepicker_imageview);
//            ImageLoader.getInstance().displayImage(imageAll.images.get(0).path, imagepicker_imageview, options, animateFirstListener);

            String path[] = imageAll.images.get(0).path.split("://");

            Log.e("abc","debugdebugimage "+path[1]);


            mClipImageLayout = (ProfileClipImageLayout) findViewById(R.id.profile_id_clipImageLayout);
            //mClipImageLayout.setHorizontalPadding(20);
            contview = findViewById(R.id.profilecontive);
//            mClipImageLayout.getZoomImageView()
//                    .setImageBitmap(BitmapFactory.decodeFile(path[1]));
            ImageLoader.getInstance().displayImage(imageAll.images.get(0).path, mClipImageLayout.getZoomImageView(), options, animateFirstListener);
        }

        @Override
        public int getCount() {
            return imageAll.images.size();
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

                            if(null == imageAll.images.get(position).path)return;

                            //ImageLoader.getInstance().displayImage(imageAll.images.get(position).path, imagepicker_imageview, options, animateFirstListener);

                            String path[] = imageAll.images.get(position).path.split("://");
//                            mClipImageLayout.getZoomImageView()
//                                    .setImageBitmap(BitmapFactory.decodeFile(path[1]));
                            ImageLoader.getInstance().displayImage(imageAll.images.get(position).path, mClipImageLayout.getZoomImageView(), options, animateFirstListener);


//                            Intent intent = new Intent();
//
//                            Uri mUri =  Uri.parse(imageAll.images.get(position).path);
//
//                            intent.setAction("com.android.camera.action.CROP");
//                            intent.setDataAndType(mUri, "image/*");// mUri是已经选择的图片Uri
//                            intent.putExtra("crop", "true");
//                            intent.putExtra("aspectX", 1);// 裁剪框比例
//                            intent.putExtra("aspectY", 1);
//                            intent.putExtra("outputX", 150);// 输出图片大小
//                            intent.putExtra("outputY", 150);
//                            intent.putExtra("return-data", true);
//
//                            ImagePickerActivity.this.startActivityForResult(intent, 200);


                        }
                    }
            );

            ImageLoader.getInstance().displayImage(imageAll.images.get(position).path, holder.imageView, options, animateFirstListener);

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
