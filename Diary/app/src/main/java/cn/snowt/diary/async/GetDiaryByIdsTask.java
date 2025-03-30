package cn.snowt.diary.async;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.blog.BlogService;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;

public class GetDiaryByIdsTask extends MyAsyncTask{
    private List<Integer> ids;  //diary的IDs
    private List<Integer> blogIds;  //blog的IDS
    private DiaryService diaryService = new DiaryServiceImpl();
    private BlogService blogService = new BlogService();

    public GetDiaryByIdsTask(Handler handler) {
        super(handler);
    }

    @Override
    void doAsync() {
        if((null==ids  ||  ids.isEmpty()) && (null==blogIds  ||  blogIds.isEmpty())){
            result.setSuccess(false);
            result.setMsg("IDs为空");
        }else{
            List<DiaryVo> diaryVoList = new ArrayList<>();
            if(null!=ids&&!ids.isEmpty()){
                ids.forEach(integer -> {
                    SimpleResult diaryVoById = diaryService.getDiaryVoById(integer);
                    if(diaryVoById.getSuccess()){
                        DiaryVo diaryVo = (DiaryVo) diaryVoById.getData();
                        diaryVoList.add(diaryVo);
                    }
                });
            }
            if(null!=blogIds&&!blogIds.isEmpty()){
                blogIds.forEach(integer -> {
                    DiaryVo diaryVo = blogService.getSimpleBlogAsDiaryVo(integer);
                    if(null!=diaryVo){
                        diaryVoList.add(diaryVo);
                    }
                });
            }
            result.setSuccess(true);
            result.setData(diaryVoList);
        }
    }

    /**
     * 这个只是纯粹的展示Diary，从2025-03-15开始应该弃用，改用两个参数的那个
     * @param ids
     */
    @Deprecated
    public void getDiaryVoByIds(List<Integer> ids){
        result = new SimpleResult();
        this.ids = ids;
        startAsyncTask();
    }

    /**
     * 根据所给DiaryIds和BlogIds，展示数据
     * @param diaryIds diaryIds
     * @param blogIds blogIds
     */
    public void getDiaryVoByIds(List<Integer> diaryIds,List<Integer> blogIds){
        result = new SimpleResult();
        this.ids = diaryIds;
        this.blogIds = blogIds;
        startAsyncTask();
    }
}
