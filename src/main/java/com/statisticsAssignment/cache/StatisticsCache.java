package com.statisticsAssignment.cache;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class StatisticsCache<Long, Statistics> extends LinkedHashMap<Long, Statistics> {

    private static final int MIN_CACHE_CAPACITY = 60;
    private static final int MAX_CACHE_CAPACITY = 60000;
    private int capacity = MIN_CACHE_CAPACITY;

    public StatisticsCache(){
        super();
    }

    public StatisticsCache(int capacity){
        super();
        if(capacity < 60){
            capacity = MIN_CACHE_CAPACITY;
        } else if(capacity > MAX_CACHE_CAPACITY){
            capacity = MAX_CACHE_CAPACITY;
        } else if(capacity % 6 != 0){
            int rem = capacity % 6;
            capacity += (6 - rem);
        }
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Long, Statistics> eldest) {
        return this.size() > capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}
