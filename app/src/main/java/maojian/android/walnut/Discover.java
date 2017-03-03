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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
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
import maojian.android.walnut.base.DialogClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressLint("NewApi")
public class Discover extends Fragment  implements
        SearchView.OnQueryTextListener {

    private GridView listView;
    private ListView lv;
    private SearchView sv;

    private userlistAdapter iadapter;

    private ImageAdapter discoverpostadapter;

    public List<AVObject> postobjectArray;

    public List<AVObject> funspotobjectArray;

    public static List<AVObject> postfilterobjectArray;

    private ArrayList<String> PostImageurl;

    private List<AVObject> PostImageCount;

    private final String[] postUrl = new String[400];

    private final String[] profileUrl = new String[400];

    private boolean isSearching = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);

        PostImageurl = new ArrayList<>();

        postobjectArray = new ArrayList<AVObject>();
        postfilterobjectArray = new ArrayList<AVObject>();
        PostImageCount = new ArrayList<AVObject>();

        funspotobjectArray = new ArrayList<AVObject>();

        getWallImages();

        getUserlist();


//        sv = (SearchView) rootView.findViewById(R.id.searchView);
//        sv.setQueryHint("Search");
//        sv.setIconifiedByDefault(true);
//
//        sv.setOnQueryTextListener(this);
        final EditText searchEdit = (EditText) rootView.findViewById(R.id.searchedit);
        final ImageView searchClear = (ImageView) rootView.findViewById(R.id.searchclear);

        searchEdit.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {

                        if(b){
                            lv.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.INVISIBLE);
                            isSearching = true;
                            searchClear.setBackground(getResources().getDrawable(R.drawable.ic_close));

                        }
                        else{
                            lv.setVisibility(View.INVISIBLE);
                            listView.setVisibility(View.VISIBLE);
                            isSearching = false;
                            searchClear.setBackground(getResources().getDrawable(R.drawable.ic_search));
                        }

                    }
                }
        );

        searchEdit.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                        Log.e("abc", "filtertext" + editable.toString());

                        Log.e("abc", "clearfilter0 " + postfilterobjectArray.size() + ":: " + postobjectArray.size());
                        postfilterobjectArray.clear();
                        Log.e("abc", "clearfilter " + postfilterobjectArray.size() + ":: " + postobjectArray.size());

                        int j = 0;
                        for (int i = 0; i < postobjectArray.size(); i++) {

                            AVUser a = (AVUser) postobjectArray.get(i);
                            Log.e("abc", "debugging0: " + postobjectArray.size());

                            if (a.getUsername().contains(editable)) {
                                postfilterobjectArray.add((AVUser) postobjectArray.get(i));
                                profileUrl[j] = a.getAVFile("profileImage").getUrl();
                                j++;
                            }
                            Log.e("abc", "debugging1: " + postfilterobjectArray.size());

                            //iadapter.notifyDataSetChanged();
                        }

                        //profileUrl = new String[postfilterobjectArray.size()];

                        if (editable.length() == 0) {
                            postfilterobjectArray.clear();
                            for (int i = 0; i < postobjectArray.size(); i++) {

                                postfilterobjectArray.add((AVUser) postobjectArray.get(i));
                                profileUrl[i] = ((AVUser) postobjectArray.get(i)).getAVFile("profileImage").getUrl();

                            }

                        }

                        iadapter.notifyDataSetChanged();
                        //iadapter = new userlistAdapter(getActivity());
                        //lv.setAdapter(iadapter);
                    }
                }
        );




        searchClear.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (isSearching) {
                            lv.setVisibility(View.INVISIBLE);
                            listView.setVisibility(View.VISIBLE);
                            searchEdit.clearFocus();

//                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                            postfilterobjectArray.clear();
                            for (int i = 0; i < postobjectArray.size(); i++) {

                                postfilterobjectArray.add((AVUser) postobjectArray.get(i));

                            }

                        }


                    }
                }
        );





        lv = (ListView) rootView.findViewById(R.id.discoverlistview);
        lv.setTextFilterEnabled(true);//设置lv可以被过虑

        iadapter = new userlistAdapter(getActivity());
        lv.setAdapter(iadapter);

        lv.setVisibility(View.INVISIBLE);

        listView = (GridView) rootView.findViewById(R.id.discovergridView);


        discoverpostadapter  = new ImageAdapter(getActivity());
        ((GridView) listView).setAdapter(discoverpostadapter);


//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //startImagePagerActivity(position);
//            }
//        });
//        DialogClass.LoginDialog(getActivity());

        return rootView;
        //return inflater.inflate(R.layout.fragment_discover, container, false);

    }

    @Override
    public boolean onQueryTextChange(String newText) {



        if (TextUtils.isEmpty(newText)) {
            // 清除ListView的过滤
            postfilterobjectArray = postobjectArray;
            lv.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            lv.clearTextFilter();
        } else {
            // 使用用户输入的内容对ListView的列表项进行过滤
            lv.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
            lv.setFilterText(newText);
            Log.e("abc", "debugging" + newText);

            postfilterobjectArray.clear();

            for(int i=0;i<postobjectArray.size();i++){

                AVUser a = (AVUser) postobjectArray.get(i);
                Log.e("abc", "debugging0: " + postobjectArray.size());
                if(a.getUsername().contains(newText)){
                    postfilterobjectArray.add((AVUser) postobjectArray.get(i));
                }
                Log.e("abc", "debugging1: " + postobjectArray.size());

                iadapter.notifyDataSetChanged();
            }

        }
        return true;
    }

    // 单击搜索按钮时激发该方法
    @Override
    public boolean onQueryTextSubmit(String query) {
        // 实际应用中应该在该方法内执行实际查询
        // 此处仅使用Toast显示用户输入的查询内容
        //Toast.makeText(this, "您的选择是:" + query, Toast.LENGTH_SHORT).show();

        sv.clearFocus();
        return false;
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
            return postfilterobjectArray.size();
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
                view.setTag(holder);
            } else {
                holder = (userListHolder) view.getTag();
            }

            holder.followbutton.setVisibility(View.INVISIBLE);

            if(postfilterobjectArray.size()>0){
            AVUser x = (AVUser)postfilterobjectArray.get(position);
            holder.username.setText((String) x.getUsername());
            Log.e("abc", "bugdiscover" + IMAGE_URLSS[position] + " username: " + (String) x.getUsername());
            }

            holder.profileimageView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Log.e("abc","discover: "+position);

                            Intent intent = new Intent(getActivity(), customProfileActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY__TO_FRONT);
                            Log.e("abc","testingt "+position+" objectID "+postfilterobjectArray.get(position).getObjectId());
                            intent.putExtra("ObjectId", postfilterobjectArray.get(position).getObjectId());
                            startActivity(intent);

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
    }


//    /**
//     * 动态设置ListView的高度
//     * @param listView
//     */
//    public static void setListViewHeightBasedOnChildren(ListView listView) {
//        if(listView == null) return;
//        ListAdapter listAdapter = listView.getAdapter();
//        if (listAdapter == null) {
//            // pre-condition
//            return;
//        }
//        int totalHeight = 0;
//        for (int i = 0; i < listAdapter.getCount(); i++) {
//            View listItem = listAdapter.getView(i, null, listView);
//            listItem.measure(0, 0);
//            totalHeight += listItem.getMeasuredHeight();
//        }
//        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
//        listView.setLayoutParams(params);
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
        }

        @Override
        public int getCount() {
            //Log.e("discoverdebug","getcount "+PostImageCount.size());
            return PostImageCount.size();
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
            //listView.setNumColumns(1);

            //Log.e("discoverdebug","  "+funspotobjectArray.size());

            if(position == 0){
                convertView = inflater.inflate(R.layout.item_solution2_top, null);
                convertView.setLayoutParams(new AbsListView.LayoutParams(getResources().getDisplayMetrics().widthPixels,900));

                if(funspotobjectArray.size()>0) {
                    ImageView funspot1 = (ImageView) convertView.findViewById(R.id.funspot_image1);
                    ImageView funspot2 = (ImageView) convertView.findViewById(R.id.funspot_image2);
                    ImageView funspot3 = (ImageView) convertView.findViewById(R.id.funspot_image3);

                    ImageLoader.getInstance().displayImage(funspotobjectArray.get(0).getAVFile("image").getUrl(), funspot1, options, animateFirstListener);
                    ImageLoader.getInstance().displayImage(funspotobjectArray.get(1).getAVFile("image").getUrl(), funspot2, options, animateFirstListener);
                    ImageLoader.getInstance().displayImage(funspotobjectArray.get(2).getAVFile("image").getUrl(), funspot3, options, animateFirstListener);

                    TextView funspot_comment1 = (TextView) convertView.findViewById(R.id.funspot_comment1);
                    TextView funspot_state1 = (TextView) convertView.findViewById(R.id.funspot_state1);

                    TextView funspot_comment2 = (TextView) convertView.findViewById(R.id.funspot_comment2);
                    TextView funspot_state2 = (TextView) convertView.findViewById(R.id.funspot_state2);

                    TextView funspot_comment3 = (TextView) convertView.findViewById(R.id.funspot_comment3);
                    TextView funspot_state3 = (TextView) convertView.findViewById(R.id.funspot_state3);

                    Typeface face = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Brown-Regular.otf");
                    funspot_comment1.setTypeface(face);
                    funspot_state1.setTypeface(face);
                    funspot_comment2.setTypeface(face);
                    funspot_state2.setTypeface(face);
                    funspot_comment3.setTypeface(face);
                    funspot_state3.setTypeface(face);

                    funspot_comment1.setText(funspotobjectArray.get(0).getString("comment"));
                    funspot_state1.setText(funspotobjectArray.get(0).getString("State"));

                    funspot_comment2.setText(funspotobjectArray.get(1).getString("comment"));
                    funspot_state2.setText(funspotobjectArray.get(1).getString("State"));

                    funspot_comment3.setText(funspotobjectArray.get(2).getString("comment"));
                    funspot_state3.setText(funspotobjectArray.get(2).getString("State"));

                    funspot1.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getActivity(), FunspotActivity.class);

                                    intent.putExtra("ObjectId", funspotobjectArray.get(0).getObjectId());
                                    startActivity(intent);
                                }
                            }
                    );
                    funspot2.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getActivity(), FunspotActivity.class);

                                    intent.putExtra("ObjectId", funspotobjectArray.get(1).getObjectId());
                                    startActivity(intent);
                                }
                            }
                    );
                    funspot3.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getActivity(), FunspotActivity.class);

                                    intent.putExtra("ObjectId", funspotobjectArray.get(2).getObjectId());
                                    startActivity(intent);
                                }
                            }
                    );
                }

                return convertView;
            }
            if(position == 1){
                convertView = inflater.inflate(R.layout.item_solution2_top, null);
                convertView.setVisibility(View.GONE);
                //convertView.setLayoutParams(new AbsListView.LayoutParams(getResources().getDisplayMetrics().widthPixels, 50));
                return convertView;
            }


            //listView.setNumColumns(2);

            ViewHolder holder;
            View view = convertView;

//            convertView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
//                    LinearLayout.LayoutParams.FILL_PARENT,
//                    (float) 4.0));

            if ((view == null&& inflater != null)||convertView.getTag() == null) {
                view = inflater.inflate(R.layout.discovergriditem, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            Log.e("abc","testingt "+position+" objectID "+holder);

            holder.imageView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent intent = new Intent(getActivity(), DetailActivity.class);
                            //intent.addFlags(Intent.FLAG_ACTIVITY__TO_FRONT);

                            int position1 = position-2;

                            intent.putExtra("ObjectId", PostImageCount.get(position1).getObjectId());
                            startActivity(intent);

                        }
                    }
            );

            ImageLoader.getInstance().displayImage(IMAGE_URLS[position-2], holder.imageView, options, animateFirstListener);

            return view;
        }
    }

    static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
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
        AVQuery<AVObject> discoverpostquery = new AVQuery<>("_User");

        if(AVUser.getCurrentUser()!=null){
            discoverpostquery.whereNotEqualTo("username",AVUser.getCurrentUser().getUsername());
        }

        discoverpostquery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if(list!=null) {
                    postobjectArray = list;

                    Log.e("abc","ddebugg"+postobjectArray.size());

                    for(int i=0;i<postobjectArray.size();i++){

                     postfilterobjectArray.add((AVUser)postobjectArray.get(i));

                    }
                   // postfilterobjectArray = postobjectArray;

                    Log.e("abc","loglog"+list.size());
                    for (int i = 0; i < list.size(); i++) {

                        final AVObject post = (AVObject) list.get(i);

                        AVFile postImage = post.getAVFile("profileImage");

                        //PostImageurl.add(postImage.getUrl());

                        Log.e("abc","loglog "+i+"  "+postImage.getUrl());

                        profileUrl[i] = postImage.getUrl();

                    }

                   // iadapter.notifyDataSetChanged();



                }
            }
        });


    }

    public void getWallImages() {

        AVQuery<AVObject> discoverpostquery = new AVQuery<>("Photo");
        discoverpostquery.orderByDescending("createdAt");

        discoverpostquery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                if (list != null) {

                    PostImageCount = list;

                    for (int i = 0; i < list.size(); i++) {

                        final AVObject post = (AVObject) list.get(i);
                        Log.e("abc", "strange2 " + i);
                        AVFile postImage = post.getAVFile("image");
                        Log.e("abc", "strange3 " + postImage.getUrl());
                        //PostImageurl.add(postImage.getUrl());
                        postUrl[i] = postImage.getUrl();

                    }
                }

                discoverpostadapter.notifyDataSetChanged();
            }
        });

        AVQuery<AVObject> funspotquery = new AVQuery<>("FunSpot");
        funspotquery.orderByDescending("createdAt");
        funspotquery.findInBackground(
                new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> list, AVException e) {
                        if (list != null) {

                            funspotobjectArray = list;

                        }
                    }
                }
        );

    }



    @Override
    public void onResume() {

        super.onResume();
        getWallImages();
        getUserlist();
        Log.e("abc", "discover resume");

    }

}