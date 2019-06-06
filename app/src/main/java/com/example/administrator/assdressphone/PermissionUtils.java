package com.example.administrator.assdressphone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


public class PermissionUtils {
    public static final int REQUEST_CALL_PERMISSION = 10111; //拨号请求码
    public static final int REQUEST_Location = 10112; //定位请求码
    public static final String CALL_PHONE= "";
    public static final int WriteAndRead = 10001;//读写请求码
    public static final int CACAMERA = 1003;

    /**
     * 判断是否有某项权限
     *
     * @param string_permission 权限
     * @param request_code      请求码
     * @return
     */
    public static boolean checkReadPermission(String[] string_permission, int request_code, Context context) {
        boolean flag = false;
        if (ContextCompat.checkSelfPermission(context, string_permission[0]) == PackageManager.PERMISSION_GRANTED) {//已有权限
            flag = true;
        } else {//申请权限
            ActivityCompat.requestPermissions((Activity) context, string_permission, request_code);
        }
        return flag;
    }

    /**
     * 引导设置 权限窗体
     * @param
     * @param context
     */
    public static void setPermission( final Context context){

        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("权限提醒")
                .setMessage("获取通讯录权限,将会更多体验")
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new PermissionPageUtils(context).jumpPermissionPage();
                    }
                })
                .setNegativeButton("退出程序", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity)context).finish();
                    }
                })
                .show();

    }


}
