package cn.snowt.blog;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.service.LabelService;
import cn.snowt.diary.service.impl.LabelServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.FileUtils;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.RSAUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.util.UriUtils;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author : HibaraAi github.com/HibaraAi
 * @Date : on 2025-02-15 11:26.
 * @Description : Blog的操作逻辑、数据库操作
 */
public class BlogService {
    public static final String TAG = "BlogService";

    private final LabelService labelService = new LabelServiceImpl();

    /**
     * 更新操作
     * 目前是将原来的删掉，再重新添加一个新的Blog（包括了磁盘文件的重新读写）
     * @param needEditId needEditId
     * @param dto dto
     * @return
     */
    public SimpleResult updateById(Integer needEditId, BlogDto dto) {
        //根据id读取Blog
        Blog blog = LitePal.find(Blog.class, needEditId);
        if(null!=blog){
            //因为维护“新增或者删除”的媒体资源有点麻烦
            //所以这里的修改，直接当作新增一个Blog，然后删除旧的Blog
            SimpleResult result = addOne(dto);
            if(result.getSuccess()){
                Blog newBlog = (Blog) result.getData();
                //将旧的uuid、Blog时间同步过来
                newBlog.setMyUuid(blog.getMyUuid());
                newBlog.setModifiedDate(blog.getModifiedDate());
                newBlog.save();
                //删除旧的blog
                deleteById(needEditId);
                return SimpleResult.ok();
            }else{
                return SimpleResult.error().msg("修改ID为"+needEditId+"的Blog时失败");
            }
        }else{
            return SimpleResult.error().msg("没有找到ID为"+needEditId+"的Blog");
        }
    }

    /**
     * 新增一个Blog
     * @param dto dto
     * @return SimpleResult，成功添加后，将添加后的Blog存入SimpleResult.data
     */
    public SimpleResult addOne(BlogDto dto) {
        SimpleResult result = new SimpleResult();
        result.setSuccess(true);
        Blog blog = new Blog();
        if(MyConfiguration.getInstance().isRequiredAndAbleToEncode()){
            blog.setContent(RSAUtils.encode(dto.getContent(),MyConfiguration.getInstance().getPublicKey()));
            blog.setEncryption(true);
        }else{
            blog.setContent(dto.getContent());
            blog.setEncryption(false);
        }
        blog.setModifiedDate(dto.getModifiedDate());
        blog.setMyUuid(UUID.randomUUID().toString());
        blog.setTitle(dto.getTitle());
        blog.setLabel(dto.getLabelStr());
        boolean save = blog.save();
        if(save){
            //因为保存Blog及媒体资源后之后，需要修改正文中媒体资源，所以用tempContent把原始文本存起来
            AtomicReference<String> tempContent = new AtomicReference<>(new String(dto.getContent()));
            //存储图片关系
            List<String> imgSrcList = dto.getImgSrcList();
            if (null!=imgSrcList && imgSrcList.size()!=0){
                //创建图片存储目录
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String nowMonth = sdf.format(new Date());
                File path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"image/"+nowMonth+"/");
                if(!path.exists()){
                    Log.i(TAG,"------创建目录"+path.getAbsolutePath());
                    path.mkdirs();
                }
                String absolutePath = path.getAbsolutePath();
                //存储图片
                imgSrcList.forEach(picSrc -> {
                    //将缓存图片移动到指定存储目录
                    File finalSavePath = new File((absolutePath + "/" + UUID.randomUUID().toString() + ".hibara"));
                    try {
                        finalSavePath.createNewFile();
                        UriUtils.copyStream(new FileInputStream(new File(picSrc)),new FileOutputStream(finalSavePath));
                        Log.i(TAG,"------复制图片成功，新图片为:"+finalSavePath.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,"------保存Blog失败,复制图片失败");
                        result.setSuccess(false);
                        result.setMsg("复制图片失败");
                        blog.delete();
                        //这里还得加个图片
                        return;
                    }
                    BlogMedia blogMedia = new BlogMedia();
                    blogMedia.setBlogId(blog.getId());
                    blogMedia.setMediaSrc(finalSavePath.getAbsolutePath());
                    blogMedia.setMediaType(BlogMedia.TYPE_IMAGE);
                    boolean save1 = blogMedia.save();
                    if(save1){
                        tempContent.set(tempContent.get().replace(picSrc,blogMedia.getMediaSrc()));
                    }
                });
            }
            //存储视频关系
            List<String> videoSrcList = dto.getVideoSrcList();
            if(null!=videoSrcList & videoSrcList.size()!=0){
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String nowMonth = sdf.format(new Date());
                File path2 = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"video/"+nowMonth+"/");
                if(!path2.exists()){
                    Log.i(TAG,"------创建目录"+path2.getAbsolutePath());
                    path2.mkdirs();
                }
                String absolutePath2 = path2.getAbsolutePath();
                videoSrcList.forEach(videoSrc->{
                    File finalSavePath = new File((absolutePath2 + "/" + UUID.randomUUID().toString() + ".hibara"));
                    try {
                        finalSavePath.createNewFile();
                        UriUtils.copyStream(new FileInputStream(new File(videoSrc)),new FileOutputStream(finalSavePath));
                        Log.i(TAG,"------复制视频成功，新视频为:"+finalSavePath.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,"------保存Blog失败,复制视频失败");
                        result.setSuccess(false);
                        result.setMsg("复制视频失败");
                        blog.delete();
                        //这里还得加个删视频、图片
                        return;
                    }
                    BlogMedia blogMedia = new BlogMedia();
                    blogMedia.setBlogId(blog.getId());
                    blogMedia.setMediaSrc(finalSavePath.getAbsolutePath());
                    blogMedia.setMediaType(BlogMedia.TYPE_VIDEO);
                    boolean save1 = blogMedia.save();
                    if(save1){
                        tempContent.set(tempContent.get().replace(videoSrc,blogMedia.getMediaSrc()));
                    }
                });
            }
            //存了外存，需要将更新后的富文本保存(更新了媒体资源的存储地址)
            if(MyConfiguration.getInstance().isRequiredAndAbleToEncode()){
                blog.setContent(RSAUtils.encode(tempContent.get(),MyConfiguration.getInstance().getPublicKey()));
                blog.setEncryption(true);
            }else{
                blog.setContent(tempContent.get());
                blog.setEncryption(false);
            }
            blog.update(blog.getId());
            result.setData(blog);
        }else{
            Log.e(TAG,"------保存Blog失败,Blog数据库存入失败");
            result.setSuccess(false);
            result.setMsg("Blog数据库存入失败");
        }
        return result;
    }

    /**
     * 获取所有已存储的Blog
     * @return List&lt;BlogSimpleVo&gt;
     */
    public List<BlogSimpleVo> getAllBlogs() {
        //读库
        List<Blog> Blogs = LitePal.findAll(Blog.class);
        List<BlogSimpleVo> voList = new ArrayList<>(Blogs.size());
        //转成VO
        Blogs.forEach(blog -> {
            BlogSimpleVo vo = new BlogSimpleVo();
            vo.setId(blog.getId());
            vo.setTitle(blog.getTitle());
            if(blog.getEncryption()){
                vo.setSimpleContent(RSAUtils.decodePartial(blog.getContent(),MyConfiguration.getInstance().getPrivateKey(),2));
            }else{
                vo.setSimpleContent(blog.getContent().length()>60?blog.getContent().substring(0,57)+"...":blog.getContent());
            }
            vo.setMediaSrc(getCover(blog.getId()));
            vo.setDate(BaseUtils.dateToString(blog.getModifiedDate()));
            vo.setLabelStr(blog.getLabel());
            voList.add(vo);
        });
        return voList;
    }

    /**
     * 获取第一张图片作为封面
     * @param id BlogID
     * @return 没有就返回null
     */
    public String getCover(Integer id){
        //尝试读取一张图片
        List<BlogMedia> blogMedia = LitePal.select().where("blogId = " + id + " AND mediaType = "+BlogMedia.TYPE_IMAGE).limit(1).find(BlogMedia.class);
        if(!blogMedia.isEmpty()){
            return blogMedia.get(0).getMediaSrc();
        }else{
            List<BlogMedia> blogMedia2 = LitePal.select().where("blogId = " + id + " AND mediaType = "+BlogMedia.TYPE_VIDEO).limit(1).find(BlogMedia.class);
            if(!blogMedia2.isEmpty()){
                return blogMedia2.get(0).getMediaSrc();
            }else
                return null;
        }
    }

    /**
     * 根据ID获取具体的Blog（已完整解密）
     * @param blogId blogId
     * @return SimpleResult
     */
    public SimpleResult getBlogVoById(int blogId) {
        Blog blog = LitePal.find(Blog.class, blogId);
        if(null==blog){  //没根据id找到
            return SimpleResult.error().msg("没有找到ID为"+blogId+"的Blog");
        }else{
            BlogVo vo = new BlogVo();
            vo.setId(blog.getId());
            vo.setTitle(blog.getTitle());
            //解析正文中的资源地址
            if(blog.getEncryption()){
                vo.setContent(RSAUtils.decode(blog.getContent(),MyConfiguration.getInstance().getPrivateKey()));
            }else{
                vo.setContent(blog.getContent());
            }
            vo.setDate(BaseUtils.dateToString(blog.getModifiedDate()));
            //解析标签
            vo.setLabelStr(blog.getLabel());
            //PicList
            SimpleResult result = getImgSrcListByBlogId(blogId);
            if(result.getSuccess()){
                vo.setImgSrc((List<String>) result.getData());
            }else{
                vo.setImgSrc(new ArrayList<>());
            }
            //videoList
            SimpleResult result2 = geVideoSrcList(blogId);
            if(result2.getSuccess()){
                vo.setVideoSrc((List<String>) result2.getData());
            }else{
                vo.setVideoSrc(new ArrayList<>());
            }
            return SimpleResult.ok().data(vo);
        }
    }


    /**
     * 根据ID删除一个Blog，级联删除媒体资源
     * @param blogId blogId
     * @return SimpleResult
     */
    public SimpleResult deleteById(int blogId) {
        Blog blog = LitePal.find(Blog.class, blogId);
        if(null!=blog){
            deleteBlogMediaByBlogId(blog.getId());
            blog.delete();
            return SimpleResult.ok();
        }else{
            return SimpleResult.error().msg("没有找到id为"+blogId+"的Blog");
        }
    }

    /**
     * 根据blogId删除media
     * @param id blogId
     */
    private void deleteBlogMediaByBlogId(Integer id) {
        List<BlogMedia> blogMedia = LitePal.select().where("blogId = " + id).find(BlogMedia.class);
        blogMedia.forEach(blogMedia1 -> {
            FileUtils.safeDeleteFolder(blogMedia1.getMediaSrc());
            blogMedia1.delete();
        });
    }

    /**
     * 根据blogId获取媒体资源地址（图片）
     * @param blogId blogId
     * @return SimpleResult
     */
    private SimpleResult getImgSrcListByBlogId(int blogId) {
        List<BlogMedia> blogMedia1 = LitePal.select().where("blogId = " + blogId + " AND mediaType = " + BlogMedia.TYPE_IMAGE).find(BlogMedia.class);
        if(blogMedia1.isEmpty()){
            return SimpleResult.error().msg("没有图片");
        }else{
            List<String> srcList = new ArrayList<>(blogMedia1.size());
            blogMedia1.forEach(picture -> {
                srcList.add(picture.getMediaSrc());
            });
            return SimpleResult.ok().data(srcList);
        }
    }

    /**
     * 根据blogId获取媒体资源地址（视频）
     * @param blogId
     * @return
     */
    private SimpleResult geVideoSrcList(int blogId) {
        List<BlogMedia> blogMedia1 = LitePal.select().where("blogId = " + blogId + " AND mediaType = " + BlogMedia.TYPE_VIDEO).find(BlogMedia.class);
        if(blogMedia1.isEmpty()){
            return SimpleResult.error().msg("没有视频");
        }else{
            List<String> srcList = new ArrayList<>(blogMedia1.size());
            blogMedia1.forEach(video -> {
                srcList.add(video.getMediaSrc());
            });
            return SimpleResult.ok().data(srcList);
        }
    }

    /**
     * 根据标签值获取简易的BlogList
     * @param labelStr 标签值
     * @return SimpleResult
     */
    public List<DiaryVo> getSimpleBlogAsDiaryVo(String labelStr){
        //查询同名标签
        List<String> sameLabels = labelService.getSameLabelsByOne(labelStr);
        List<Blog> blogList = new ArrayList<>();
        sameLabels.forEach(label->{
            blogList.addAll(
                    LitePal.where("label LIKE ?", "%"+label+"%")
                            .order("modifiedDate desc")
                            .find(Blog.class)
            );
        });
        //多个同名标签，结果集按时间排序
        if(sameLabels.size()>1){
            blogList.sort((o1, o2) -> {
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
        blogList.forEach(blog -> {
            DiaryVo vo = new DiaryVo();
            vo.setQuoteDiaryUuid(DiaryVo.BLOG_FLAG);
            vo.setId(blog.getId());
            String subDateStr = BaseUtils.dateToString(blog.getModifiedDate()).substring(0, 10);
            vo.setModifiedDate(subDateStr);
            if(blog.getEncryption()){
                //需要解密
                blog.setContent(RSAUtils.decodePartial(blog.getContent(),MyConfiguration.getInstance().getPrivateKey(),1));
            }
            String subDiary = (blog.getContent().length()>30)
                    ? (blog.getContent().substring(0,30)+"...")
                    : blog.getContent();
            subDiary = subDiary.replaceAll("\n","");
            vo.setContent("[这是一条Blog]  "+blog.getTitle()+"\n"+subDiary);
            String cover = getCover(blog.getId());
            List<String> picSrcList = new ArrayList<>();
            picSrcList.add(cover);
            vo.setPicSrcList(picSrcList);
            voList.add(vo);
        });
        return voList;
    }

    /**
     * 根据时间范围获取简易的BlogList
     * @param date1 date1
     * @param date2 date2
     * @return
     */
    public List<DiaryVo> getSimpleBlogAsDiaryVo(Date date1, Date date2) {
        String date1ToString = BaseUtils.dateToString(date1);
        String date2ToString = BaseUtils.dateToString(date2);
        String s = date1ToString.replaceAll("00:00:00", "00:00:01");
        String s1 = date2ToString.replaceAll("00:00:00", "23:59:59");
        date1 = BaseUtils.stringToDate(s);
        date2 = BaseUtils.stringToDate(s1);
        List<Blog> blogList = LitePal.where("modifiedDate >= ? AND modifiedDate <= ?", date1.getTime()+"", date2.getTime()+"")
                .order("modifiedDate desc")
                .find(Blog.class);
        List<DiaryVo> voList = new ArrayList<>();
        blogList.forEach(blog -> {
            DiaryVo vo = new DiaryVo();
            vo.setQuoteDiaryUuid(DiaryVo.BLOG_FLAG);
            vo.setId(blog.getId());
            String subDateStr = BaseUtils.dateToString(blog.getModifiedDate()).substring(0, 10);
            vo.setModifiedDate(subDateStr);
            if(blog.getEncryption()){
                //需要解密
                blog.setContent(RSAUtils.decodePartial(blog.getContent(),MyConfiguration.getInstance().getPrivateKey(),1));
            }
            String subDiary = (blog.getContent().length()>30)
                    ? (blog.getContent().substring(0,30)+"...")
                    : blog.getContent();
            subDiary = subDiary.replaceAll("\n","");
            vo.setContent("[这是一条Blog]  "+blog.getTitle()+"\n"+subDiary);
            String cover = getCover(blog.getId());
            List<String> picSrcList = new ArrayList<>();
            picSrcList.add(cover);
            vo.setPicSrcList(picSrcList);
            voList.add(vo);
        });
        return voList;
    }

    /**
     * 根据ID获取简易的Blog，但其实比其他两个会完整一些
     * @param blogId blogId
     * @return 查找成功就返回DiaryVo，如果失败就返回null
     */
    public DiaryVo getSimpleBlogAsDiaryVo(Integer blogId) {
        Blog blog = LitePal.find(Blog.class, blogId);
        if(null!=blog){
            DiaryVo vo = new DiaryVo();
            vo.setQuoteDiaryUuid(DiaryVo.BLOG_FLAG);
            vo.setId(blog.getId());
            vo.setModifiedDate(BaseUtils.dateToString(blog.getModifiedDate()));
            if(blog.getEncryption()){
                //需要解密
                blog.setContent(RSAUtils.decodePartial(blog.getContent(),MyConfiguration.getInstance().getPrivateKey(),1));
            }
            String subDiary = (blog.getContent().length()>30)
                    ? (blog.getContent().substring(0,30)+"...")
                    : blog.getContent();
            subDiary = subDiary.replaceAll("\n","");
            vo.setContent("[这是一条Blog]  "+blog.getTitle()+"\n"+subDiary);
            String cover = getCover(blog.getId());
            List<String> picSrcList = new ArrayList<>();
            picSrcList.add(cover);
            vo.setPicSrcList(picSrcList);
            vo.setVideoSrcList(new ArrayList<>());
            vo.setCommentList(new ArrayList<>());
            vo.setLabelStr(blog.getLabel());
            return vo;
        }else{
            return null;
        }
    }

    /**
     * 获取Blog中所有的图片资源
     * @return
     */
    public Map<Integer,List<String>> getAllBlogPic(){
        Map<Integer ,List<String>> resultMap = new HashMap();
        List<BlogMedia> allPic = LitePal.where( "mediaType = " + BlogMedia.TYPE_IMAGE).find(BlogMedia.class);
        allPic.forEach(blogMedia -> {
            String imgSrc =blogMedia.getMediaSrc();
            File file = new File(imgSrc);
            if(file.exists()){
                List<String> tempDrawingList = new ArrayList<>();
                Integer blogId = blogMedia.getBlogId();
                Set<Integer> keySet = resultMap.keySet();
                if(keySet.contains(blogId)){
                    List<String> drawings = resultMap.get((Integer) blogId);
                    assert drawings != null;
                    drawings.add(imgSrc);
                }else{
                    List<String> list = new ArrayList<>();
                    list.add(imgSrc);
                    resultMap.put(blogId,list);
                }
            }
        });
        return resultMap;
    }

    /**
     * 获取Blog中所有的视频资源
     * @return
     */
    public Map<Integer,List<String>> getAllBlogVideo(){
        Map<Integer ,List<String>> resultMap = new HashMap();
        List<BlogMedia> allPic = LitePal.where( "mediaType = " + BlogMedia.TYPE_VIDEO).find(BlogMedia.class);
        allPic.forEach(blogMedia -> {
            String imgSrc =blogMedia.getMediaSrc();
            File file = new File(imgSrc);
            if(file.exists()){
                List<String> tempDrawingList = new ArrayList<>();
                Integer blogId = blogMedia.getBlogId();
                Set<Integer> keySet = resultMap.keySet();
                if(keySet.contains(blogId)){
                    List<String> drawings = resultMap.get((Integer) blogId);
                    assert drawings != null;
                    drawings.add(imgSrc);
                }else{
                    List<String> list = new ArrayList<>();
                    list.add(imgSrc);
                    resultMap.put(blogId,list);
                }
            }
        });
        return resultMap;
    }

    /**
     * 根据Blog的ID获取该Blog的日期
     * @param integer id
     * @return 如果没有则返回null
     */
    public Date getDateById(Integer integer) {
        Blog blog = LitePal.select("modifiedDate").where("id = " + integer).findFirst(Blog.class);
        if(blog==null){
            return null;
        }else{
            return blog.getModifiedDate();
        }
    }

    /**
     * 根据Blog图片（包含视频）地址获取BlogID
     * @param imageSrc imageSrc
     * @return 返回BlogId，如果没有则返回-1
     */
    public Integer getBlogIdByPicSre(String imageSrc) {
        BlogMedia first = LitePal.where("mediaSrc like '" + imageSrc+"'").findFirst(BlogMedia.class);
        if(null==first){
            return -1;
        }else{
            return first.getBlogId();
        }
    }

    /**
     * 给定一个资源地址，看看是不是Blog的配图
     * @param src 资源地址
     * @return 返回true就是图片资源，返回false只能表示它不是图片，可能是不存在也不能是视频
     */
    public boolean isImageSrc(String src) {
        BlogMedia first = LitePal.where("mediaSrc like '" + src+"'").findFirst(BlogMedia.class);
        if(null==first){
            return false;
        }else if (first.getMediaType().equals(BlogMedia.TYPE_IMAGE)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取所有Blog并封装成BlogVoForBackup
     * @return List<BlogVoForBackup>
     */
    public List<BlogVoForBackup> getAllBlogVoForBackup(){
        List<BlogVoForBackup> backupList = new ArrayList<>();
        //读库
        List<Blog> blogs = LitePal.findAll(Blog.class);
        if(!blogs.isEmpty()){
            blogs.forEach(blog -> {
                BlogVoForBackup blogVoForBackup = new BlogVoForBackup();
                blogVoForBackup.setId(blog.getId());
                blogVoForBackup.setContent(blog.getContent());
                blogVoForBackup.setModifiedDate(blog.getModifiedDate());
                blogVoForBackup.setLabel(blog.getLabel());
                blogVoForBackup.setTitle(blog.getTitle());
                blogVoForBackup.setEncryption(blog.getEncryption());
                blogVoForBackup.setMyUuid(blog.getMyUuid());
                blogVoForBackup.setBlogMediaList(getAllBlogMediaByBlogId(blog.getId()));
                backupList.add(blogVoForBackup);
            });
        }
        return backupList;
    }

    /**
     * 根据BlogId获取该Blog关联的所有媒体资源
     * @param blogId blogId
     * @return 如果没有就返回一个空的List
     */
    public List<BlogMedia> getAllBlogMediaByBlogId(Integer blogId){
        return LitePal.where("blogId = " + blogId).find(BlogMedia.class);
    }


    /**
     * 从blogVoForBackup新增一个Blog（级联新增对应的媒体资源BlogMedia）
     * @param blogVoForBackup blogVoForBackup
     * @return 新增成功返回true
     */
    public boolean addOne(BlogVoForBackup blogVoForBackup,String privateKeyInput) {
        //1.先存储Blog
        if(uuidAlreadyExists(blogVoForBackup.getMyUuid())){
            return false;
        }else{
            Blog blog = new Blog();
            String decode = RSAUtils.decode(blogVoForBackup.getContent(), privateKeyInput);
            if(null==decode){
                blog.setContent("");
            }else{
                blog.setContent(RSAUtils.encode(decode,MyConfiguration.getInstance().getPublicKey()));
            }
            blog.setMyUuid(blogVoForBackup.getMyUuid());
            blog.setTitle(blogVoForBackup.getTitle());
            blog.setLabel(blogVoForBackup.getLabel());
            blog.setEncryption(blogVoForBackup.getEncryption());
            blog.setModifiedDate(blogVoForBackup.getModifiedDate());
            boolean save = blog.save();
            if (save){
                //2.后存储BlogMedia
                List<BlogMedia> blogMediaList = blogVoForBackup.getBlogMediaList();
                if(null!=blogMediaList && !blogMediaList.isEmpty()){
                    blogMediaList.forEach(blogMedia -> {
                        BlogMedia media = new BlogMedia();
                        media.setMediaSrc(blogMedia.getMediaSrc());
                        media.setBlogId(blog.getId());
                        media.setMediaType(blogMedia.getMediaType());
                        media.save();
                    });
                }
                return true;
            }else{
                return false;
            }
        }
    }

    /**
     * 检查Blog的uuid是否已存在
     * 如果uuid为null或""，视为不存在
     * @param uuid uuid
     * @return true已存在
     */
    public boolean uuidAlreadyExists(String uuid){
        //如果uuid为null或""，视为不存在
        if(null==uuid || uuid.isEmpty()){
            return false;
        }
        Blog myUuid = LitePal.select("myUuid")
                .where("myUuid = ?", uuid)
                .findFirst(Blog.class);
        return null != myUuid;
    }
}
