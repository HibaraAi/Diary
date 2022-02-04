package cn.snowt.diary.service.impl;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import cn.snowt.diary.entity.Comment;
import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.entity.Location;
import cn.snowt.diary.entity.SpecialDay;
import cn.snowt.diary.entity.Video;
import cn.snowt.diary.entity.Weather;
import cn.snowt.diary.service.CommentService;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.DrawingService;
import cn.snowt.diary.service.LabelService;
import cn.snowt.diary.service.SpecialDayService;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.FileUtils;
import cn.snowt.diary.util.MD5Utils;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.RSAUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.util.UriUtils;
import cn.snowt.diary.vo.DiaryVo;
import cn.snowt.diary.vo.DiaryVoForBackup;
import cn.snowt.diary.vo.DiaryVoForFunny;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-16 14:57
 * @Description:
 */
public class DiaryServiceImpl implements DiaryService {
    public static final String TAG = "DiaryServiceImpl";

    private final CommentService commentService = new CommentServiceImpl();
    private final LabelService labelService = new LabelServiceImpl();
    private final DrawingService drawingService = new DrawIngServiceImpl();

    @Override
    public List<DiaryVo> getDiaryVoList(int startIndex, int needNum) {
        List<DiaryVo> voList = new ArrayList<>();
        //此方法的查询应该更改为连表查询
        //String sql = "SELECT * FROM diary ORDER BY id LIMIt "+startIndex+","+needNum+"";
        List<Diary> all = LitePal.order("modifiedDate desc").limit(needNum).offset(startIndex).find(Diary.class);
        all.forEach(diary -> {
            DiaryVo vo = new DiaryVo();
            vo.setId(diary.getId());
            if(diary.getEncryption()){
                //此条记录被加密过
                vo.setContent(RSAUtils.decode(diary.getContent(),MyConfiguration.getInstance().getPrivateKey()));
            }else{
                vo.setContent(diary.getContent());
            }
            //vo.setContent(diary.getContent());
            vo.setModifiedDate(BaseUtils.dateToString(diary.getModifiedDate()));
            vo.setLabelStr(diary.getLabel());
            //地址
            if(null!=diary.getLocationId()){
                Location location = LitePal.find(Location.class, diary.getLocationId());
                vo.setLocationStr(location.getLocationString());
            }
            //天气
            if(null!=diary.getWeatherId()){
                Weather weather = LitePal.find(Weather.class, diary.getWeatherId());
                vo.setWeatherStr(weather.getWeather());
            }
            //图片
            List<Drawing> drawingList = LitePal.where("diaryId = ?",diary.getId()+"").find(Drawing.class);
            List<String> picSrcList = new ArrayList<>();
            drawingList.forEach(drawing -> picSrcList.add(drawing.getImgSrc()));
            vo.setPicSrcList(picSrcList);
            //视频
            List<Video> videos = LitePal.where("diaryId = ?", diary.getId() + "").find(Video.class);
            ArrayList<String> videoSrcList = new ArrayList<>();
            videos.forEach(video -> videoSrcList.add(video.getVideoSrc()));
            vo.setVideoSrcList(videoSrcList);
            //评论
            List<Comment> commentList = LitePal.where("diaryId = ?",diary.getId()+"").order("modifiedDate desc").find(Comment.class);
            commentList.forEach(comment ->{
                if (comment.getEncryption()){
                    //此条记录需要解密
                    comment.setContent(RSAUtils.decode(comment.getContent(),MyConfiguration.getInstance().getPrivateKey()));
                }
            });
            vo.setCommentList(commentList);
            if(null==diary.getMyUuid() || "".equals(diary.getMyUuid())){
                vo.setMyUuid("noUuid");
            }else{
                vo.setMyUuid(diary.getMyUuid());
            }
            //引用日记
            if(null!=diary.getQuoteDiaryUuid() && !"".equals(diary.getQuoteDiaryUuid())){
                Diary quoteDiary = LitePal
                        .select("myUuid,content,encryption")
                        .where("myUuid = ?", diary.getQuoteDiaryUuid())
                        .findFirst(Diary.class);
                if(null!=quoteDiary){
                    vo.setQuoteDiaryUuid(quoteDiary.getMyUuid());
                    String showStr;
                    if(quoteDiary.getEncryption()){
                        showStr = RSAUtils.decode(quoteDiary.getContent(),MyConfiguration.getInstance().getPrivateKey());
                    }else{
                        showStr = quoteDiary.getContent();
                    }
                    if(showStr.length()>200){
                        showStr = showStr.substring(0,200);
                        showStr += "\n...\n..\n.";
                    }
                    vo.setQuoteDiaryStr(showStr);
                }else{
                    //引用日记不存在(已删除)，赋予特定值"del"
                    vo.setQuoteDiaryUuid("del");
                }
            }
            voList.add(vo);
        });
        return voList;
    }

    @Override
    public SimpleResult addOneByArgs(String diaryContent, String labelStr, String locationStr, String weatherStr, List<String> tempImgSrcList,Date date,ArrayList<String> tempVideoSrcList,String quoteDiaryUuid) {
        SimpleResult result = new SimpleResult();
        result.setSuccess(true);
        if(date==null){
            date = new Date();
        }
        //先保存天气和位置，因为需要他们的主键要当外键
        Weather weather = new Weather(null,weatherStr,null,null);
        Location location = new Location(null,null,null,locationStr);
        if(!"".equals(weather.getWeather())){
            weather.save();
        }
        if(!"".equals(location.getLocationString())){
            location.save();
        }
        //再存Diary，它的主键要当图片的外键
        Diary diary;
        if(MyConfiguration.getInstance().isRequiredAndAbleToEncode()){
            //开启解密
            diary = new Diary(null,
                    labelStr,
                    RSAUtils.encode(diaryContent,MyConfiguration.getInstance().getPublicKey()),
                    date,
                    weather.getId(),
                    location.getId(),true,quoteDiaryUuid,UUID.randomUUID().toString());
        }else{
            //不需要加密
            diary = new Diary(null,
                    labelStr,
                    diaryContent,
                    date,
                    weather.getId(),
                    location.getId(),false,quoteDiaryUuid,UUID.randomUUID().toString());
        }
        if (diary.save()) {
            //存储图片关系
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String nowMonth = sdf.format(new Date());
            File path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"image/"+nowMonth+"/");
            if(!path.exists()){
                Log.i(TAG,"------创建目录"+path.getAbsolutePath());
                path.mkdirs();
            }
            String absolutePath = path.getAbsolutePath();
            tempImgSrcList.forEach(picSrc->{
                //将缓存图片移动到指定存储目录
//                @SuppressLint("SimpleDateFormat")
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
//                String nowMonth = sdf.format(new Date());
//                File path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"image/"+nowMonth+"/");
//                if(!path.exists()){
//                    Log.i(TAG,"------创建目录"+path.getAbsolutePath());
//                    path.mkdirs();
//                }
//                String absolutePath = path.getAbsolutePath();
                File finalSavePath = new File((absolutePath + "/" + UUID.randomUUID().toString() + ".hibara"));
                try {
                    finalSavePath.createNewFile();
                    UriUtils.copyStream(new FileInputStream(new File(picSrc)),new FileOutputStream(finalSavePath));
                    Log.i(TAG,"------复制图片成功，新图片为:"+finalSavePath.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG,"------保存日记失败,复制图片失败");
                    result.setSuccess(false);
                    result.setMsg("复制图片失败");
                    diary.delete();
                    weather.delete();
                    location.delete();
                    return;
                }
                Drawing drawing = new Drawing(null, finalSavePath.getAbsolutePath(), diary.getId());
                drawing.save();
            });
            File path2 = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"video/"+nowMonth+"/");
            if(!path2.exists()){
                Log.i(TAG,"------创建目录"+path.getAbsolutePath());
                path2.mkdirs();
            }
            String absolutePath2 = path2.getAbsolutePath();
            tempVideoSrcList.forEach(videoSrc->{
                File finalSavePath = new File((absolutePath2 + "/" + UUID.randomUUID().toString() + ".hibara"));
                try {
                    finalSavePath.createNewFile();
                    UriUtils.copyStream(new FileInputStream(new File(videoSrc)),new FileOutputStream(finalSavePath));
                    Log.i(TAG,"------复制视频成功，新视频为:"+finalSavePath.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG,"------保存日记失败,复制视频失败");
                    result.setSuccess(false);
                    result.setMsg("复制视频失败");
                    diary.delete();
                    weather.delete();
                    location.delete();
                    //这里还得加个删视频
                    return;
                }
                Video video = new Video(null, finalSavePath.getAbsolutePath(), diary.getId());
                video.save();
            });
            Log.i(TAG,"------保存日记成功");
        }else{
            Log.e(TAG,"------保存日记失败,Diary数据库存入失败");
            result.setSuccess(false);
            result.setMsg("Diary数据库存入失败");
        }
        return result;
    }

    @Override
    public SimpleResult deleteById(Integer diaryId) {
        Diary diary = LitePal.find(Diary.class, diaryId);
        if(null==diary){
            return SimpleResult.error().msg("不存在该条日记，或许已经被删除了呢");
        }
        //先删除天气、位置信息
        if(null!=diary.getWeatherId()){
            Weather weather = LitePal.find(Weather.class, diary.getWeatherId());
            if(null!=weather){
                weather.delete();
            }
        }
        if(null!=diary.getLocationId()){
            Location location = LitePal.find(Location.class, diary.getLocationId());
            if(null!=location){
                location.delete();
            }
        }
        //删除评论
        commentService.deleteByDiaryId(diaryId);
        //删除配图
        List<Drawing> drawings = LitePal.where("diaryId = ?", diaryId+"").find(Drawing.class);
        if(null!=drawings && drawings.size()>0){
            drawings.forEach(drawing -> {
                new File(drawing.getImgSrc()).delete();
                drawing.delete();
            });
        }
        //删除视频
        List<Video> videos = LitePal.where("diaryId = ?", diaryId + "").find(Video.class);
        if(null!=videos && videos.size()>0){
            videos.forEach(video -> {
                new File(video.getVideoSrc()).delete();
                video.delete();
            });
        }
        diary.delete();
        return SimpleResult.ok();
    }

    @Override
    public SimpleResult getSimpleDiaryByDate(Date date1, Date date2) {
        String date1ToString = BaseUtils.dateToString(date1);
        String date2ToString = BaseUtils.dateToString(date2);
        String s = date1ToString.replaceAll("00:00:00", "00:00:01");
        String s1 = date2ToString.replaceAll("00:00:00", "23:59:59");
        date1 = BaseUtils.stringToDate(s);
        date2 = BaseUtils.stringToDate(s1);
        List<Diary> diaryList = LitePal.select("id,content,modifiedDate,encryption")
                .where("modifiedDate >= ? AND modifiedDate <= ?", date1.getTime()+"", date2.getTime()+"")
                .order("modifiedDate desc")
                .find(Diary.class);
        List<DiaryVo> voList = new ArrayList<>();
        diaryList.forEach(diary -> {
            DiaryVo vo = new DiaryVo();
            vo.setId(diary.getId());
            String subDateStr = BaseUtils.dateToString(diary.getModifiedDate()).substring(0, 10);
            vo.setModifiedDate(subDateStr);
            if(diary.getEncryption()){
                //需要解密
                diary.setContent(RSAUtils.decode(diary.getContent(),MyConfiguration.getInstance().getPrivateKey()));
            }
            String subDiary = (diary.getContent().length()>30)
                    ? (diary.getContent().substring(0,30)+"...")
                    : diary.getContent();
            subDiary = subDiary.replaceAll("\n","");
            vo.setContent(subDiary);
            List<Drawing> drawingList = LitePal.where("diaryId = ?",diary.getId()+"").limit(1).find(Drawing.class);
            List<String> picSrcList = new ArrayList<>();
            drawingList.forEach(drawing -> picSrcList.add(drawing.getImgSrc()));
            vo.setPicSrcList(picSrcList);
            voList.add(vo);
        });
        return SimpleResult.ok().data(voList);
    }

    @Override
    public SimpleResult getDiaryVoById(int diaryId) {
        Diary diary = LitePal.find(Diary.class, diaryId);
        if(null==diary){
            return SimpleResult.error().msg("没有查询到日记详情");
        }
        DiaryVo vo = new DiaryVo();
        vo.setId(diary.getId());
        if(diary.getEncryption()){
            //需要解密
            vo.setContent(RSAUtils.decode(diary.getContent(),MyConfiguration.getInstance().getPrivateKey()));
        }else{
            vo.setContent(diary.getContent());
        }
        vo.setModifiedDate(BaseUtils.dateToString(diary.getModifiedDate()));
        vo.setLabelStr(diary.getLabel());
        //地址
        if(null!=diary.getLocationId()){
            Location location = LitePal.find(Location.class, diary.getLocationId());
            vo.setLocationStr(location.getLocationString());
        }
        //天气
        if(null!=diary.getWeatherId()){
            Weather weather = LitePal.find(Weather.class, diary.getWeatherId());
            vo.setWeatherStr(weather.getWeather());
        }
        //图片
        List<Drawing> drawingList = LitePal.where("diaryId = ?",diary.getId()+"").find(Drawing.class);
        List<String> picSrcList = new ArrayList<>();
        drawingList.forEach(drawing -> picSrcList.add(drawing.getImgSrc()));
        vo.setPicSrcList(picSrcList);
        //视频
        List<Video> videos = LitePal.where("diaryId = ?", diary.getId() + "").find(Video.class);
        ArrayList<String> videoSrcList = new ArrayList<>();
        videos.forEach(video -> videoSrcList.add(video.getVideoSrc()));
        vo.setVideoSrcList(videoSrcList);
        //评论
        List<Comment> commentList = LitePal.where("diaryId = ?",diary.getId()+"").order("modifiedDate desc").find(Comment.class);
        commentList.forEach(comment ->{
            if (comment.getEncryption()){
                comment.setContent(RSAUtils.decode(comment.getContent(),MyConfiguration.getInstance().getPrivateKey()));
            }
        });
        vo.setCommentList(commentList);
        //引用日记
        if(null!=diary.getQuoteDiaryUuid() && !"".equals(diary.getQuoteDiaryUuid())){
            Diary quoteDiary = LitePal
                    .select("myUuid,content,encryption")
                    .where("myUuid = ?", diary.getQuoteDiaryUuid())
                    .findFirst(Diary.class);
            if(null!=quoteDiary){
                vo.setQuoteDiaryUuid(quoteDiary.getMyUuid());
                String showStr;
                if(quoteDiary.getEncryption()){
                    showStr = RSAUtils.decode(quoteDiary.getContent(),MyConfiguration.getInstance().getPrivateKey());
                }else{
                    showStr = quoteDiary.getContent();
                }
                if(showStr.length()>200){
                    showStr = showStr.substring(0,200);
                    showStr += "\n...\n..\n.";
                }
                vo.setQuoteDiaryStr(showStr);
            }else{
                //引用日记不存在(已删除)，赋予特定值"del"
                vo.setQuoteDiaryUuid("del");
            }
        }
        return SimpleResult.ok().data(vo);
    }

    @Override
    public SimpleResult searchDiary(String searchValue) {
        List<Diary> diaryList = LitePal
                .select("id")
                .where("(label like ? OR (encryption = 0 AND content like ?))",("%"+searchValue+"%"),("%"+searchValue+"%"))
                .order("modifiedDate desc")
                .find(Diary.class);
        if(diaryList.isEmpty()){
            return SimpleResult.error().msg("没有符合搜索值为["+searchValue+"]的日记");
        }else{
            List<Integer> idList = new ArrayList<>();
            diaryList.forEach(diary -> idList.add(diary.getId()));
            return SimpleResult.ok().data(idList);
        }
    }

    @Override
    public List<DiaryVo> getSimpleDiaryByIds(ArrayList<Integer> ids) {
        if(ids.isEmpty()){
            return new ArrayList<DiaryVo>();
        }
        long[] idsArray = new long[ids.size()];
        for (int i=0;i<ids.size();i++){
            idsArray[i] = ids.get(i);
        }
        //此处应该SELECT指定列，IN语句限制id范围
        List<Diary> diaryList = LitePal.findAll(Diary.class, idsArray);
        List<DiaryVo> voList = new ArrayList<>();
        diaryList.forEach(diary -> {
            DiaryVo vo = new DiaryVo();
            vo.setId(diary.getId());
            String subDateStr = BaseUtils.dateToString(diary.getModifiedDate()).substring(0, 10);
            vo.setModifiedDate(subDateStr);
            if(diary.getEncryption()){
                //需要解密
                diary.setContent(RSAUtils.decode(diary.getContent(),MyConfiguration.getInstance().getPrivateKey()));
            }
            String subDiary = (diary.getContent().length()>30)
                    ? (diary.getContent().substring(0,30)+"...")
                    : diary.getContent();
            subDiary = subDiary.replaceAll("\n","");
            vo.setContent(subDiary);
            List<Drawing> drawingList = LitePal.where("diaryId = ?",diary.getId()+"").limit(1).find(Drawing.class);
            List<String> picSrcList = new ArrayList<>();
            drawingList.forEach(drawing -> picSrcList.add(drawing.getImgSrc()));
            vo.setPicSrcList(picSrcList);
            voList.add(vo);
        });
        Collections.reverse(voList);
        return voList;
    }

    @Override
    public SimpleResult backupDiary(String publicKey,String pinKey) {
        SimpleResult result = new SimpleResult();
        //List<Diary> diaryList = LitePal.findAll(Diary.class);
        //按时间排序是希望恢复日记时能按照日记顺序恢复，方便操作日记引用
        List<Diary> diaryList = LitePal.order("modifiedDate asc").find(Diary.class);
        if(diaryList.isEmpty()){
            result.setSuccess(false);
            result.setMsg("没有查询到有日记，备份啥呢?");
        }else{
            //1.读取日记
            List<DiaryVoForBackup> vos = new ArrayList<>(diaryList.size());
            diaryList.forEach(diary -> {
                DiaryVoForBackup vo = new DiaryVoForBackup();
                vo.setEncryption(diary.getEncryption());
                vo.setContent(diary.getContent());
                vo.setLabelStr(diary.getLabel());
                vo.setModifiedDate(diary.getModifiedDate());
                if(null!=diary.getLocationId()){
                    vo.setLocation(LitePal.find(Location.class, diary.getLocationId()));
                }
                if(null!=diary.getWeatherId()){
                    vo.setWeather(LitePal.find(Weather.class,diary.getWeatherId()));
                }
                vo.setCommentList(LitePal.where("diaryId = ?",diary.getId()+"").find(Comment.class));
                vo.setDrawingList(LitePal.where("diaryId = ?",diary.getId()+"").find(Drawing.class));
                vo.setVideoList(LitePal.where("diaryId = ?",diary.getId()+"").find(Video.class));
                vo.setQuoteDiaryUuid(diary.getQuoteDiaryUuid());
                vo.setMyUuid(diary.getMyUuid());
                vos.add(vo);
            });
            //读取纪念日
            SpecialDayService specialDayService = new SpecialDayServiceImpl();
            List<SpecialDay> specialDays = specialDayService.getAll();
            //2.封装数据
            Map<String,Object> map = new HashMap<>(11);
            String uuid = UUID.randomUUID().toString();
            map.put(Constant.BACKUP_ARGS_NAME_UUID,uuid);
            map.put(Constant.BACKUP_ARGS_NAME_DATA_NAME,vos);
            map.put(Constant.BACKUP_ARGS_NAME_PUBLIC_KEY,publicKey);
            map.put(Constant.BACKUP_ARGS_NAME_PIN_KEY, MD5Utils.encrypt(Constant.PASSWORD_PREFIX+pinKey));
            map.put(Constant.BACKUP_ARGS_NAME_VERSION,Constant.INTERNAL_VERSION);
            map.put(Constant.BACKUP_ARGS_NAME_ENCODE_UUID,MD5Utils.encrypt(Constant.PASSWORD_PREFIX+uuid));
            map.put("SpecialDay",specialDays);
            String mapJson = JSON.toJSONString(map);
            //3.输出文件
            String fileName = "XiaoXiaoLe_Backup_"+BaseUtils.dateToString(new Date())+".dll";
            boolean saveFlag = FileUtils.saveAsFileWriter(mapJson, fileName);
            if(saveFlag){
                String absolutePath = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION + "output/").getAbsolutePath();
                result.setMsg("备份文件保存至["+absolutePath+"/"+fileName+"],建议马上移动文件到其他文件夹,用完之后应及时删除.");
                result.setSuccess(true);
            }else{
                result.setSuccess(false);
                result.setMsg("备份文件保存失败,请重试");
            }
        }
        return result;
    }

    @Override
    public SimpleResult recoveryDiary(String pinKeyInput, String privateKeyInput, Map<String, Object> map) {
        SimpleResult result = new SimpleResult();
        //版本校验

        //文件可用性校验
        String uuidInFile = (String) map.get(Constant.BACKUP_ARGS_NAME_UUID);
        String encodeUuidInFile = (String) map.get(Constant.BACKUP_ARGS_NAME_ENCODE_UUID);
        if(encodeUuidInFile==null || !encodeUuidInFile.equals(MD5Utils.encrypt(Constant.PASSWORD_PREFIX+uuidInFile))){
            result.setSuccess(false);
            result.setMsg("这个文件已被人为修改过，或者被因为多次输入错误口令而被软件自毁。这个文件已失效，请删除");
        }else{
            //校验口令
            String pinInMap = (String) map.get(Constant.BACKUP_ARGS_NAME_PIN_KEY);
            boolean isBadPin = (pinInMap==null || !pinInMap.equals(MD5Utils.encrypt(Constant.PASSWORD_PREFIX+pinKeyInput)));
            boolean isBadKey = !"".equals(privateKeyInput) && !RSAUtils.testPrivateKey(privateKeyInput);
            if(isBadPin || isBadKey){
                result.setSuccess(false);
                result.setMsg("输入的口令或密钥不正确!");
                Log.i(TAG,"输入的口令或密钥不正确!");
                //这里处理多次口令错误的文件自毁操作
                //破坏UUID的加密值、破坏正确密钥的值、拉黑UUID等
            }else{
                //所有校验通过，开始读取Diary
                List<JSONObject> data = (List<JSONObject>) map.get(Constant.BACKUP_ARGS_NAME_DATA_NAME);
                AtomicBoolean flag = new AtomicBoolean(false);
                AtomicInteger successNum = new AtomicInteger();
                data.forEach(jsonObject -> {
                    DiaryVoForBackup diaryVoForBackup = JSON.toJavaObject(jsonObject, DiaryVoForBackup.class);
                    //uuid已存在数据库了，视为该日记已被存储，不需要恢复
                    if(!diaryUuidAlreadyInDb(diaryVoForBackup.getMyUuid())){
                        if(addOneByBackupVo(diaryVoForBackup,privateKeyInput)){
                            flag.set(true);
                            successNum.getAndIncrement();
                        }
                    }
                });
                List<JSONObject> specialDay = (List<JSONObject>) map.get("SpecialDay");
                AtomicInteger successNum2 = new AtomicInteger();
                if(specialDay!=null && !specialDay.isEmpty()){
                    specialDay.forEach(jsonObject -> {
                        SpecialDay day = JSON.toJavaObject(jsonObject, SpecialDay.class);
                        day.setId(null);
                        day.save();
                        successNum2.getAndIncrement();
                    });
                }
                if(flag.get()){
                    result.setSuccess(true);
                    result.setMsg("从备份文件恢复成功(图片/视频资源你得自己复制，详见帮助)，共写入\n"+successNum+"条日记和\n"+successNum2+"个纪念日");
                    Log.i(TAG,"从备份文件恢复成功，共写入"+successNum+"条日记");
                }else{
                    result.setSuccess(false);
                    result.setMsg("文件读取成功，但数据库存储失败，请重试");
                    Log.i(TAG,"文件读取成功，但数据库存储失败，请重试");
                }
            }
        }
        return result;
    }

    @Override
    public List<DiaryVo> getSimpleDiaryByLabel(String labelStr) {
        //查询同名标签
        List<String> sameLabels = labelService.getSameLabelsByOne(labelStr);
        List<Diary> diaryList = new ArrayList<>();
        sameLabels.forEach(label->{
            diaryList.addAll(
                    LitePal.select("id,content,modifiedDate,encryption")
                        .where("label LIKE ?", "%"+label+"%")
                        .order("modifiedDate desc")
                        .find(Diary.class)
            );
        });
        //多个同名标签，结果集按时间排序
        if(sameLabels.size()>1){
            diaryList.sort((o1, o2) -> {
                if(o1.getModifiedDate().before(o2.getModifiedDate())){
                    return 1;
                }else if (o1.getModifiedDate().after(o2.getModifiedDate())){
                    return -1;
                }else{
                    return 0;
                }
            });
        }
        List<DiaryVo> voList = new ArrayList<>();
        diaryList.forEach(diary -> {
            DiaryVo vo = new DiaryVo();
            vo.setId(diary.getId());
            String subDateStr = BaseUtils.dateToString(diary.getModifiedDate()).substring(0, 10);
            vo.setModifiedDate(subDateStr);
            if(diary.getEncryption()){
                //需要解密
                diary.setContent(RSAUtils.decode(diary.getContent(),MyConfiguration.getInstance().getPrivateKey()));
            }
            String subDiary = (diary.getContent().length()>30)
                    ? (diary.getContent().substring(0,30)+"...")
                    : diary.getContent();
            subDiary = subDiary.replaceAll("\n","");
            vo.setContent(subDiary);
            List<Drawing> drawingList = LitePal.where("diaryId = ?",diary.getId()+"").limit(1).find(Drawing.class);
            List<String> picSrcList = new ArrayList<>();
            drawingList.forEach(drawing -> picSrcList.add(drawing.getImgSrc()));
            vo.setPicSrcList(picSrcList);
            voList.add(vo);
        });
        return voList;
    }

    @Override
    public void addHelpDiary() throws InterruptedException {
        Weather weather1 = new Weather(null, Weather.WEATHER_SUNNY, null, null);
        weather1.save();
        Location location1 = new Location(null,null,null,"广东省河源市");
        location1.save();
        Weather weather2 = new Weather(null, Weather.WEATHER_SUNNY, null, null);
        weather2.save();
        Location location2 = new Location(null,null,null,"广东省河源市");
        location2.save();
        Weather weather3 = new Weather(null, Weather.WEATHER_SUNNY, null, null);
        weather3.save();
        Location location3 = new Location(null,null,null,"广东省河源市");
        location3.save();
        Weather weather4 = new Weather(null, Weather.WEATHER_SUNNY, null, null);
        weather4.save();
        Location location4 = new Location(null,null,null,"广东省河源市");
        location4.save();
        Weather weather5 = new Weather(null, Weather.WEATHER_SUNNY, null, null);
        weather5.save();
        Location location5 = new Location(null,null,null,"广东省河源市");
        location5.save();
        Weather weather6 = new Weather(null, Weather.WEATHER_SUNNY, null, null);
        weather6.save();
        Location location6 = new Location(null,null,null,"广东省河源市");
        location6.save();
        Weather weather7 = new Weather(null, Weather.WEATHER_SUNNY, null, null);
        weather7.save();
        Location location7 = new Location(null,null,null,"广东省河源市");
        location7.save();
        Weather weather8 = new Weather(null, Weather.WEATHER_SUNNY, null, null);
        weather8.save();
        Location location8 = new Location(null,null,null,"广东省河源市");
        location8.save();
        Weather weather9 = new Weather(null, Weather.WEATHER_SUNNY, null, null);
        weather9.save();
        Location location9 = new Location(null,null,null,"广东省河源市");
        location9.save();
        Diary diary1 = new Diary(null,"#初次使用软件指引#",Constant.STRING_ABOUT,new Date(), weather1.getId(),location1.getId(),false,null,UUID.randomUUID().toString());
        Thread.sleep(1000);
        Diary diary2 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_7,new Date(),weather2.getId(),location2.getId(),false,null,UUID.randomUUID().toString());
        Thread.sleep(1000);
        Diary diary3 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_6,new Date(),weather3.getId(),location3.getId(),false,null,UUID.randomUUID().toString());
        Thread.sleep(1000);
        Diary diary4 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_5,new Date(),weather4.getId(),location4.getId(),false,null,UUID.randomUUID().toString());
        Thread.sleep(1000);
        Diary diary5 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_4,new Date(),weather5.getId(),location5.getId(),false,null,UUID.randomUUID().toString());
        Thread.sleep(1000);
        Diary diary6 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_3,new Date(),weather6.getId(),location6.getId(),false,null,UUID.randomUUID().toString());
        Thread.sleep(1000);
        Diary diary7 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_2,new Date(),weather7.getId(),location7.getId(),false,null,UUID.randomUUID().toString());
        Thread.sleep(1000);
        Diary diary8 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_1,new Date(),weather8.getId(),location8.getId(),false,null,UUID.randomUUID().toString());
        Thread.sleep(1000);
        String s = "前言\n" +
//                "    作者很早就想自己做一个日记类的软件了，“正经人谁写日记啊”说是这么说，" +
//                "但我始终认为写日记还是很有必要的，每天一记或者几天一记，记录自己经历过的人和事，" +
//                "如果长期坚持，十年、五十年甚至一生，你的日记就是你整个一生的记录，如果装册成书，" +
//                "我无法想象会有多厚。\n" +
//                "    本来从小学我就有记日记的习惯，当时还是用纸质记录呢，无奈老有人要偷看我的日记，" +
//                "后来我就很长很长一段时间不记录了，而且我现在也找不到那个时候日记本了，属实可惜，" +
//                "现在唯一能找到一本还是高中在校记录的，这个暑假(2021-07)被我无意翻出来了，" +
//                "看到当时记录的发生的所有，除了感到有趣、好笑外，更多的就是感慨时间过的真快。" +
//                "刚好那本日记本有些旧了，重抄一份是不可能的了，所以就激发了我做这个软件的念想。\n" +
//                "    上了大学，有了自己的手机电脑就更没手写过日记了，现在就更不可能手写的了。" +
//                "我想更多的人应该是把朋友圈/QQ/微博当作记录生活吧，但是数据始终的是存储在别人的地方，" +
//                "很多秘密的事情也不可能记录在那些地方，更可恶的是，他们三者都没有数据导出的功能，" +
//                "特别是微信，数以亿计的用户体量，竟然不给用户导出聊天记录、朋友圈等用户自己的数据，" +
//                "真是可笑至极，我们所有的数据都是腾讯垄断的砝码罢了。所以我是坚决不会把日记这种" +
//                "私密信息记录在别人的软件上的。\n" +
//                "    在着手设计这个软件的时候，我也参考了很多同类型的软件，市面上也有很多日记类软件，" +
//                "但它们大多都是有联网的，指不定数据就会被偷偷上传，这是我所顾虑的，所以我做的这款一定" +
//                "不能有网络服务。然后就是记录形式，本来是打算像知乎回答那样的富文本编辑，我就搜索怎么" +
//                "用安卓实现，不看不知道，一看吓一跳。这么复杂的吗？比电脑端复杂太多了吧。我用在这个" +
//                "软件项目的时间不能很多，我要快速开发出来，不学这个。中途我还看到一个博主的吐槽，" +
//                "他说“你越描述越像word，怎么你要自己实现word编辑器吗？你这么折腾干啥呢？" +
//                "是不是word不好用？”哈哈哈哈哈，给我尬住了。他说的好有道理啊。东找西凑后决定使用" +
//                "朋友圈那样的信息流做主界面，整个布局学(chao)习(xi)share的，配色当然要学习BiliBili的" +
//                "猛男色，就这样东学一下西抄一下做出了这个软件。其实Github上也有类似的本地日记记录软件，" +
//                "但它们不是界面我不满意就是功能我不满意，所以自己动手丰衣足食。\n" +
//                "    取名“消消乐”是因为作者做的大部分软件都是AAB式的词语，而且消消乐也蛮有寓意的，" +
//                "不管你开心的不开心的都记下来，消消就乐了。“消消乐”所有数据都是本地存储，支持加密，" +
//                "对你绝对忠诚。你可以把它当树洞，也可以当成一个守口如瓶的、你专属的聆听者，你所有的所有" +
//                "都可以和它分享。\n\n" +
                "    接下来还有8条软件指引。是直接从“帮助&关于”搬过来的。可以看也可以不看。删除完软件指引就开始使用“消消乐吧”";
        Diary diary9 = new Diary(null, "#初次使用软件指引#", s, new Date(), weather9.getId(), location9.getId(), false,null,UUID.randomUUID().toString());
        diary1.save();
        diary2.save();
        diary3.save();
        diary4.save();
        diary5.save();
        diary6.save();
        diary7.save();
        diary8.save();
        diary9.save();
    }

    @Override
    public SimpleResult outputForTxt(String pinInput) {
        //肯定pinInput已不为null和""
        SimpleResult result = new SimpleResult();
        //验证密码
        boolean flag = BaseUtils.getSharedPreference().getString("loginPassword", "").equals(MD5Utils.encrypt(Constant.PASSWORD_PREFIX + pinInput));
        if(flag){
            StringBuilder sb = new StringBuilder();
            List<Diary> ids = LitePal.select("id").order("modifiedDate").find(Diary.class);
            int diaryCount = ids.size();
            for(int i=0;i<diaryCount;i++){
                SimpleResult diaryVo = getDiaryVoById(ids.get(i).getId());
                DiaryVo data = (DiaryVo) diaryVo.getData();
                sb.append("第(").append(i+1).append("/").append(diaryCount).append(")条日记\n");
                sb.append("日记头:").append(data.getModifiedDate()).append("   ")
                        .append(data.getWeatherStr()).append("   ")
                        .append(data.getLocationStr()).append("\n");
                sb.append("标签:").append(data.getLabelStr()).append("\n");
                sb.append("正文:").append(data.getContent()).append("\n");
                if (!data.getCommentList().isEmpty()) {
                    sb.append("评论:\n");
                    data.getCommentList().forEach(comment -> {
                        sb.append("\t")
                                .append(BaseUtils.dateToString(comment.getModifiedDate())).append("  ")
                                .append(comment.getContent()).append("\n");
                    });
                }
                sb.append("\n\n");
            }
            String txtStr = sb.toString();
            //3.输出文件
            String fileName = "导出日记_"+BaseUtils.dateToString(new Date())+".txt";
            boolean saveFlag = FileUtils.saveAsFileWriter2(txtStr, fileName);
            if(saveFlag){
                String absolutePath = LitePalApplication.getContext().getExternalFilesDir("").getAbsolutePath();
                result.setMsg("txt文件导出至["+absolutePath+"/"+fileName+"]\n\n提示：请立马转移该文件，否则应用卸载时将会删除此文件。");
                result.setSuccess(true);
            }else{
                result.setSuccess(false);
                result.setMsg("导出txt失败,请重试");
            }
        }else{
            result.setSuccess(false);
            result.setMsg("登录密码校验失败");
        }
        return result;
    }

    @Override
    public List<DiaryVo> getDiaryVoListAsc(int startIndex, int needNum) {
        List<DiaryVo> voList = new ArrayList<>();
        //此方法的查询应该更改为连表查询
        List<Diary> all = LitePal.order("modifiedDate").limit(needNum).offset(startIndex).find(Diary.class);
        all.forEach(diary -> {
            DiaryVo vo = new DiaryVo();
            vo.setId(diary.getId());
            if(diary.getEncryption()){
                //此条记录被加密过
                vo.setContent(RSAUtils.decode(diary.getContent(),MyConfiguration.getInstance().getPrivateKey()));
            }else{
                vo.setContent(diary.getContent());
            }
            //vo.setContent(diary.getContent());
            vo.setModifiedDate(BaseUtils.dateToString(diary.getModifiedDate()));
            vo.setLabelStr(diary.getLabel());
            //地址
            if(null!=diary.getLocationId()){
                Location location = LitePal.find(Location.class, diary.getLocationId());
                vo.setLocationStr(location.getLocationString());
            }
            //天气
            if(null!=diary.getWeatherId()){
                Weather weather = LitePal.find(Weather.class, diary.getWeatherId());
                vo.setWeatherStr(weather.getWeather());
            }
            //图片
            List<Drawing> drawingList = LitePal.where("diaryId = ?",diary.getId()+"").find(Drawing.class);
            List<String> picSrcList = new ArrayList<>();
            drawingList.forEach(drawing -> picSrcList.add(drawing.getImgSrc()));
            vo.setPicSrcList(picSrcList);
            //视频
            List<Video> videos = LitePal.where("diaryId = ?", diary.getId() + "").find(Video.class);
            ArrayList<String> videoSrcList = new ArrayList<>();
            videos.forEach(video -> videoSrcList.add(video.getVideoSrc()));
            vo.setVideoSrcList(videoSrcList);
            //评论
            List<Comment> commentList = LitePal.where("diaryId = ?",diary.getId()+"").order("modifiedDate desc").find(Comment.class);
            commentList.forEach(comment ->{
                if (comment.getEncryption()){
                    //此条记录需要解密
                    comment.setContent(RSAUtils.decode(comment.getContent(),MyConfiguration.getInstance().getPrivateKey()));
                }
            });
            vo.setCommentList(commentList);
            voList.add(vo);
        });
        return voList;
    }

    @Override
    public Set<String> getAllLabels() {
        Log.w(TAG,"请优化这里。1.这里没有使用distinct 2.标签应该单独成表，而不是现在这样解析");
        Set<String> labelSet = new HashSet<>();
        List<Diary> labels = LitePal.select("label").order("modifiedDate desc").find(Diary.class);
        Set<String> tempSet = new HashSet<>();
        labels.forEach(l->{
            if(null!=l.getLabel() && !"".equals(l.getLabel())){
                tempSet.add(l.getLabel());
            }
        });
        tempSet.forEach(labelStr->{
            final String[] items = labelStr.split("##");
            if(items.length>1){
                items[0] = items[0]+"#";
                items[items.length-1] = "#"+items[items.length-1];
            }
            if(items.length>=3){
                for(int i=1;i<=items.length-2;i++){
                    items[i] = "#"+items[i]+"#";
                }
            }
            labelSet.addAll(Arrays.asList(items));
        });
        return labelSet;
    }

    @Override
    public SimpleResult updateDiaryContentById(Diary diary) {
        //此方法目前仅用于更新日记文本，修改错别字，仅此而已
        SimpleResult result = new SimpleResult();
        Diary diaryInDb = LitePal.find(Diary.class, diary.getId());
        if(null==diaryInDb){
            result.setSuccess(false);
            result.setMsg("没有找到需要更新的日记");
        }else{
            if(MyConfiguration.getInstance().isRequiredAndAbleToEncode()){
                diaryInDb.setContent(RSAUtils.encode(diary.getContent(),MyConfiguration.getInstance().getPublicKey()));
            }else{
                diaryInDb.setContent(diary.getContent());
            }
            int update = diaryInDb.update(diaryInDb.getId());
            if(1==update){
                result.setSuccess(true);
                result.setMsg("更新成功，请刷新");
            }else{
                result.setSuccess(false);
                result.setMsg("数据库更新失败，请重试");
            }
        }
        return result;
    }

    @Override
    public List<DiaryVoForFunny> getDiaryVoForFunny() {
        List<DiaryVoForFunny> voList = new ArrayList<>();
        List<Diary> all = LitePal.order("modifiedDate desc").find(Diary.class);
        all.forEach(diary -> {
            DiaryVoForFunny vo = new DiaryVoForFunny();
            vo.setId(diary.getId());
            if(diary.getEncryption()){
                //此条记录被加密过
                vo.setContent(RSAUtils.decode(diary.getContent(),MyConfiguration.getInstance().getPrivateKey()));
            }else{
                vo.setContent(diary.getContent());
            }
            vo.setModifiedDate(diary.getModifiedDate());
            vo.setLabelList(labelService.parseLabelFromStr(diary.getLabel()));
            //地址
            if(null!=diary.getLocationId()){
                Location location = LitePal.find(Location.class, diary.getLocationId());
                vo.setLocationStr(location.getLocationString());
            }
            //天气
            if(null!=diary.getWeatherId()){
                Weather weather = LitePal.find(Weather.class, diary.getWeatherId());
                vo.setWeatherStr(weather.getWeather());
            }
            vo.setPicNum(drawingService.getDrawingsByDiaryId(vo.getId()).size());
            List<Comment> commentList = commentService.getDecodeCommentByDiaryId(vo.getId());
            List<String> commentStrList = new ArrayList<>();
            commentList.forEach(comment -> commentStrList.add(comment.getContent()));
            vo.setCommentList(commentStrList);
            voList.add(vo);
        });
        return voList;
    }

    @Override
    public List<DiaryVo> getDiaryVoByDate(Date date) {
        List<DiaryVo> vos = new ArrayList<>();
        Date date1;
        Date date2;
        String date1ToString = BaseUtils.dateToString(date);
        String substring = date1ToString.substring(0, 10);
        String s = substring+" 00:00:01";
        String s1 = substring+" 23:59:59";
        date1 = BaseUtils.stringToDate(s);
        date2 = BaseUtils.stringToDate(s1);
        List<Diary> diaryList = LitePal.select("id")
                .where("modifiedDate >= ? AND modifiedDate <= ?", date1.getTime()+"", date2.getTime()+"")
                .order("modifiedDate desc")
                .find(Diary.class);
        diaryList.forEach(diary -> {
            vos.add((DiaryVo) getDiaryVoById(diary.getId()).getData());
        });
        return vos;
    }

    @Override
    public List<DiaryVo> getFormerYear(Date date) {
        List<DiaryVo> voList = new ArrayList<>();
        if(null==date){
            Log.e(TAG,"提供的date为null");
            return voList;
        }
        Calendar calendar = Calendar.getInstance();
        //获取第一条日记的时间
        List<DiaryVo> diaryVoListAsc = getDiaryVoListAsc(0, 1);
        if (diaryVoListAsc.isEmpty()){
            return voList;
        }
        Date firstDate = BaseUtils.stringToDate(diaryVoListAsc.get(0).getModifiedDate());
        calendar.setTime(firstDate);
        calendar.add(Calendar.DAY_OF_MONTH,-1);
        firstDate = calendar.getTime();
        calendar.setTime(date);
        while(date.after(firstDate)){
            voList.addAll(getDiaryVoByDate(date));
            calendar.add(Calendar.YEAR,-1);
            date = calendar.getTime();
        }
        return voList;
    }

    @Override
    public SimpleResult decodeSearch(String searchValue) {
        List<DiaryVo> resultVos = new ArrayList<>();
        List<DiaryVo> diaryVoList = getDiaryVoList(0, Integer.MAX_VALUE);
        diaryVoList.forEach(diaryVo -> {
            if (diaryVo.getContent().contains(searchValue)){
                resultVos.add(diaryVo);
            }
            String subDateStr = diaryVo.getModifiedDate().substring(0, 10);
            diaryVo.setModifiedDate(subDateStr);
            String subDiary = (diaryVo.getContent().length()>30)
                    ? (diaryVo.getContent().substring(0,30)+"...")
                    : diaryVo.getContent();
            subDiary = subDiary.replaceAll("\n","");
            diaryVo.setContent(subDiary);
        });
        if (resultVos.isEmpty()){
            return SimpleResult.error().msg("没有包含关键字为["+searchValue+"]的日记");
        }else{
            return SimpleResult.ok().data(resultVos);
        }
    }

    @Override
    public Date getDateById(Integer id) {
        Diary diary = LitePal.select("modifiedDate").where("id = " + id).findFirst(Diary.class);
        if(diary==null){
            return null;
        }else{
            return diary.getModifiedDate();
        }
    }

    @Override
    public Boolean diaryUuidAlreadyInDb(String uuid) {
        //如果uuid为null或""，视为不存在
        if(null==uuid || "".equals(uuid)){
            return false;
        }
        Diary diary = LitePal
                .select("myUuid")
                .where("myUuid = ?", uuid)
                .findFirst(Diary.class);
        //没有找到该日记，视为不存在,找到了表示已存在
        return null != diary;
    }

    @Override
    public Integer diaryUuidToId(String uuid) {
        if(null==uuid || "".equals(uuid)){
            return -1;
        }
        Diary diary = LitePal
                .select("id")
                .where("myUuid = ?", uuid)
                .findFirst(Diary.class);
        if (null==diary){
            return -1;
        }else{
            return diary.getId();
        }
    }

    /**
     * 从DiaryVoForBackup写入一条日记记录
     * @param privateKey 解密时的密钥
     * @param backupVo DiaryVoForBackup
     * @return 保存成功返回true
     */
    private boolean addOneByBackupVo(DiaryVoForBackup backupVo,String privateKey){
        //LitePal.beginTransaction();
        AtomicBoolean flag = new AtomicBoolean(false);
        String content = backupVo.getContent();
        List<Comment> commentList = backupVo.getCommentList();
        Date modifiedDate = backupVo.getModifiedDate();
        Weather weather = backupVo.getWeather();
        Location location = backupVo.getLocation();
        String labelStr = backupVo.getLabelStr();
        Boolean encryption = backupVo.getEncryption();
        List<Drawing> drawingList = backupVo.getDrawingList();
        List<Video> videoList = backupVo.getVideoList();
        //先保存天气和位置，因为需要他们的主键要当外键
        if(null!=weather && !"".equals(weather.getWeather())){
            weather.save();
        }else{
            weather = new Weather(null,null,null,null);
        }
        if(null!=location && !"".equals(location.getLocationString())){
            location.save();
        }else{
            location = new Location(null,null,null,null);
        }
        //再存Diary，它的主键要当图片的外键
        Diary diary = new Diary();
        String newContentStr;
        if(encryption){
            //此条记录被加密过，需要解密后再次加密
            String decode = RSAUtils.decode(content, privateKey);
            if(MyConfiguration.getInstance().isRequiredAndAbleToEncode()){
                newContentStr = RSAUtils.encode(decode,MyConfiguration.getInstance().getPublicKey());
            }else{
                newContentStr = decode;
            }
        }else{
            //没有加密过
            if(MyConfiguration.getInstance().isRequiredAndAbleToEncode()){
                newContentStr = RSAUtils.encode(content,MyConfiguration.getInstance().getPublicKey());
            }else{
                newContentStr = content;
            }
        }
        if(null==backupVo.getMyUuid() || "".equals(backupVo.getMyUuid())){
            diary.setMyUuid(UUID.randomUUID().toString());
        }else{
            diary.setMyUuid(backupVo.getMyUuid());
        }
        diary.setQuoteDiaryUuid(backupVo.getQuoteDiaryUuid());
        diary.setEncryption(MyConfiguration.getInstance().isRequiredAndAbleToEncode());
        diary.setModifiedDate(modifiedDate);
        diary.setContent(newContentStr);
        diary.setLabel(labelStr);
        diary.setLocationId(location.getId());
        diary.setWeatherId(weather.getId());
        if (diary.save()) {
            //存储图片关系
            if(null!=drawingList && drawingList.size()!=0){
                drawingList.forEach(picSrc->{
                    Drawing drawing = new Drawing(null, picSrc.getImgSrc(), diary.getId());
                    if (!drawing.save()) {
                        flag.set(false);
                    }
                });
            }
            //存储视频
            if(null!=videoList && videoList.size()!=0){
                videoList.forEach(videoSrc->{
                    Video video = new Video(null, videoSrc.getVideoSrc(), diary.getId());
                    if (!video.save()) {
                        flag.set(false);
                    }
                });
            }
            //存储评论
            if(commentList!=null && commentList.size()!=0){
                commentList.forEach(comment -> {
                    if(comment.getEncryption()){
                        //加密过的评论，需要解密再存储
                        comment.setContent(RSAUtils.decode(comment.getContent(),privateKey));
                    }
                    if(MyConfiguration.getInstance().isRequiredAndAbleToEncode()){
                        comment.setContent(RSAUtils.encode(comment.getContent(),MyConfiguration.getInstance().getPublicKey()));
                        comment.setEncryption(true);
                    }else{
                        comment.setEncryption(false);
                    }
                    comment.setId(null);
                    comment.setDiaryId(diary.getId());
                    if (!comment.save()) {
                        flag.set(false);
                    }
                });
            }
            flag.set(true);
        }else{
            flag.set(false);
        }
//        if(flag.get()){
//            LitePal.setTransactionSuccessful();
//        }else{
//            LitePal.endTransaction();
//        }
        if(!flag.get()){
            //事务回滚？
            if(null!=diary.getId()){
                deleteById(diary.getId());
            }
        }
        return flag.get();
    }
}
