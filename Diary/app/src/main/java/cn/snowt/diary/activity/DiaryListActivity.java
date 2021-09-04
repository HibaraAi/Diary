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
        if(null==date1 || null==date2){
            BaseUtils.shortTipInCoast(DiaryListActivity.this,"输入的时间格式有误，请返回后重新输入");
            tipView.setText("输入的时间格式有误，请返回后重新输入");
        }else{
            //date1在前
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
    }

    /**
     * 展示从搜索结果来的数据
     * @param ids
     */
    private void showSimpleDiary(ArrayList<Integer> ids,String searchValue){
        diaryList = diaryService.getSimpleDiaryByIds(ids);
        tipView.setText("在标签中或未加密日记中，包含字符["+searchValue+"]的日记共有"+diaryList.size()+"条");
        adapter = new DiaryAxisAdapter(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
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