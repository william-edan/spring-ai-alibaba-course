package com.ai.course.functioncalling.config;

import com.ai.course.functioncalling.model.CompareRequest;
import com.ai.course.functioncalling.model.FlightInfo;
import com.ai.course.functioncalling.model.FlightQuery;
import com.ai.course.functioncalling.service.MockFlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
public class FlightToolConfig {

    private static final Logger log = LoggerFactory.getLogger(FlightToolConfig.class);

    /**
     * 机票查询工具——带 ToolContext
     * 用 FunctionToolCallback.builder() 而不是 @Bean + Function，因为需要 ToolContext
     */
    @Bean
    public ToolCallback searchFlightsFunction(MockFlightService service) {
        return FunctionToolCallback.builder("searchFlights",
                (FlightQuery query, ToolContext ctx) -> {
                    // 从 ToolContext 获取用户身份——不经过 LLM，安全可靠
                    String userId = ctx.getContext().getOrDefault("userId", "anonymous").toString();
                    log.info("[Tool] 用户 {} 查询航班: {} → {} ({})", userId, query.from(), query.to(), query.date());

                    List<FlightInfo> flights = service.search(query.from(), query.to(), query.date());
                    if (flights.isEmpty()) {
                        return "未找到从" + query.from() + "到" + query.to() + "的航班，建议换个日期试试";
                    }

                    // 返回值为 LLM 优化——格式清晰、精简、无敏感信息
                    StringBuilder sb = new StringBuilder();
                    sb.append("查询到 ").append(flights.size()).append(" 个航班：\n");
                    for (FlightInfo f : flights) {
                        sb.append(String.format("- %s(%s) %s-%s ¥%d [%s]\n",
                            f.flightNo(), f.airline(),
                            f.departureTime(), f.arrivalTime(),
                            f.price(), f.aircraft()));
                    }
                    return sb.toString();
                })
            .description("根据出发城市、目的城市和日期查询可用航班。" +
                         "返回航班号、航空公司、起飞到达时间、价格和机型。" +
                         "仅支持国内航线，不支持国际航班。")
            .inputType(FlightQuery.class)
            .build();
    }

    /**
     * 航班对比工具——不需要 ToolContext，用更简单的写法
     */
    @Bean
    public ToolCallback compareFlightsFunction(MockFlightService service) {
        return FunctionToolCallback.builder("compareFlights",
                (CompareRequest req) -> {
                    List<FlightInfo> flights = service.getByFlightNos(req.flightNos());
                    if (flights.size() < 2) {
                        return "至少需要2个有效航班号才能对比，请先查询航班获取航班号";
                    }

                    StringBuilder sb = new StringBuilder("航班对比结果：\n");
                    for (FlightInfo f : flights) {
                        sb.append(String.format("- %s(%s) %s-%s ¥%d 机型%s\n",
                            f.flightNo(), f.airline(),
                            f.departureTime(), f.arrivalTime(),
                            f.price(), f.aircraft()));
                    }

                    FlightInfo cheapest = flights.stream()
                        .min(Comparator.comparingInt(FlightInfo::price)).orElse(null);
                    FlightInfo earliest = flights.stream()
                        .min(Comparator.comparing(FlightInfo::departureTime)).orElse(null);

                    sb.append("\n推荐：");
                    if (cheapest != null) {
                        sb.append("最便宜 ").append(cheapest.flightNo())
                          .append("(¥").append(cheapest.price()).append(") ");
                    }
                    if (earliest != null) {
                        sb.append("最早出发 ").append(earliest.flightNo())
                          .append("(").append(earliest.departureTime()).append(")");
                    }

                    return sb.toString();
                })
            .description("对比多个航班的价格、时间和机型，给出最便宜和最早出发的推荐。" +
                         "需要先通过 searchFlights 获取航班号后才能调用此工具。")
            .inputType(CompareRequest.class)
            .build();
    }
}
