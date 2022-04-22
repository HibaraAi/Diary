package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.ThanksAdapter;
import cn.snowt.diary.vo.ThanksVo;

/**
 * @Author: HibaraAi
 * @Date: 2022-04-22, 0022 18:32:33
 * @Description: 鸣谢界面
 */
public class ThanksActivity extends AppCompatActivity {

    private List<ThanksVo> thanksVoList;

    private void initThanks(){
        thanksVoList = new ArrayList<>();
        thanksVoList.add(new ThanksVo(R.drawable.thanks_01,"戟间血：","BUG反馈--Android11的存储兼容性问题"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanks);
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar supportActionBar = getSupportActionBar();
        if(null!=supportActionBar){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("感谢！");
        }
        initThanks();
        RecyclerView recyclerView = findViewById(R.id.at_thank_ryv);
        ThanksAdapter thanksAdapter = new ThanksAdapter(thanksVoList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(thanksAdapter);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                break;
            }
        }
        return true;
    }

}