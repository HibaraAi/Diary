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

    /**
     * 加密标志
     */
    private Boolean encryption;

    /**
     * 引用了哪个日记，无引用为null或""
     */
    private String quoteDiaryUuid;

    /**
     * 恢复备份时回有重复日记，而且没有uuid时，恢复日记中的引用日记会恢复不到
     * 其实一开始就应该用uuid作为主键，目前使用在备份/恢复中
     */
    private String myUuid;
}
