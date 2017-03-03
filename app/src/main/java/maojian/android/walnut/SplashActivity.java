package maojian.android.walnut;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Window;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        doSplashTask();
    }

    private void doSplashTask() {
        // 延时800ms执行
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();

                intent.setClass(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();


            }
        }, 1000);
    }
}
