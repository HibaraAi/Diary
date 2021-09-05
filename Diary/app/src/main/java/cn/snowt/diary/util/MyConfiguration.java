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

    private static MyConfiguration myConfiguration;
    private MyConfiguration() {
    }

    public static MyConfiguration getInstance(){
        if (myConfiguration == null) {
            synchronized (MyConfiguration.class) {
                if (myConfiguration == null) {
                    myConfiguration = new MyConfiguration();
                }
            }
        }
        SharedPreferences sharedPreference = BaseUtils.getSharedPreference();
        username = sharedPreference.getString(Constant.SHARE_PREFERENCES_USERNAME,"到侧滑菜单中编辑用户名");
        headImg = sharedPreference.getString(Constant.SHARE_PREFERENCES_HEAD_SRC,null);
        motto = sharedPreference.getString(Constant.SHARE_PREFERENCES_MOTTO,"到侧滑菜单中编辑个性签名");
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
}
