package com.statisticsAssignment.controller;

import com.statisticsAssignment.model.StatisticsRequest;
import com.statisticsAssignment.model.StatisticsResponse;
import com.statisticsAssignment.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Controller
public class StatisticsController {

    private IStatisticsService statisticsService;

    @Autowired
    public StatisticsController(IStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public Mono<ResponseEntity<StatisticsResponse>> getStatistics() {
        long current = Instant.now().toEpochMilli();
        return statisticsService.getStatistics(current)
                .map(statisticsResponse -> ResponseEntity.ok(statisticsResponse));
    }

    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    public Mono<ResponseEntity> addStatistics(@RequestBody StatisticsRequest request){
        long current = Instant.now().toEpochMilli();
        return statisticsService.addStatistics(request, current)
                .map(added -> {
                    var httpStatus = added ? HttpStatus.CREATED:HttpStatus.NO_CONTENT;
                    return new ResponseEntity(httpStatus);
                });
    }

    @RequestMapping(value = "/transactions", method = RequestMethod.DELETE)
    public Mono<ResponseEntity> deleteStatistics() {
        return statisticsService.clearCache();
    }
}
