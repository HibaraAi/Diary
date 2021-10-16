package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.SpecialDayAdapter;
import cn.snowt.diary.service.SpecialDayService;
import cn.snowt.diary.service.impl.SpecialDayServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.vo.SpecialDayVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-09 22:48:21
 * @Description:
 */
public class SpecialDayActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private RecyclerView recyclerView = null;

    private List<SpecialDayVo> voList = new ArrayList<>();
    private SpecialDayService specialDayService = new SpecialDayServiceImpl();
    private SpecialDayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_day);
        bindViewAndSetListener();
        refreshDataForShow();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refreshDataForShow();
    }

    /**
     * 刷新数据
     */
    private void refreshDataForShow() {
        voList = specialDayService.getAllDays();
        adapter = new SpecialDayAdapter(voList);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void bindViewAndSetListener() {
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("纪念日");
        }
        recyclerView = findViewById(R.id.special_day_rv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_special_day,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                break;
            }
            case R.id.toolbar_day_help:{
                String tip = "纪念日是后来追加的功能，所以并不打算做得很完善，" +
                        "并且和扫雷一样是游离于整个日记主体之外的(不参与数据备份/导出/加密等)。" +
                        "\n你仅可以新增一个公历日期作为起始日，再额外附带一些简单描述。" +
                        "纪念日的提醒功能只会在软件启动时才有通知，且日期累计数小于一年时" +
                        "逢整百天提示，大于一年时逢整年才提示。停止计数后不再提醒。\n" +
                        "另外，你不能删除某个纪念日，因为默认你记录了就表示已经发生了，你只可以停止计数。" +
                        "就好像你刚上大学设置了开学日，毕业后应该是停掉这个计数日，而不是删除。";
                BaseUtils.alertDialogToShow(this,"说明",tip);
                break;
            }
            case R.id.toolbar_day_add:{
                BaseUtils.gotoActivity(this,AddSpecialDayActivity.class);
                break;
            }
            default:break;
        }
        return true;
    }
}