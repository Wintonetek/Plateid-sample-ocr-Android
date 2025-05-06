package com.kernal.permission;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

/**
 * Created by huangzhen on 2020/5/12.
 * 权限帮助类
 */

public class PermissionHelper {
    private Activity mActivity;
    private PermissionInterface mPermissionInterface;
    public static final int APP_SETTINGS_RC = 2048;

    public PermissionHelper(@NonNull Activity activity, @NonNull PermissionInterface permissionInterface) {
        mActivity = activity;
        mPermissionInterface = permissionInterface;
    }

    /**
     * 开始请求权限。
     * 方法内部已经对Android M 或以上版本进行了判断，外部使用不再需要重复判断。
     * 如果设备还不是M或以上版本，则也会回调到requestPermissionsSuccess方法。
     */
    public void requestPermissions() {
        String[] deniedPermissions = PermissionUtil.getDeniedPermissions(mActivity, mPermissionInterface.getPermissions());
        if (deniedPermissions != null && deniedPermissions.length > 0) {
            List<String> deniedShowRationalePermissions = new ArrayList<>();
            // Should we show an explanation?
            // Permission is not granted
            for (String deniedPermission : deniedPermissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, deniedPermission)) {
                    SharedPreferencesHelper.putInt(mActivity, "callbackInterfaceType", 2);
                    deniedShowRationalePermissions.add(deniedPermission);
                }
            }
            if (deniedShowRationalePermissions.size() > 0) {
                mPermissionInterface.onShowRationale(deniedShowRationalePermissions.toArray(new String[deniedShowRationalePermissions.size()]));
                return;
            }
            PermissionUtil.requestPermissions(mActivity, deniedPermissions, mPermissionInterface.getPermissionsRequestCode());

        } else {
            mPermissionInterface.requestPermissionsSuccess();
        }
    }

    /**
     * 在Activity中的onRequestPermissionsResult中调用
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @return true 代表对该requestCode感兴趣，并已经处理掉了。false 对该requestCode不感兴趣，不处理。
     */
    public boolean requestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == mPermissionInterface.getPermissionsRequestCode()) {
            boolean isAllGranted = true;//是否全部权限已授权
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                //已全部授权
                mPermissionInterface.requestPermissionsSuccess();
            } else {
                //权限有缺失
                if (SharedPreferencesHelper.getInt(mActivity, "callbackInterfaceType", 0) == 1) {
                    mPermissionInterface.requestPermissionsFail();
                }
            }
            /**
             * callbackInterfaceType 0 1 2
             * 0代表第一次进行申请授权操作
             * 1代表不是第一次进入申请授权操作，并且直接执行请求失败接口
             * 2代表不是第一次进入申请授权操作，并且直接执行解释操作接口，不执行请求失败接口
             */
            SharedPreferencesHelper.putInt(mActivity, "callbackInterfaceType", 1);
            return true;
        }
        return false;
    }

    /**
     * 打开 APP 的权限详情设置
     * 在onActivityResult中接收requestCode=2048的权限回调结果，重新执行权限相关逻辑
     *
     * @param permissionShow 权限描述
     */
    public void openAppDetails(String... permissionShow) {
        if (mActivity == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("需要给该应用授权");
        StringBuilder msg = new StringBuilder();
        if (permissionShow != null && permissionShow.length > 0) {
            for (int i = 0; i < permissionShow.length; i++) {
                msg.append(permissionShow[i]);
                msg.append("\n");
            }
        }
        msg.append("\n请到 “应用信息 -> 权限” 中授予！");
        builder.setMessage(msg.toString());
        builder.setPositiveButton("去手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + mActivity.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mActivity.startActivityForResult(intent, APP_SETTINGS_RC);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * Android6.0及以上权限被拒绝解释说明弹窗
     *
     * @param deniedShowRationalePermissions
     */
    public void showRationale(final String[] deniedShowRationalePermissions) {
        if (mActivity == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("说明");
        String msg = "";
        for (int i = 0; i < deniedShowRationalePermissions.length; i++) {
            String permissionIllustrate = deniedShowRationalePermissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) ? "存储权限" :
                    deniedShowRationalePermissions[i].equals(Manifest.permission.CAMERA) ? "相机权限" :
                            deniedShowRationalePermissions[i].equals(Manifest.permission.READ_PHONE_STATE) ? "电话权限" :
                                    "";
            if (permissionIllustrate.equals("")) {
                continue;
            }
            msg = msg.equals("") ? permissionIllustrate : msg + "和" + permissionIllustrate;
        }
        builder.setMessage("此功能需要" + msg + ",请点击确定选择允许开启所需要的权限");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PermissionUtil.requestPermissions(mActivity, deniedShowRationalePermissions, mPermissionInterface.getPermissionsRequestCode());
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
}
