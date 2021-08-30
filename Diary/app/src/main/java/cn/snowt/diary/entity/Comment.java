package cn.snowt.diary.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 09:26
 * @Description: 评论
 */

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Comment extends LitePalSupport implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 外键,被回复日记的id
     */
    private Integer diaryId;

    /**
     * 修改时间
     */
    private Date modifiedDate;
}
