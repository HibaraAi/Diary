package cn.snowt.diary.util;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.TitleBarStyle;

import org.litepal.LitePalApplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.snowt.diary.R;

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
    public static void alertDialogToShow(Context context,String title,String content){
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
     * java.util.Date转String
     * @param date Date
     * @return 指定格式的String日期
     */
    public static String dateToString(Date date){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATE_FORMAT);
        String format = sdf.format(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekday == 1) {
            format += " 星期天";
        } else if (weekday == 2) {
            format += " 星期一";
        } else if (weekday == 3) {
            format += " 星期二";
        } else if (weekday == 4) {
            format += " 星期三";
        } else if (weekday == 5) {
            format += " 星期四";
        } else if (weekday == 6) {
            format += " 星期五";
        } else if (weekday == 7) {
            format += " 星期六";
        }
        return format;
    }

    /**
     * java.util.Date转String
     * @param date Date
     * @return 指定格式的String日期 不带周几
     */
    public static String dateToStringWithout(Date date){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATE_FORMAT);
        return sdf.format(date);
    }

    /**
     * String转java.util.Date
     * @param dateStr 指定格式的String日期
     * @return Date 转换失败返回null
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
     * 获取默认的获取SharedPreferences
     * @return
     */
    public static SharedPreferences getDefaultSharedPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(LitePalApplication.getContext());
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

    /**
     * 产生一小下的震动
     */
    public static void createOneShotByVibrator() {
        Vibrator vibrator = (Vibrator) LitePalApplication.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE));
    }

    /**
     * 长时间的Snack提示
     * @param view
     * @param tip
     */
    public static void longTipInSnack(View view,String tip){
        Snackbar.make(view,tip,Snackbar.LENGTH_LONG).show();
    }

    /**
     * 短时间的Snack提示
     * @param view
     * @param tip
     */
    public static void shortTipInSnack(View view,String tip){
        Snackbar.make(view,tip,Snackbar.LENGTH_SHORT).show();
    }

    /**
     * 打开相册选择相片
     * @param context context
     * @param type 此次打开相册的请求类型
     * @param requestCode requestCode
     */
    public static void openAlbum(Context context,int type,int requestCode) {
        switch (type) {
            case Constant.OPEN_ALBUM_TYPE_HEAD:{
                BaseUtils.shortTipInCoast(context,"请选择新头像");
                break;
            }
            case Constant.OPEN_ALBUM_TYPE_MAIN_BG:{
                BaseUtils.shortTipInCoast(context,"请选择新的首页背景图");
                break;
            }
            case Constant.OPEN_ALBUM_TYPE_KEEP_DIARY_ADD_PIC:{
                BaseUtils.shortTipInCoast(context,"请选择一张你要插入的图片");
                break;
            }
            case Constant.OPEN_ALBUM_TYPE_ADD_DAY_ADD_PIC:{
                BaseUtils.shortTipInCoast(context,"请选择一张图片作为纪念日背景");
                break;
            }
            default:return;
        }
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        AppCompatActivity appCompatActivity = (AppCompatActivity) context;
        appCompatActivity.startActivityForResult(intent,requestCode);
    }

    /**
     * 简单的系统任务栏通知
     * 只能显示一行字，没有点击操作
     * @param context
     * @param notice
     */
    public static void simpleSysNotice(Context context, String notice){
        String channelId = "small"; // 通知渠道
        Notification notification = new Notification.Builder(context,channelId)
                .setChannelId(channelId)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(notice)
                .build();
        // 2. 获取系统的通知管理器(必须设置channelId)
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "消消乐提示",
                NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
        // 3. 发送通知(Notification与NotificationManager的channelId必须对应)
        notificationManager.notify(2, notification);
    }


    public static void longTextSysNotice(Context context,String notice){
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                "big",
                "消消乐提示",
                NotificationManager.IMPORTANCE_LOW);
        manager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "big")
                .setContentText(notice)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);
        //创建大文本样式
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(notice);
        builder.setStyle(bigTextStyle); //设置大文本样式
        Notification notification = builder.build();
        manager.notify(1, notification);
    }

    /**
     * 使用PictureSelector预览资源，注意仅解析Hibara文件夹下的资源
     * @param position 图所在的位置
     * @param imageSrcList 资源总列表
     */
    public static void openHibaraMediaPreview(Context context, int position, List<String> imageSrcList){
        if(null==imageSrcList || imageSrcList.isEmpty()){
            return;
        }
        ArrayList<LocalMedia> imageList = new ArrayList<>();
        ArrayList<LocalMedia> videoList = new ArrayList<>();
        AtomicInteger imageIndex = new AtomicInteger();
        AtomicInteger videoIndex = new AtomicInteger();
        AtomicInteger i = new AtomicInteger();
        imageSrcList.forEach(s -> {
            //根据提供的文件路径，判断媒体类型，因为存的时候一定存在了自己的目录下，包含image的为图片、包含video的为视频
            if(s.contains("image")){
                LocalMedia localMedia = LocalMedia.generateLocalMedia(context, s);
                localMedia.setMimeType("image/*");
                imageList.add(localMedia);
                if(i.get() ==position){
                    imageIndex.set(imageList.size() - 1);
                }
            }else if(s.contains("video")){
                LocalMedia localMedia = LocalMedia.generateLocalMedia(context, s);
                localMedia.setMimeType("video/*");
                videoList.add(localMedia);
                if(i.get() ==position){
                    videoIndex.set(videoList.size() - 1);
                }
            }
            i.getAndIncrement();
        });
        PictureSelectorStyle pictureSelectorStyle = new PictureSelectorStyle();
        if(context.getResources().getConfiguration().uiMode == 0x11){
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorPrimary,typedValue,true);
            TitleBarStyle titleBarStyle = new TitleBarStyle();
            titleBarStyle.setTitleBackgroundColor(typedValue.data);
            pictureSelectorStyle.setTitleBarStyle(titleBarStyle);
        }
        //对点击的资源进行解析，以打开对应的预览器
        String s = imageSrcList.get(position);
        if(s.contains("video")){
            PictureSelector.create(context)
                    .openPreview()
                    .setImageEngine(GlideEngine.createGlideEngine())
                    .setSelectorUIStyle(pictureSelectorStyle)
                    .setExternalPreviewEventListener(new OnExternalPreviewEventListener() {
                        @Override
                        public void onPreviewDelete(int position) {

                        }
                        @Override
                        public boolean onLongPressDownload(Context context, LocalMedia media) {
                            return false;
                        }
                    }).startActivityPreview(videoIndex.get(), false, videoList);
        }else{
            PictureSelector.create(context)
                    .openPreview()
                    .setImageEngine(GlideEngine.createGlideEngine())
                    .setSelectorUIStyle(pictureSelectorStyle)
                    .setExternalPreviewEventListener(new OnExternalPreviewEventListener() {
                        @Override
                        public void onPreviewDelete(int position) {

                        }
                        @Override
                        public boolean onLongPressDownload(Context context, LocalMedia media) {
                            return false;
                        }
                    }).startActivityPreview(imageIndex.get(), false, imageList);
        }
    }
}
