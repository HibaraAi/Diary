package cn.snowt.diary.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


/**
 * @Author: HibaraAi
 * @Date: 2022-04-21 20:58:32
 * @Description: 权限相关
 */
public class PermissionUtils {

    /**
     * 判断有没有外存的读写权限
     * @param context
     * @return true-有 false-无
     */
    public static boolean haveExternalStoragePermission(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //高于或等于Android11
            return Environment.isExternalStorageManager();
        }else{
            //为Android 10
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void applyExternalStoragePermission(Context context,Integer requestCode){
       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //高于或等于Android11
           AlertDialog.Builder builder = new AlertDialog.Builder(context);
           builder.setMessage("图片读取/存储需要外部存储的读写权限。\n即将打开授权界面，请找到“消消乐”并授予权限.")
                   .setPositiveButton("去授权", (dialog, which) -> {
                       AppCompatActivity activity = (AppCompatActivity) context;
                       Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                       activity.startActivityForResult(intent,requestCode);
                   })
                   .show();
        }else{
            //为Android 10
           ActivityCompat.requestPermissions((Activity) context,
                   new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},requestCode);
       }
    }
}
