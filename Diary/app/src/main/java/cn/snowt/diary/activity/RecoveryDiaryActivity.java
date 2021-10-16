package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.snowt.diary.R;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.MD5Utils;
import cn.snowt.diary.util.RSAUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.util.UriUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-05 09:35
 * @Description: 读取备份文件UI
 */
public class RecoveryDiaryActivity extends AppCompatActivity {

    public static final Integer SELECT_BACKUP_FILE = 1;
    public static final Integer SELECT_KEY_FILE = 2;

    private Button loadBackupBtn;
    private Button loadKeyBtn;
    private Button saveBtn;
    private TextView tipView;
    private EditText pinKeyView;

    private Map<String,Object> map;
    /**
     * 存储从密钥文件读取到的密钥
     */
    private String privateKey="";

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
        loadBackupBtn = findViewById(R.id.re_diary_btn_load_backup);
        loadKeyBtn = findViewById(R.id.re_diary_btn_load_key);
        saveBtn = findViewById(R.id.re_diary_btn_save);
        tipView = findViewById(R.id.re_diary_tip);
        pinKeyView = findViewById(R.id.re_diary_pin);
        loadBackupBtn.setOnClickListener(v->{
            OpenFile(SELECT_BACKUP_FILE);
            BaseUtils.longTipInCoast(RecoveryDiaryActivity.this,"请选择备份文件");
        });
        loadKeyBtn.setOnClickListener(v->{
            OpenFile(SELECT_KEY_FILE);
            BaseUtils.longTipInCoast(this,"请选择密钥文件");
        });
        saveBtn.setOnClickListener(v->{
            String pinKey = pinKeyView.getText().toString();
            if("".equals(pinKey)){
                BaseUtils.shortTipInSnack(saveBtn,"没有输入口令，怎么验证？");
            }else{
                //校验口令
                String pinInMap = (String) map.get(Constant.BACKUP_ARGS_NAME_PIN_KEY);
                boolean isBadPin = (pinInMap==null || !pinInMap.equals(MD5Utils.encrypt(Constant.PASSWORD_PREFIX+pinKey)));
                if(isBadPin){
                    String tipStr = "你提供的口令是错误的呢";
                    BaseUtils.shortTipInSnack(tipView,"你提供的口令是错误的呢");
                    tipView.setText(tipStr);
                }else{
                    BaseUtils.longTipInCoast(this,"后台已经在执行恢复日记的后续操作，执行结果将在通知栏告知你");
                    new Thread(() -> {
                        DiaryService diaryService = new DiaryServiceImpl();
                        SimpleResult result = diaryService.recoveryDiary(pinKey, privateKey, map);
                        BaseUtils.simpleSysNotice(RecoveryDiaryActivity.this,result.getMsg());
                    }).start();
                    this.finish();
                }
            }
        });
    }

    /**
     * 选择文件
     * @param selectType 选择文件的类型
     */
    public void OpenFile(Integer selectType) {
        // 指定类型
        String[] mimeTypes = new String[0];
        if(selectType.equals(SELECT_BACKUP_FILE)){
            mimeTypes = new String[]{"application/x-msdos-program"};
        }else if (selectType.equals(SELECT_KEY_FILE)){
            mimeTypes = new String[]{"text/plain"};
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        StringBuilder mimeTypesStr = new StringBuilder();
        for (String mimeType : mimeTypes) {
            mimeTypesStr.append(mimeType).append("|");
        }
        intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
        startActivityForResult(Intent.createChooser(intent, "ChooseFile"), selectType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_BACKUP_FILE && resultCode == RESULT_OK) {
            pinKeyView.setVisibility(View.INVISIBLE);
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
                    loadKeyBtn.setEnabled(false);
                }else{
                    tipStr = "成功读取文件["+UriUtils.getFileName(uri)+"],共有"+dataInJson.size()+"条日记。";
                    if("".equals((String)map.get(Constant.BACKUP_ARGS_NAME_PRIVATE_KEY))){
                        tipStr += "\n这个文件没有包含加密日记，不需要使用密钥";
                        loadKeyBtn.setEnabled(false);
                        saveBtn.setEnabled(true);
                    }else{
                        tipStr += "\n这个文件包含加密日记，请选择密钥文件";
                        saveBtn.setEnabled(false);
                        loadKeyBtn.setEnabled(true);
                    }
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
        }else if(requestCode == SELECT_KEY_FILE && resultCode == RESULT_OK){
            Uri uri = data.getData();
            byte[] bytes = new byte[0];
            try {
                bytes = UriUtils.getBytesByUri(RecoveryDiaryActivity.this,uri);
                String s = new String(bytes);
                Boolean aBoolean = RSAUtils.testPrivateKey(s);
                if(aBoolean){
                    //格式正确，再次测试密钥是否正确
                    String publicKeyInMap = (String) map.get(Constant.BACKUP_ARGS_NAME_PUBLIC_KEY);
                    String testValue = "垃圾程序 by HibaraAi 2021-10-14";
                    String decode = RSAUtils.decode(RSAUtils.encode(testValue, publicKeyInMap), s);
                    if(testValue.equals(decode)){
                        saveBtn.setEnabled(true);
                        String tipStr = "密钥文件正确，最后在下方输入口令验证就可以恢复了。";
                        tipView.setText(tipStr);
                        pinKeyView.setVisibility(View.VISIBLE);
                        privateKey = s;
                    }else{
                        saveBtn.setEnabled(false);
                        String tipStr = "你选择的密钥与文件中的密钥不符，是不是你选错密钥了呢?";
                        BaseUtils.alertDialogToShow(RecoveryDiaryActivity.this,"提示", tipStr);
                        tipView.setText(tipStr);
                        pinKeyView.setVisibility(View.INVISIBLE);
                    }
                }else{
                    //密钥格式不正确
                    saveBtn.setEnabled(false);
                    String tipStr = "密钥正确与否不说，你这个密钥格式都不对，请重新选择。";
                    BaseUtils.alertDialogToShow(RecoveryDiaryActivity.this,"提示", tipStr);
                    tipView.setText(tipStr);
                    pinKeyView.setVisibility(View.INVISIBLE);
                }
            } catch (IOException e) {
                e.printStackTrace();
                saveBtn.setEnabled(false);
                String tipStr = "读取密钥文件失败，请重试。";
                BaseUtils.alertDialogToShow(RecoveryDiaryActivity.this,"提示", tipStr);
                tipView.setText(tipStr);
                pinKeyView.setVisibility(View.INVISIBLE);
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