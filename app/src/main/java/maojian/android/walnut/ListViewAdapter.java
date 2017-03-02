package maojian.android.walnut;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVObject;

import java.util.List;

public class ListViewAdapter extends BaseAdapter {

    private Activity activity;
    private List<AVObject> data;
    private static LayoutInflater inflater=null;
    //public ImageLoader imageLoader; //用来下载图片的类，后面有介绍

    public ListViewAdapter(Activity a, List<AVObject> d) {
        activity = a;

        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        data = AVService.getWallImages();
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.vlist, null);

        TextView username = (TextView)vi.findViewById(R.id.userName); // 标题

        ImageView profile=(ImageView)vi.findViewById(R.id.Profile); // 缩略图
        ImageView post=(ImageView)vi.findViewById(R.id.Post); // 缩略图

        AVObject postImage = data.get(position);

        // 设置ListView的相关值
        username.setText((String) postImage.get("username"));
        //profile.setImageBitmap(postImage.get("Profile"));

        return vi;
         }
}