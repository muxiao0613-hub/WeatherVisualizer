package com.backend.lxy.client;

import com.backend.lxy.domain.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class MockDataFactory {

    private static final Random random = new Random();

    public CurrentWeatherDTO mockCurrentWeather(String city, Double lat, Double lon) {
        int seed = city.hashCode();
        Random cityRandom = new Random(seed);

        String[] windDirs = {"北风", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风"};
        String[] descriptions = {"晴", "多云", "阴", "小雨", "中雨", "大雨", "雷阵雨", "雪"};

        return CurrentWeatherDTO.builder()
                .city(city)
                .lat(lat)
                .lon(lon)
                .temp(15 + cityRandom.nextDouble() * 15)
                .feelsLike(14 + cityRandom.nextDouble() * 15)
                .description(descriptions[cityRandom.nextInt(descriptions.length)])
                .icon("01d")
                .windSpeed(2 + cityRandom.nextDouble() * 8)
                .windDeg(cityRandom.nextInt(360))
                .windDir(windDirs[cityRandom.nextInt(windDirs.length)])
                .windScale((1 + cityRandom.nextInt(4)) + "-" + (1 + cityRandom.nextInt(4)))
                .humidity(40 + cityRandom.nextInt(50))
                .pressure(1000.0 + cityRandom.nextInt(30))
                .visibility(5 + cityRandom.nextDouble() * 15)
                .precip(cityRandom.nextDouble() * 10)
                .cloud(cityRandom.nextInt(100))
                .dew(10 + cityRandom.nextDouble() * 10)
                .timestamp(System.currentTimeMillis() / 1000)
                .build();
    }

    public List<HourlyForecastDTO> mockHourlyForecast(String city, Double lat, Double lon) {
        List<HourlyForecastDTO> forecasts = new ArrayList<>();
        int seed = city.hashCode();
        Random cityRandom = new Random(seed);

        String[] descriptions = {"晴", "多云", "阴", "小雨"};
        String[] windDirs = {"北风", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风"};

        for (int i = 0; i < 24; i++) {
            LocalDateTime time = LocalDateTime.now().plusHours(i);
            forecasts.add(HourlyForecastDTO.builder()
                    .city(city)
                    .lat(lat)
                    .lon(lon)
                    .time(time)
                    .temp(15 + cityRandom.nextDouble() * 10)
                    .feelsLike(14 + cityRandom.nextDouble() * 10)
                    .description(descriptions[cityRandom.nextInt(descriptions.length)])
                    .icon("01d")
                    .windSpeed(2 + cityRandom.nextDouble() * 8)
                    .windDeg(cityRandom.nextInt(360))
                    .windDir(windDirs[cityRandom.nextInt(windDirs.length)])
                    .windScale((1 + cityRandom.nextInt(4)) + "-" + (1 + cityRandom.nextInt(4)))
                    .humidity(40 + cityRandom.nextInt(40))
                    .pressure(1000.0 + cityRandom.nextInt(30))
                    .visibility(5 + cityRandom.nextDouble() * 15)
                    .precip(cityRandom.nextDouble() * 10)
                    .cloud(cityRandom.nextInt(100))
                    .dew(10 + cityRandom.nextDouble() * 10)
                    .pop(cityRandom.nextDouble() * 50)
                    .build());
        }

        return forecasts;
    }

    public List<DailyForecastDTO> mockDailyForecast(String city, Double lat, Double lon) {
        List<DailyForecastDTO> forecasts = new ArrayList<>();
        int seed = city.hashCode();
        Random cityRandom = new Random(seed);

        String[] descriptions = {"晴", "多云", "阴", "小雨", "中雨"};
        String[] windDirs = {"北风", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风"};
        String[] moonPhases = {"新月", "娥眉月", "上弦月", "盈凸月", "满月", "亏凸月", "下弦月", "残月"};

        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            forecasts.add(DailyForecastDTO.builder()
                    .city(city)
                    .lat(lat)
                    .lon(lon)
                    .date(date)
                    .tempMin(10 + cityRandom.nextDouble() * 8)
                    .tempMax(20 + cityRandom.nextDouble() * 10)
                    .iconDay("100")
                    .textDay(descriptions[cityRandom.nextInt(descriptions.length)])
                    .iconNight("150")
                    .textNight(descriptions[cityRandom.nextInt(descriptions.length)])
                    .wind360Day(cityRandom.nextInt(360))
                    .windDirDay(windDirs[cityRandom.nextInt(windDirs.length)])
                    .windScaleDay((1 + cityRandom.nextInt(4)) + "-" + (1 + cityRandom.nextInt(4)))
                    .windSpeedDay(2 + cityRandom.nextDouble() * 8)
                    .wind360Night(cityRandom.nextInt(360))
                    .windDirNight(windDirs[cityRandom.nextInt(windDirs.length)])
                    .windScaleNight((1 + cityRandom.nextInt(4)) + "-" + (1 + cityRandom.nextInt(4)))
                    .windSpeedNight(1 + cityRandom.nextDouble() * 6)
                    .humidity(40 + cityRandom.nextInt(40))
                    .pressure(1000.0 + cityRandom.nextInt(30))
                    .visibility(5 + cityRandom.nextDouble() * 15)
                    .precip(cityRandom.nextDouble() * 10)
                    .cloud(cityRandom.nextInt(100))
                    .uvIndex(cityRandom.nextInt(11))
                    .sunrise(String.format("%02d:%02d", 5 + cityRandom.nextInt(2), cityRandom.nextInt(60)))
                    .sunset(String.format("%02d:%02d", 17 + cityRandom.nextInt(2), cityRandom.nextInt(60)))
                    .moonrise(String.format("%02d:%02d", cityRandom.nextInt(24), cityRandom.nextInt(60)))
                    .moonset(String.format("%02d:%02d", cityRandom.nextInt(24), cityRandom.nextInt(60)))
                    .moonPhase(moonPhases[cityRandom.nextInt(moonPhases.length)])
                    .moonPhaseIcon("80" + (1 + cityRandom.nextInt(8)))
                    .build());
        }

        return forecasts;
    }

    public List<AlertDTO> mockAlerts(String city, Double lat, Double lon) {
        List<AlertDTO> alerts = new ArrayList<>();
        int seed = city.hashCode();
        Random cityRandom = new Random(seed);

        String[] alertTypes = {"暴雨预警", "大风预警", "高温预警", "寒潮预警", "台风预警"};
        String[] levels = {"蓝色", "黄色", "橙色", "红色"};

        int alertCount = 1 + cityRandom.nextInt(2);

        for (int i = 0; i < alertCount; i++) {
            String alertType = alertTypes[cityRandom.nextInt(alertTypes.length)];
            String level = levels[cityRandom.nextInt(levels.length)];

            LocalDateTime start = LocalDateTime.now().plusHours(cityRandom.nextInt(6));
            LocalDateTime end = start.plusHours(6 + cityRandom.nextInt(18));

            alerts.add(AlertDTO.builder()
                    .city(city)
                    .lat(lat)
                    .lon(lon)
                    .event(alertType)
                    .description(String.format("%s%s：预计未来24小时内，%s地区将出现%s天气，请注意防范。",
                            level, alertType, city, alertType.replace("预警", "")))
                    .start(start)
                    .end(end)
                    .level(level)
                    .tags(level)
                    .build());
        }

        return alerts;
    }

    public String mockAiResponse(String question, String city) {
        String[] responses = {
            "根据" + city + "的天气数据，今天天气" + getRandomWeatherDesc() + "，建议" + getRandomAdvice(),
            "关于" + city + "的天气，目前" + getRandomWeatherDesc() + "，" + getRandomAdvice(),
            city + "的天气预报显示" + getRandomWeatherDesc() + "，适合" + getRandomActivity(),
            "根据当前天气数据，" + city + "地区" + getRandomWeatherDesc() + "，请注意" + getRandomWarning()
        };

        int seed = (question + city).hashCode();
        Random random = new Random(seed);
        return responses[random.nextInt(responses.length)];
    }

    public List<CityDTO> searchCities(String keyword) {
        List<CityDTO> cities = new ArrayList<>();
        String[] cityNames = {"北京", "上海", "广州", "深圳", "成都", "杭州", "武汉", "西安", "南京", "天津", "重庆", "苏州", "抚州"};
        
        for (String cityName : cityNames) {
            if (cityName.contains(keyword) || keyword.contains(cityName)) {
                cities.add(CityDTO.builder()
                        .name(cityName)
                        .country("中国")
                        .state(getProvince(cityName))
                        .lat(30.0 + Math.random() * 10)
                        .lon(110.0 + Math.random() * 10)
                        .build());
            }
        }
        
        return cities;
    }

    private String getRandomWeatherDesc() {
        String[] descs = {"晴朗", "多云", "阴天", "有小雨", "有中雨", "有雷阵雨"};
        return descs[random.nextInt(descs.length)];
    }

    private String getRandomAdvice() {
        String[] advices = {
            "适当增减衣物",
            "注意防晒",
            "带好雨具",
            "注意保暖",
            "适合户外活动",
            "注意交通安全"
        };
        return advices[random.nextInt(advices.length)];
    }

    private String getRandomActivity() {
        String[] activities = {
            "户外运动",
            "散步",
            "室内活动",
            "购物",
            "郊游",
            "休息"
        };
        return activities[random.nextInt(activities.length)];
    }

    private String getRandomWarning() {
        String[] warnings = {
            "防晒",
            "防雨",
            "保暖",
            "防风",
            "交通安全"
        };
        return warnings[random.nextInt(warnings.length)];
    }

    private String getProvince(String city) {
        if (city.equals("北京") || city.equals("天津") || city.equals("重庆")) {
            return city + "市";
        }
        if (city.equals("上海")) {
            return "上海市";
        }
        if (city.equals("广州") || city.equals("深圳") || city.equals("苏州") || city.equals("抚州")) {
            return "广东省";
        }
        if (city.equals("成都")) {
            return "四川省";
        }
        if (city.equals("杭州")) {
            return "浙江省";
        }
        if (city.equals("武汉")) {
            return "湖北省";
        }
        if (city.equals("西安")) {
            return "陕西省";
        }
        if (city.equals("南京")) {
            return "江苏省";
        }
        return "未知";
    }
}
