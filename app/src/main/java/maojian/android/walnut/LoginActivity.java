package maojian.android.walnut;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SignUpCallback;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
//import com.twitter.sdk.android.core.Result;
//import com.twitter.sdk.android.core.TwitterException;
//import com.twitter.sdk.android.core.TwitterSession;
//import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import maojian.android.walnut.login.LoginApi;
import maojian.android.walnut.login.OnLoginListener;
import maojian.android.walnut.login.UserInfo;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LoginActivity extends AnyTimeActivity {

    Button loginButton;
    ImageButton registerButton;
    ImageButton forgetPasswordButton;
    EditText userNameEditText;
    EditText userPasswordEditText;
    private ProgressDialog progressDialog;

    CallbackManager callbackManager;
    private AccessToken accessToken;


    ImageButton facebook_loginButton;

//	private TwitterAuthClient client;

    private ImageButton withoutLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(this);
        super.onCreate(savedInstanceState);
//		TwitterAuthConfig authConfig = new TwitterAuthConfig("u9uyYYnlumipcGUU6YJSlPtno", "kc1UL9CPPI3FDVbGMlYN6PVrCeRzw7cISrJzOA2cShV1T2TzFu");
//		Fabric.with(LoginActivity.this, new Twitter(authConfig));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();


//		try {
//			PackageInfo info = getPackageManager().getPackageInfo(
//					"maojian.android.walnut",
//					PackageManager.GET_SIGNATURES);
//			for (Signature signature : info.signatures) {
//				MessageDigest md = MessageDigest.getInstance("SHA");
//				md.update(signature.toByteArray());
//				Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//			}
//		} catch (PackageManager.NameNotFoundException e) {
//
//		} catch (NoSuchAlgorithmException e) {
//
//		}


//		client= new TwitterAuthClient();
        ShareSDK.initSDK(this);
        AVAnalytics.trackAppOpened(getIntent());

        AVService.initPushService(this);

        loginButton = (Button) findViewById(R.id.button_login);
        registerButton = (ImageButton) findViewById(R.id.button_register);
        forgetPasswordButton = (ImageButton) findViewById(R.id.button_forget_password);
        userNameEditText = (EditText) findViewById(R.id.editText_userName);
        userPasswordEditText = (EditText) findViewById(R.id.editText_userPassword);


        withoutLogin = (ImageButton) findViewById(R.id.withoutLogin);
        withoutLogin.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(LoginActivity.this, DeviceActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.bottom_in,
                                R.anim.bottom_out);
                    }
                }
        );

        if (getUserId() != null) {
            Intent mainIntent = new Intent(activity, MainActivity.class);
            startActivity(mainIntent);
            activity.finish();
        }

        loginButton.setOnClickListener(loginListener);
        registerButton.setOnClickListener(registerListener);
        forgetPasswordButton.setOnClickListener(forgetPasswordListener);


        // Facebook login
        facebook_loginButton = (ImageButton) findViewById(R.id.login_facebook);

        facebook_loginButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("abc", "facebook onclick");
                        login("Facebook");
//						LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends"));
                    }
                }
        );


        //幫 LoginManager 增加callback function

        //這邊為了方便 直接寫成inner class

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            //登入成功

            @Override
            public void onSuccess(LoginResult loginResult) {

                //accessToken之後或許還會用到 先存起來

                accessToken = loginResult.getAccessToken();

                Log.d("FB", "access token got.");

                //send request and call graph api

                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {

                            //當RESPONSE回來的時候

                            @Override
                            public void onCompleted(final JSONObject object, GraphResponse response) {

                                //讀出姓名 ID FB個人頁面連結

                                Log.e("FB1", "complete");
                                Log.e("FB1", object.optString("name"));
                                Log.e("FB1", object.optString("link"));
                                Log.e("FB1", object.optString("id"));

                                AVQuery<AVObject> followquery = new AVQuery<>("_User");
                                followquery.whereEqualTo("socialID", object.optString("id"));

                                Log.e("FB1", "find user");

                                followquery.findInBackground(
                                        new FindCallback<AVObject>() {
                                            @Override
                                            public void done(List<AVObject> list, AVException e) {

                                                Log.e("FB1", "find done " + list);
                                                if (list != null) {

                                                    if (list.size() > 0) {

                                                        AVUser cUser = (AVUser) list.get(0);
                                                        Log.e("abc", "facebook login debug");
                                                        AVUser.logInInBackground(cUser.getUsername(),
                                                                "WALNUTTT",
                                                                new LogInCallback() {
                                                                    public void done(AVUser user, AVException e) {
                                                                        if (user != null) {
                                                                            progressDialogDismiss();
                                                                            Intent mainIntent = new Intent(activity,
                                                                                    MainActivity.class);
                                                                            startActivity(mainIntent);
                                                                            activity.finish();
                                                                        } else {
                                                                            progressDialogDismiss();
                                                                            showLoginError();
                                                                        }
                                                                    }
                                                                });
                                                    } else {
                                                        final SignUpCallback signUpCallback = new SignUpCallback() {
                                                            public void done(AVException e) {

                                                                final AVFile profileImage = new AVFile("profile", "http://ac-4jow6b01.clouddn.com/e342297307144f23.png", new HashMap<String, Object>());
                                                                profileImage.saveInBackground(
                                                                        new SaveCallback() {
                                                                            @Override
                                                                            public void done(AVException e) {

                                                                                AVUser cUser = AVUser.getCurrentUser();
                                                                                cUser.put("profileImage", profileImage);
                                                                                cUser.saveInBackground(
                                                                                        new SaveCallback() {
                                                                                            @Override
                                                                                            public void done(AVException e) {

                                                                                                progressDialogDismiss();
                                                                                                if (e == null) {
                                                                                                    showRegisterSuccess();
                                                                                                    Intent mainIntent = new Intent(activity, MainActivity.class);
                                                                                                    startActivity(mainIntent);
                                                                                                    activity.finish();
                                                                                                } else {
                                                                                                    switch (e.getCode()) {
                                                                                                        case 202:
                                                                                                            showError(activity
                                                                                                                    .getString(R.string.error_register_user_name_repeat));
                                                                                                            break;
                                                                                                        case 203:
                                                                                                            showError(activity
                                                                                                                    .getString(R.string.error_register_email_repeat));
                                                                                                            break;
                                                                                                        default:
                                                                                                            showError(activity
                                                                                                                    .getString(R.string.network_error));
                                                                                                            break;
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                );

                                                                            }
                                                                        }
                                                                );
                                                            }
                                                        };


                                                        //AVFile file = new AVFile("profile", "http://ac-4jow6b01.clouddn.com/e342297307144f23.png", new HashMap<String, Object>());

                                                        AVService.signUp(object.optString("name"), "WALNUTTT", "", object.optString("id"), signUpCallback);


                                                    }

                                                }

                                            }
                                        }
                                );


                            }
                        });

                //包入你想要得到的資料 送出request

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link");
                request.setParameters(parameters);
                request.executeAsync();
            }

            //登入取消

            @Override
            public void onCancel() {
                // App code

                Log.d("FB", "CANCEL");
            }

            //登入失敗

            @Override
            public void onError(FacebookException exception) {
                // App code

                Log.e("abc", exception.toString());
            }
        });


        // Twitter login
//		final TwitterAuthClient mTwitterAuthClient= new TwitterAuthClient();

        ImageButton twitter_custom_button = (ImageButton) findViewById(R.id.login_twitter);
        twitter_custom_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                login("Twitter");


//				client.authorize(LoginActivity.this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {
//
//					@Override
//					public void success(Result<TwitterSession> twitterSessionResult) {
//						// Success
//						final TwitterSession session = twitterSessionResult.data;
//						// TODO: Remove toast and use the TwitterSession's userID
//
//						Log.e("abc","twitter login success "+session.getAuthToken().token);
//						// with your app's user model
////						String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")"+" "+session.getAuthToken().token;
////						Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
//
//
//						final String[] strarray=session.getAuthToken().token.split("-");
//						AVQuery<AVObject> followquery = new AVQuery<>("_User");
//						followquery.whereEqualTo("socialID", strarray[0]);
//
//						followquery.findInBackground(
//								new FindCallback<AVObject>() {
//									@Override
//									public void done(List<AVObject> list, AVException e) {
//
//										if(list!=null){
//
//											if(list.size()>0){
//
//												AVUser cUser = (AVUser)list.get(0);
//												Log.e("abc","facebook login debug");
//												AVUser.logInInBackground(cUser.getUsername(),
//														"WALNUTTT",
//														new LogInCallback() {
//															public void done(AVUser user, AVException e) {
//																if (user != null) {
//																	progressDialogDismiss();
//																	Intent mainIntent = new Intent(activity,
//																			MainActivity.class);
//																	startActivity(mainIntent);
//																	activity.finish();
//																} else {
//																	progressDialogDismiss();
//																	showLoginError();
//																}
//															}
//														});
//											}
//											else{
//												final SignUpCallback signUpCallback = new SignUpCallback() {
//													public void done(AVException e) {
//
//														final AVFile profileImage = new AVFile("profile", "http://ac-4jow6b01.clouddn.com/e342297307144f23.png", new HashMap<String, Object>());
//														profileImage.saveInBackground(
//																new SaveCallback() {
//																	@Override
//																	public void done(AVException e) {
//
//																		AVUser cUser = AVUser.getCurrentUser();
//																		cUser.put("profileImage",profileImage);
//																		cUser.saveInBackground(
//																				new SaveCallback() {
//																					@Override
//																					public void done(AVException e) {
//
//																						progressDialogDismiss();
//																						if (e == null) {
//																							showRegisterSuccess();
//																							Intent mainIntent = new Intent(activity, MainActivity.class);
//																							startActivity(mainIntent);
//																							activity.finish();
//																						} else {
//																							switch (e.getCode()) {
//																								case 202:
//																									showError(activity
//																											.getString(R.string.error_register_user_name_repeat));
//																									break;
//																								case 203:
//																									showError(activity
//																											.getString(R.string.error_register_email_repeat));
//																									break;
//																								default:
//																									showError(activity
//																											.getString(R.string.network_error));
//																									break;
//																							}
//																						}
//																					}
//																				}
//																		);
//
//																	}
//																}
//														);
//													}
//												};
//
//
//												//AVFile file = new AVFile("profile", "http://ac-4jow6b01.clouddn.com/e342297307144f23.png", new HashMap<String, Object>());
//
//												AVService.signUp(session.getUserName(), "WALNUTTT", "",strarray[0], signUpCallback);
//
//
//											}
//
//										}
//
//									}
//								}
//						);
//
//
//
//					}
//
//					@Override
//					public void failure(TwitterException e) {
//						e.printStackTrace();
//					}
//				});


            }
        });

//		showShare();
    }

    /*
     * 演示执行第三方登录/注册的方法
     * <p>
     * 这不是一个完整的示例代码，需要根据您项目的业务需求，改写登录/注册回调函数
     *
     * @param platformName 执行登录/注册的平台名称，如：SinaWeibo.NAME
     */
    private void login(String platformName) {
        LoginApi api = new LoginApi();
        //设置登陆的平台后执行登陆的方法
        api.setPlatform(platformName);
        api.setOnLoginListener(new OnLoginListener() {
            public boolean onLogin(String platform, HashMap<String, Object> res) {
                // 在这个方法填写尝试的代码，返回true表示还不能登录，需要注册
                // 此处全部给回需要注册
                return true;
            }

            public boolean onRegister(UserInfo info) {
                // 填写处理注册信息的代码，返回true表示数据合法，注册页面可以关闭
                return true;
            }
        });
        api.login(this);
    }

    private void showShare() {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网、QQ和QQ空间使用
        oks.setTitle("标题");
        // titleUrl是标题的网络链接，仅在Linked-in,QQ和QQ空间使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("我是分享文本");
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("ShareSDK");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");

// 启动分享GUI
        oks.show(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        callbackManager.onActivityResult(requestCode, responseCode, intent);
//		client.onActivityResult(requestCode, responseCode, intent);
    }


    OnClickListener loginListener = new OnClickListener() {

        @SuppressLint("NewApi")
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public void onClick(View arg0) {
            String username = userNameEditText.getText().toString();
            if (username.isEmpty()) {
                showUserNameEmptyError();
                return;
            }
            if (password().isEmpty()) {
                showUserPasswordEmptyError();
                return;
            }
            progressDialogShow();
            AVUser.logInInBackground(username,
                    password(),
                    new LogInCallback() {
                        public void done(AVUser user, AVException e) {
                            if (user != null) {
                                progressDialogDismiss();
                                Intent mainIntent = new Intent(activity,
                                        MainActivity.class);
                                startActivity(mainIntent);
                                activity.finish();
                            } else {
                                progressDialogDismiss();
                                showLoginError();
                            }
                        }
                    });
        }

        private String password() {
            return userPasswordEditText.getText().toString();
        }
    };


    OnClickListener forgetPasswordListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            Intent forgetPasswordIntent = new Intent(activity, ForgetPasswordActivity.class);
            startActivity(forgetPasswordIntent);
            activity.finish();
        }
    };

    OnClickListener registerListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent registerIntent = new Intent(activity, RegisterActivity.class);
            startActivity(registerIntent);
            activity.finish();
        }
    };

    private void progressDialogDismiss() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    private void progressDialogShow() {
        progressDialog = ProgressDialog
                .show(activity,
                        activity.getResources().getText(
                                R.string.dialog_message_title),
                        activity.getResources().getText(
                                R.string.dialog_text_wait), true, false);
    }

    private void showLoginError() {
        new AlertDialog.Builder(activity)
                .setTitle(
                        activity.getResources().getString(
                                R.string.dialog_error_title))
                .setMessage(
                        activity.getResources().getString(
                                R.string.error_login_error))
                .setNegativeButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }

    private void showUserPasswordEmptyError() {
        new AlertDialog.Builder(activity)
                .setTitle(
                        activity.getResources().getString(
                                R.string.dialog_error_title))
                .setMessage(
                        activity.getResources().getString(
                                R.string.error_register_password_null))
                .setNegativeButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }

    private void showUserNameEmptyError() {
        new AlertDialog.Builder(activity)
                .setTitle(
                        activity.getResources().getString(
                                R.string.dialog_error_title))
                .setMessage(
                        activity.getResources().getString(
                                R.string.error_register_user_name_null))
                .setNegativeButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }


    private void showRegisterSuccess() {
        new AlertDialog.Builder(activity)
                .setTitle(
                        activity.getResources().getString(
                                R.string.dialog_message_title))
                .setMessage(
                        activity.getResources().getString(
                                R.string.success_register_success))
                .setNegativeButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }

}

