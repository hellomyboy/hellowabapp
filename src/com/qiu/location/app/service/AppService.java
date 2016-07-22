package com.qiu.location.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.fallenpanda.location.manager.MainLocationManager;
import com.qiu.location.app.utils.StringUtils;

/**
 * Created by Android on 2014/9/16.
 */
public class AppService extends Service {

    private static final String TAG = "AppService";

    private MainLocationManager mainLocationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");
        if(mainLocationManager==null)
            mainLocationManager = MainLocationManager.getInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand");
        if (intent != null) {
            String action = intent.getAction();
            if(StringUtils.isNotEmpty(action)){
                if(AppReceiver.ACTION_LOCATION_ONCE.equals(action)){
                    getLocationOnce(true);
                }else if(AppReceiver.ACTION_LOCATION_AUTO.equals(action)){
                    getLocationAuto(3000);
                }
            }
        }
        return START_STICKY;
    }

    /**
     * 启动Service
     * @param context
     */
    public static void startService(Context context) {
        context.startService(new Intent(context, AppService.class));
    }

    /**
     * 停止Service
     * @param context
     */
    public static void stopService(Context context) {
        context.stopService(new Intent(context, AppService.class));
    }

    /**
     * 定位一次
     * @param context
     */
    public static void getLocationOnce(Context context){
        Intent localIntent = new Intent(context, AppService.class);
        localIntent.setAction(AppReceiver.ACTION_LOCATION_ONCE);
        context.startService(localIntent);
    }

    /**
     * 定位一次
     * @param context
     */
    public static void getLocationAuto(Context context){
        Intent localIntent = new Intent(context, AppService.class);
        localIntent.setAction(AppReceiver.ACTION_LOCATION_AUTO);
        context.startService(localIntent);
    }

    /**
     * 定位一次
     */
    private void getLocationOnce(boolean isNeedAddress){
        mainLocationManager.getBaiduLocationOnce(isNeedAddress);
    }

    /**
     * 定位多次
     * @param scanSpan 间隔
     */
    private void getLocationAuto(int scanSpan){
        mainLocationManager.getBaiduLocationAuto(scanSpan);
    }

}
