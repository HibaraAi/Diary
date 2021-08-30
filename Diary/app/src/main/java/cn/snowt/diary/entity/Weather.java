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
 * @Date: 2021-08-15 09:01
 * @Description: 天气
 */

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Weather extends LitePalSupport implements Serializable {
    public static final String WEATHER_CLOUDY = "多云";
    public static final String WEATHER_RAIN = "下雨";
    public static final String WEATHER_SUNNY = "晴";
    public static final String WEATHER_OVERCAST = "阴天";
    public static final String WEATHER_SNOW = "下雪";
    public static final String WEATHER_HAIL = "下冰雹";

    /**
     * 主键
     */
    private Integer id;

    /**
     * 天气情况
     */
    private String weather;

    /**
     * 温度
     */
    private Integer temperature;

    /**
     * 风力
     */
    private Integer wind;
}
