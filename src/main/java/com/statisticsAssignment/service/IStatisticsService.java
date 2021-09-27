package com.statisticsAssignment.service;


import com.statisticsAssignment.model.StatisticsRequest;
import com.statisticsAssignment.model.StatisticsResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface IStatisticsService {
    Mono<Boolean> addStatistics(StatisticsRequest request, long timestamp);
    Mono<StatisticsResponse> getStatistics(long timestamp);
    Mono<ResponseEntity> clearCache();
}
