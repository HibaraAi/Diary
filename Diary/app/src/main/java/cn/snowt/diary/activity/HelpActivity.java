package cn.snowt.diary.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 22:58
 * @Description: 帮助和关于
 */
public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        List<String> list = new ArrayList<>();
        list.add(Constant.STRING_HELP);
        list.add(Constant.STRING_ABOUT);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(HelpActivity.this,
                android.R.layout.simple_list_item_1,
                list);
        ListView helpContent = findViewById(R.id.help_content);
        helpContent.setAdapter(adapter);
        helpContent.setOnItemLongClickListener((parent, view, position, id) -> {
            if(1==position){
                if (BaseUtils.copyInClipboard(this,"https://github.com/HibaraAi/PasswordManager")) {
                    BaseUtils.shortTipInCoast(HelpActivity.this,"开源代码的网址已复制");
                }
            }
            return false;
        });
    }
}