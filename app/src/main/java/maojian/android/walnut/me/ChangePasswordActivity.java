package maojian.android.walnut.me;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.avos.avoscloud.AVAnalytics;
import maojian.android.walnut.AnyTimeActivity;
import maojian.android.walnut.R;

public class ChangePasswordActivity extends AnyTimeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        setTitle("ChangePassword");
    }
    protected void onResume() {
        super.onResume();

    }
    @Override
    public void onClickEvent(View v) {

    }
}
