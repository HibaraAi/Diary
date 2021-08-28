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
 * @Date: 2021-08-15 08:56
 * @Description: 日记主体
 */

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Diary extends LitePalSupport implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 标签，不加密，可搜索
     */
    private String label;

    /**
     * 正文内容，加密，不可搜索
     */
    private String content;

    /**
     * 修改时间
     */
    private Date modifiedDate;

    /**
     * 外键,当天天气
     */
    private Integer weatherId;

    /**
     * 外键,所在位置
     */
    private Integer locationId;
}
