package cn.snowt.diary.util;

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
     * 密码名称的最大长度
     */
    public static final Integer NAME_MAX_LENGTH = 8;
    /**
     * 账号及密码的最长长度
     */
    public static final Integer ACCOUNT_MAX_LENGTH = 40;
    /**
     * 备注的最长长度
     */
    public static final Integer REMARKS_MAX_LENGTH = 20;
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
     * 生成密码时包含的特殊字符
     */
    public static final String SPECIAL_CHAR = "_&$@-+*/.";

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
}
