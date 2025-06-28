package cn.snowt.diary.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Date 2025年5月17日 19点23分
 * @Author HibaraAi
 * @Description 强制使用加密，需要将之前未加密的数据加密后存储，又怕加密出错，
 * 所以加密保存前，先将原文存起来（不加密存储），如果万一真的加密存储失败，可以在这里恢复。这个数据不提供查询，也不提供备份恢复
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackupString extends LitePalSupport implements Serializable {
    public static final Integer TYPE_DIARY = 1;
    public static final Integer TYPE_BLOG = 2;
    public static final Integer TYPE_DIARY_COMMENT = 3;

    /**
     * 内容
     */
    private String str;

    /**
     * 内容的类型
     */
    private Integer type;

    /**
     * 内容的日期
     */
    private Date date;

    /**
     * 如果是日记评论，则记录所属日记的UUID
     */
    private String uuidOfDiary;
}
