package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cn.snowt.diary.R;
import cn.snowt.diary.service.LabelService;
import cn.snowt.diary.service.impl.LabelServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-07 14:33
 * @Description: 设置同名标签的Activity
 */
public class SetSameLabelActivity extends AppCompatActivity {
    private ActionBar actionBar;

    private final LabelService labelService = new LabelServiceImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_same_label);
        bindViewAndSetListener();
        showAlreadyAddSameLabel();
    }

    /**
     * 展示已经添加的同名标签
     */
    private void showAlreadyAddSameLabel() {
        Map<Integer,String> sameLabel = labelService.getAllSameLabel();
        if(sameLabel.isEmpty()){
            return;
        }
        //将map转成list展示
        List<String> list = new ArrayList<>(sameLabel.size());
        sameLabel.forEach((integer, s) -> list.add(integer+": "+s));
        Collections.reverse(list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                list);
        ListView listView = findViewById(R.id.label_list);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            //解析id和原标签
            TextView view1 = (TextView) view;
            String[] split = view1.getText().toString().split(": ");
            System.out.println("id:"+split[0]);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("修改");
            builder.setMessage("修改同名标签，空输入视为删除");
            EditText editText = new EditText(this);
            editText.setHint("#AA##BB#");
            editText.setText(split[1]);
            builder.setView(editText);
            builder.setPositiveButton("修改", (dialog, which) -> {
                String s = editText.getText().toString();
                SimpleResult result = labelService.updateSameLabel(Integer.valueOf(split[0]),s);
                if(!result.getSuccess()){
                    BaseUtils.alertDialogToShow(this,"修改失败",result.getMsg());
                }else{
                    BaseUtils.alertDialogToShow(this,"修改成功",result.getMsg());
                    showAlreadyAddSameLabel();
                }
            });
            builder.setNegativeButton("取消",null);
            builder.show();
            return true;
        });
    }

    private void bindViewAndSetListener(){
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("设置同名标签");
        }
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
                String tip = "在为日记设置标签时，难免有“重复”。" +
                        "例如添加标签时有时写#杨幂#，有时写#大幂幂#，" +
                        "但它们实际为同一个标签。因此，这里新增一个同名标签的设置，" +
                        "存的时候不管你用哪个标签，查(仅限按标签查找)的时候却一视同仁。" +
                        "当你在查#大幂幂#标签的日记时，#杨幂#的也会一并查询展示。" +
                        "\n长按已有标签进行修改，修改成空输入则视为删除。";
                BaseUtils.alertDialogToShow(this,"说明",tip);
                break;
            }
            case R.id.toolbar_day_add:{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("新增");
                builder.setMessage("输入形如#大幂幂##杨幂#的同名标签");
                EditText editText = new EditText(this);
                editText.setHint("#AA##BB#");
                builder.setView(editText);
                builder.setPositiveButton("添加", (dialog, which) -> {
                    String s = editText.getText().toString();
                    SimpleResult result = labelService.addSameLabel(s);
                    if(!result.getSuccess()){
                        BaseUtils.alertDialogToShow(this,"新增失败",result.getMsg());
                    }else{
                        BaseUtils.alertDialogToShow(this,"添加成功",result.getMsg());
                        showAlreadyAddSameLabel();
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.show();
                break;
            }
            default:break;
        }
        return true;
    }
}