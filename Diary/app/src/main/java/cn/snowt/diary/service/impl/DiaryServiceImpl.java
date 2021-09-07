package cn.snowt.diary.service.impl;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cn.snowt.diary.entity.Comment;
import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.entity.Location;
import cn.snowt.diary.entity.Weather;
import cn.snowt.diary.service.CommentService;
import cn.snowt.diary.service.DiaryService;
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

/**
 * @Author: HibaraAi
 * @Date: 2021-08-16 14:57
 * @Description:
 */
public class DiaryServiceImpl implements DiaryService {
    public static final String TAG = "DiaryServiceImpl";

    private final CommentService commentService = new CommentServiceImpl();

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
    public SimpleResult addOneByArgs(String diaryContent, String labelStr, String locationStr, String weatherStr, List<String> tempImgSrcList,Date date) {
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
                    location.getId(),true);
        }else{
            //不需要加密
            diary = new Diary(null,
                    labelStr,
                    diaryContent,
                    date,
                    weather.getId(),
                    location.getId(),false);
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
        //评论
        List<Comment> commentList = LitePal.where("diaryId = ?",diary.getId()+"").order("modifiedDate desc").find(Comment.class);
        commentList.forEach(comment ->{
            if (comment.getEncryption()){
                comment.setContent(RSAUtils.decode(comment.getContent(),MyConfiguration.getInstance().getPrivateKey()));
            }
        });
        vo.setCommentList(commentList);
        return SimpleResult.ok().data(vo);
    }

    @Override
    public SimpleResult searchDiary(String searchValue) {
        List<Diary> diaryList = LitePal.select("id").where("(label like ? OR (encryption = 0 AND content like ?))",("%"+searchValue+"%"),("%"+searchValue+"%")).find(Diary.class);
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
    public SimpleResult backupDiary(String privateKey, String publicKey,String pinKey) {
        SimpleResult result = new SimpleResult();
        List<Diary> diaryList = LitePal.findAll(Diary.class);
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
                vos.add(vo);
            });
            //2.封装数据
            Map<String,Object> map = new HashMap<>(11);
            String uuid = UUID.randomUUID().toString();
            map.put(Constant.BACKUP_ARGS_NAME_UUID,uuid);
            map.put(Constant.BACKUP_ARGS_NAME_DATA_NAME,vos);
            map.put(Constant.BACKUP_ARGS_NAME_USELESS_ARGS3,privateKey.replaceAll("w","g"));
            map.put(Constant.BACKUP_ARGS_NAME_PUBLIC_KEY,publicKey);
            map.put(Constant.BACKUP_ARGS_NAME_PIN_KEY, MD5Utils.encrypt(Constant.PASSWORD_PREFIX+pinKey));
            map.put(Constant.BACKUP_ARGS_NAME_VERSION,Constant.INTERNAL_VERSION);
            map.put(Constant.BACKUP_ARGS_NAME_USELESS_ARGS1,privateKey.replaceAll("s","a"));
            map.put(Constant.BACKUP_ARGS_NAME_PRIVATE_KEY,privateKey);
            map.put(Constant.BACKUP_ARGS_NAME_USELESS_ARGS2,publicKey.replaceAll("s","r"));
            map.put(Constant.BACKUP_ARGS_NAME_USELESS_ARGS4,publicKey.replaceAll("h","q"));
            map.put(Constant.BACKUP_ARGS_NAME_ENCODE_UUID,MD5Utils.encrypt(Constant.PASSWORD_PREFIX+uuid));
            String mapJson = JSON.toJSONString(map);
            //3.输出文件
            String fileName = "XiaoXiaoLe_Backup_"+BaseUtils.dateToString(new Date())+".dll";
            boolean saveFlag = FileUtils.saveAsFileWriter(mapJson, fileName);
            if(saveFlag){
                String absolutePath = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION + "output/").getAbsolutePath();
                result.setMsg("备份文件保存至["+absolutePath+fileName+"],建议马上移动文件到其他文件夹,用完之后应及时删除.");
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
                //这里处理多次口令错误的文件自毁操作
                //破坏UUID的加密值、破坏正确密钥的值、拉黑UUID等
            }else{
                //所有校验通过，开始读取Diary
                List<JSONObject> data = (List<JSONObject>) map.get(Constant.BACKUP_ARGS_NAME_DATA_NAME);
                AtomicBoolean flag = new AtomicBoolean(false);
                AtomicInteger successNum = new AtomicInteger();
                data.forEach(jsonObject -> {
                    DiaryVoForBackup diaryVoForBackup = JSON.toJavaObject(jsonObject, DiaryVoForBackup.class);
                    if(addOneByBackupVo(diaryVoForBackup,privateKeyInput)){
                        flag.set(true);
                        successNum.getAndIncrement();
                    }
                });
                if(flag.get()){
                    result.setSuccess(true);
                    result.setMsg("从备份文件恢复成功，共写入"+successNum+"条日记");
                }else{
                    result.setSuccess(false);
                    result.setMsg("文件读取成功，但数据库存储失败，请重试");
                }
            }
        }
        return result;
    }

    @Override
    public List<DiaryVo> getSimpleDiaryByLabel(String labelStr) {
        List<Diary> diaryList = LitePal.select("id,content,modifiedDate,encryption")
                .where("label LIKE ?", "%"+labelStr+"%")
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
    public void addHelpDiary() {
        Weather weather = new Weather(null, Weather.WEATHER_SUNNY, null, null);
        weather.save();
        Location location = new Location(null,null,null,"广东省河源市");
        location.save();
        Diary diary1 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_1,new Date(), weather.getId(),location.getId(),false);
        Diary diary2 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_2,new Date(),weather.getId(),location.getId(),false);
        Diary diary3 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_3,new Date(),weather.getId(),location.getId(),false);
        Diary diary4 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_4,new Date(),weather.getId(),location.getId(),false);
        Diary diary5 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_5,new Date(),weather.getId(),location.getId(),false);
        Diary diary6 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_6,new Date(),weather.getId(),location.getId(),false);
        Diary diary7 = new Diary(null,"#初次使用软件指引#",Constant.STRING_HELP_7,new Date(),weather.getId(),location.getId(),false);
        Diary diary8 = new Diary(null,"#初次使用软件指引#",Constant.STRING_ABOUT,new Date(),weather.getId(),location.getId(),false);
        Diary diary9 = new Diary(null, "#初次使用软件指引#", "看完并删除软件指引就开始使用“消消乐吧”", new Date(), weather.getId(), location.getId(), false);
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
