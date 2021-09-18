package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import org.litepal.LitePal;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryAxisAdapter;
import cn.snowt.diary.entity.TempDiary;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-01 14:16
 * @Description: 简易日记的RecyclerView展示UI，class旧名字叫TimeAxisActivity
 */
public class DiaryListActivity extends AppCompatActivity {
    public static final String TAG = "DiaryListActivity";
    public static final String OPEN_FROM_TYPE = "openType";

    public static final String DATE_ONE = "date1";
    public static final String DATE_TWO = "date2";

    public static final int OPEN_FROM_TIME_AXIS = 1;
    public static final int OPEN_FROM_TEMP_DIARY = 2;
    public static final int OPEN_FROM_SEARCH_DIARY = 3;
    public static final int OPEN_FROM_SEARCH_LABEL = 4;
    public static final int OPEN_FROM_LABEL_LIST = 5;

    private final DiaryService diaryService = new DiaryServiceImpl();

    private RecyclerView recyclerView;
    private TextView tipView;
    private Toolbar toolbar;

    private List<DiaryVo> diaryList;
    private DiaryAxisAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_time_axis);
        bindViewAndSetListener();
        Intent intent = getIntent();
        int openType = intent.getIntExtra(OPEN_FROM_TYPE, -1);
        switch (openType) {
            case OPEN_FROM_TIME_AXIS:{
                showSimpleDiary(intent.getStringExtra(DATE_ONE),intent.getStringExtra(DATE_TWO));
                break;
            }
            case OPEN_FROM_TEMP_DIARY:{
                showSimpleDiary();
                break;
            }
            case OPEN_FROM_SEARCH_DIARY:{
                showSimpleDiary(intent.getIntegerArrayListExtra("ids"),intent.getStringExtra("searchValue"));
                break;
            }
            case OPEN_FROM_SEARCH_LABEL:{
                showSimpleDiary(intent.getStringExtra("label"));
                break;
            }
            case OPEN_FROM_LABEL_LIST:{
                showAllLabel();
                break;
            }
            default:break;
        }


    }

    private void bindViewAndSetListener(){
        recyclerView = findViewById(R.id.axis_recyclerview);
        tipView = findViewById(R.id.axis_tip);
        toolbar = findViewById(R.id.axis_toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if(null!=supportActionBar){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 展示从草稿箱来的数据
     */
    private void showSimpleDiary(){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("草稿箱");
        List<TempDiary> tempDiaries = LitePal.order("id desc").find(TempDiary.class);
        if(tempDiaries.size()>0){
            tipView.setText("草稿箱共有"+tempDiaries.size()+"条记录");
            List<DiaryVo> vos = new ArrayList<>();
            tempDiaries.forEach(tempDiary -> {
                DiaryVo vo = new DiaryVo();
                vo.setId(tempDiary.getId());
                String subDiary = tempDiary.getContent().length()>30?
                        (tempDiary.getContent().substring(0,30))+"...":tempDiary.getContent();
                vo.setContent(subDiary);
                vo.setModifiedDate("");
                vo.setPicSrcList(new ArrayList<>());
                vos.add(vo);
            });
            diaryList = vos;
            adapter = new DiaryAxisAdapter(diaryList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
        }else{
            tipView.setText("草稿箱暂时还没有内容");
        }
    }

    /**
     * 展示从时间轴来的数据
     * @param dateOneStr
     * @param dateTwoStr
     */
    private void showSimpleDiary(String dateOneStr,String dateTwoStr){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("时间轴");;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = dateFormat.parse(dateOneStr);
            date2 = dateFormat.parse(dateTwoStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //date1在前
        assert date2 != null;
        if(date2.before(date1)){
            Date temp = date1;
            date1 = date2;
            date2 = temp;
        }
        SimpleResult result = diaryService.getSimpleDiaryByDate(date1,date2);
        diaryList = (List<DiaryVo>) result.getData();
        tipView.setText("在"+dateOneStr+"到"+dateTwoStr+"的时间段内，共有"+diaryList.size()+"条日记");
        adapter = new DiaryAxisAdapter(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 展示从搜索结果来的数据
     * @param ids
     */
    private void showSimpleDiary(ArrayList<Integer> ids,String searchValue){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("搜索结果");
        diaryList = diaryService.getSimpleDiaryByIds(ids);
        tipView.setText("在标签中或未加密日记中，包含字符["+searchValue+"]的日记共有"+diaryList.size()+"条");
        adapter = new DiaryAxisAdapter(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 展示从标签选择来的数据
     * @param labelStr
     */
    private void showSimpleDiary(String labelStr){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle(labelStr);
        diaryList = diaryService.getSimpleDiaryByLabel(labelStr);
        tipView.setText("含有标签["+labelStr+"]的日记共有"+diaryList.size()+"条");
        adapter = new DiaryAxisAdapter(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 展示所有标签
     */
    private void showAllLabel(){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("标签集");
        Set<String> labels = diaryService.getAllLabels();
        if(labels.isEmpty()){
            tipView.setText("系统中还没有日记加过标签");
        }else{
            tipView.setText("所有日记中，共统计到["+labels.size()+"]个标签。\n点击标签即可查询该标签下的所有日记");
            List<DiaryVo> vos = new ArrayList<>();
            labels.forEach(label -> {
                DiaryVo vo = new DiaryVo();
                vo.setId(-1);
                vo.setContent(label);
                vo.setModifiedDate("");
                vo.setPicSrcList(new ArrayList<>());
                vos.add(vo);
            });
            diaryList = vos;
            adapter = new DiaryAxisAdapter(diaryList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
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