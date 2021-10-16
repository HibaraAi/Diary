package cn.snowt.diary.util;

import android.content.SharedPreferences;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-16 14:13
 * @Description: 我的软件配置
 */
public class MyConfiguration {
    private static String headImg;
    private static String username;
    private static String motto;
    private static String bgImg;
    private static String privateKey;
    private static String publicKey;
    private static boolean requiredAndAbleToEncode;
    private static float fontSize;
    private static boolean autoOpenCommentArea;
    private static int nightStart;
    private static int nightEnd;
    private static boolean autoNight;
    private static boolean needFirstLoginNotice;

    private static MyConfiguration myConfiguration;
    private MyConfiguration() {
        SharedPreferences sharedPreference = BaseUtils.getSharedPreference();
        username = sharedPreference.getString(Constant.SHARE_PREFERENCES_USERNAME,"用户名");
        headImg = sharedPreference.getString(Constant.SHARE_PREFERENCES_HEAD_SRC,null);
        motto = sharedPreference.getString(Constant.SHARE_PREFERENCES_MOTTO,"个性签名");
        bgImg = sharedPreference.getString(Constant.SHARE_PREFERENCES_MAIN_IMG_BG,null);
        privateKey = BaseUtils.getSharedPreference().getString(Constant.SHARE_PREFERENCES_PRIVATE_KEY, "");
        publicKey = BaseUtils.getSharedPreference().getString(Constant.SHARE_PREFERENCES_PUBLIC_KEY,"");
        boolean useEncode = BaseUtils.getDefaultSharedPreferences().getBoolean("useEncode", false);
        boolean haveSetEncodeKey = false;
        String publicKey = BaseUtils.getSharedPreference().getString(Constant.SHARE_PREFERENCES_PUBLIC_KEY, "");
        if(!"".equals(publicKey)){
            haveSetEncodeKey = true;
        }
        requiredAndAbleToEncode = (useEncode && haveSetEncodeKey);
        fontSize = BaseUtils.getSharedPreference().getFloat(Constant.SHARE_PREFERENCES_DIARY_FONT_SIZE,-1.0F);
        autoOpenCommentArea = BaseUtils.getDefaultSharedPreferences().getBoolean("openCommentArea",false);
        autoNight = BaseUtils.getDefaultSharedPreferences().getBoolean("autoNight",false);
        nightStart = BaseUtils.getSharedPreference().getInt("nightStart",2300);
        nightEnd = BaseUtils.getSharedPreference().getInt("nightEnd",800);
        needFirstLoginNotice = BaseUtils.getDefaultSharedPreferences().getBoolean("firstLoginNotice",false);
    }

    public static MyConfiguration getInstance(){
        if (myConfiguration == null) {
            synchronized (MyConfiguration.class) {
                if (myConfiguration == null) {
                    myConfiguration = new MyConfiguration();
                }
            }
        }
        return myConfiguration;
    }


    /**
     * 获取头像
     * @return 如果没有设置头像，返回null
     */
    public String getHeadImg() {
        return headImg;
    }

    public String getUsername() {
        return username;
    }

    public String getMotto() {
        return motto;
    }

    /**
     * 获取首页背景图
     * @return 如果没有设置，则返回null
     */
    public String getBgImg() {
        return bgImg;
    }

    /**
     * 获取私钥(长密钥)
     * 如果没有设置，则返回""
     * @return
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * 获取公钥(短密钥)
     * 如果没有设置，则返回""
     * @return
     */
    public String getPublicKey() {
        return publicKey;
    }

    public boolean isRequiredAndAbleToEncode() {
        return requiredAndAbleToEncode;
    }

    public float getFontSize() {
        return fontSize;
    }

    public boolean isAutoOpenComment() {
        return autoOpenCommentArea;
    }

    public int getNightStart() {
        return nightStart;
    }

    public int getNightEnd() {
        return nightEnd;
    }

    public boolean isAutoNight() {
        return autoNight;
    }

    public boolean isNeedFirstLoginNotice() {
        return needFirstLoginNotice;
    }
}
