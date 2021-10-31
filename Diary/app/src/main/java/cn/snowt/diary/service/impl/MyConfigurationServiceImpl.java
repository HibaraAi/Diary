package cn.snowt.diary.service.impl;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.litepal.LitePalApplication;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import cn.snowt.diary.service.MyConfigurationService;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.util.UriUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-28 09:57
 * @Description:
 */
public class MyConfigurationServiceImpl implements MyConfigurationService {

    private static final String TAG = "MyConfigurationServiceImpl";

    @Override
    public SimpleResult updateHeadImg(Uri headImgUri) {
        SimpleResult result = new SimpleResult();
        //此方法已过时
        File path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"Config/");
        if(!path.exists()){
            Log.i(TAG,"------创建目录"+path.getAbsolutePath());
            path.mkdirs();
        }
        String absolutePath = path.getAbsolutePath();
        File newImgFile = new File((absolutePath + "/" + UUID.randomUUID().toString() + ".hibara"));
        try {
            newImgFile.createNewFile();
            UriUtils.copyFile(LitePalApplication.getContext(),headImgUri, newImgFile);
            Log.i(TAG,"------新图片的路径:"+newImgFile.getAbsolutePath());
            result.setMsg(newImgFile.getAbsolutePath());
            SharedPreferences sharedPreference = BaseUtils.getSharedPreference();
            //String oldHeadImg = MyConfiguration.getInstance().getHeadImg();
            String oldHeadImg = sharedPreference.getString(Constant.SHARE_PREFERENCES_HEAD_SRC,null);
            if(null!=oldHeadImg){
                File file = new File(oldHeadImg);
                file.delete();
                Log.i(TAG,"------旧头像已删除");
            }
            SharedPreferences.Editor edit = sharedPreference.edit();
            edit.putString(Constant.SHARE_PREFERENCES_HEAD_SRC,result.getMsg());
            edit.apply();
            result.setSuccess(true);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"------创建文件失败");
            result.setSuccess(false);
            result.setMsg("创建头像图片文件失败");
        }finally {
            File tempFile = new File(UriUtils.getFilePathFromURI(LitePalApplication.getContext(),headImgUri));
            if (tempFile.exists()){
                tempFile.delete();
                Log.i(TAG,"------删除临时文件成功");
            }
        }
        return result;
    }

    @Override
    public void updateMotto(String motto) {
        SharedPreferences sharedPreference = BaseUtils.getSharedPreference();
        SharedPreferences.Editor edit = sharedPreference.edit();
        edit.putString(Constant.SHARE_PREFERENCES_MOTTO,motto);
        edit.apply();
    }

    @Override
    public void updateUsername(String username) {
        SharedPreferences sharedPreference = BaseUtils.getSharedPreference();
        SharedPreferences.Editor edit = sharedPreference.edit();
        edit.putString(Constant.SHARE_PREFERENCES_USERNAME,username);
        edit.apply();
    }

    @Override
    public SimpleResult updateMainBgImage(Uri bgImageUri) {
        SimpleResult result = new SimpleResult();
        //此方法已过时
        File path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"Config/");
        if(!path.exists()){
            Log.i(TAG,"------创建目录"+path.getAbsolutePath());
            path.mkdirs();
        }
        String absolutePath = path.getAbsolutePath();
        File newImgFile = new File((absolutePath + "/" + UUID.randomUUID().toString() + ".hibara"));
        try {
            newImgFile.createNewFile();
            UriUtils.copyFile(LitePalApplication.getContext(),bgImageUri, newImgFile);
            Log.i(TAG,"------新图片的路径:"+newImgFile.getAbsolutePath());
            result.setMsg(newImgFile.getAbsolutePath());
            SharedPreferences sharedPreference = BaseUtils.getSharedPreference();
            //不能使用这个，由于没有更新后没有改变单例中存储的值，所以多次选择的话只会删除最初的那一张
            //String oldBgImg = MyConfiguration.getInstance().getBgImg();
            String oldBgImg = sharedPreference.getString(Constant.SHARE_PREFERENCES_MAIN_IMG_BG,null);
            if(null!=oldBgImg){
                File file = new File(oldBgImg);
                file.delete();
                Log.i(TAG,"------旧图片已删除");
            }
            SharedPreferences.Editor edit = sharedPreference.edit();
            edit.putString(Constant.SHARE_PREFERENCES_MAIN_IMG_BG,result.getMsg());
            edit.apply();
            result.setSuccess(true);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"------创建文件失败");
            result.setSuccess(false);
            result.setMsg("创建背景图片文件失败");
        }finally {
            File tempFile = new File(UriUtils.getFilePathFromURI(LitePalApplication.getContext(),bgImageUri));
            if (tempFile.exists()){
                tempFile.delete();
                Log.i(TAG,"------删除临时文件成功");
            }
        }
        return result;
    }
}
