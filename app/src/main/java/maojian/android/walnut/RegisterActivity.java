package maojian.android.walnut;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
//import com.twitter.sdk.android.core.Result;
//import com.twitter.sdk.android.core.TwitterException;
//import com.twitter.sdk.android.core.TwitterSession;
//import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RegisterActivity extends AnyTimeActivity {

	Button registerButton;
	EditText userName;
//	EditText userEmail;
	EditText userPassword;
	EditText userPasswordAgain;
	private ProgressDialog progressDialog;

	private AccessToken accessToken;
	CallbackManager callbackManager;

//    private TwitterAuthClient client;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_register);
//		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		callbackManager = CallbackManager.Factory.create();

//        client = new TwitterAuthClient();

		registerButton = (Button) findViewById(R.id.button_i_need_register);
		userName = (EditText) findViewById(R.id.editText_register_userName);
//		userEmail = (EditText) findViewById(R.id.editText_register_email);
		userPassword = (EditText) findViewById(R.id.editText_register_userPassword);
		userPasswordAgain = (EditText) findViewById(R.id.editText_register_userPassword_again);

		registerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (userPassword.getText().toString()
						.equals(userPasswordAgain.getText().toString())) {
					if (!userPassword.getText().toString().isEmpty()) {
						if (!userName.getText().toString().isEmpty()) {
//							if (!userEmail.getText().toString().isEmpty()) {
//								progressDialogShow();
//								register();
//							} else {
//								showError(activity
//										.getString(R.string.error_register_email_address_null));
//							}
						} else {
							showError(activity
									.getString(R.string.error_register_user_name_null));
						}
					} else {
						showError(activity
								.getString(R.string.error_register_password_null));
					}
				} else {
					showError(activity
							.getString(R.string.error_register_password_not_equals));
				}
			}
		});




		// Facebook login
		ImageButton facebook_loginButton = (ImageButton) findViewById(R.id.register_facebook);

		facebook_loginButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.e("abc", "facebook onclick");
						LoginManager.getInstance().logInWithReadPermissions(RegisterActivity.this, Arrays.asList("public_profile", "user_friends"));					}
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


        // twitter login
        ImageButton twitter_custom_button = (ImageButton) findViewById(R.id.register_twitter);
        twitter_custom_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {


//                client.authorize(RegisterActivity.this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {
//
//                    @Override
//                    public void success(Result<TwitterSession> twitterSessionResult) {
//                        // Success
//                        final TwitterSession session = twitterSessionResult.data;
//                        // TODO: Remove toast and use the TwitterSession's userID
//
//                        Log.e("abc", "twitter login success " + session.getAuthToken().token);
//                        // with your app's user model
////						String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")"+" "+session.getAuthToken().token;
////						Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
//
//
//                        final String[] strarray = session.getAuthToken().token.split("-");
//                        AVQuery<AVObject> followquery = new AVQuery<>("_User");
//                        followquery.whereEqualTo("socialID", strarray[0]);
//
//                        followquery.findInBackground(
//                                new FindCallback<AVObject>() {
//                                    @Override
//                                    public void done(List<AVObject> list, AVException e) {
//
//                                        if (list != null) {
//
//                                            if (list.size() > 0) {
//
//                                                AVUser cUser = (AVUser) list.get(0);
//                                                Log.e("abc", "facebook login debug");
//                                                AVUser.logInInBackground(cUser.getUsername(),
//                                                        "WALNUTTT",
//                                                        new LogInCallback() {
//                                                            public void done(AVUser user, AVException e) {
//                                                                if (user != null) {
//                                                                    progressDialogDismiss();
//                                                                    Intent mainIntent = new Intent(activity,
//                                                                            MainActivity.class);
//                                                                    startActivity(mainIntent);
//                                                                    activity.finish();
//                                                                } else {
//                                                                    progressDialogDismiss();
//                                                                    showLoginError();
//                                                                }
//                                                            }
//                                                        });
//                                            } else {
//                                                final SignUpCallback signUpCallback = new SignUpCallback() {
//                                                    public void done(AVException e) {
//
//                                                        final AVFile profileImage = new AVFile("profile", "http://ac-4jow6b01.clouddn.com/e342297307144f23.png", new HashMap<String, Object>());
//                                                        profileImage.saveInBackground(
//                                                                new SaveCallback() {
//                                                                    @Override
//                                                                    public void done(AVException e) {
//
//                                                                        AVUser cUser = AVUser.getCurrentUser();
//                                                                        cUser.put("profileImage", profileImage);
//                                                                        cUser.saveInBackground(
//                                                                                new SaveCallback() {
//                                                                                    @Override
//                                                                                    public void done(AVException e) {
//
//                                                                                        progressDialogDismiss();
//                                                                                        if (e == null) {
//                                                                                            showRegisterSuccess();
//                                                                                            Intent mainIntent = new Intent(activity, MainActivity.class);
//                                                                                            startActivity(mainIntent);
//                                                                                            activity.finish();
//                                                                                        } else {
//                                                                                            switch (e.getCode()) {
//                                                                                                case 202:
//                                                                                                    showError(activity
//                                                                                                            .getString(R.string.error_register_user_name_repeat));
//                                                                                                    break;
//                                                                                                case 203:
//                                                                                                    showError(activity
//                                                                                                            .getString(R.string.error_register_email_repeat));
//                                                                                                    break;
//                                                                                                default:
//                                                                                                    showError(activity
//                                                                                                            .getString(R.string.network_error));
//                                                                                                    break;
//                                                                                            }
//                                                                                        }
//                                                                                    }
//                                                                                }
//                                                                        );
//
//                                                                    }
//                                                                }
//                                                        );
//                                                    }
//                                                };
//
//
//                                                //AVFile file = new AVFile("profile", "http://ac-4jow6b01.clouddn.com/e342297307144f23.png", new HashMap<String, Object>());
//
//                                                AVService.signUp(session.getUserName(), "WALNUTTT", "", strarray[0], signUpCallback);
//
//
//                                            }
//
//                                        }
//
//                                    }
//                                }
//                        );
//
//
//                    }
//
//                    @Override
//                    public void failure(TwitterException e) {
//                        e.printStackTrace();
//                    }
//                });


            }
        });

	}

	@Override
	public void onClickEvent(View v) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Intent LoginIntent = new Intent(this, LoginActivity.class);
			startActivity(LoginIntent);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void register() {
    final SignUpCallback signUpCallback = new SignUpCallback() {
      public void done(AVException e) {

          final AVFile profileImage = new AVFile("profile", "http://ac-4jow6b01.clouddn.com/e342297307144f23.png", new HashMap<String, Object>());


          profileImage.saveInBackground(
                  new SaveCallback() {
                      @Override
                      public void done(AVException e) {

                          AVUser cUser = AVUser.getCurrentUser();
                          cUser.put("profileImage",profileImage);
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

//        progressDialogDismiss();
//        if (e == null) {
//          showRegisterSuccess();
//          Intent mainIntent = new Intent(activity, MainActivity.class);
//          startActivity(mainIntent);
//          activity.finish();
//        } else {
//          switch (e.getCode()) {
//            case 202:
//              showError(activity
//                  .getString(R.string.error_register_user_name_repeat));
//              break;
//            case 203:
//              showError(activity
//                  .getString(R.string.error_register_email_repeat));
//              break;
//            default:
//              showError(activity
//                  .getString(R.string.network_error));
//              break;
//          }
//        }
      }
    };
    final String username = userName.getText().toString();
    final String password = userPassword.getText().toString();
//    final String email = userEmail.getText().toString();

//	Resources res=getResources();
//	Bitmap bmp= BitmapFactory.decodeResource(res, R.drawable.notloading);
//
//    Bitmap.CompressFormat format= Bitmap.CompressFormat.JPEG;
//    int quality = 100;
//    OutputStream stream = null;
//    try {
//        stream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/walnutprofile.png");
//        bmp.compress(format, quality, stream);
//    } catch (FileNotFoundException e) {
//// TODO Auto-generated catch block
//        e.printStackTrace();
//    }

        AVService.signUp(username, password, "email",Environment.getExternalStorageDirectory() + "/walnutprofile.png", signUpCallback);
	}

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
}
