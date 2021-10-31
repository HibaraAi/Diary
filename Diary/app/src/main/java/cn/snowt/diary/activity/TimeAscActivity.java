package cn.snowt.diary.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.BezierRadarHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;

import java.util.ArrayList;
import java.util.Date;
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
    public static final String OPEN_FROM_TYPE = "openFrom";
    public static final int OPEN_FROM_FORMER_YEARS = 1;
    public static final int OPEN_FROM_SIMPLE_DIARY_LIST = 2;
    private List<DiaryVo> voList = new ArrayList<>();
    private DiaryService diaryService = new DiaryServiceImpl();
    private DiaryAdapter diaryAdapter;
    private int nowIndex = 0;
    private RefreshLayout refreshLayout;
    private RecyclerView recyclerView = null;
    private FloatingActionButton fab;
    private ActionBar actionBar;

    @SuppressLint("NotifyDataSetChanged")
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
        Intent intent = getIntent();
        int intExtra = intent.getIntExtra(OPEN_FROM_TYPE, -1);
        switch (intExtra){
            case OPEN_FROM_FORMER_YEARS:{
                actionBar.setTitle("往年今日");
                stopRefreshLayout();
                showForMerYears();
                break;
            }
            case OPEN_FROM_SIMPLE_DIARY_LIST:{
                actionBar.setTitle("临时信息流");
                stopRefreshLayout();
                showByIds(intent.getIntegerArrayListExtra("ids"),intent.getIntExtra("sortType",1));
                break;
            }
            default:{
                getDiaryForFirstShow();
                break;
            }
        }
        diaryAdapter = new DiaryAdapter(voList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(diaryAdapter);
    }

    /**
     * 展示给定的几个id
     * @param ids
     * @param sortType 排序类型，1为时间倒序，2为时间顺序
     */
    private void showByIds(ArrayList<Integer> ids,int sortType) {
        ids.forEach(id->voList.add((DiaryVo) diaryService.getDiaryVoById(id).getData()));
        if(sortType==2){
            System.out.println("************-----------------showByIds");
            voList.sort((o1, o2) -> {
                Date o1Date = BaseUtils.stringToDate(o1.getModifiedDate());
                Date o2Date = BaseUtils.stringToDate(o2.getModifiedDate());
                if (o1Date.after(o2Date)) {
                    return 1;
                } else if (o1Date.before(o2Date)) {
                    return -1;
                } else {
                    return 0;
                }
            });
        }
    }

    /**
     * 停用下拉刷新和上拉加载
     */
    private void stopRefreshLayout() {
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            BaseUtils.shortTipInSnack(recyclerView,"不支持刷新 Orz");
            refreshLayout.finishRefresh();
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            BaseUtils.shortTipInSnack(recyclerView,"没有更多了 QaQ");
            refreshLayout.finishLoadMore();
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void bindViewAndSetListener(){
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("时间升序浏览");
        }
        fab = findViewById(R.id.asc_fab);
        fab.setOnClickListener(v->{
            recyclerView.scrollToPosition(0);
            BaseUtils.shortTipInSnack(this.recyclerView,"已返回顶部 OvO");
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

    /**
     * 原先此Activity只用于时间升序展示日记，但后来增加了其他功能
     */
    private void getDiaryForFirstShow(){
        voList.addAll(diaryService.getDiaryVoListAsc(0, 5));
        nowIndex = voList.size();
    }

    /**
     * 时间升序浏览时用的刷新数据
     */
    @SuppressLint("NotifyDataSetChanged")
    private void refreshDiary() {
        voList.clear();
        getDiaryForFirstShow();
        diaryAdapter.notifyDataSetChanged();
    }

    /**
     * 时间升序浏览时用的加载更多
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadMoreDiary() {
        List<DiaryVo> diaryVoList = diaryService.getDiaryVoListAsc(nowIndex, 5);
        if(diaryVoList.size()==0){
            BaseUtils.shortTipInSnack(recyclerView,"没有更多日记了。QaQ");
        }else{
            voList.addAll(diaryVoList);
            nowIndex += diaryVoList.size();
            diaryAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 加载往年今日的日记
     */
    private void showForMerYears(){
        voList = diaryService.getFormerYear(new Date());
        if(voList.isEmpty()){
            BaseUtils.shortTipInSnack(recyclerView,"往年的今日没有记录 UnU");
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