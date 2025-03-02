package cn.snowt.diary;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import cn.snowt.blog.view.IImageLoader;
import cn.snowt.blog.view.TransformationScale;
import cn.snowt.blog.view.XRichText;
import cn.snowt.diary.util.BaseUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-12-24 13:38:31
 * @Description:
 */
public class MyApplication extends org.litepal.LitePalApplication{
    @Override
    public void onCreate() {
        super.onCreate();
        MyCrashHandler crashHandler = MyCrashHandler.getInstance();

        //异常捕获相关
        crashHandler.init(getApplicationContext());

        //LitePal初始化
        LitePal.initialize(this);

        //这里是为了设置主色调的
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                int theme = BaseUtils.getSharedPreference().getInt("theme", R.style.Theme_green);
                if(-20250215 == theme){  //随机主色调
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    int i = calendar.get(Calendar.DAY_OF_YEAR);
                    int i1 = calendar.get(Calendar.HOUR_OF_DAY);
                    int i2 = calendar.get(Calendar.DAY_OF_WEEK);
                    switch ((i*(i1+i2))%7) {
                        case 1:{
                            theme = R.style.Theme_blue;
                            break;
                        }
                        case 2:{
                            theme = R.style.Theme_black;
                            break;
                        }
                        case 3:{
                            theme = R.style.Theme_消消乐;
                            break;
                        }
                        case 4:{
                            theme = R.style.Theme_red;
                            break;
                        }
                        case 5:{
                            theme = R.style.Theme_yellow;
                            break;
                        }
                        case 6:{
                            theme = R.style.Theme_purple;
                            break;
                        }
                        case 0:{
                            theme = R.style.Theme_green;
                            break;
                        }
                        default:theme = R.style.Theme_green;break;
                    }
                }
                activity.setTheme(theme);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        //富文本中图片显示相关
        XRichText.getInstance().setImageLoader(new IImageLoader() {
            @Override
            public void loadImage(final String imagePath, final ImageView imageView, final int imageHeight) {
                Log.e("---", "imageHeight: "+imageHeight);
                //如果是网络图片
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://")){

                } else { //如果是本地图片
                    if (imageHeight > 0) {//固定高度
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT, imageHeight);//固定图片高度，记得设置裁剪剧中
                        lp.bottomMargin = 10;//图片的底边距
                        imageView.setLayoutParams(lp);

                        Glide.with(getApplicationContext()).asBitmap().load(imagePath).centerCrop()
                                .placeholder(R.drawable.ps_image_placeholder).error(R.drawable.ps_image_placeholder).into(imageView);
                    } else {//自适应高度
                        Glide.with(getApplicationContext()).asBitmap().load(imagePath)
                                .placeholder(R.drawable.ps_image_placeholder).error(R.drawable.ps_image_placeholder).into(new TransformationScale(imageView));
                    }
                }
            }
        });
    }
}
