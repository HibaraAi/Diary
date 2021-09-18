package cn.snowt.diary.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.litepal.LitePalApplication;

import java.io.File;

import cn.snowt.diary.R;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.FileUtils;
import cn.snowt.diary.util.MD5Utils;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-01 11:46
 * @Description: 设置
 */
public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("设置");
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                break;
            }
            default:break;
        }
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
//        SharedPreferences sharedPreferences = BaseUtils.getDefaultSharedPreferences();
//        boolean testFun = sharedPreferences.getBoolean("testFun", false);
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference clearCachePreference = findPreference("clearCache");
            //读取缓存大小并展示
            clearCachePreference.setSummary(
                    FileUtils.getStringForDirSize(
                            LitePalApplication.getContext()
                                    .getExternalCacheDir().getAbsolutePath()));
            //读取外存中的数据大小并展示
            Preference clearROMPreference = findPreference("clearROM");
            clearROMPreference.setSummary(FileUtils.getStringForDirSize(
                    Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION).getAbsolutePath()
            ));
            //测试区功能
            boolean openTestFun = BaseUtils.getSharedPreference().getBoolean("openTestFun", false);
            findPreference("customDate").setEnabled(openTestFun);
            //读取当前字体大小
            float fontSize = MyConfiguration.getInstance().getFontSize();
            if(fontSize!=-1){
                findPreference("setDiarySize").setSummary("当前字体大小："+fontSize+"dp");
            }else{
                findPreference("setDiarySize").setSummary("当前为默认字体大小");
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            Context context = preference.getContext();
            switch (preference.getKey()) {
                case "setPassword":{
                    BaseUtils.gotoActivity((Activity) context,SetPasswordActivity.class);
                    break;
                }
                case "clearCache":{
                    File externalCacheDir = LitePalApplication.getContext().getExternalCacheDir();
                    FileUtils.deleteFolder(externalCacheDir.getAbsolutePath());
                    //重新展示缓存大小
                    Preference clearCachePreference = findPreference("clearCache");
                    //读取缓存大小并展示
                    clearCachePreference.setSummary(
                            FileUtils.getStringForDirSize(
                                    LitePalApplication.getContext()
                                            .getExternalCacheDir().getAbsolutePath()));

                    break;
                }
                case "clearROM":{
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("警告");
                    builder.setMessage("此操作仅供卸载前执行。清除外存数据会删除所有软件图片，包括日记图片等数据。请输入登录密码确认此操作");
                    EditText editText = new EditText(context);
                    editText.setBackgroundResource(R.drawable.background_input);
                    editText.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER );
                    editText.setHint("输入登录密码");
                    builder.setView(editText);
                    builder.setPositiveButton("确认清除数据", (dialog, which) -> {
                        String s = editText.getText().toString();
                        boolean truePassword = BaseUtils.getSharedPreference().getString("loginPassword","").equals(MD5Utils.encrypt(Constant.PASSWORD_PREFIX+s));
                        if(truePassword){
                            String path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION).getAbsolutePath();
                            FileUtils.deleteFolder(path);
                            //重新展示缓存大小
                            Preference clearROMPreference = findPreference("clearROM");
                            //读取缓存大小并展示
                            clearROMPreference.setSummary(
                                    FileUtils.getStringForDirSize(path));
                            BaseUtils.longTipInCoast(context,"成功清除外部存储的数据，请放心卸载本程序");
                        }else{
                            BaseUtils.shortTipInCoast(context,"密码校验失败");
                        }
                    });
                    builder.setNegativeButton("取消",null);
                    builder.show();
                    break;
                }
                case "changeEncodeKey":{
                    BaseUtils.gotoActivity((Activity) context,SetRSAActivity.class);
                    break;
                }
                case "useEncode":{
                    BaseUtils.alertDialogToShow(context,"提示","你已更改日记的存储安全策略，请立马重启软件。\n如果你还没有修改过加密密钥，则此项设置无效");
                    break;
                }
                case "backupDiary":{
                    if(ContextCompat.checkSelfPermission(context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED){
                        BaseUtils.alertDialogToShow(context,"提示","你并没有授予外部存储的读写权限,在你许可之前，你不能使用备份功能，但你可以使用导出功能。你可以去修改头像的地方进行授权外部存储的读写权限");
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("设置读取密钥");
                        builder.setMessage("为备份文件设置读取口令\n（提示：\n1.从备份文件读取日记时，如果日记加密过，还需要提供加密密钥，如果你没有加密密钥，则会读取失败\n2.过程可能需要很长时间(取决于你的日记数量),如果有卡住现象是正常情况，耐心等待\n3.备份文件的安全性很差，此功能的设计初衷是为了设备间的数据转移。）");
                        EditText pinView = new EditText(context);
                        pinView.setHint("设置一个读取口令");
                        pinView.setBackgroundResource(R.drawable.background_input);
                        pinView.setMinLines(2);
                        pinView.setMaxLines(2);
                        builder.setView(pinView);
                        builder.setCancelable(false);
                        builder.setPositiveButton("备份", (dialog, which) -> {
                            String privateKeyInJson;
                            String publicKeyInJson;
                            privateKeyInJson = MyConfiguration.getInstance().getPrivateKey();
                            publicKeyInJson = MyConfiguration.getInstance().getPublicKey();
                            DiaryService diaryService = new DiaryServiceImpl();
                            Log.w(TAG,"------此处应该改为service后台进行操作");
                            String pinInput = pinView.getText().toString();
                            if("".equals(pinInput)){
                                BaseUtils.longTipInCoast(context,"口令为空?你在想什么呢?已停止备份");
                            }else{
                                SimpleResult result = diaryService.backupDiary(privateKeyInJson, publicKeyInJson,pinInput);
                                BaseUtils.alertDialogToShow(context,"提示",result.getMsg());
                            }
                        });
                        builder.setNegativeButton("取消",null);
                        builder.show();
                    }
                    break;
                }
                case "recoveryDiary":{
                    if(ContextCompat.checkSelfPermission(context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED){
                        BaseUtils.alertDialogToShow(context,"提示","你并没有授予外部存储的读写权限,在你许可之前，你不能使用恢复功能。你可以去修改头像的地方进行授权外部存储的读写权限");
                    }else{
                        BaseUtils.gotoActivity((Activity) context,RecoveryDiaryActivity.class);
                    }
                    break;
                }
                case "testFun":{
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("输入测试码");
                    builder.setMessage("测试功能仅共开发者测试使用,输入测试码开启");
                    builder.setCancelable(false);
                    EditText editText = new EditText(context);
                    editText.setBackgroundResource(R.drawable.background_input);
                    editText.setMinLines(2);
                    editText.setMaxLines(2);
                    editText.setPadding(30,10,10,30);
                    builder.setView(editText);
                    builder.setPositiveButton("开启", (dialog, which) -> {
                        if(Constant.TEST_FUN_KEY.equals(editText.getText().toString())){
                            SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
                            edit.putBoolean("openTestFun",true);
                            edit.apply();
                            findPreference("customDate").setEnabled(true);
                        }
                    });
                    builder.setNegativeButton("直接关闭测试功能",(dialog, which) -> {
                        findPreference("customDate").setEnabled(false);
                        BaseUtils.getDefaultSharedPreferences().edit().putBoolean("customDate",false).apply();
                        SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
                        edit.putBoolean("openTestFun",false);
                        edit.apply();
                    });
                    builder.show();
                    break;
                }
                case "setDiarySize":{
                    BaseUtils.gotoActivity((Activity) context,SetDiarySizeActivity.class);
                    break;
                }
                case "txtOutput":{
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("验证你的身份");
                    builder.setMessage("将所有日记以纯文本、不加密的方式导出到一个txt文件中。\n提示：导出需要解密数据，此过程非常久，有卡住现象属正常，请耐心等待，完成后自有提示。\n\n输入登录密码验证你的身份以继续");
                    EditText pinView = new EditText(context);
                    pinView.setHint("输入登陆密码");
                    pinView.setBackgroundResource(R.drawable.background_input);
                    pinView.setMinLines(2);
                    pinView.setMaxLines(2);
                    builder.setView(pinView);
                    builder.setCancelable(false);
                    pinView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setPositiveButton("验证密码并导出", (dialog, which) -> {
                        DiaryService diaryService = new DiaryServiceImpl();
                        String pinInput = pinView.getText().toString();
                        if("".equals(pinInput)){
                            BaseUtils.longTipInCoast(context,"你不验证密码我就不导出。");
                        }else{
                            SimpleResult result = diaryService.outputForTxt(pinInput);
                            BaseUtils.alertDialogToShow(context,"提示",result.getMsg());
                        }
                    });
                    builder.setNegativeButton("取消",null);
                    builder.show();
                    break;
                }
                default:return false;
            }
            return true;
        }
    }
}