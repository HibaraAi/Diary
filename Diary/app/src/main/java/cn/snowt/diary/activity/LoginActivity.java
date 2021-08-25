package cn.snowt.diary.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.snowt.diary.R;
import cn.snowt.diary.service.LoginService;
import cn.snowt.diary.service.impl.LoginServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.mine.MineGameActivity;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-14 22:08
 * @Description: 登录界面
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private  Button[] buttons = new Button[12];
    private TextView password;
    private TextView tip;

    private LoginService loginService = new LoginServiceImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bindViewAndSetListener();
        doWhenFirstLogin();
        initKeyboard();
    }

    /**
     * 初始化密码键盘
     */
    private void initKeyboard(){
        Random random = new Random();
         List<Integer> pool = new ArrayList<>();
         pool.add(0);pool.add(1);pool.add(2);pool.add(3);pool.add(4);pool.add(5);
         pool.add(6);pool.add(7);pool.add(8);pool.add(9);
         for(int i=0;i<=9;i++){
             int numIndex = random.nextInt(pool.size());
             buttons[i].setText(pool.get(numIndex)+"");
             pool.remove((Integer)pool.get(numIndex));
         }
    }

    private void bindViewAndSetListener(){
        password = findViewById(R.id.login_text_password);
        tip = findViewById(R.id.login_text_tip);
        buttons[0] = findViewById(R.id.login_btn_num0);
        buttons[1] = findViewById(R.id.login_btn_num1);
        buttons[2] = findViewById(R.id.login_btn_num2);
        buttons[3] = findViewById(R.id.login_btn_num3);
        buttons[4] = findViewById(R.id.login_btn_num4);
        buttons[5] = findViewById(R.id.login_btn_num5);
        buttons[6] = findViewById(R.id.login_btn_num6);
        buttons[7] = findViewById(R.id.login_btn_num7);
        buttons[8] = findViewById(R.id.login_btn_num8);
        buttons[9] = findViewById(R.id.login_btn_num9);
        buttons[10] = findViewById(R.id.login_btn_del);
        buttons[11] = findViewById(R.id.login_btn_login);
        for (Button button : buttons) {
            button.setOnClickListener(this);
        }

        buttons[10].setOnLongClickListener(v -> {
            password.setText("");
            return true;
        });
    }

    @Override
    public void onClick(View v) {
        BaseUtils.createOneShotByVibrator();
        switch (v.getId()){
            case R.id.login_btn_del:{
                String s = password.getText().toString();
                if(!"".equals(s)){
                    s = s.substring(0,s.length()-1);
                    password.setText(s);
                }
                break;
            }
            case R.id.login_btn_login:{
                String pinUserInput = password.getText().toString();
                if("".equals(pinUserInput)){
                    return;
                }
                //游戏跳转
                String gameTag = "123";
                if(gameTag.equals(pinUserInput)){
                    BaseUtils.gotoActivity(this, MineGameActivity.class);
                    this.finish();
                    return;
                }
                password.setText("");
                SimpleResult result = loginService.login(pinUserInput);
                if(result.getSuccess()){
                    BaseUtils.gotoActivity(this,MainActivity.class);
                    this.finish();
                }else{
                    tip.setText(result.getMsg());
                    BaseUtils.shortTipInCoast(this,result.getMsg());
                }
                break;
            }
            default:{
                String s = password.getText().toString();
                password.setText(s+((Button) v).getText().toString());
                break;
            }
        }
    }


    /**
     * 由于第一次使用本程序需要跳转设置登录密码，所以此Activity有逻辑代码
     */
    private void doWhenFirstLogin(){
        SharedPreferences sharedPreferences = BaseUtils.getSharedPreference();
        boolean firstUse = sharedPreferences.getBoolean("firstUse", true);
        if(firstUse){
            //第一次使用本程序
            //创建数据库
            LitePal.getDatabase();
            //跳转设置登录密码界面
            BaseUtils.gotoActivity(this, SetPasswordActivity.class);
        }
    }
}