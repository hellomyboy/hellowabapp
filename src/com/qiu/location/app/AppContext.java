package com.qiu.location.app;

import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.fallenpanda.location.manager.MainLocationManager;
import com.qiu.location.app.base.BaseApplication;

/**
 * 全局应用程序类
 * ============================================================================
 * 版权所有 2014 。
 *
 * @author fallenpanda
 *
 * @version 1.0 2014-12-08
 * ============================================================================
 */
public class AppContext extends BaseApplication {

    private static final String TAG = "AppContext";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Application-onCreate");
        MainLocationManager.init(this);
//        startService();
    }

    @Override
    public void onTerminate() {
        MainLocationManager.destroy();
        super.onTerminate();
        Log.i(TAG,"Application-onTerminate");
    }

//    public void startService() {
//        AppService.startService(this);
//    }
//
//    public void stopService() {
//        AppService.stopService(this);
//    }

    public void exit() {
//        stopService();
        MainLocationManager.destroy();
        AppManager.getAppManager().AppExit(this);
    }

}
