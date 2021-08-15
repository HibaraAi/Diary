package cn.snowt.diary.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-14 22:07
 * @Description:
 */
public class MainActivity extends AppCompatActivity {
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViewAndSetListener();
    }

    @SuppressLint("NonConstantResourceId")
    private void bindViewAndSetListener(){
        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.nav_settings:{
                    BaseUtils.gotoActivity(MainActivity.this,SetPasswordActivity.class);
                    break;
                }
                case R.id.nav_help:{
                    BaseUtils.gotoActivity(MainActivity.this,HelpActivity.class);
                    break;
                }
                default:{
                    Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            return true;
        });
    }
}