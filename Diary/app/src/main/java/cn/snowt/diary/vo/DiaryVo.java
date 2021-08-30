package cn.snowt.diary.vo;

import java.io.Serializable;
import java.util.List;

import cn.snowt.diary.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-16 13:53
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryVo implements Serializable {
    private Integer id;
    private String content;
    private String modifiedDate;
    private String weatherStr;
    private String locationStr;
    private String labelStr;
    private List<String> picSrcList;
    private List<Comment> commentList;
}
