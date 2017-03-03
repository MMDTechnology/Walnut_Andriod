package maojian.android.walnut.base;

import android.app.Application;
import android.content.Context;

/**
 * @author hezuzhi
 * @Description: (Application)
 * @date 2016/5/3  17:25.
 * @version: 1.0
 */
public class BaseApplication extends Application {
    private static BaseApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //崩溃日志本地保存
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
        // 初始化全局数据信息
//        UserInfo.initUserInfo(this);
//        BaseConstant.initUrl();
//        JPushInterface.init(this);
    }

    public static Context getApplicationInstance() {
        return instance;
    }

}

