package com.ai.course.functioncalling.model;

/**
 * 航班信息——返回给 LLM 的数据
 * 只包含 LLM 需要的字段，不暴露内部 ID、成本价等敏感信息
 */
public record FlightInfo(
    String flightNo,
    String airline,
    String departure,
    String arrival,
    String departureTime,
    String arrivalTime,
    int price,
    String aircraft
) {}
