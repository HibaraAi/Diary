package cn.snowt.note;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;

public class FinishActivity extends AppCompatActivity {
    private ActionBar actionBar;

    public List<Item> itemList;

    private RecyclerView recyclerView;

    private ItemAdapter itemAdapter;

    private ItemDao itemDao = new ItemDao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        bindViewAndSetListener();
        showData();
    }

    @SuppressLint("WrongViewCast")
    public void showData() {
        itemList = itemDao.getAllFinishDescCreate();
        recyclerView = findViewById(R.id.at_finish_list);
        View parent = (View) recyclerView.getParent();
        if(this.getResources().getConfiguration().uiMode == 0x11){
            //parent.setBackgroundResource(R.drawable.day_detail_bg);
            parent.setBackgroundColor(Color.parseColor("#eeeeee"));
        }else{
//            parent.setBackgroundResource(R.drawable.night_bg);
            parent.setBackgroundColor(Color.parseColor("#212b2e"));
        }
        itemAdapter= new ItemAdapter(itemList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(itemAdapter);
        Snackbar.make(recyclerView,"共"+itemList.size()+"条数据",Snackbar.LENGTH_SHORT).show();
    }

    @SuppressLint("WrongViewCast")
    public void showData2() {
        itemList = itemDao.getAllFinishDescFinish();
        recyclerView = findViewById(R.id.at_finish_list);
        itemAdapter= new ItemAdapter(itemList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(itemAdapter);
    }

    private void bindViewAndSetListener() {
        setSupportActionBar(findViewById(R.id.at_finish_toolbar));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("已完成");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_diary_lits,menu);
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
            case R.id.toolbar_diary_list_flow:{
                BaseUtils.shortTipInSnack(recyclerView,"按完成时间排序");
                showData2();
                break;
            }
            default:break;
        }
        return true;
    }
}