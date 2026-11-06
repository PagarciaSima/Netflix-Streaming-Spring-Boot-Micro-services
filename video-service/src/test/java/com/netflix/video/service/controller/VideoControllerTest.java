package com.netflix.video.service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.netflix.video.service.service.VideoService;

/**
 * Unit tests for {@link VideoController}.
 * <p>
 * Tests the controller method directly with a mocked {@link VideoService},
 * without Spring's {@code @WebMvcTest}.
 */
@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    @Mock
    private VideoService videoService;

    @InjectMocks
    private VideoController videoController;

    @Test
    void uploadVideo_shouldReturnOkWhenFileIsValid() throws Exception {
        MultipartFile file = new MockMultipartFileImpl("file", "video.mp4",
                "video/mp4", "fake video content".getBytes());

        when(videoService.uploadVideo(eq("movie-123"), any()))
                .thenReturn("raw/movie-123/uuid_video.mp4");

        ResponseEntity<String> result = videoController.uploadVideo("movie-123", file);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void uploadVideo_shouldReturnBadRequestWhenFileIsEmpty() throws Exception {
        MultipartFile emptyFile = new MockMultipartFileImpl("file", "video.mp4",
                "video/mp4", new byte[0]);

        ResponseEntity<String> result = videoController.uploadVideo("movie-123", emptyFile);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("File is empty", result.getBody());
    }

    /** Simple MockMultipartFile implementation for unit testing without Spring Test. */
    static class MockMultipartFileImpl implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        MockMultipartFileImpl(String name, String originalFilename,
                              String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content == null ? 0 : content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) {
            // no-op for testing
        }
    }
}