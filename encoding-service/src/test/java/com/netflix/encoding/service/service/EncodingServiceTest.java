package com.netflix.encoding.service.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.netflix.encoding.service.event.VideoEncodedEvent;
import com.netflix.encoding.service.event.VideoUploadedEvent;

import software.amazon.awssdk.services.s3.S3Client;

/**
 * Unit tests for {@link EncodingService}.
 * <p>
 * Uses a test subclass that overrides {@code encodeToHLS} to avoid running
 * FFmpeg. Verifies the full pipeline: download from S3, simulated encoding,
 * upload to S3, and Kafka event publishing.
 */
@ExtendWith(MockitoExtension.class)
class EncodingServiceTest {

    static class TestEncodingService extends EncodingService {

        protected TestEncodingService(S3Client s3Client,
                                      KafkaTemplate<String, VideoEncodedEvent> kafkaTemplate) {
            super(s3Client, kafkaTemplate);
        }

        @Override
        protected void encodeToHLS(String inputPath, String outputDir,
                                   int width, int height, int bitrate) throws IOException {
            // Simulate FFmpeg: create an empty playlist file
            java.nio.file.Path playlist = java.nio.file.Paths.get(outputDir, "playlist.m3u8");
            Files.createDirectories(playlist.getParent());
            Files.writeString(playlist, "#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=1000\n");
        }
    }

    @Mock
    private S3Client s3Client;

    @Mock
    private KafkaTemplate<String, VideoEncodedEvent> kafkaTemplate;

    @Test
    void encodeVideo_shouldCompletePipelineAndPublishEvent() throws Exception {
        VideoUploadedEvent event = new VideoUploadedEvent();
        event.setMovieId("movie-123");
        event.setVideoKey("raw/movie-123/video.mp4");
        event.setBucketName("test-bucket");
        event.setOriginalFileName("video.mp4");
        event.setFileSizeBytes(1000L);

        TestEncodingService service = new TestEncodingService(s3Client, kafkaTemplate);
        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(service, "ffmpegPath", "/usr/bin/ffmpeg");
        ReflectionTestUtils.setField(service, "tempDir",
                Files.createTempDirectory("encoding-test").toString());

        assertDoesNotThrow(() -> service.encodeVideo(event));

        verify(kafkaTemplate).send(eq("video.encoded"), any(String.class), any(VideoEncodedEvent.class));
    }

    @Test
    void encodeVideo_withNullEvent_shouldThrow() {
        TestEncodingService service = new TestEncodingService(s3Client, kafkaTemplate);
        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(service, "ffmpegPath", "/usr/bin/ffmpeg");
        ReflectionTestUtils.setField(service, "tempDir", System.getProperty("java.io.tmpdir"));

        assertThrows(NullPointerException.class, () -> service.encodeVideo(null));
    }
}