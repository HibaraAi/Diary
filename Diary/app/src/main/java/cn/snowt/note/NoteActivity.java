package cn.snowt.note;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Date;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.LoginActivity;
import cn.snowt.diary.activity.MainActivity;
import cn.snowt.diary.activity.SettingsActivity;
import cn.snowt.diary.util.BaseUtils;

public class NoteActivity extends AppCompatActivity {
    public static final Integer OPEN_FROM_MAIN_ACTIVITY = 1;  //从主界面打开的
    public static final Integer OPEN_FROM_BEFORE_LOGIN = 2;  //登录之前就已经打开
    public static final String OPEN_FROM = "open_from";
    //记录这个open_from的值
    private Integer OPEN_FROM_VALUE = 0;

    private ActionBar actionBar;

    public List<Item> itemList;

    private RecyclerView recyclerView;

    private ItemAdapter itemAdapter;

    private final ItemDao itemDao = new ItemDao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        bindViewAndSetListener();
        OPEN_FROM_VALUE = getIntent().getIntExtra(OPEN_FROM,0);
        if (OPEN_FROM_VALUE.equals(OPEN_FROM_MAIN_ACTIVITY)){
            //如果是主界面来的，显示返回键
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        showData();
    }

    @SuppressLint("WrongViewCast")
    public void showData() {
        itemList = itemDao.getAllUnfinished();
        recyclerView = findViewById(R.id.at_list);
        View parent = (View) recyclerView.getParent();
        if(this.getResources().getConfiguration().uiMode == 0x11){
            parent.setBackgroundResource(R.drawable.day_detail_bg);

        }else{
            parent.setBackgroundResource(R.drawable.night_bg);
        }
        itemAdapter= new ItemAdapter(itemList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(itemAdapter);
        Snackbar.make(recyclerView,"共"+itemList.size()+"条数据",Snackbar.LENGTH_SHORT).show();
    }

    private void bindViewAndSetListener() {
        setSupportActionBar(findViewById(R.id.at_toolbar));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("便签");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
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
            case R.id.toolbar_add:{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle("新增");
                EditText editText = new EditText(this);
                editText.setLines(10);
                editText.setGravity(Gravity.TOP);
                editText.setBackgroundResource(R.drawable.edge);
                editText.setHint("输入......");
                editText.setMinLines(10);
                editText.setMaxLines(10);
                editText.setPadding(10,10,10,10);
                builder.setView(editText);
                builder.setPositiveButton("添加", (dialog, which) -> {
                    String text = editText.getText().toString();
                    if(text.isEmpty()){
                        Toast.makeText(NoteActivity.this,"不能为空!",Toast.LENGTH_SHORT).show();
                    }else{
                        if("123".equals(text) && OPEN_FROM_VALUE.equals(OPEN_FROM_BEFORE_LOGIN)){
                            Intent intent = new Intent(NoteActivity.this,LoginActivity.class);
                            intent.putExtra("trueLogin",true);
                            NoteActivity.this.startActivity(intent);
                            finish();
                            return;
                        }
                        itemDao.addOne(new Item(text,new Date()));
                        showData();
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.show();
                break;
            }
            case R.id.toolbar_finish:{
                Intent intent = new Intent(this,FinishActivity.class);
                startActivity(intent);
            }
            default:break;
        }
        return true;
    }
}