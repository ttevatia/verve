package com.verve.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class LoggingService {
    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Scheduled(fixedRate = 60000)
    public void logUniqueCount() {
        int count = redisTemplate.keys("unique-id:*").size();
        kafkaProducerService.sendUniqueCount(count);
        logger.info("Unique request count in the last minute: {}", count);
    }
}