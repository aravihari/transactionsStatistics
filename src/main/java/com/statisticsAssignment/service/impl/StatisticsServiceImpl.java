package com.statisticsAssignment.service.impl;

import com.statisticsAssignment.builder.StatisticsResponseBuilder;
import com.statisticsAssignment.cache.StatisticsCache;
import com.statisticsAssignment.model.Statistics;
import com.statisticsAssignment.model.StatisticsRequest;
import com.statisticsAssignment.model.StatisticsResponse;
import com.statisticsAssignment.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

import static com.statisticsAssignment.util.Constants.ONE_MINUTE_IN_MS;

@Service
public class StatisticsServiceImpl implements IStatisticsService{

    private final StatisticsCache<Long, Statistics> cache;

    @Autowired
    public StatisticsServiceImpl(StatisticsCache<Long, Statistics> cache) {
        this.cache = cache;
    }

    public Mono<Boolean> addStatistics(StatisticsRequest request, long timestamp) {
        long requestTime = request.getTimestamp();
        long delay = timestamp - requestTime;
        if (delay < 0 || delay >= ONE_MINUTE_IN_MS) {
            return Mono.just(false);
        } else {
            Long key = getKeyFromTimestamp(requestTime);
            Statistics s = cache.get(key);
            if(s == null) {
                synchronized (cache) {
                    s = cache.get(key);
                    if (s == null) {
                        s = new Statistics();
                        cache.put(key, s);
                    }
                }
            }
            s.updateStatistics(request.getAmount());
        }
        return Mono.just(true);
    }

    public Mono<StatisticsResponse> getStatistics(long timestamp) {
        Map<Long, Statistics> copy = cache.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getStatistics()));
        return Mono.just(getStatisticsFromCacheCopy(copy, timestamp));
    }

    private StatisticsResponse getStatisticsFromCacheCopy(Map<Long, Statistics> copy, long timestamp) {
        double sum = 0;
        double avg = 0;
        double max = 0;
        double min = Double.MAX_VALUE;
        long count = 0;
        Long key = getKeyFromTimestamp(timestamp);

        for (Map.Entry<Long, Statistics> e : copy.entrySet()) {
            Long eKey = e.getKey();
            Long timeFrame = key - eKey;
            if(timeFrame >= 0 && timeFrame < cache.getCapacity()) {
                Statistics eValue = e.getValue();
                if(eValue.getCount() > 0) {
                    sum += eValue.getSum();
                    min = min < eValue.getMin() ? min : eValue.getMin();
                    max = max > eValue.getMax() ? max : eValue.getMax();
                    count += eValue.getCount();
                }
            }
        }
        if(count == 0) {
            min = 0;
            avg = 0;
        } else {
            avg = sum / count;
        }

        return StatisticsResponseBuilder.createStatisticsResponse().withSum(sum).withAvg(avg).withMax(max).withMin(min).withCount(count).build();
    }

    private Long getKeyFromTimestamp(Long timestamp) {
        return (timestamp * cache.getCapacity()) / ONE_MINUTE_IN_MS;
    }

    public Mono<ResponseEntity> clearCache() {
        cache.clear();
        return null;
    }
}
