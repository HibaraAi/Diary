package cn.snowt.diary.util;

import android.content.SharedPreferences;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 07:20
 * @Description: 常量类
 */
public class Constant {
    /**
     * 软件内部版本
     */
    public static final Integer INTERNAL_VERSION = 1;
    /**
     * MD5加密的前缀
     */
    public static final String PASSWORD_PREFIX = "MD5*jiami-de$qianzhui#";

    /**
     * 时间格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 最大密码错误次数, 达到后将受到制裁
     */
    public static final Integer MAX_LOGIN_FAIL_COUNT = 5;

    /**
     * 最大制裁等级, 达到后将删库
     */
    public static final Integer MAX_PUNISHMENT_LEVEL = 50;

    /**
     * 关于的信息
     */
    public static final String STRING_ABOUT = "作者: HibaraAi\n" +
            "版本: 1.0\n" +
            "更新日期: 2021-08-06\n" +
            "开源代码(长按复制): https://github.com/HibaraAi/PasswordManager";

    /**
     * 帮助信息
     */
    public static final String STRING_HELP = "" +
            "一、功能介绍\n";

    public static final String SHARE_PREFERENCES_HEAD_SRC = "headSrc";
    public static final String SHARE_PREFERENCES_USERNAME = "username";
    public static final String SHARE_PREFERENCES_MOTTO = "motto";
    public static final String SHARE_PREFERENCES_MAIN_IMG_BG = "main_bg_image";

    public static final int OPEN_ALBUM_TYPE_HEAD = 1;
    public static final int OPEN_ALBUM_TYPE_MAIN_BG = 2;

    /**
     * 本应用在外部存储使用的路径名称
     */
    public static final String EXTERNAL_STORAGE_LOCATION = "/Hibara/Diary/";


}
