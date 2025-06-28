package cn.snowt.diary.async;

import android.os.Handler;

import java.util.Date;
import java.util.List;

import cn.snowt.blog.BlogService;
import cn.snowt.blog.BlogSimpleVo;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;

/**
 * 读取Blog列表的异步任务
 */
public class LoadingBlogListTask extends MyAsyncTask{

    public LoadingBlogListTask(Handler handler) {
        super(handler);
    }

    @Override
    void doAsync() {
        BlogService blogService = new BlogService();
        List<BlogSimpleVo>  voList = blogService.getAllBlogs();
        if(voList.isEmpty()){
            result.setSuccess(false);
            result.setMsg("没有Blog。。。");
        }else{
            voList.sort((o1, o2) -> {
                Date o1Date = BaseUtils.stringToDate(o1.getDate());
                Date o2Date = BaseUtils.stringToDate(o2.getDate());
                if (o1Date.after(o2Date)) {
                    return -1;
                } else if (o1Date.before(o2Date)) {
                    return 1;
                } else {
                    return 0;
                }
            });
            result.setSuccess(true);
            result.setMsg("查询到"+voList.size()+"条Blog");
            result.setData(voList);
        }
    }

    public void getAllBlogsDesc(){
        result = new SimpleResult();
        startAsyncTask();
    }
}
