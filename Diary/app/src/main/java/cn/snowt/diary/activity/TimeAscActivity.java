package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.BezierRadarHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryAdapter;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-17 20:19
 * @Description: 时间升序浏览专用
 */
public class TimeAscActivity extends AppCompatActivity {
    private List<DiaryVo> voList = new ArrayList<>();
    private DiaryService diaryService = new DiaryServiceImpl();
    private DiaryAdapter diaryAdapter;
    private int nowIndex = 0;
    private RefreshLayout refreshLayout;
    private RecyclerView recyclerView = null;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_time_asc);
        bindViewAndSetListener();
        getDiaryForFirstShow();
        diaryAdapter = new DiaryAdapter(voList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(diaryAdapter);
    }

    @SuppressLint("NonConstantResourceId")
    private void bindViewAndSetListener(){
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("时间升序浏览");
        }
        fab = findViewById(R.id.asc_fab);
        fab.setOnClickListener(v->{
            recyclerView.scrollToPosition(0);
            BaseUtils.shortTipInSnack(this.recyclerView,"已返回顶部");
        });
        recyclerView = findViewById(R.id.asc_recyclerview);
        refreshLayout = findViewById(R.id.asc_refresh);
        refreshLayout.setRefreshHeader(new BezierRadarHeader(this)
                .setEnableHorizontalDrag(true)
                .setPrimaryColor(Color.parseColor("#FA7298")));
        refreshLayout.setRefreshFooter(new BallPulseFooter(this).setSpinnerStyle(SpinnerStyle.FixedBehind));
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            refreshDiary();
            refreshLayout.finishRefresh();
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            loadMoreDiary();
            refreshLayout.finishLoadMore();
        });
    }

    private void getDiaryForFirstShow(){
        voList.addAll(diaryService.getDiaryVoListAsc(0, 5));
        nowIndex = voList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshDiary() {
        voList.clear();
        getDiaryForFirstShow();
        diaryAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadMoreDiary() {
        List<DiaryVo> diaryVoList = diaryService.getDiaryVoListAsc(nowIndex, 5);
        if(diaryVoList.size()==0){
            BaseUtils.shortTipInSnack(recyclerView,"没有更多日记了。");
        }else{
            voList.addAll(diaryVoList);
            nowIndex += diaryVoList.size();
            diaryAdapter.notifyDataSetChanged();
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
}