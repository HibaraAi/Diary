package cn.snowt.note;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class Item extends LitePalSupport  implements Serializable {
    public static Integer STATE_FINISH = 1;
    public static Integer STATE_UNFINISHED = 2;

    private Integer id;
    private String content;
    private Date createDate;
    private Date finishDate;

    private Integer state;

    public Item() {}

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


}
