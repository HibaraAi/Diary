package cn.snowt.diary.util;

import android.content.SharedPreferences;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-16 14:13
 * @Description: 全局单例变量?
 */
public class SingleVar {
    private static String headImg;
    private static String username;
    private static String motto;

    private static SingleVar singleVar;
    private SingleVar() {
    }

    public static SingleVar getInstance(){
        if (singleVar == null) {
            synchronized (SingleVar.class) {
                if (singleVar == null) {
                    singleVar = new SingleVar();
                }
            }
        }
        SharedPreferences sharedPreference = BaseUtils.getSharedPreference();
        username = sharedPreference.getString("username","到设置中编辑用户名");
        headImg = sharedPreference.getString("headImg",null);
        motto = sharedPreference.getString("motto","到设置中编辑个性签名");
        return singleVar;
    }


    public static String getHeadImg() {
        getInstance();
        return headImg;
    }

    public static String getUsername() {
        getInstance();
        return username;
    }

    public static String getMotto() {
        getInstance();
        return motto;
    }
}
