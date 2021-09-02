package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
            supportActionBar.setTitle("重置密码");
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
            tip = "\t提示:\n\t第一次使用本软件，请为本软件设置启动密码。\n\t请牢记密码，本软件不支持密码找回!";
            oldPassword.setVisibility(View.GONE);
        }else{
            tip = new String("\t提示:\n\t凭就密码修改启动密码。\n\t请牢记密码，本软件不支持密码找回!");
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