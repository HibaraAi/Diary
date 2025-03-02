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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.FileUtils;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.RSAUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.util.UriUtils;

/**
 * @Author : HibaraAi github.com/HibaraAi
 * @Date : on 2025-02-15 11:26.
 * @Description : Blog的操作逻辑、数据库操作
 */
public class BlogService {
    public static final String TAG = "BlogService";

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
    private String getCover(Integer id){
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
}
