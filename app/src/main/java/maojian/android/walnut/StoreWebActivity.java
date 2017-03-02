package maojian.android.walnut;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

/**
 * Created by android on 17/1/17.
 */
public class StoreWebActivity  extends Activity {

    private WebView webStoreView;

    private ImageButton webStore_backbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_storeweb);

        webStoreView = (WebView) findViewById(R.id.webStoreView);
        webStoreView.loadUrl("https://m.dji.com");

        webStoreView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return false;
            }
        });

        webStore_backbutton = (ImageButton) findViewById(R.id.webstore_backbutton);
        webStore_backbutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(webStoreView.canGoBack())
                            webStoreView.goBack();
                        else
                            StoreWebActivity.this.finish();
                    }
                }
        );
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

//    int action = event.getAction();
//    if (action== KeyEvent.ACTION_UP) {
//            count--;
//            Log.e("volume key debug","ACTION_UP "+count);
//
//            return true;
//        }

            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:

                    if(webStoreView.canGoBack())
                        webStoreView.goBack();
                    else
                        StoreWebActivity.this.finish();

                    return  true;
            }

            return super.onKeyDown(keyCode, event);
        }

}
