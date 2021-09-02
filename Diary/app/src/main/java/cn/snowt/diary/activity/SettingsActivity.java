package cn.snowt.diary.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.litepal.LitePalApplication;

import java.io.File;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.FileUtils;

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
                default:return false;
            }
            return true;
        }
    }
}