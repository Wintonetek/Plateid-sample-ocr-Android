package com.kernal.permission;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Time:2020/10/29
 * Author:A@H
 * Description:
 */
public class PlateBaseActivity extends AppCompatActivity implements PermissionInterface {
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionHelper=new PermissionHelper(this,this);
        permissionHelper.requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.requestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 可设置请求权限请求码
     */
    @Override
    public int getPermissionsRequestCode() {
        return 1000;
    }

    /**
     * 设置需要请求的权限
     */
    @Override
    public String[] getPermissions() {
        return new String[0];
    }

    /**
     * 请求权限成功回调
     */
    @Override
    public void requestPermissionsSuccess() {

    }

    /**
     * 请求权限失败回调
     */
    @Override
    public void requestPermissionsFail() {
        permissionHelper.openAppDetails("相机权限","电话权限","存储权限");
    }

    /**
     * 用户拒绝某授权，下一次弹框用于展示和解释为什么需要这些权限
     *
     * @param deniedShowRationalePermissions
     */
    @Override
    public void onShowRationale(String[] deniedShowRationalePermissions) {
        permissionHelper.showRationale(deniedShowRationalePermissions);
    }

}
