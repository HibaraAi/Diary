package cn.snowt.diary.async;

import android.annotation.SuppressLint;
import android.os.Handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.SimpleResult;

/**
 * 处理时间轴界面的动画
 */
public class GetSimpleDiaryByDateTask extends MyAsyncTask{
    private DiaryService diaryService = new DiaryServiceImpl();

    private Date date1;
    private Date date2;

    public GetSimpleDiaryByDateTask(Handler handler) {
        super(handler);
    }

    @Override
    void doAsync() {
        try {
            SimpleResult simpleResult = diaryService.getSimpleDiaryByDate(date1, date2);
            result.setSuccess(true);
            result.setData(simpleResult.getData());
        }catch (Exception e){
            result.setSuccess(false);
        }
    }

    public void getSimpleDiaryByDate(String dateOneStr, String  dateTwoStr){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = dateFormat.parse(dateOneStr);
            date2 = dateFormat.parse(dateTwoStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //date1在前
        assert date2 != null;
        if(date2.before(date1)){
            Date temp = date1;
            date1 = date2;
            date2 = temp;
        }
        this.date1 = date1;
        this.date2 = date2;
        result = new SimpleResult();
        result.setMsg(dateOneStr+"#"+dateTwoStr);
        startAsyncTask();
    }
}
