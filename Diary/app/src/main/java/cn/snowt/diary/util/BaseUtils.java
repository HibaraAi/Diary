package cn.snowt.diary.util;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.litepal.LitePalApplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 07:20
 * @Description:
 */
public class BaseUtils {

    /**
     * 短时间的Coast提示
     * @param context context
     * @param tip 提示内容
     */
    public static void shortTipInCoast(Context context, String tip){
        Toast.makeText(context,tip,Toast.LENGTH_SHORT).show();
    }

    /**
     * 长时间的Coast提示
     * @param context context
     * @param tip 提示内容
     */
    public static void longTipInCoast(Context context,String tip){
        Toast.makeText(context,tip,Toast.LENGTH_LONG).show();
    }

    /**
     * 弹窗提示
     * @param context context
     * @param title 标题
     * @param content 提示内容
     */
    public static void alertDialog(Context context,String title,String content){
        AlertDialog.Builder builder  = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setPositiveButton("OK" ,  null );
        builder.show();
    }

    /**
     * 单纯的Activity跳转
     * @param context context
     * @param clazz 跳转目标Activity的Class
     */
    public static void gotoActivity(Activity context,Class clazz){
        Intent intent = new Intent(context,clazz);
        context.startActivity(intent);
    }

    /**
     * 带RequestCode的Activity跳转
     * @param context context
     * @param clazz 跳转目标Activity的Class
     * @param requestCode requestCode
     */
    public static void gotoActivity(Activity context,Class clazz,Integer requestCode){
        Intent intent = new Intent(context, clazz);
        context.startActivityForResult(intent,requestCode);
    }

    /**
     * java.util.Date转String
     * @param date Date
     * @return 指定格式的String日期
     */
    public static String dateToString(Date date){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATE_FORMAT);
        return sdf.format(date);
    }

    /**
     * String转java.util.Date
     * @param dateStr 指定格式的String日期
     * @return Date
     */
    public static Date stringToDate(String dateStr){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATE_FORMAT);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            Log.e("BaseUtils","String转Date失败");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取SharedPreferences
     * @return
     */
    public static SharedPreferences getSharedPreference(){
        return LitePalApplication.getContext().getSharedPreferences("appSetting",MODE_PRIVATE);
    }

    /**
     * 将内容复制到系统剪贴板
     * @param context context
     * @param content 需要复制的内容
     * @return true-复制成功
     */
    public static Boolean copyInClipboard(Context context,String content){
        try{
            //获取剪贴板管理器：
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", content);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
