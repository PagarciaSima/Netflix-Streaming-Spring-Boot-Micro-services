package com.netflix.encoding.service.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.netflix.encoding.service.event.VideoUploadedEvent;

/**
 * Unit tests for {@link VideoEventConsumer}.
 * <p>
 * Verifies that the Kafka consumer delegates to {@link EncodingService#encodeVideo}
 * and catches exceptions without propagating them.
 */
@ExtendWith(MockitoExtension.class)
class VideoEventConsumerTest {

    @Mock
    private EncodingService encodingService;

    @InjectMocks
    private VideoEventConsumer consumer;

    @Test
    void consumeVideoUploadedEvent_shouldDelegateToEncodingService() {
        VideoUploadedEvent event = new VideoUploadedEvent();
        event.setMovieId("movie-123");
        event.setVideoKey("raw/movie-123/video.mp4");
        event.setBucketName("bucket");
        event.setOriginalFileName("video.mp4");
        event.setFileSizeBytes(1000L);

        consumer.consumeVideoUploadedEvent(event);

        verify(encodingService).encodeVideo(event);
    }

    @Test
    void consumeVideoUploadedEvent_shouldCatchExceptionAndNotPropagate() {
        VideoUploadedEvent event = new VideoUploadedEvent();
        event.setMovieId("movie-123");

        doThrow(new RuntimeException("FFmpeg failed")).when(encodingService).encodeVideo(event);

        // Should not throw
        consumer.consumeVideoUploadedEvent(event);

        verify(encodingService).encodeVideo(event);
    }
}