package cn.snowt.diary.service;


import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 07:25
 * @Description: 登录相关的操作
 */
public interface LoginService {
    /**
     * 用户登录
     * @param inputPassword 用户输入的密码
     * @return
     */
    SimpleResult login(String inputPassword);

    /**
     * 设置登录密码
     * @param isFirstUse 是第一次使用本软件吗
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param newPasswordAgain 二次输入新密码
     * @return
     */
    SimpleResult setPassword(Boolean isFirstUse,String oldPassword,String newPassword,String newPasswordAgain);
}
