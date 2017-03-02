package maojian.android.walnut;

/**
 * Created by android on 19/7/16.
 */

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@SuppressLint("NewApi")
public class iMessage extends Fragment implements ReFlashListView.IReflashListener
{
    private iMessageAdapter messageadapter;
    public List<AVObject> postobjectArray;

    private ListView lv;

    private ReFlashListView mlistview;

    private final String[] profileUrl = new String[100];

    private final String[] postUrl = new String[100];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.fragment_message, container, false);

        postobjectArray = new ArrayList<AVObject>();

        getWallImages();

        //lv = (ListView) rootView.findViewById(R.id.message_listview);

        mlistview =  ((ReFlashListView) rootView.findViewById(R.id.message_listview) );
        mlistview.setInterface(this);


//        messageadapter = new iMessageAdapter(getActivity());
//        lv.setAdapter(messageadapter);


        return rootView;

    }

    @Override
    public void onReflash() {
        AVQuery<AVObject> discoverpostquery = new AVQuery<>("Activity");
        discoverpostquery.include("fromUser");
        discoverpostquery.include("photo");
        discoverpostquery.whereEqualTo("toUser", AVUser.getCurrentUser());
        discoverpostquery.whereNotEqualTo("fromUser", AVUser.getCurrentUser());

        discoverpostquery.orderByDescending("createdAt");
        discoverpostquery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                postobjectArray = list;

                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {

                        AVObject post = (AVObject) list.get(i);

                        AVUser fromuser = (AVUser) post.get("fromUser");

                        AVFile profuleImage = fromuser.getAVFile("profileImage");

                        if (post.get("photo") != null) {
                            AVObject a = (AVObject) post.get("photo");
                            postUrl[i] = a.getAVFile("image").getUrl();
                        }

                        profileUrl[i] = profuleImage.getUrl();

                    }
                }
                //messageadapter = new iMessageAdapter(getActivity());
                // lv.setAdapter(messageadapter);
                //mlistview.setAdapter(messageadapter);

                messageadapter.notifyDataSetChanged();
                mlistview.reflashComplete();
            }
        });
    }

//    private class GetDataTask extends AsyncTask<Void, Void, String[]> {
//
//        @Override
//        protected String[] doInBackground(Void... params) {
//            // Simulates a background job.
//
//            AVQuery<AVObject> discoverpostquery = new AVQuery<>("Activity");
//            discoverpostquery.include("fromUser");
//            discoverpostquery.include("photo");
//            discoverpostquery.whereEqualTo("toUser", AVUser.getCurrentUser());
//            discoverpostquery.whereNotEqualTo("fromUser", AVUser.getCurrentUser());
//
//            discoverpostquery.orderByDescending("createdAt");
//
//            discoverpostquery.findInBackground(new FindCallback<AVObject>() {
//                @Override
//                public void done(List<AVObject> list, AVException e) {
//
//                    postobjectArray = list;
//
//                    if (list != null) {
//                        for (int i = 0; i < list.size(); i++) {
//
//                            AVObject post = (AVObject) list.get(i);
//
//                            AVUser fromuser = (AVUser) post.get("fromUser");
//
//                            AVFile profuleImage = fromuser.getAVFile("profileImage");
//
//                            if (post.get("photo") != null) {
//                                AVObject a = (AVObject) post.get("photo");
//                                postUrl[i] = a.getAVFile("image").getUrl();
//                            }
//
//                            profileUrl[i] = profuleImage.getUrl();
//
//                        }
//                    }
//                    //messageadapter = new iMessageAdapter(getActivity());
//                    // lv.setAdapter(messageadapter);
//                    //mlistview.setAdapter(messageadapter);
//
//
//                    messageadapter.notifyDataSetChanged();
//
//
//                }
//            });
//
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                ;
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String[] result) {
//            //mListItems.addFirst("Added after refresh...");
//            // Call onRefreshComplete when the list has been refreshed.
//            //   mlistview.onRefreshComplete();
//            super.onPostExecute(result);
//        }
//    }

    private class iMessageAdapter extends BaseAdapter {

        private  String[] IMAGE_URLS = profileUrl;



        private LayoutInflater inflater;


        private DisplayImageOptions options;
        private DisplayImageOptions postoptions;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        iMessageAdapter(Context context) {
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
            postoptions = new DisplayImageOptions.Builder()
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
            if(postobjectArray!=null)
                return postobjectArray.size();
            else
                return 0;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            //         if (view == null&& inflater != null) {
            view = inflater.inflate(R.layout.message_listitem, parent, false);
            holder = new ViewHolder();
            assert view != null;
            holder.imageView = (ImageView) view.findViewById(R.id.message_userprofile);
            holder.username = (TextView) view.findViewById(R.id.message_username);

            Typeface face1 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Brown-Light.otf");
            holder.username.setTypeface(face1);
            holder.messagecontent = (TextView) view.findViewById(R.id.message_content);
            holder.messagecontent.setTypeface(face1);

            holder.messagepost = (ImageView) view.findViewById(R.id.message_post);
            view.setTag(holder);

//            } else {
//                holder = (ViewHolder) view.getTag();
//            }
            Typeface face2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Brown-Regular.otf");

            if(postobjectArray.size()>0) {
                AVObject post = (AVObject) postobjectArray.get(position);

                Date curDate = new Date(System.currentTimeMillis());

                long difference = post.getCreatedAt().getTime() - curDate.getTime();
                Log.v("Time difference:", String.valueOf(difference));

                String timediff = "today";

                if ((-difference) < 60 * 1000) {
                    timediff = (-difference / 1000) + " s";
                    Log.e("abc", "timedebug " + timediff + "  " + difference);
                } else if ((-difference) < 60 * 60 * 1000) {
                    timediff = (-difference / 1000 / 60) + " m";

                } else if ((-difference) < 60 * 60 * 24 * 1000) {
                    timediff = (-difference / 1000 / 60 / 60) + " h";
                } else {
                    //Log.e("timedebugging","??? "+(int)(-difference/1000/60/60/24)+"difff "+difference);
                    timediff = (-difference / 1000 / 60 / 60 / 24) + " d";
                }



                AVUser fromuser = (AVUser) post.get("fromUser");

                holder.username.setText(fromuser.getUsername());

                holder.messagecontent.setText((String) post.get("type"));

                if(((String) post.get("type")).equals("follow")){
                    Log.e("message", "followpost" + position);

//                    String w = "Started following you.  "+timediff;
//                    int start = 0;
//                    int end = w.indexOf('u');
//                    Spannable word = new SpannableString(w);
//                    word.setSpan(new StyleSpan(Typeface.BOLD), start, end,
//                            Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                    holder.messagecontent.setText("Started following you.  "+timediff);
                    //holder.messagepost.setBackgroundColor(Color.parseColor("#FFFFFF"));

                }
                else if(((String) post.get("type")).equals("comment")){
                    Log.e("message", "commentpost" + position);
                    holder.messagecontent.setText("Commented you.  "+timediff);
                    ImageLoader.getInstance().displayImage(postUrl[position], holder.messagepost, postoptions, animateFirstListener);

                }
                else if(((String) post.get("type")).equals("like")){
                    Log.e("message", "likepost" + position);
                    holder.messagecontent.setText("Liked your post.  "+timediff);
                    ImageLoader.getInstance().displayImage(postUrl[position], holder.messagepost, postoptions, animateFirstListener);
                }


            }

            ImageLoader.getInstance().displayImage(IMAGE_URLS[position], holder.imageView, options, animateFirstListener);

            return view;
        }
    }

    static class ViewHolder {
        ImageView imageView;
        TextView username;
        TextView messagecontent;

        ImageView messagepost;
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

    public void getWallImages() {

        AVQuery<AVObject> discoverpostquery = new AVQuery<>("Activity");
        discoverpostquery.include("fromUser");
        discoverpostquery.include("photo");
        discoverpostquery.whereEqualTo("toUser", AVUser.getCurrentUser());
        discoverpostquery.whereNotEqualTo("fromUser", AVUser.getCurrentUser());

        discoverpostquery.orderByDescending("createdAt");

        discoverpostquery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                postobjectArray = list;

                if(list!=null) {
                    for (int i = 0; i < list.size(); i++) {

                        AVObject post = (AVObject) list.get(i);

                        AVUser fromuser = (AVUser)post.get("fromUser");

                        AVFile profuleImage = fromuser.getAVFile("profileImage");

                        if(post.get("photo")!=null){
                            AVObject a = (AVObject)post.get("photo");
                            postUrl[i] = a.getAVFile("image").getUrl();
                        }

                        profileUrl[i] = profuleImage.getUrl();

                    }
                }

                messageadapter = new iMessageAdapter(getActivity());
                // lv.setAdapter(messageadapter);
                //   messageadapter.notifyDataSetChanged();

                mlistview.setAdapter(messageadapter);
            }
        });
    }

    @Override
    public void onResume() {

        super.onResume();
        getWallImages();
        Log.e("abc", "home resume");

    }


}