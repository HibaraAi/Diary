package cn.snowt.diary.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import cn.snowt.diary.R;
import cn.snowt.diary.service.LoginService;
import cn.snowt.diary.service.impl.LoginServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 07:30
 * @Description: 设置密码
 */
public class SetPasswordActivity extends AppCompatActivity {


    private LoginService loginService = new LoginServiceImpl();

    private TextView textTip = null;
    private EditText oldPassword = null;
    private EditText newPassword = null;
    private EditText newPasswordAgain = null;
    private Button btnCommit = null;
    private Button btnCancel = null;
    private TextView resultTip = null;

    private Boolean isFirstUse = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        bindViewAndSetListener();
        isFirstUse = autoUi();
    }


    private void bindViewAndSetListener(){
        textTip = findViewById(R.id.setpassword_tip);
        oldPassword = findViewById(R.id.setpassword_input_old);
        newPassword = findViewById(R.id.setpassword_input_new);
        newPasswordAgain = findViewById(R.id.setpassword_input_new_again);
        btnCommit = findViewById(R.id.setpassword_btn_commit);
        btnCancel = findViewById(R.id.setpassword_btn_cancel);
        resultTip = findViewById(R.id.setpassword_result_tip);

        btnCommit.setOnClickListener(v->{
            String oldPasswordStr = oldPassword.getText().toString();
            String newPasswordStr = newPassword.getText().toString();
            String newPasswordAgainStr = newPasswordAgain.getText().toString();
            oldPassword.setText("");
            newPassword.setText("");
            newPasswordAgain.setText("");
            SimpleResult result = loginService.setPassword(isFirstUse, oldPasswordStr, newPasswordStr, newPasswordAgainStr);
            BaseUtils.shortTipInCoast(this,result.getMsg());
            if (result.getSuccess()){
                finish();
            }else{
                resultTip.setText(result.getMsg());
            }
        });

        btnCancel.setOnClickListener(v-> finish());

        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar supportActionBar = getSupportActionBar();
        if(null!=supportActionBar){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("重置登录密码");
        }
    }

    /**
     * 首次使用软件的密码设置界面需要不一样的展示，所以此Activity有逻辑代码
     * @return true-第一次使用
     */
    private Boolean autoUi(){
        SharedPreferences sharedPreferences = getSharedPreferences("appSetting", MODE_PRIVATE);
        boolean firstUse = sharedPreferences.getBoolean("firstUse", true);
        String tip;
        if(firstUse){
            tip = "\t提示:\n\t第一次使用本软件，请为本软件设置启动密码。\n\t请牢记密码，本软件不支持密码找回!\n\n" +
                    "不要试图以猜密码的方式来找回密码，猜错5次会受到一次制裁，第一次制裁时间为1分钟，" +
                    "第n次制裁时间为n^3分钟，(制裁等级的n最大为10。当内部记录的n达到15时，" +
                    "软件会自动清空本软件存储的所有数据。每当输入正确密码，n重置为1。)";
            oldPassword.setVisibility(View.INVISIBLE);
        }else{
            tip = new String("\t提示:\n\t凭旧密码修改启动密码。\n\t请牢记密码，本软件不支持密码找回!\n\n" +
                    "特殊说明：\n1. 不要试图以猜密码的方式来找回密码，猜错5次会受到一次制裁，第一次制裁时间为1分钟，" +
                    "第n次制裁时间为n^3分钟，(制裁等级的n最大为10。当内部记录的n达到15时，" +
                    "软件会自动清空本软件存储的所有数据。每当输入正确密码，n重置为1。)\n2. 有用户反馈，修改密码后马上就忘记了，" +
                    "导致数据全解不开。考虑到确实会出现忘记刚修改的密码的情况，所以特地加了一个”旧密码可用功能“，每次更改密码后，" +
                    "新旧密码都能登录，使用新密码登录成功5次后，再彻底删除旧密码。");
        }
        textTip.setText(tip);
        return firstUse;
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