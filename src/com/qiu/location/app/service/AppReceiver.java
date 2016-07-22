package com.qiu.location.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.qiu.location.app.utils.StringUtils;

/**
 * Created by Android on 2014/9/19.
 */
public class AppReceiver extends BroadcastReceiver {

    private static final String TAG = "AppReceiver";

    public static final String ACTION_LOCATION_ONCE = "com.qiu.location.app.LOCATION_ONCE";//单次定位
    public static final String ACTION_LOCATION_AUTO = "com.qiu.location.app.LOCATION_AUTO";//间隔定位

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"onReceive :" + intent.getAction());
        String str = intent.getAction();
        if(StringUtils.isNotEmpty(str)){
            if(ACTION_LOCATION_ONCE.equals(str)){
                AppService.getLocationOnce(context);
            }else if(ACTION_LOCATION_AUTO.equals(str)){
                AppService.getLocationAuto(context);
            }
        }
    }
}
