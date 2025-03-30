package cn.snowt.diary.async;

import android.annotation.SuppressLint;
import android.os.Handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.snowt.blog.BlogService;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;

/**
 * 处理时间轴界面的动画
 */
public class GetSimpleDiaryByDateTask extends MyAsyncTask{
    private DiaryService diaryService = new DiaryServiceImpl();
    private BlogService blogService = new BlogService();

    private Date date1;
    private Date date2;

    public GetSimpleDiaryByDateTask(Handler handler) {
        super(handler);
    }

    @Override
    void doAsync() {
        try {
            SimpleResult simpleResult = diaryService.getSimpleDiaryByDate(date1, date2);
            List<DiaryVo> blosList =  blogService.getSimpleBlogAsDiaryVo(date1, date2);
            result.setSuccess(true);
            List<DiaryVo> diaryVoList = (List<DiaryVo>) simpleResult.getData();
            diaryVoList.addAll(blosList);
            diaryVoList.sort((o1, o2) -> {
                if(BaseUtils.stringToDate(o1.getModifiedDate()+" 00:00:00").before(BaseUtils.stringToDate(o2.getModifiedDate()+" 00:00:00"))){
                    return 1;
                }else if (BaseUtils.stringToDate(o1.getModifiedDate()+" 00:00:00").after(BaseUtils.stringToDate(o2.getModifiedDate()+" 00:00:00"))){
                    return -1;
                }else{
                    return 0;
                }
            });
            result.setData(diaryVoList);
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
