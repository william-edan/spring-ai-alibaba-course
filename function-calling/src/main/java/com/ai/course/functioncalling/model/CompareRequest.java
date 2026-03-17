package com.ai.course.functioncalling.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 航班对比请求参数
 */
public record CompareRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("要对比的航班号列表，如 ['MU5678', 'CA1234']")
    List<String> flightNos
) {}
