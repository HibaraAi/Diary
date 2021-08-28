package cn.snowt.diary.service;

import android.net.Uri;

import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-28 09:57
 * @Description:
 */
public interface MyConfigurationService {
    /**
     * 更新头像
     * @param headImgUri
     * @return
     */
    SimpleResult updateHeadImg(Uri headImgUri);

    /**
     * 更新个性签名
     * @param motto
     */
    void updateMotto(String motto);

    /**
     * 更新用户名
     * @param username
     */
    void updateUsername(String username);

    /**
     * 更新背景图
     * @param bgImageUri
     * @return
     */
    SimpleResult updateMainBgImage(Uri bgImageUri);
}
