package cn.snowt.note;

import org.litepal.crud.LitePalSupport;

import java.util.Date;

public class Item extends LitePalSupport {
    public static Integer STATE_FINISH = 1;
    public static Integer STATE_UNFINISHED = 2;

    private Integer id;
    private String content;
    private Date createDate;
    private Date finishDate;

    private Integer state;

    public Item(Integer id, String content, Date createDate) {
        this.id = id;
        this.content = content;
        this.createDate = createDate;
    }

    public Item(String content, Date createDate) {
        this.content = content;
        this.createDate = createDate;
        this.state = STATE_UNFINISHED;
    }

    public Item() {
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
