package cn.snowt.diary.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 专门为分析统计的DiaryVo
 * @Author: HibaraAi
 * @Date: 2021-10-05 17:50
 * @Description:
 */
@Data
public class DiaryVoForFunny {
    private Integer id;
    //已解密的日记内容
    private String content;
    private Date modifiedDate;
    private String weatherStr;
    private String locationStr;
    //已经解析好的标签
    private List<String> labelList;
    //配了多少张图
    private Integer picNum;
    //已解密的评论
    private List<String> commentList;
}
