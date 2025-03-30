package cn.snowt.diary.async;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.SimpleResult;

public class SaveDiaryTask extends MyAsyncTask{
    private String diaryInputStr;
    private String labelStr;
    private String  locationStr;
    private String weatherStr;
    private List<String> imageTempSrcList;
    private Date date;
    private ArrayList<String> videoTempSrcList;
    private String quoteDiaryId;
    public SaveDiaryTask(Handler handler) {
        super(handler);
    }

    @Override
    void doAsync() {
        DiaryService diaryService = new DiaryServiceImpl();
        result = diaryService.addOneByArgs(diaryInputStr,
                labelStr,
                locationStr,
                weatherStr,
                imageTempSrcList,date,videoTempSrcList,quoteDiaryId);
    }

    public void asyncSaveDiary(String diaryInputStr,
                               String labelStr,
                               String  locationStr,
                               String weatherStr,
                               List<String> imageTempSrcList,
                               Date date,
                               ArrayList<String> videoTempSrcList,
                               String quoteDiaryId){
        result = new SimpleResult();
        this.diaryInputStr = diaryInputStr;
        this.labelStr= labelStr;
        this.locationStr = locationStr;
        this.weatherStr = weatherStr;
        this.imageTempSrcList = imageTempSrcList;
        this.date = date;
        this.videoTempSrcList = videoTempSrcList;
        this.quoteDiaryId = quoteDiaryId;
        startAsyncTask();
    }
}
