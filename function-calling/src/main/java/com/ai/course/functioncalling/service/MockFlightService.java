package com.ai.course.functioncalling.service;

import com.ai.course.functioncalling.model.FlightInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Mock 航班数据源
 * 生产环境替换为真实的航班 API 调用
 */
@Service
public class MockFlightService {

    private static final List<FlightInfo> FLIGHTS = List.of(
        new FlightInfo("MU5678", "东方航空", "北京", "上海", "08:00", "10:15", 520, "A320"),
        new FlightInfo("CA1234", "中国国航", "北京", "上海", "12:30", "14:40", 680, "B737"),
        new FlightInfo("HU7890", "海南航空", "北京", "上海", "17:00", "19:20", 550, "A330"),
        new FlightInfo("CZ3456", "南方航空", "北京", "广州", "09:00", "12:00", 890, "A321"),
        new FlightInfo("MU2345", "东方航空", "北京", "广州", "14:00", "17:10", 760, "B787"),
        new FlightInfo("CA5678", "中国国航", "上海", "深圳", "10:00", "12:30", 620, "A320"),
        new FlightInfo("ZH1234", "深圳航空", "上海", "深圳", "15:30", "17:50", 480, "B737"),
        new FlightInfo("MU9999", "东方航空", "北京", "深圳", "07:30", "10:45", 920, "A350")
    );

    public List<FlightInfo> search(String from, String to, String date) {
        return FLIGHTS.stream()
            .filter(f -> f.departure().equals(from) && f.arrival().equals(to))
            .toList();
    }

    public List<FlightInfo> getByFlightNos(List<String> flightNos) {
        return FLIGHTS.stream()
            .filter(f -> flightNos.contains(f.flightNo()))
            .toList();
    }
}
