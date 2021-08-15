package cn.snowt.diary.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 09:24
 * @Description: 配图
 */

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Drawing extends LitePalSupport implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 图片地址
     */
    private String imgSrc;

    /**
     * 外键,日记Id
     */
    private Integer diaryId;
}
