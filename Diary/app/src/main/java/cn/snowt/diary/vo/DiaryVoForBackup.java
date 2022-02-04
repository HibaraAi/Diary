package cn.snowt.diary.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import cn.snowt.diary.entity.Comment;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.entity.Location;
import cn.snowt.diary.entity.Video;
import cn.snowt.diary.entity.Weather;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-04 18:15
 * @Description: 专门用来备份
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryVoForBackup implements Serializable {
    private Integer id;
    private String content;
    private Date modifiedDate;
    private Weather weather;
    private Location location;
    private String labelStr;
    private List<Comment> commentList;
    private List<Drawing> drawingList;
    private List<Video> videoList;
    private Boolean encryption;
    private String quoteDiaryUuid;
    private String myUuid;
}
