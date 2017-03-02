package maojian.android.walnut;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.avos.avoscloud.AVACL;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;

/**
 * Created by android on 19/9/16.
 */
public class FeedbackActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contact);

        ImageButton backButton = (ImageButton) findViewById(R.id.feedback_backbutton);

        backButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        FeedbackActivity.this.finish();
                    }
                }
        );

        final Button postButton = (Button) findViewById(R.id.feedback_sendbutton);
        final EditText searchEdit = (EditText) findViewById(R.id.feedback_edittext);
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
                        if(editable.length()>0){
                            postButton.setTextColor(Color.parseColor("#00EAAB"));
                            postButton.setEnabled(true);

                        }
                        else  {
                            postButton.setTextColor(Color.parseColor("#E4E4E4"));
                            postButton.setEnabled(false);
                        }


                    }
                }
        );

        postButton.setEnabled(false);

        postButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        postButton.setEnabled(false);

                        AVObject likeobject = new AVObject("Activity");
                        likeobject.put("type", "feedback");
                        likeobject.put("content", searchEdit.getText().toString());
                        likeobject.put("fromUser", AVUser.getCurrentUser());




                        AVACL likeACL = new AVACL();
                        likeACL.setPublicReadAccess(true);// 设置公开的「读」权限，任何人都可阅读
                        //likeACL.setWriteAccess(AVUser.getCurrentUser(), true);//为当前用户赋予「写」权限

                        likeobject.setACL(likeACL);

                        likeobject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    // 存储成功
                                    Log.e("abc", "commentObject success");
                                    Toast.makeText(FeedbackActivity.this, "Feedback Sent", Toast.LENGTH_SHORT).show();

//                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                                    FeedbackActivity.this.finish();

//                                    AVQuery pushQuery = AVInstallation.getQuery();
//                                    pushQuery.whereEqualTo("owner", commentObject.get("user"));
//
//                                    AVPush.sendMessageInBackground(AVUser.getCurrentUser().getUsername() + " commented your post", pushQuery, new SendCallback() {
//                                        @Override
//                                        public void done(AVException e) {
//                                            Log.e("abc", "comment push done");
//                                        }
//                                    });




                                } else {
                                    Log.e("abc", "error" + e);
                                    // 失败的话，请检查网络环境以及 SDK 配置是否正确
                                }
                            }
                        });

                    }
                }
        );


    }



}
