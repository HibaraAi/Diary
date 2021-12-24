package cn.snowt.diary;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.litepal.LitePal;

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
        LitePal.initialize(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                int theme = BaseUtils.getSharedPreference().getInt("theme", R.style.Theme_消消乐);
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
    }
}
