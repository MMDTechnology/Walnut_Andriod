package maojian.android.walnut;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVUser;

public abstract class AnyTimeActivity extends FragmentActivity implements
        View.OnClickListener {

    public AnyTimeActivity activity;
    private String userId;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        userId = null;
        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getObjectId();
        }

    }

    public void setTitle(String title) {
        tvTitle = (TextView) findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
        findViewById(R.id.iv_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.iv_back && findViewById(R.id.iv_back) != null) finish();
        onClickEvent(v);
    }

    /**
     * onclick事件
     */
    public abstract void onClickEvent(View v);

    public String getUserId() {
        return userId;
    }

    protected void showError(String errorMessage) {
        showError(errorMessage, activity);
    }

    public void showError(String errorMessage, Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(
                        activity.getResources().getString(
                                R.string.dialog_message_title))
                .setMessage(errorMessage)
                .setNegativeButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }

    protected void onPause() {
        super.onPause();
        AVAnalytics.onPause(this);
    }

    protected void onResume() {
        super.onResume();
        AVAnalytics.onResume(this);
    }
}
