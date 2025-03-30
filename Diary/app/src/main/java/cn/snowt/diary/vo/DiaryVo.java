package cn.snowt.diary.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.snowt.diary.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-16 13:53
 * @Description: 2025年3月15日留：这个原本是Diary的Vo，现在需要将Blog列表预览的功能也加入到这里。
 * 初步想法是将quoteDiaryUuid设置为-1作为Blog的标记。继续改造屎山代码。。。。。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryVo implements Serializable {
    public static final String BLOG_FLAG = "-1";

    private Integer id;
    private String content;
    private String modifiedDate;
    private String weatherStr;
    private String locationStr;
    private String labelStr;
    private List<String> picSrcList; //这个目前只能是ArrayList 2021-10-20留
    private ArrayList<String> videoSrcList;
    private List<Comment> commentList;
    private String quoteDiaryStr;
    private String quoteDiaryUuid;
    private String myUuid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiaryVo diaryVo = (DiaryVo) o;
        return Objects.equals(myUuid, diaryVo.myUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myUuid);
    }
}
