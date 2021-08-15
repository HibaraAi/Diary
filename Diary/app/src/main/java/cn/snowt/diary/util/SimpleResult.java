package cn.snowt.diary.util;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 07:18
 * @Description: 封装的基本返回值
 */
public class SimpleResult {
    private Boolean success;
    private String msg;
    private Object data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public SimpleResult() {
    }

    private SimpleResult(Boolean success, String msg, Object data) {
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    public static SimpleResult ok(){
        return new SimpleResult(true,"OK",null);
    }

    public static SimpleResult error(){
        return new SimpleResult(false,"ERROR",null);
    }

    public SimpleResult msg(String msg){
        this.msg = msg;
        return this;
    }

    public SimpleResult data(Object data){
        this.data = data;
        return this;
    }
}
