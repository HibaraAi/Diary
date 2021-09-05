package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import cn.snowt.diary.R;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.RSAUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.util.UriUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-05 09:35
 * @Description: 读取备份文件UI
 */
public class RecoveryDiaryActivity extends AppCompatActivity {

    private Button loadBtn;
    private Button saveBtn;
    private TextView tipView;
    private EditText privateKeyView;
    private EditText pinKeyView;

    private Map<String,Object> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_diary);
        findViewAndSetListener();
    }

    private void findViewAndSetListener() {
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar supportActionBar = getSupportActionBar();
        if(null!=supportActionBar){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("恢复日记");
        }
        loadBtn = findViewById(R.id.re_diary_btn_load);
        saveBtn = findViewById(R.id.re_diary_btn_save);
        tipView = findViewById(R.id.re_diary_tip);
        privateKeyView = findViewById(R.id.re_diary_private);
        pinKeyView = findViewById(R.id.re_diary_pin);
        loadBtn.setOnClickListener(v->{
            OpenFile(this.loadBtn);
            BaseUtils.longTipInCoast(RecoveryDiaryActivity.this,"请选择备份文件");
        });
        saveBtn.setOnClickListener(v->{
            String pinKey = pinKeyView.getText().toString();
            String privateKey = privateKeyView.getText().toString();
            if("".equals(pinKey)){
                BaseUtils.shortTipInSnack(saveBtn,"没有输入口令，怎么验证？");
            }else{
                if(!"".equals(map.get(Constant.BACKUP_ARGS_NAME_PRIVATE_KEY)) && "".equals(privateKey)){
                    BaseUtils.shortTipInSnack(saveBtn,"这个文件包含加密日记，请提供长短两个密钥进行解密");
                }else{
                    DiaryService diaryService = new DiaryServiceImpl();
                    SimpleResult result = diaryService.recoveryDiary(pinKey, privateKey, map);
                    if (result.getSuccess()){
                        saveBtn.setEnabled(false);
                        loadBtn.setEnabled(false);
                    }
                    BaseUtils.shortTipInSnack(saveBtn,result.getMsg());
                }
            }
        });
    }

    /**
     * 选择备份文件
     * @param view
     */
    public void OpenFile(View view) {
        // 指定类型
        String[] mimeTypes = {"application/x-msdos-program"};
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        StringBuilder mimeTypesStr = new StringBuilder();
        for (String mimeType : mimeTypes) {
            mimeTypesStr.append(mimeType).append("|");
        }
        intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
        startActivityForResult(Intent.createChooser(intent, "ChooseFile"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                byte[] bytes = UriUtils.getBytesByUri(RecoveryDiaryActivity.this,uri);
                String s = new String(bytes);
                map = (Map<String,Object>) JSON.parse(s);
                String pinKey = (String) map.get(Constant.BACKUP_ARGS_NAME_PIN_KEY);
                List<JSONObject> dataInJson = (List<JSONObject>) map.get(Constant.BACKUP_ARGS_NAME_DATA_NAME);
                String tipStr;
                if(null==pinKey || "".equals(pinKey)){
                    tipStr = "读取备份文件失败，请重试。\n(请确保备份文件正确且没有被破坏过)";
                    saveBtn.setEnabled(false);
                }else{
                    tipStr = "成功读取文件["+UriUtils.getFileName(uri)+"],共有"+dataInJson.size()+"条日记。请输入密钥、口令后点击“检验口令并恢复日记”按钮";
                    if("".equals((String)map.get(Constant.BACKUP_ARGS_NAME_PRIVATE_KEY))){
                        privateKeyView.setHint("这个文件没有包含加密日记，不需要使用密钥");
                        privateKeyView.setEnabled(false);
                    }else{
                        privateKeyView.setHint("长密钥——在这里输入加密密钥，本次需要的密钥为——长密钥");
                        privateKeyView.setEnabled(true);
                    }
                    saveBtn.setEnabled(true);
                }
                BaseUtils.alertDialogToShow(RecoveryDiaryActivity.this,"提示", tipStr);
                tipView.setText(tipStr);
            } catch (Exception e) {
                e.printStackTrace();
                saveBtn.setEnabled(false);
                String tipStr = "读取备份文件失败，请重试。\n(请确保备份文件正确且没有被破坏过)";
                BaseUtils.alertDialogToShow(RecoveryDiaryActivity.this,"提示", tipStr);
                tipView.setText(tipStr);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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