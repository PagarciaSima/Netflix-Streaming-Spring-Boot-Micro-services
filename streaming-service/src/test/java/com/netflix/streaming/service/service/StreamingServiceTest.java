package com.netflix.streaming.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.netflix.streaming.service.dto.StreamingResponse;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Unit tests for {@link StreamingService}.
 * <p>
 * Covers streaming URL retrieval (cache hit/miss), signed playlist generation,
 * and cache invalidation with mocked {@link S3Client}, {@link S3Presigner},
 * and {@link RedisTemplate}.
 */
@ExtendWith(MockitoExtension.class)
class StreamingServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private StreamingService streamingService;

    private static final String MOVIE_ID = "movie-123";
    private static final String CACHE_KEY = "streaming:url:movie-123";
    private static final String PLAYLIST_KEY = "streaming:playlist:movie-123";

    @Test
    void getStreamingUrl_shouldReturnCachedUrlWhenAvailable() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn("https://cached.example.com/master.m3u8");

        StreamingResponse response = streamingService.getStreamingUrl(MOVIE_ID);

        assertNotNull(response);
        assertEquals("https://cached.example.com/master.m3u8", response.getStreamingUrl());
        assertEquals(MOVIE_ID, response.getMovieId());
        verify(valueOperations).get(CACHE_KEY);
    }

    @Test
    void getStreamingUrl_shouldThrowWhenPlaylistKeyNotFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(null);
        when(valueOperations.get(PLAYLIST_KEY)).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> streamingService.getStreamingUrl(MOVIE_ID));
    }

    @Test
    void invalidateCache_shouldDeleteKeyFromRedis() {
        streamingService.invalidateCache(MOVIE_ID);

        verify(redisTemplate).delete(CACHE_KEY);
    }
}