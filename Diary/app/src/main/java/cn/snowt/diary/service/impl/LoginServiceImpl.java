package cn.snowt.diary.service.impl;

import android.content.SharedPreferences;
import android.os.Environment;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.snowt.diary.entity.TempDiary;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.LoginService;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.FileUtils;
import cn.snowt.diary.util.MD5Utils;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.PermissionUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 07:25
 * @Description:
 */
public class LoginServiceImpl implements LoginService {
    public static final String TAG = "LoginService";
    private final SharedPreferences sharedPreferences = BaseUtils.getSharedPreference();
    private final DiaryService diaryService = new DiaryServiceImpl();

    @Override
    public SimpleResult login(String inputPassword) {
        String tip = null;
        SimpleResult result = new SimpleResult();

        if(isBadLogin()){
            //需要制裁走这里
            int punishmentLevel = sharedPreferences.getInt("punishmentLevel", 1);
            String allowDateStr = sharedPreferences.getString("allowDateStr", "");
            tip = "密码错误次数过多, 已受到"+(punishmentLevel-1)+"级制裁。\n请于"+allowDateStr+"后再试。\n现在是:"+BaseUtils.dateToString(new Date())+"。\n(制裁效果会越来越严重,请注意)";
            result.setSuccess(false);
            result.setMsg(tip);
        }else{
            //不需要的走这里
            if(checkLoginPassword(inputPassword)){
                //密码正确
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("loginFailCount",0);
                editor.putInt("punishmentLevel",1);
                editor.apply();
                result.setSuccess(true);
            }else{
                //密码错误
                int loginFailCount = sharedPreferences.getInt("loginFailCount", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("loginFailCount",loginFailCount+1);
                editor.apply();
                tip = "登录失败: 密码错误!  \n已错误次数："+(loginFailCount+1)+"次\n(累计错误"+Constant.MAX_LOGIN_FAIL_COUNT+"次后将受到制裁)";
                result.setSuccess(false);
                result.setMsg(tip);
            }
        }
        return result;
    }

    @Override
    public SimpleResult setPassword(Boolean isFirstUse,String oldPassword, String newPassword, String newPasswordAgain) {
        SimpleResult result = null;
        if(isFirstUse){
            result = tryUpdatePassword(newPassword, newPasswordAgain);
        }else{
            Boolean oldPasswordIsRight = checkOldPassword(Constant.PASSWORD_PREFIX + oldPassword);
            if(oldPasswordIsRight){
                result = tryUpdatePassword(newPassword,newPasswordAgain);
            }else{
                result = new SimpleResult();
                result.setSuccess(false);
                result.setMsg("旧密码错误, 已禁止修改启动密码");
            }
        }
        return result;
    }

    @Override
    public String doFirstLoginOfTheDay() {
        String tip = "";
        //1.读取是否有往年今日内容
        //2.纪念日是否逢整百天整年
        if (MyConfiguration.getInstance().isNeedFirstLoginNotice()) {
            //查询往年今日
            List<DiaryVo> voList = new DiaryServiceImpl().getFormerYear(new Date());
            if(!voList.isEmpty()){
                tip = "[往年今日]有"+voList.size()+"条记录。\n";
            }
            //查询今天是否是纪念日整百/整年
            String haveSpecialCount = new SpecialDayServiceImpl().haveSpecialCount();
            tip += haveSpecialCount;
            tip = tip.trim();
            if(!"".equals(tip)){
                BaseUtils.longTextSysNotice(LitePalApplication.getContext(),tip);
            }
        }
        //3.清除沙盒中的cache
        File externalCacheDir = LitePalApplication.getContext().getExternalCacheDir();
        FileUtils.deleteFolder(externalCacheDir.getAbsolutePath());
        //4.自动备份日记
        //有存储权限且设置了要备份才继续
        if (PermissionUtils.haveExternalStoragePermission(LitePalApplication.getContext())
                && BaseUtils.getDefaultSharedPreferences().getBoolean("autoBackup",false)) {
            boolean backupSuccess = diaryService.autoBackupDiary();
            if(backupSuccess){
                tip = tip+"\n\n自动备份成功。";
            }
        }
        //5.由于将草稿箱的功能移除了，现需要在登录的时候检查草稿箱有没有内容，如果有就将草稿箱的内容存起来
        saveTempDiary();
        return tip;
    }

    /**
     * 由于将草稿箱的功能移除了，现需要在登录的时候检查草稿箱有没有内容，如果有就将草稿箱的内容存起来
     */
    private void saveTempDiary() {
        //查询有没有
        List<TempDiary> tempDiaryList = LitePal.findAll(TempDiary.class);
        if(!tempDiaryList.isEmpty()){
            tempDiaryList.forEach(tempDiary -> {
                SimpleResult simpleResult = diaryService.addOneByArgs(tempDiary.getContent(), "#草稿箱自动保存#", "", "", new ArrayList<>(), new Date(), new ArrayList<>(), null);
                if (simpleResult.getSuccess()) {
                    tempDiary.delete();
                }
            });
        }
    }

    @Override
    public Boolean isFirstLoginInTheDay() {
        String keyTag = "lastLoginDate";
        boolean flag = false;
        String lastLoginDateStr = BaseUtils.getSharedPreference().getString(keyTag, null);
        if(lastLoginDateStr==null){
            flag = true;
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(keyTag,BaseUtils.dateToString(new Date()).substring(0, 10));
            edit.apply();
        }else{
            Date today = new Date();
            String todayStr = BaseUtils.dateToString(today).substring(0, 10);
            if(!todayStr.equals(lastLoginDateStr)){
                flag = true;
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString(keyTag,todayStr);
                edit.apply();
            }
        }
        return flag;
    }

//    /**
//     * 检查用户输入的登录密码是否正确
//     * @param inputPassword 用户输入的面膜
//     * @return true-正确
//     */
//    private Boolean checkLoginPassword(String inputPassword){
//        return sharedPreferences.getString("loginPassword","").equals(MD5Utils.encrypt(Constant.PASSWORD_PREFIX+inputPassword));
//    }

    /**
     * 第二版登录，可以使用旧密码登录，旧密码之只能用五次
     * 检查用户输入的登录密码是否正确
     * @param inputPassword 用户输入的面膜
     * @return true-正确
     */
    private Boolean checkLoginPassword(String inputPassword){
        boolean nowPassword = sharedPreferences.getString("loginPassword", "").equals(MD5Utils.encrypt(Constant.PASSWORD_PREFIX + inputPassword));
        if(nowPassword){
            //新密码登录成功
            if(!"".equals(sharedPreferences.getString("oldLoginPassword", ""))){
                SharedPreferences.Editor edit = sharedPreferences.edit();
                //存在旧密码，旧密码的可用次数减一
                int oldPasswordNum = sharedPreferences.getInt("oldPasswordNum", 5);
                oldPasswordNum = oldPasswordNum-1;
                if(0==oldPasswordNum){
                    //可用次数为0，清除旧密码
                    BaseUtils.longTipInCoast(LitePalApplication.getContext(),"已使用新密码登录5次，旧密码不再可用");
                    edit.putString("oldLoginPassword","");
                }
                else{
                    BaseUtils.longTipInCoast(LitePalApplication.getContext(),"提示：再用新密码登录"+oldPasswordNum+"次后，旧密码即被删除。");
                    edit.putInt("oldPasswordNum",oldPasswordNum);
                }
                edit.apply();
            }
            return true;
        }else{
            boolean oldPassword = sharedPreferences.getString("oldLoginPassword", "").equals(MD5Utils.encrypt(Constant.PASSWORD_PREFIX + inputPassword));
            if(oldPassword){
                //旧密码登录成功，提示应该使用新密码登录
                BaseUtils.longTipInCoast(LitePalApplication.getContext(),"建议使用新密码登录。如忘记新密码请及时修改");
                return true;
            }
        }
        //新旧密码都不对
        return false;
    }

    /**
     * 制裁猜密码
     * @return
     */
    private Boolean isBadLogin(){
        Date allowDate = BaseUtils.stringToDate(sharedPreferences.getString("allowDateStr", "2000-12-30 23:59:59"));
        if(new Date().before(allowDate)){
            //当前时间在允许登陆时间之前
            return true;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int loginFailCount = sharedPreferences.getInt("loginFailCount", 0);
        if(loginFailCount >= Constant.MAX_LOGIN_FAIL_COUNT){
            //错误次数已经到达要制裁的次数
            int punishmentLevel = sharedPreferences.getInt("punishmentLevel", 1);
            if(punishmentLevel>=Constant.MAX_PUNISHMENT_LEVEL){
                //删除数据库
                LitePal.deleteDatabase("diary");
                String path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION).getAbsolutePath();
                FileUtils.safeDeleteFolder(path);
                BaseUtils.longTipInCoast(LitePalApplication.getContext(),"你输入错误密码的次数太多了，程序已自动删除所有存储的数据,密码已无意义，请卸载本程序。");
            }
            int tempLevel = Math.min((punishmentLevel), 10);
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.SECOND,60*tempLevel*tempLevel*tempLevel);
            editor.putInt("loginFailCount",0);
            editor.putInt("punishmentLevel", (punishmentLevel+1));
            editor.putString("allowDateStr",BaseUtils.dateToString(cal.getTime()));
            editor.apply();
            return true;
        }
        //能走到这里，不需要制裁
        return false;
    }

    /**
     * 检查新密码是否合格
     * @param new1 new1
     * @param new2 new2
     * @return  SimpleResult
     */
    private SimpleResult checkNewPassword(String new1,String new2){
        if(null==new1 || "".equals(new1)){
            return SimpleResult.error().msg("密码不能为空!");
        }else if(new1.length()<4){
            return SimpleResult.error().msg("密码长度不能小于4位");
        }else if(!new1.equals(new2)){
            return SimpleResult.error().msg("新密码的两次输入不一致!");
        }else{
            return SimpleResult.ok();
        }
    }

//    /**
//     * 检查旧密码是否正确
//     * @param old 输入的旧密码
//     * @return SimpleResult
//     */
//    private Boolean checkOldPassword(String old){
//        String loginPinInSharedP = sharedPreferences.getString("loginPassword", null);
//        return loginPinInSharedP.equals(MD5Utils.encrypt(old));
//    }

    /**
     * 第二版，旧密码可用时，旧密码也可以用户修改登录密码
     * 检查旧密码是否正确
     * @param old 输入的旧密码
     * @return SimpleResult
     */
    private Boolean checkOldPassword(String old){
        String loginPinInSharedP = sharedPreferences.getString("loginPassword", null);
        String loginPinInSharedP2 = sharedPreferences.getString("oldLoginPassword", null);
        return loginPinInSharedP.equals(MD5Utils.encrypt(old)) || loginPinInSharedP2.equals(MD5Utils.encrypt(old));
    }

//    /**
//     * 尝试更新密码
//     * @param newPasswordStr newPassword
//     * @param newPasswordAgainStr newPasswordAgain
//     * @return SimpleResult
//     */
//    private SimpleResult tryUpdatePassword(String newPasswordStr,String newPasswordAgainStr){
//        SimpleResult result = checkNewPassword(newPasswordStr, newPasswordAgainStr);
//        if(result.getSuccess()){
//            SharedPreferences.Editor edit = sharedPreferences.edit();
//            edit.putString("loginPassword",MD5Utils.encrypt(Constant.PASSWORD_PREFIX+newPasswordStr));
//            edit.putBoolean("firstUse",false);
//            edit.apply();
//            return result.msg("设置启动密码成功!");
//        }else{
//            return result;
//        }
//    }

    /**
     * 第二版，把旧密码也存起来
     * 尝试更新密码
     * @param newPasswordStr newPassword
     * @param newPasswordAgainStr newPasswordAgain
     * @return SimpleResult
     */
    private SimpleResult tryUpdatePassword(String newPasswordStr,String newPasswordAgainStr){
        SimpleResult result = checkNewPassword(newPasswordStr, newPasswordAgainStr);
        if(result.getSuccess()){
            String string = sharedPreferences.getString("loginPassword", "");
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("oldLoginPassword",string);
            edit.putInt("oldPasswordNum",5);
            edit.putString("loginPassword",MD5Utils.encrypt(Constant.PASSWORD_PREFIX+newPasswordStr));
            edit.putBoolean("firstUse",false);
            edit.apply();
            return result.msg("设置启动密码成功!");
        }else{
            return result;
        }
    }
}
