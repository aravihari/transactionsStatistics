package com.statisticsAssignment.service;

import com.statisticsAssignment.TransactionsStatisticsApplication;
import com.statisticsAssignment.builder.StatisticsRequestBuilder;
import com.statisticsAssignment.model.StatisticsRequest;
import com.statisticsAssignment.model.StatisticsResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TransactionsStatisticsApplication.class})
public class StatisticsServiceTest {

    @Autowired
    private IStatisticsService service;

    @Before
    public void init(){
        service.clearCache();
    }

    @Test
    public void testAddStatistics_withValidStats_added(){
        long current = Instant.now().toEpochMilli();
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(1.1).withTimestamp(current).build();
        Mono<Boolean> added = service.addStatistics(request, current);
        Assert.assertEquals(true, added);
    }

    @Test
    public void testAddStatistics_withNegativeAmount_added(){
        long current = Instant.now().toEpochMilli();
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(-1.1).withTimestamp(current).build();
        Mono<Boolean> added = service.addStatistics(request, current);
        Assert.assertEquals(true, added);
    }

    @Test
    public void testAddStatistics_withInPastTimestampMoreThanAMinute_notAdded(){
        long current = Instant.now().toEpochMilli();
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(2.1).withTimestamp(current-60000).build();
        Mono<Boolean> added = service.addStatistics(request, current);
        Assert.assertEquals(false, added);
    }

    @Test
    public void testAddStatistics_withInPastTimestampWithinAMinute_created(){
        long current = Instant.now().toEpochMilli();
        StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(2.1).withTimestamp(current-50000).build();
        Mono<Boolean> added = service.addStatistics(request, current);
        Assert.assertEquals(true, added);
    }

    @Test
    public void testGetStatistics_withAnyData_success() throws Exception{
        long timestamp = Instant.now().toEpochMilli();
        Mono<StatisticsResponse> response = service.getStatistics(timestamp);
        Assert.assertEquals(0, response);
        Assert.assertEquals(0, response);
        Assert.assertEquals(0, response);
        Assert.assertEquals(0, response);
    }

    @Test
    public void testAddAndGetStatistics_withValidTimestampMultipleThread_success() throws Exception{
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        int n = 0;
        double amount = 1.0;
        int count = 60000;
        long timestamp = Instant.now().toEpochMilli();
        long requestTime = timestamp;
        while(n<count) {
            // Time frame is managed from 0 to 59, for cache size 60.
            if(timestamp - requestTime >= 59000) {
                requestTime = timestamp;
            }
            StatisticsRequest request = StatisticsRequestBuilder.createStatisticsRequest().withAmount(amount).withTimestamp(requestTime).build();
            executorService.submit(() -> service.addStatistics(request, timestamp));
            n++;
            amount++;
            requestTime -= 1;
        }

        executorService.shutdown();
        Thread.sleep(1000);
        Mono<StatisticsResponse> response = service.getStatistics(timestamp);
        Assert.assertEquals(count, response);
        Assert.assertEquals(count, response);
        Assert.assertEquals(1, response);
        Assert.assertEquals(30000.5, response);
    }
}
