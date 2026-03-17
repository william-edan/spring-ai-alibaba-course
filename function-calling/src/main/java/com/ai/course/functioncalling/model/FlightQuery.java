package com.ai.course.functioncalling.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 航班查询参数——LLM 会自动填充这些字段
 * 每个字段的 description 会出现在 JSON Schema 里，影响 LLM 的参数提取准确率
 */
public record FlightQuery(
    @JsonProperty(required = true)
    @JsonPropertyDescription("出发城市名称，如'北京'、'上海'")
    String from,

    @JsonProperty(required = true)
    @JsonPropertyDescription("目的城市名称")
    String to,

    @JsonProperty(required = true)
    @JsonPropertyDescription("出发日期，格式 yyyy-MM-dd")
    String date
) {}
