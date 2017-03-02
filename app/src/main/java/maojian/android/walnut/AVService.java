package maojian.android.walnut;

import android.content.Context;
import android.util.Log;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.FunctionCallback;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.RequestPasswordResetCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SignUpCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lzw on 14-9-11.
 */
public class AVService {



  public static void countDoing(String doingObjectId, CountCallback countCallback) {
    AVQuery<AVObject> query = new AVQuery<AVObject>("DoingList");
    query.whereEqualTo("doingListChildObjectId", doingObjectId);
    Calendar c = Calendar.getInstance();
    c.add(Calendar.MINUTE, -10);
    // query.whereNotEqualTo("userObjectId", userId);
    query.whereGreaterThan("createdAt", c.getTime());
    query.countInBackground(countCallback);
  }

  //Use callFunctionMethod
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void getAchievement(String userObjectId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userObjectId", userObjectId);
    AVCloud.callFunctionInBackground("hello", parameters,
        new FunctionCallback() {
          @Override
          public void done(Object object, AVException e) {
            if (e == null) {
              Log.e("at", object.toString());// processResponse(object);
            } else {
              // handleError();
            }
          }
        });
  }

  public static List<AVObject> getWallImages() {

    AVQuery<AVObject> followquery = new AVQuery<>("Activity");
    followquery.whereEqualTo("type", "follow");
    followquery.whereEqualTo("fromUser", AVUser.getCurrentUser());

    AVQuery<AVObject>photosFromFollowedUsersQuery = new AVQuery<>("Photo");
    photosFromFollowedUsersQuery.whereMatchesKeyInQuery("user", "toUser", followquery);
    photosFromFollowedUsersQuery.whereExists("image");
    photosFromFollowedUsersQuery.include("createdAt");

    AVQuery<AVObject>photosFromCurrentUserQuery = new AVQuery<>("Photo");
    photosFromCurrentUserQuery.whereEqualTo("user", AVUser.getCurrentUser());
    photosFromCurrentUserQuery.whereExists("image");
    photosFromCurrentUserQuery.include("createdAt");

    List<AVObject> post;
    post = new ArrayList<AVObject>();
    AVQuery<AVObject> query = AVQuery.or(Arrays.asList(photosFromFollowedUsersQuery, photosFromCurrentUserQuery));
    query.limit(8);

        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

              List<AVObject> post = list;
              Log.e("sdas","dss"+post.size());

            }
        });

    Log.e("sdas","ds"+post.size());
    return post;
  }

  public static void createDoing(String userId, String doingObjectId) {
    AVObject doing = new AVObject("DoingList");
    doing.put("userObjectId", userId);
    doing.put("doingListChildObjectId", doingObjectId);
    doing.saveInBackground();
  }

  public static void requestPasswordReset(String email, RequestPasswordResetCallback callback) {
    AVUser.requestPasswordResetInBackground(email, callback);
  }

  public static void findDoingListGroup(FindCallback<AVObject> findCallback) {
    AVQuery<AVObject> query = new AVQuery<AVObject>("DoingListGroup");
    query.orderByAscending("Index");
    query.findInBackground(findCallback);
  }

  public static void findChildrenList(String groupObjectId, FindCallback<AVObject> findCallback) {
    AVQuery<AVObject> query = new AVQuery<AVObject>("DoingListChild");
    query.orderByAscending("Index");
    query.whereEqualTo("parentObjectId", groupObjectId);
    query.findInBackground(findCallback);
  }

  public static void initPushService(Context ctx) {
    PushService.setDefaultPushCallback(ctx, LoginActivity.class);
    PushService.subscribe(ctx, "public", LoginActivity.class);
    AVInstallation.getCurrentInstallation().saveInBackground();
  }

  public static void signUp(String username, String password, String email,String socialID, final SignUpCallback signUpCallback) {
    final AVUser user = new AVUser();
    user.setUsername(username);
    user.setPassword(password);
    user.setEmail(email);

    user.put("socialID",socialID);

    //AVFile file = new AVFile("profile", "http://ac-4jow6b01.clouddn.com/e342297307144f23.png", new HashMap<String, Object>());

    //user.put("profileImage",file);
////
    user.signUpInBackground(signUpCallback);
  }

  public static void logout() {
    AVUser.logOut();
  }

  public static void createAdvice(String userId, String advice, SaveCallback saveCallback) {
    AVObject doing = new AVObject("SuggestionByUser");
    doing.put("UserObjectId", userId);
    doing.put("UserSuggestion", advice);
    doing.saveInBackground(saveCallback);
  }


}
