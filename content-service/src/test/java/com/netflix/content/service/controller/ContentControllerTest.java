package com.netflix.content.service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.netflix.content.service.dto.MovieRequest;
import com.netflix.content.service.dto.MovieResponse;
import com.netflix.content.service.model.Genre;
import com.netflix.content.service.model.VideoStatus;
import com.netflix.content.service.service.ContentService;

/**
 * Unit tests for {@link ContentController}.
 * <p>
 * Tests the controller methods directly with a mocked {@link ContentService},
 * without Spring's {@code @WebMvcTest} (which requires spring-boot-starter-test).
 */
@ExtendWith(MockitoExtension.class)
class ContentControllerTest {

    @Mock
    private ContentService contentService;

    @InjectMocks
    private ContentController contentController;

    @Test
    void addMovie_shouldReturnCreated() {
        MovieRequest request = new MovieRequest();
        request.setTitle("New Movie");
        request.setGenre(Genre.ACTION);
        request.setReleaseYear(2023);
        request.setRating(7.5);
        request.setDurationMinutes(100);

        MovieResponse response = new MovieResponse();
        response.setId("movie-1");
        response.setTitle("New Movie");
        response.setVideoStatus(VideoStatus.PENDING);

        when(contentService.addMovie(any(MovieRequest.class))).thenReturn(response);

        ResponseEntity<MovieResponse> result = contentController.addMovie(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("movie-1", result.getBody().getId());
        assertEquals("New Movie", result.getBody().getTitle());
        assertEquals(VideoStatus.PENDING, result.getBody().getVideoStatus());
    }

    @Test
    void getAllMovies_shouldReturnList() {
        MovieResponse response = new MovieResponse();
        response.setId("movie-1");
        response.setTitle("Movie 1");

        when(contentService.getAllMovies()).thenReturn(List.of(response));

        ResponseEntity<List<MovieResponse>> result = contentController.getAllMovies();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals("movie-1", result.getBody().get(0).getId());
        assertEquals("Movie 1", result.getBody().get(0).getTitle());
    }

    @Test
    void getMovieById_shouldReturnMovie() {
        MovieResponse response = new MovieResponse();
        response.setId("movie-1");
        response.setTitle("Movie 1");

        when(contentService.getMovieById("movie-1")).thenReturn(response);

        ResponseEntity<MovieResponse> result = contentController.getMovieById("movie-1");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("movie-1", result.getBody().getId());
        assertEquals("Movie 1", result.getBody().getTitle());
    }

    @Test
    void getMovieById_shouldThrowWhenNotFound() {
        when(contentService.getMovieById("unknown"))
                .thenThrow(new RuntimeException("Movie not found: unknown"));

        assertThrows(RuntimeException.class,
                () -> contentController.getMovieById("unknown"));
    }

    @Test
    void getMoviesByGenre_shouldReturnFilteredList() {
        MovieResponse response = new MovieResponse();
        response.setId("movie-1");
        response.setGenre(Genre.ACTION);

        when(contentService.getMoviesByGenre(Genre.ACTION)).thenReturn(List.of(response));

        ResponseEntity<List<MovieResponse>> result = contentController.getMoviesByGenre(Genre.ACTION);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(Genre.ACTION, result.getBody().get(0).getGenre());
    }

    @Test
    void searchMovies_shouldReturnMatchingResults() {
        MovieResponse response = new MovieResponse();
        response.setId("movie-1");
        response.setTitle("Test Movie");

        when(contentService.searchMovies("Test")).thenReturn(List.of(response));

        ResponseEntity<List<MovieResponse>> result = contentController.searchMovies("Test");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals("Test Movie", result.getBody().get(0).getTitle());
    }
}