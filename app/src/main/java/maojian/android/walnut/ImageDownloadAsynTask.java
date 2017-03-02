package maojian.android.walnut;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownloadAsynTask extends AsyncTask<Void, Void, Drawable> {

    private Context context;
    private String imageUrl;
    private ImageView imageView;
    private String sdPath="/sdcard/netImages";
    ProgressDialog progressDialog;

    public ImageDownloadAsynTask(Context context, String imageUrl,ImageView imageView) {
        this.context=context;
        this.imageUrl=imageUrl;
        this.imageView=imageView;
    }

    /* 后台执行，比较耗时的操作都可以放在这里。注意这里不能直接操作UI
     * 不需要传入什么参数，返回一个Drawable
     */
    @Override
    protected Drawable doInBackground(Void... params) {
        String filename=sdPath+imageUrl.substring(imageUrl.lastIndexOf("/"));
        File file=new File(filename);
        if(file.exists()==true){
            Bitmap bitmap= BitmapFactory.decodeFile(filename);
            BitmapDrawable bitmapDrawable=new BitmapDrawable(bitmap);
            return bitmapDrawable;
        }else{
            try {
                URL url=new URL(imageUrl);
                URLConnection connection=url.openConnection();
                connection.setDoInput(true);// 使用 URL 连接进行输入
                connection.connect();
                InputStream is = connection.getInputStream();
                Bitmap b=BitmapFactory.decodeStream(is);
                BitmapDrawable bd=new BitmapDrawable(b);
                saveFile(bd,filename);
//				connection.getContent();
                return bd;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**通过outPutStream、bitmap.compress(),flush()把图片保存到指定路径
     * @param bd
     * @param filename
     */
    private void saveFile(BitmapDrawable bd, String filename) {
        File file = new File(sdPath);
        if(!file.exists()){
            file.mkdir();
        }
        File f=new File(filename);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
            Bitmap b=bd.getBitmap();
            b.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //progressDialog.show(context, "","正在下载图片。。。");
    }


    /**
     * 相当于Handler 处理UI的方式，在这里面可以使用在doInBackground 得到的结果处理操作UI。
     * 此方法在主线程执行，任务执行的结果作为此方法的参数返回
     */
    @Override
    protected void onPostExecute(Drawable result) {
        super.onPostExecute(result);
        if(result!=null){//如果doInBackground()获取的结果不为空
            imageView.setBackgroundDrawable(result);//那么就在这一步更新UI
        }
        //progressDialog.dismiss();
    }


}
