package cn.snowt.diary.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author: HibaraAi
 * @Date: 2022-01-14 12:04:54
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Video extends LitePalSupport implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 图片地址
     */
    private String videoSrc;

    /**
     * 外键,日记Id
     */
    private Integer diaryId;
}
