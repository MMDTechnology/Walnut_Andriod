package maojian.android.walnut;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;

import maojian.android.walnut.ImagePicker.ProfilePickerActivity;
import maojian.android.walnut.me.ChangePasswordActivity;

/**
 * Created by jie on 2016/8/1.
 */
public class EditprofileActivity extends AnyTimeActivity {

    private ImageView avatar;
    DisplayImageOptions avatarstyle;
    private EditText username;
    private EditText useremail;
    private Button epsave;

    private EditText usergender;
    private EditText userskatename;
    private EditText userbio;
//    private EditText userwebsite;

    private TextView editprofile_button;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_editprofile);

        avatarstyle = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer(Color.WHITE, 1))
                .build();

        final View epview = getWindow().getDecorView();
        epview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                epview.setFocusable(true);
                epview.setFocusableInTouchMode(true);
                epview.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(username.getWindowToken(), 0);
                return false;
            }
        });

        avatar = (ImageView) findViewById(R.id.imageView2);
        username = (EditText) findViewById(R.id.epusername);
        useremail = (EditText) findViewById(R.id.epmail);
        epsave = (Button) findViewById(R.id.epsave);
        getdata();
        epsave.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AVObject todo = AVObject.createWithoutData("_User", AVUser.getCurrentUser().getObjectId());

                        // 修改 content
                        todo.put("username", username.getText().toString());
                        todo.put("email", useremail.getText().toString());

                        todo.put("gender", usergender.getText().toString());
                        todo.put("bio", userbio.getText().toString());
                        todo.put("skatebotName", userskatename.getText().toString());
//                        todo.put("personalwebsite", userwebsite.getText().toString());

                        // 保存到云端
                        todo.saveInBackground(
                                new SaveCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        if (e == null) {

                                            Log.e("abc", "Save done");

                                        } else {
                                            Log.e("abc", "Save exception" + e);
                                        }
                                    }
                                }
                        );
                    }
                }
        );
        findViewById(R.id.epbackButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        EditprofileActivity.this.finish();
                    }
                }
        );

        editprofile_button = (TextView) findViewById(R.id.editprofile_editbutton);
        editprofile_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(EditprofileActivity.this, ProfilePickerActivity.class);
                        startActivity(intent);


                    }
                }
        );

        Button logout_button = (Button) findViewById(R.id.logout_button);
        logout_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AVService.logout();
                        Intent loginIntent = new Intent(EditprofileActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        EditprofileActivity.this.finish();
                    }
                }
        );


        usergender = (EditText) findViewById(R.id.epusergender);
        userskatename = (EditText) findViewById(R.id.epskatebotname);
        userbio = (EditText) findViewById(R.id.epuserbio);
//        userwebsite = (EditText) findViewById(R.id.epwebsite);

        findViewById(R.id.report_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditprofileActivity.this, FeedbackActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.changepassword).setOnClickListener(this);
        findViewById(R.id.pushsetting).setOnClickListener(this);
        findViewById(R.id.savephoto).setOnClickListener(this);

    }

    @Override
    protected void onResume() {

        super.onResume();

        getdata();

    }

    @Override
    public void onClickEvent(View v) {
        switch (v.getId()) {
            case R.id.changepassword:
                startActivity(new Intent(EditprofileActivity.this, ChangePasswordActivity.class));
                break;
            case R.id.pushsetting:

                v.setSelected(!v.isSelected());
                break;
            case R.id.savephoto:
                v.setSelected(!v.isSelected());
                break;
        }

    }

    public void getdata() {
        AVQuery<AVObject> avQuery = new AVQuery<>("_User");
        avQuery.getInBackground((String) AVUser.getCurrentUser().getObjectId(), new GetCallback<AVObject>() {
            @Override
            public void done(AVObject avObject, AVException e) {
                if (avObject == null) return;
                ImageLoader.getInstance().displayImage(avObject.getAVFile("profileImage").getUrl(), avatar, avatarstyle);
                username.setText(avObject.getString("username"));
                useremail.setText(avObject.getString("email"));

                usergender.setText(avObject.getString("gender"));
                userbio.setText(avObject.getString("bio"));
                userskatename.setText(avObject.getString("skatebotName"));
//                userwebsite.setText(avObject.getString("personalwebsite"));
            }
        });
    }
}
