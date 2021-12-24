package cn.snowt.diary.service.impl;

import android.os.Environment;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.service.DrawingService;
import cn.snowt.diary.service.MyConfigurationService;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.MyConfiguration;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-05 18:15
 * @Description:
 */
public class DrawIngServiceImpl implements DrawingService {

    @Override
    public List<Drawing> getDrawingsByDiaryId(Integer diaryId) {
        return LitePal.where("diaryId = ?",diaryId+"").find(Drawing.class);
    }

    @Override
    public void delUselessPic() {
        //1.扫描本地的所有图片
        File path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"image");

        //2.获取数据库中正在用的图片
        List<String> pathInDb = getAllPicPathInDb();
        //3.删除多余图片
    }

    @Override
    public Map<Integer ,List<Drawing>> getAllPic() {
        Map<Integer ,List<Drawing>> result = new HashMap();
        List<Drawing> allPic = LitePal.findAll(Drawing.class);
        allPic.forEach(p->{
            String imgSrc = p.getImgSrc();
            File file = new File(imgSrc);
            if(file.exists()){
                List<Drawing> tempDrawingList = new ArrayList<>();
                Integer diaryId = p.getDiaryId();
                Set<Integer> keySet = result.keySet();
                if(keySet.contains(diaryId)){
                    List<Drawing> drawings = result.get((Integer) diaryId);
                    drawings.add(p);
                }else{
                    List<Drawing> list = new ArrayList<>();
                    list.add(p);
                    result.put(diaryId,list);
                }
            }
        });
        return result;
    }

    @Override
    public Integer getDiaryIdByPicSre(String imageSrc) {
        Drawing first = LitePal.where("imgSrc like '" + imageSrc+"'").findFirst(Drawing.class);
        if(null==first){
            return -1;
        }else{
            return first.getDiaryId();
        }
    }

    /**
     * 获取数据库中所有存在的图片路径
     * @return
     */
    private List<String> getAllPicPathInDb(){
        List<String> finalList = new ArrayList<>();
        List<Drawing> allPic = LitePal.findAll(Drawing.class);
        allPic.forEach(p->{
            String imgSrc = p.getImgSrc();
            File file = new File(imgSrc);
            if(file.exists()){
                finalList.add(imgSrc);
            }
        });
        return finalList;
    }
}
