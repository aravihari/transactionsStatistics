package com.statisticsAssignment.controller;

import com.statisticsAssignment.TransactionsStatisticsApplication;
import com.statisticsAssignment.builder.StatisticsRequestBuilder;
import com.statisticsAssignment.model.StatisticsRequest;
import com.statisticsAssignment.model.StatisticsResponse;
import com.statisticsAssignment.service.IStatisticsService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TransactionsStatisticsApplication.class})
public class StatisticsControllerTest {

    @Autowired
    private StatisticsController controller;

    @Autowired
    private IStatisticsService service;

    @Before
    public void init(){
        service.clearCache();
    }

    @Test
    public void testAddStatistics_withValidStats_created(){
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(1.1).withTimestamp(Instant.now().toEpochMilli()).build();
        Mono<ResponseEntity> responseEntity = controller.addStatistics(request);
        Assert.assertEquals(HttpStatus.CREATED, responseEntity);
    }

    @Test
    public void testAddStatistics_withNegativeAmount_created(){
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(-1.1).withTimestamp(Instant.now().toEpochMilli()).build();
        Mono<ResponseEntity> responseEntity = controller.addStatistics(request);
        Assert.assertEquals(HttpStatus.CREATED, responseEntity);
    }

    @Test
    public void testAddStatistics_withInPastTimestampMoreThanAMinute_noContent(){
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(2.1).withTimestamp(Instant.now().toEpochMilli()-60000).build();
        Mono<ResponseEntity> responseEntity = controller.addStatistics(request);
        Assert.assertEquals(HttpStatus.NO_CONTENT, responseEntity);
    }

    @Test
    public void testAddStatistics_withInPastTimestampWithinAMinute_created(){
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(2.1).withTimestamp(Instant.now().toEpochMilli()-50000).build();
        Mono<ResponseEntity> responseEntity = controller.addStatistics(request);
        Assert.assertEquals(HttpStatus.CREATED, responseEntity);
    }

    @Test
    public void testAddAndGetStatistics_withInValidTimestampWithinAMinuteWithSameTime_success() throws Exception{
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        int n = 0;
        double amount = 1.0;
        int count = 50000;
        while(n<count) {
            StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(amount).withTimestamp(Instant.now().toEpochMilli()).build();
            executorService.submit(() -> controller.addStatistics(request));
            n++;
            amount += 1;
        }

        executorService.shutdown();
        Thread.sleep(1000);
        Mono<ResponseEntity<StatisticsResponse>> response = controller.getStatistics();
        Assert.assertEquals(HttpStatus.OK, response);
    }

}
