package com.qiu.location.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fallenpanda.location.bean.MyLocation;
import com.fallenpanda.location.manager.MainLocationListener;
import com.fallenpanda.location.manager.MainLocationManager;
import com.qiu.location.app.R;
import com.qiu.location.app.ui.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 定位
 * ============================================================================
 * 版权所有 2015 。
 *
 * @author fallenpanda
 * @version 1.0 2014/05/20
 *          ============================================================================
 */
public class MyActivity extends BaseActivity {

    private static final String TAG = "MyActivity";

    @Bind(R.id.textview)
    TextView mTvText;
    @Bind(R.id.edit1)
    EditText mEtScanSpan;
    @Bind(R.id.isAddrInfocb)
    CheckBox mCbNeedAddr;

    private long exitTime = 0;

    private MainLocationManager mainLocationManager;

    @Override
    protected void onResume() {
        super.onResume();
        mainLocationManager.registerListener(mainLocationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainLocationManager.unregisterListener(mainLocationListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        ButterKnife.bind(this);

        mainLocationManager = MainLocationManager.getInstance();
    }

    @OnClick(R.id.btnMap)
    public void onBtnMapClick() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btnOnce)
    public void onBtnOnceClick() {
        boolean isNeedAress = mCbNeedAddr.isChecked();
        mainLocationManager.getBaiduLocationOnce(isNeedAress);
    }

    @OnClick(R.id.btnAuto)
    public void onBtnAutoClick() {
        String scanSpan = mEtScanSpan.getText().toString().trim();
        mainLocationManager.getBaiduLocationAuto(Integer.valueOf(scanSpan));
    }

    @OnClick(R.id.btnStop)
    public void onBtnStopClick() {
        mainLocationManager.stopBaiduLocation();
    }

    private MainLocationListener mainLocationListener = new MainLocationListener() {
        @Override
        public void onLocationChanged(MyLocation location) {
            if (location == null)
                return;
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getmTime());
            sb.append("\ntype : ");
            sb.append(location.getmCoorType());
            sb.append("\nlatitude : ");
            sb.append(location.getmLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getmLongitude());
            if (location.ismHasAccuracy()) {
                sb.append("\nradius : ");
                sb.append(location.getmAccuracy());
            }
            if (location.ismHasSpeed()) {
                sb.append("\nspeed : ");
                sb.append(location.getmSpeed());
            }
            if (location.ismHasAddress()) {
                sb.append("\n省：");
                sb.append(location.getmProvince());
                sb.append("\n市：");
                sb.append(location.getmCity());
                sb.append("\n区/县：");
                sb.append(location.getmDistrict());
                sb.append("\naddr : ");
                sb.append(location.getmAddress());
            }
            mTvText.setText(sb);
        }

        @Override
        public void onLocationFail(String provider, String message) {
            mTvText.setText(message);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * 监听返回--是否退出程序
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean flag = true;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                context.exit();
            }
        } else {
            flag = super.onKeyDown(keyCode, event);
        }
        return flag;
    }

}
