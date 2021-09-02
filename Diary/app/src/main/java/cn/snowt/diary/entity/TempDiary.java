package cn.snowt.diary.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-01 18:12
 * @Description: 草稿箱存储的文本
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TempDiary extends LitePalSupport implements Serializable {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 内容
     */
    private String content;
}
