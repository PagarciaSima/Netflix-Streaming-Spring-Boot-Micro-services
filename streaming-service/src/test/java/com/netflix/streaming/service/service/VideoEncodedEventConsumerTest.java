package com.netflix.streaming.service.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.netflix.streaming.service.event.VideoEncodedEvent;


/**
 * Unit tests for {@link VideoEncodedEventConsumer}.
 * <p>
 * Verifies that encoding success stores the master playlist key in Redis
 * and that failure does not.
 */
@ExtendWith(MockitoExtension.class)
class VideoEncodedEventConsumerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private VideoEncodedEventConsumer consumer;

    @Test
    void consumeVideoEncodedEvent_shouldStoreKeyOnSuccess() {
        VideoEncodedEvent event = new VideoEncodedEvent();
        event.setMovieId("movie-123");
        event.setSuccess(true);
        event.setMasterPlaylistKey("encoded/movie-123/master.m3u8");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        consumer.consumeVideoEncodedEvent(event);

        verify(valueOperations).set("streaming:playlist:movie-123",
                "encoded/movie-123/master.m3u8");
    }

    @Test
    void consumeVideoEncodedEvent_shouldNotStoreKeyOnFailure() {
        VideoEncodedEvent event = new VideoEncodedEvent();
        event.setMovieId("movie-123");
        event.setSuccess(false);
        event.setErrorMessage("FFmpeg failed");

        consumer.consumeVideoEncodedEvent(event);

        verify(redisTemplate, never()).opsForValue();
    }
}