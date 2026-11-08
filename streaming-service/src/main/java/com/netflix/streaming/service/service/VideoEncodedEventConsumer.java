
package com.netflix.streaming.service.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.netflix.streaming.service.event.VideoEncodedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoEncodedEventConsumer {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String MASTER_PLAYLIST_KEY_PREFIX = "streaming:playlist:";

    /**
     * Listens to video.encoded Kafka topic.
     * Stores master playlist key in Redis when encoding is complete.
     *
     * @param event the consumed video encoded event
     */
    @KafkaListener(
            topics = "video.encoded",
            groupId = "streaming-service-group"
    )
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void consumeVideoEncodedEvent(@Payload VideoEncodedEvent event) {

        String movieId = event.getMovieId();
        String masterPlaylistKey = event.getMasterPlaylistKey();
        boolean success = event.isSuccess();

        log.info("Consumed VideoEncodedEvent for movie: {} success: {}",
                movieId, success);

        if (success) {
            String cacheKey = MASTER_PLAYLIST_KEY_PREFIX + movieId;

            redisTemplate.opsForValue().set(cacheKey, masterPlaylistKey);

            log.info("Master playlist key stored in Redis for movie: {}", movieId);

        } else {
            log.error("Encoding failed for movie: {} — {}",
                    movieId, event.getErrorMessage());
        }
    }

    /**
     * Recovery method invoked when all retry attempts are exhausted.
     *
     * @param event the event that failed processing
     * @param e the last exception thrown
     */
    @Recover
    public void recoverVideoEncoded(VideoEncodedEvent event, Exception e) {

        log.error(
                "FINAL FAILURE processing VideoEncodedEvent for movie: {} after 3 retries — error: {}",
                event.getMovieId(),
                e.getMessage(),
                e
        );
    }
}
