package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryAdapter;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-02 09:31
 * @Description: 单条日记的详情页面
 */
public class DiaryDetailActivity extends AppCompatActivity {
    public static final String TAG = "DiaryDetailActivity";

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private final DiaryService diaryService = new DiaryServiceImpl();

    private int diaryId = -1;
    private DiaryAdapter adapter;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_diary_detail);
        Intent intent = getIntent();
        if(null!=intent){
            diaryId = intent.getIntExtra("id",-1);
        }
        bingViewAndSetListener();
        showDiaryDetail();
    }

    private void showDiaryDetail() {
        if(-1!=diaryId){
            SimpleResult result = diaryService.getDiaryVoById(diaryId);
            if (result.getSuccess()){
                DiaryVo diaryVo = (DiaryVo) result.getData();
                List<DiaryVo> diaryVos = new ArrayList<>();
                diaryVos.add(diaryVo);
                adapter = new DiaryAdapter(diaryVos);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(layoutManager);
            }else{
                toolbar.setTitle(result.getMsg());
            }
        }
    }

    private void bingViewAndSetListener() {
        toolbar = findViewById(R.id.detail_toolbar);
        recyclerView = findViewById(R.id.detail_recyclerview);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if(null!=supportActionBar){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        fab = findViewById(R.id.detail_fab);
        fab.setOnClickListener(v->{
            showDiaryDetail();
            BaseUtils.shortTipInSnack(this.recyclerView,"日记已刷新");
        });
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