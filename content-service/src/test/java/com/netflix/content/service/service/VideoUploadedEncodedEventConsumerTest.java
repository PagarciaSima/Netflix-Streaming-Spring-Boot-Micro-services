package com.netflix.content.service.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.netflix.content.service.model.VideoStatus;

/**
 * Unit tests for {@link VideoUploadedEncodedEventConsumer}.
 * <p>
 * Verifies that Kafka events trigger the correct {@link ContentService}
 * update methods for both upload and encoding completion (success/failure).
 */
@ExtendWith(MockitoExtension.class)
class VideoUploadedEncodedEventConsumerTest {

    @Mock
    private ContentService contentService;

    @InjectMocks
    private VideoUploadedEncodedEventConsumer consumer;

    @Test
    void consumerVideoUploadedEvent_shouldCallUpdateVideoKey() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("movieId", "movie-123");
        payload.put("videoKey", "raw/movie-123/video.mp4");

        consumer.consumerVideoUploadedEvent(payload);

        verify(contentService).updateVideoKey("movie-123", "raw/movie-123/video.mp4");
    }

    @Test
    void consumeVideoEncodedEvent_shouldCallUpdateHlsUrlOnSuccess() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("movieId", "movie-123");
        payload.put("hlsUrl", "https://cdn.example.com/master.m3u8");
        payload.put("success", true);

        consumer.consumeVideoEncodedEvent(payload);

        verify(contentService).updateHlsUrl("movie-123", "https://cdn.example.com/master.m3u8");
    }

    @Test
    void consumeVideoEncodedEvent_shouldCallUpdateVideoStatusFailedOnFailure() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("movieId", "movie-123");
        payload.put("success", false);
        payload.put("errorMessage", "FFmpeg exited with code 1");

        consumer.consumeVideoEncodedEvent(payload);

        verify(contentService).updateVideoStatus("movie-123", VideoStatus.FAILED);
    }
}