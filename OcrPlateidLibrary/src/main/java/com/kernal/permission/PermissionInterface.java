package com.kernal.permission;

/**
 * Created by Micky on 2018/12/17.
 * 权限请求接口
 */

public interface PermissionInterface {
    /**
     * 可设置请求权限请求码
     */
    int getPermissionsRequestCode();

    /**
     * 设置需要请求的权限
     */
    String[] getPermissions();

    /**
     * 请求权限成功回调
     */
    void requestPermissionsSuccess();

    /**
     * 请求权限失败回调
     */
    void requestPermissionsFail();

    /**
     * 用户拒绝某授权，下一次弹框用于展示和解释为什么需要这些权限
     */
    void onShowRationale(String[] deniedShowRationalePermissions);
}
