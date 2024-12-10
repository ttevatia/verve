package com.verve.service.controller;

import com.verve.service.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@RestController
public class VerveController {
    private static final Logger logger = LoggerFactory.getLogger(VerveController.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @GetMapping("/api/verve/accept")
    public String acceptRequest(@RequestParam int id, @RequestParam(required = false) String endpoint) {
        try {
            String redisKey = "unique-id:" + id;
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "true", 1, TimeUnit.MINUTES);

            if (!isNew) {
                return "ok"; // Already processed
            }

            if (endpoint != null) {
                RestTemplate restTemplate = new RestTemplate();
                var requestPayload = new UniqueCountRequest(redisTemplate.keys("unique-id:*").size());
                var response = restTemplate.postForEntity(endpoint, requestPayload, String.class);
                logger.info("POST to {}: Status = {}", endpoint, response.getStatusCode());
            }

            return "ok";
        } catch (Exception e) {
            logger.error("Error processing request", e);
            return "failed";
        }
    }

    static class UniqueCountRequest {
        private int uniqueCount;

        public UniqueCountRequest(int uniqueCount) {
            this.uniqueCount = uniqueCount;
        }

        public int getUniqueCount() {
            return uniqueCount;
        }

        public void setUniqueCount(int uniqueCount) {
            this.uniqueCount = uniqueCount;
        }
    }
}