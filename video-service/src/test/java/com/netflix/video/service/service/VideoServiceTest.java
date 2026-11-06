package com.netflix.video.service.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.netflix.video.service.event.VideoUploadedEvent;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import org.springframework.kafka.core.KafkaTemplate;

/**
 * Unit tests for {@link VideoService}.
 * <p>
 * Covers video upload to S3 and Kafka event publishing with mocked
 * {@link S3Client} and {@link KafkaTemplate}.
 */
@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private KafkaTemplate<String, VideoUploadedEvent> kafkaTemplate;

    @InjectMocks
    private VideoService videoService;

    @Test
    void uploadVideo_shouldUploadToS3AndPublishEvent() throws IOException {
        String movieId = "movie-123";
        MultipartFile file = new MockMultipartFile("file", "video.mp4",
                "video/mp4", "fake video content".getBytes());

        when(kafkaTemplate.send(any(String.class), any(String.class), any(VideoUploadedEvent.class)))
                .thenReturn(null);

        String videoKey = videoService.uploadVideo(movieId, file);

        assertNotNull(videoKey);
        assertDoesNotThrow(() -> {
            if (videoKey == null || videoKey.isEmpty()) {
                throw new AssertionError("videoKey should not be empty");
            }
        });
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(kafkaTemplate).send(eq("video.uploaded"), eq(movieId), any(VideoUploadedEvent.class));
    }

    @Test
    void uploadVideo_shouldThrowWhenS3Fails() throws IOException {
        String movieId = "movie-123";
        MultipartFile file = new MockMultipartFile("file", "video.mp4",
                "video/mp4", "content".getBytes());

        // Mock the S3 putObject to throw by making the internal call fail
        // Since S3Client.putObject doesn't declare checked exceptions,
        // we simulate by having uploadVideo throw via a different mechanism.
        // Instead, verify that uploadVideo propagates S3Client errors.
        doThrow(new RuntimeException("S3 error")).when(s3Client)
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThrows(RuntimeException.class, () -> videoService.uploadVideo(movieId, file));
    }
}