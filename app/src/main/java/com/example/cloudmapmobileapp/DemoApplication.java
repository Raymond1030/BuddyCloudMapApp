package com.example.BuddyCloudMapApp;

import android.app.Application;

import androidx.annotation.NonNull;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

public class DemoApplication extends Application {
    @Override
    public void onCreate()
    {
        super.onCreate();
        // 在这里同意隐私政策
        SDKInitializer.setAgreePrivacy(getApplicationContext(), true);
        // 初始化SDK
        SDKInitializer.initialize(this);
        // 设置坐标类型
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }
}
