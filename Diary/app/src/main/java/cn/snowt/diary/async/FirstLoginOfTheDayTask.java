package cn.snowt.diary.async;

import android.os.Handler;
import android.util.Log;

import cn.snowt.diary.service.LoginService;
import cn.snowt.diary.service.impl.LoginServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;

public class FirstLoginOfTheDayTask extends MyAsyncTask{
    public FirstLoginOfTheDayTask(Handler handler) {
        super(handler);
    }

    @Override
    void doAsync() {
        result.setSuccess(false);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LoginService loginService = new LoginServiceImpl();
        if (loginService.isFirstLoginInTheDay()) {
            String loginTip = loginService.doFirstLoginOfTheDay();
            if(loginTip.trim().isEmpty()){
                result.setSuccess(false);
            }else{
                result.setData(loginTip.trim());
                result.setSuccess(true);
            }
        }
    }

    public void doFirstLoginOfTheDay(){
        result = new SimpleResult();
        startAsyncTask();
    }
}
