package com.backend.lxy.client;

import com.backend.lxy.domain.dto.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class MockDataFactory {

    private static final String[] CITIES = {"Beijing", "Shanghai", "Guangzhou", "Shenzhen", "Chengdu",
            "Hangzhou", "Wuhan", "Xi'an", "Nanjing", "Tianjin", "Chongqing", "Suzhou",
            "New York", "London", "Tokyo", "Paris", "Sydney", "Moscow", "Berlin", "Rome"};

    private static final String[] DESCRIPTIONS = {"晴朗", "多云", "阴天", "小雨", "中雨", "雷阵雨", "雾", "霾"};
    private static final String[] ICONS = {"01d", "02d", "03d", "10d", "10d", "11d", "50d", "50d"};

    private Random getRandomForCity(String city) {
        long seed = city.hashCode();
        return new Random(seed);
    }

    public CurrentWeatherDTO mockCurrentWeather(String city, Double lat, Double lon) {
        Random random = getRandomForCity(city);
        int baseTemp = 15 + random.nextInt(20);
        return CurrentWeatherDTO.builder()
                .city(city)
                .lat(lat != null ? lat : 39.9042 + random.nextDouble() * 0.1)
                .lon(lon != null ? lon : 116.4074 + random.nextDouble() * 0.1)
                .temp(Double.valueOf(baseTemp))
                .feelsLike(Double.valueOf(baseTemp - 2 + random.nextInt(5)))
                .description(DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)])
                .icon(ICONS[random.nextInt(ICONS.length)])
                .windSpeed(1.0 + random.nextDouble() * 8.0)
                .windDeg(random.nextInt(360))
                .humidity(40 + random.nextInt(50))
                .pressure(Double.valueOf(1000 + random.nextInt(30)))
                .visibility(5.0 + random.nextDouble() * 15.0)
                .timestamp(System.currentTimeMillis() / 1000)
                .country("CN")
                .aqi(30 + random.nextInt(150))
                .build();
    }

    public List<HourlyForecastDTO> mockHourlyForecast(String city, Double lat, Double lon) {
        List<HourlyForecastDTO> forecasts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        Random random = getRandomForCity(city);
        int baseTemp = 15 + random.nextInt(20);

        for (int i = 0; i < 24; i++) {
            LocalDateTime time = now.plusHours(i);
            int hour = time.getHour();
            int tempVariation = (hour < 6 || hour > 18) ? -5 : (hour >= 12 && hour <= 15) ? 3 : 0;

            forecasts.add(HourlyForecastDTO.builder()
                    .city(city)
                    .lat(lat != null ? lat : 39.9042 + random.nextDouble() * 0.1)
                    .lon(lon != null ? lon : 116.4074 + random.nextDouble() * 0.1)
                    .time(time)
                    .temp((double) (baseTemp + tempVariation + random.nextInt(3)))
                    .feelsLike((double) (baseTemp + tempVariation + random.nextInt(3) - 1))
                    .description(DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)])
                    .icon(ICONS[random.nextInt(ICONS.length)])
                    .windSpeed(1.0 + random.nextDouble() * 8.0)
                    .humidity(40 + random.nextInt(50))
                    .pop(random.nextDouble() * 0.6)
                    .build());
        }
        return forecasts;
    }

    public List<DailyForecastDTO> mockDailyForecast(String city, Double lat, Double lon) {
        List<DailyForecastDTO> forecasts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Random random = getRandomForCity(city);
        int baseTemp = 15 + random.nextInt(20);

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            int dayVariation = (i % 3 == 0) ? 3 : (i % 3 == 1) ? -2 : 0;

            forecasts.add(DailyForecastDTO.builder()
                    .city(city)
                    .lat(lat != null ? lat : 39.9042 + random.nextDouble() * 0.1)
                    .lon(lon != null ? lon : 116.4074 + random.nextDouble() * 0.1)
                    .date(date)
                    .tempMin((double) (baseTemp + dayVariation - 3 - random.nextInt(3)))
                    .tempMax((double) (baseTemp + dayVariation + 3 + random.nextInt(3)))
                    .description(DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)])
                    .icon(ICONS[random.nextInt(ICONS.length)])
                    .windSpeed(1.0 + random.nextDouble() * 8.0)
                    .humidity(40 + random.nextInt(50))
                    .pop(random.nextDouble() * 0.6)
                    .build());
        }
        return forecasts;
    }

    public List<AlertDTO> mockAlerts(String city, Double lat, Double lon) {
        List<AlertDTO> alerts = new ArrayList<>();
        Random random = getRandomForCity(city);
        LocalDateTime now = LocalDateTime.now();
        String[] alertTypes = {"高温预警", "暴雨预警", "大风预警", "雾霾预警", "雷电预警", "寒潮预警", "台风预警"};
        String[] levels = {"黄色", "橙色", "红色"};

        int alertCount = 1 + random.nextInt(2);
        for (int i = 0; i < alertCount; i++) {
            String alertType = alertTypes[random.nextInt(alertTypes.length)];
            String level = levels[random.nextInt(levels.length)];
            
            alerts.add(AlertDTO.builder()
                    .city(city)
                    .lat(lat != null ? lat : 39.9042 + random.nextDouble() * 0.1)
                    .lon(lon != null ? lon : 116.4074 + random.nextDouble() * 0.1)
                    .event(alertType)
                    .description("预计未来24小时内可能出现" + alertType + "（" + level + "），请注意防范。建议减少户外活动，做好防护措施。")
                    .start(now)
                    .end(now.plusHours(24))
                    .level(level)
                    .tags("weather,warning")
                    .build());
        }

        return alerts;
    }

    public String mockAiResponse(String question, String city) {
        Random random = getRandomForCity(city + question);
        String[] responses = {
                "根据" + city + "当前的天气情况，建议您外出时携带雨具，注意保暖。",
                city + "未来几天气温变化较大，建议您根据天气变化适时增减衣物。",
                "考虑到" + city + "的湿度较高，建议您注意室内通风，保持空气流通。",
                city + "今日风力较大，外出请注意安全，远离广告牌等临时搭建物。",
                "根据天气预报，" + city + "未来几天适合户外活动，但请注意防晒。"
        };

        return responses[random.nextInt(responses.length)];
    }

    public List<CityDTO> searchCities(String keyword) {
        List<CityDTO> results = new ArrayList<>();
        Random random = getRandomForCity(keyword);

        for (String city : CITIES) {
            if (city.toLowerCase().contains(keyword.toLowerCase())) {
                results.add(CityDTO.builder()
                        .name(city)
                        .country("CN")
                        .state("")
                        .lat(30.0 + random.nextDouble() * 10.0)
                        .lon(110.0 + random.nextDouble() * 10.0)
                        .build());
            }
        }

        if (results.isEmpty()) {
            results.add(CityDTO.builder()
                    .name(keyword)
                    .country("CN")
                    .state("")
                    .lat(35.0 + random.nextDouble() * 5.0)
                    .lon(110.0 + random.nextDouble() * 10.0)
                    .build());
        }

        return results;
    }
}
