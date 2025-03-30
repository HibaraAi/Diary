package cn.snowt.diary.async;

import android.os.Handler;

import cn.snowt.blog.BlogDto;
import cn.snowt.blog.BlogService;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;

public class SaveBlogTask extends MyAsyncTask{
    private boolean isSaveNewOne;
    private BlogDto dto;
    private Integer needEditId;

    public SaveBlogTask(Handler handler) {
        super(handler);
    }

    @Override
    void doAsync() {
        BlogService blogService = new BlogService();
        if(!isSaveNewOne){
            //更新Blog
            result = blogService.updateById(needEditId,dto);
        }else{
            //保存新的Blog
            result = blogService.addOne(dto);
        }
    }

    public void asyncSaveBlog(boolean isSaveNewOne, BlogDto dto,Integer needEditId){
        this.isSaveNewOne = isSaveNewOne;
        this.dto = dto;
        this.needEditId = needEditId;
        result = new SimpleResult();
        startAsyncTask();
    }
}
