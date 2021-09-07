package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-06 16:28
 * @Description: 设置日记正文的字体大小
 */
public class SetDiarySizeActivity extends AppCompatActivity {
    private SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_diary_size);
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("设置字体大小");
        }
        findViewById(R.id.diary_size_11).setOnClickListener(v->setFontSize(11.0F));
        findViewById(R.id.diary_size_13).setOnClickListener(v->setFontSize(13.0F));
        findViewById(R.id.diary_size_default).setOnClickListener(v->setFontSize(-1.0F));
        findViewById(R.id.diary_size_16).setOnClickListener(v->setFontSize(16.0F));
        findViewById(R.id.diary_size_18).setOnClickListener(v->setFontSize(18.0F));
        findViewById(R.id.diary_size_20).setOnClickListener(v->setFontSize(20.0F));
    }

    private void setFontSize(float size){
        edit.putFloat(Constant.SHARE_PREFERENCES_DIARY_FONT_SIZE,size);
        edit.apply();
        BaseUtils.shortTipInCoast(SetDiarySizeActivity.this,"已设置字体大小，软件重启后生效");
        finish();
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
}