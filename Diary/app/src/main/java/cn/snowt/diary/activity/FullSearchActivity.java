package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import cn.snowt.diary.R;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;

public class FullSearchActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private Button button;
    private EditText editText;

    private DiaryService diaryService = new DiaryServiceImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_search);
        bindViewAndSetListener();
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:{
                    button.setText("搜索");
                    editText.setText("");
                    button.setEnabled(true);
                    editText.setEnabled(true);
                    break;
                }
                case 2:{
                    String string = msg.getData().getString("1");
                    BaseUtils.alertDialogToShow(FullSearchActivity.this,"提示", string);
                    button.setText("搜索");
                    editText.setText("");
                    button.setEnabled(true);
                    editText.setEnabled(true);
                    break;
                }
                default:break;
            }
        }
    };

    private void bindViewAndSetListener() {
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("慢速搜索");
        }
        button = findViewById(R.id.full_search_btn);
        editText = findViewById(R.id.full_search_input);
        button.setOnClickListener(v->{
            String searchValue = editText.getText().toString();
            searchValue = searchValue.trim();
            if(searchValue.length()==0 || searchValue.length()>10){
                BaseUtils.longTipInCoast(this,"关键字字数必须在1-10。");
            }else{
                String finalSearchValue = searchValue;
                button.setEnabled(false);
                editText.setEnabled(false);
                button.setText("正在搜索");
                new Thread(() -> {
                     SimpleResult result = (diaryService.decodeSearch(finalSearchValue));
                     if(result.getSuccess()){
                         List<DiaryVo> diaryVos = (List<DiaryVo>) result.getData();
                         Intent intent = new Intent(FullSearchActivity.this,DiaryListActivity.class);
                         intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_FULL_SEARCH);
                         intent.putExtra("diaryVos", (Serializable) diaryVos);
                         startActivity(intent);
                         Message message = new Message();
                         message.what = 1;
                         handler.sendMessage(message);
                     }else{
                         Message message = new Message();
                         message.what = 2;
                         Bundle bundle = new Bundle();
                         bundle.putString("1",result.getMsg());
                         message.setData(bundle);
                         handler.sendMessage(message);
                     }
                }).start();

            }
        });
    }

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