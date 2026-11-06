package com.netflix.streaming.service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.netflix.streaming.service.dto.StreamingResponse;
import com.netflix.streaming.service.service.StreamingService;

/**
 * Unit tests for {@link StreamingController}.
 * <p>
 * Tests the controller methods directly with a mocked {@link StreamingService},
 * without Spring's {@code @WebMvcTest}.
 */
@ExtendWith(MockitoExtension.class)
class StreamingControllerTest {

    @Mock
    private StreamingService streamingService;

    @InjectMocks
    private StreamingController streamingController;

    @Test
    void getStreamingUrl_shouldReturnOk() {
        StreamingResponse response = new StreamingResponse("movie-123",
                "https://cdn.example.com/master.m3u8", "1080p, 720p", 60L);

        when(streamingService.getStreamingUrl(eq("movie-123"))).thenReturn(response);

        ResponseEntity<StreamingResponse> result = streamingController.getStreamingUrl("movie-123");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("movie-123", result.getBody().getMovieId());
    }

    @Test
    void getStreamingUrl_shouldReturnNotFoundWhenMovieNotReady() {
        when(streamingService.getStreamingUrl(eq("unknown")))
                .thenThrow(new RuntimeException("Movie not ready for streaming: unknown"));

        ResponseEntity<StreamingResponse> result = streamingController.getStreamingUrl("unknown");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void getSignedPlaylist_shouldReturnOk() {
        when(streamingService.getSignedPlaylist(eq("movie-123"), any(String.class)))
                .thenReturn("#EXTM3U\nhttps://cdn.example.com/segment.ts");

        ResponseEntity<String> result = streamingController.getSignedPlaylist("movie-123",
                "encoded/movie-123/playlist.m3u8");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }
}