package cn.snowt.diary.async;

import android.os.Handler;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.blog.Blog;
import cn.snowt.blog.BlogMedia;
import cn.snowt.blog.BlogService;
import cn.snowt.diary.entity.Comment;
import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.RSAUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;

public class SearchTask extends MyAsyncTask{

    private String searchValue;

    public SearchTask(Handler handler) {
        super(handler);
    }

    @Override
    void doAsync() {
        BlogService blogService = new BlogService();
        //入参判断
        if(null==searchValue || "".equals(searchValue) || "".equals(searchValue.trim())){
            result.setSuccess(false);
            result.setMsg("不允许搜索空值");
            return;
        }
        if(searchValue.trim().length()>=10){
            result.setSuccess(false);
            result.setMsg("搜索字符长度需小于10");
            return;
        }
        searchValue = searchValue.trim();
        //=========================================以下是搜索前的准备==========================================================
        //.获取所有Diary,只获取id、时间、正文、和加密标记即可
        List<Diary> originalDiaryList = LitePal
                .select("id,content,encryption,modifiedDate")
                .order("modifiedDate desc")
                .find(Diary.class);
        //.获取所有Blog,只获取id、标题、时间、正文、和加密标记即可
        List<Blog> originalBlogList = LitePal
                .select("id,title,content,encryption,modifiedDate")
                .order("modifiedDate desc")
                .find(Blog.class);
        //.查询数据库中已有的日记数量，用于计算进度条
        int searchCount = originalDiaryList.size()+originalBlogList.size();
        List<DiaryVo> diaryVos = new ArrayList<>();  //存储搜索结果
        int i =0;
        //===========================================以下处理搜索日记======================================================
        for(;i<originalDiaryList.size();i++){
            //更新进度条
            int pro = (int) Math.ceil(((i+1)/(double) searchCount)*100);
            updateProgress(pro);
            //处理搜索
            Diary diary = originalDiaryList.get(i);
            StringBuilder subContentBuilder = new StringBuilder();
            if(diary.getEncryption()){
                //如果加密则先解密
                diary.setContent(RSAUtils.decode(diary.getContent(), MyConfiguration.getInstance().getPrivateKey()));
            }
            if(diary.getContent().contains(searchValue)){
                //正文含关键字
                int indexOf = diary.getContent().indexOf(searchValue);  //记录匹配的关键字所在位置，
                int cutCount = 25;  //保留关键字附近的25个字符用于展示
                if(indexOf+searchValue.length()<cutCount){
                    //关键字在头
                    subContentBuilder.append(diary.getContent().substring(0,(Math.min(cutCount, diary.getContent().length())))).append("...");
                }else if((diary.getContent().length()-indexOf-(1+(cutCount-searchValue.length())/2))<0){  //int footLength = 1+(cutCount-searchValue.length())/2;  //尾部的长度
                    //关键字在尾
                    subContentBuilder.append("...").append(diary.getContent().substring(diary.getContent().length()-cutCount-1));
                }else{
                    //关键字在中间
                    subContentBuilder.append("...").append(diary.getContent().substring(indexOf-((cutCount-searchValue.length()/2)-1),Math.min(diary.getContent().length(),(indexOf+((cutCount-searchValue.length()/2)-1))))).append("...");
                }
                //封装DiaryVo
                DiaryVo diaryVo = new DiaryVo();
                diaryVo.setId(diary.getId());
                diaryVo.setContent(subContentBuilder.toString());
                diaryVo.setContent(diaryVo.getContent().replaceAll("\n",""));
                diaryVo.setModifiedDate(BaseUtils.dateToString(diary.getModifiedDate()).substring(0,10));
                //查询一张图片
                List<Drawing> drawings = LitePal.where("diaryId = ?", diary.getId() + "").limit(1).find(Drawing.class);
                List<String> picList = new ArrayList<>();
                if(drawings.size()!=0){
                    picList.add(drawings.get(0).getImgSrc());
                }
                diaryVo.setPicSrcList(picList);
                diaryVos.add(diaryVo);
            }else{
                //正文不包含再来找评论是否包含
                //查询评论
                List<Comment> commentList = LitePal.where("diaryId = ?",diary.getId()+"").order("modifiedDate desc").find(Comment.class);
                commentList.forEach(comment ->{
                    if (comment.getEncryption()){
                        //此条记录需要解密
                        comment.setContent(RSAUtils.decode(comment.getContent(),MyConfiguration.getInstance().getPrivateKey()));
                    }
                    if(comment.getContent().contains(searchValue)){
                        //某条评论包含关键字
                        DiaryVo diaryVo = new DiaryVo();
                        diaryVo.setId(diary.getId());
                        diaryVo.setContent("【评论包含】..."+searchValue+"...");
                        diaryVo.setModifiedDate(BaseUtils.dateToString(diary.getModifiedDate()).substring(0,10));
                        //查询一张图片
                        List<Drawing> drawings = LitePal.where("diaryId = ?", diary.getId() + "").limit(1).find(Drawing.class);
                        List<String> picList = new ArrayList<>();
                        if(drawings.size()!=0){
                            picList.add(drawings.get(0).getImgSrc());
                        }
                        diaryVo.setPicSrcList(picList);
                        diaryVos.add(diaryVo);
                    }
                });
            }
        }
        //====================================以上部分负责日记搜过，以下部分负责Blog搜索==================================================================
        for(int j=0;j<originalBlogList.size();j++,i++){
            //更新进度条
            int pro = (int) Math.ceil(((i+1)/(double) searchCount)*100);
            updateProgress(pro);
            //处理搜索
            Blog blog = originalBlogList.get(j);
            if(blog.getTitle().contains(searchValue)){  //标题包含关键字
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
                String cover = blogService.getCover(blog.getId());
                List<String> picSrcList = new ArrayList<>();
                picSrcList.add(cover);
                vo.setPicSrcList(picSrcList);
                diaryVos.add(vo);
            }else{  //标题不包含再搜索正文
                if(blog.getEncryption()){
                    blog.setContent(RSAUtils.decode(blog.getContent(), MyConfiguration.getInstance().getPrivateKey()));
                }
                if(blog.getContent().contains(searchValue)){
                    StringBuilder subContentBuilder = new StringBuilder();
                    //正文含关键字
                    int indexOf = blog.getContent().indexOf(searchValue);  //记录匹配的关键字所在位置，
                    int cutCount = 25;  //保留关键字附近的25个字符用于展示
                    if(indexOf+searchValue.length()<cutCount){
                        //关键字在头
                        subContentBuilder.append(blog.getContent().substring(0,(Math.min(cutCount, blog.getContent().length())))).append("...");
                    }else if((blog.getContent().length()-indexOf-(1+(cutCount-searchValue.length())/2))<0){  //int footLength = 1+(cutCount-searchValue.length())/2;  //尾部的长度
                        //关键字在尾
                        subContentBuilder.append("...").append(blog.getContent().substring(blog.getContent().length()-cutCount-1));
                    }else{
                        //关键字在中间
                        subContentBuilder.append("...").append(blog.getContent().substring(indexOf-((cutCount-searchValue.length()/2)-1),Math.min(blog.getContent().length(),(indexOf+((cutCount-searchValue.length()/2)-1))))).append("...");
                    }
                    DiaryVo vo = new DiaryVo();
                    vo.setQuoteDiaryUuid(DiaryVo.BLOG_FLAG);
                    vo.setId(blog.getId());
                    String subDateStr = BaseUtils.dateToString(blog.getModifiedDate()).substring(0, 10);
                    vo.setModifiedDate(subDateStr);
                    vo.setContent("[这是一条Blog]  "+blog.getTitle()+"\n"+subContentBuilder.toString());
                    String cover = blogService.getCover(blog.getId());
                    List<String> picSrcList = new ArrayList<>();
                    picSrcList.add(cover);
                    vo.setPicSrcList(picSrcList);
                    diaryVos.add(vo);
                }
            }
        }


        //===================================搜索完成，封装结果=================================================================
        if(diaryVos.isEmpty()){
            result.setSuccess(false);
            result.setMsg("没有找到【"+searchValue+"】");
        }else{
            result.setSuccess(true);
            result.setMsg(searchValue);
            result.setData(diaryVos);
        }
    }

    /**
     * 全盘搜索
     * 对所有日记正文、及评论进行搜索，如果有加密的数据，解密后搜索
     * @param s 需要搜索的文本
     * @return
     */
    public void fullSearch(String s){
        result = new SimpleResult();
        searchValue = s;
        startAsyncTask();
    }
}
