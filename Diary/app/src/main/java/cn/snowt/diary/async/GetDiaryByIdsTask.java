package cn.snowt.diary.async;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;

public class GetDiaryByIdsTask extends MyAsyncTask{
    private List<Integer> ids;
    private DiaryService diaryService = new DiaryServiceImpl();

    public GetDiaryByIdsTask(Handler handler) {
        super(handler);
    }

    @Override
    void doAsync() {
        if(null==ids  ||  ids.isEmpty()){
            result.setSuccess(false);
            result.setMsg("IDs为空");
        }else{
            List<DiaryVo> diaryVoList = new ArrayList<>();
            ids.forEach(integer -> {
                SimpleResult diaryVoById = diaryService.getDiaryVoById(integer);
                if(diaryVoById.getSuccess()){
                    DiaryVo diaryVo = (DiaryVo) diaryVoById.getData();
                    diaryVoList.add(diaryVo);
                }
            });
            result.setSuccess(true);
            result.setData(diaryVoList);
        }
    }

    public void getDiaryVoByIds(List<Integer> ids){
        result = new SimpleResult();
        this.ids = ids;
        startAsyncTask();
    }
}
