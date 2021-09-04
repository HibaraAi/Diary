package cn.snowt.diary.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.litepal.LitePalApplication;

import java.io.File;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.FileUtils;
import cn.snowt.diary.util.MD5Utils;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-01 11:46
 * @Description: 设置
 */
public class SettingsActivity extends AppCompatActivity {

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
                default:return false;
            }
            return true;
        }
    }
}