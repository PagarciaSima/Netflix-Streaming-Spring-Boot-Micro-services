package com.netflix.content.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.netflix.content.service.dto.MovieRequest;
import com.netflix.content.service.dto.MovieResponse;
import com.netflix.content.service.model.Genre;
import com.netflix.content.service.model.Movie;
import com.netflix.content.service.model.VideoStatus;
import com.netflix.content.service.repository.MovieRepository;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private ContentService contentService;

    private MovieRequest request;
    private Movie movie;

    @BeforeEach
    void setUp() {
        request = new MovieRequest();
        request.setTitle("Test Movie");
        request.setDescription("A great movie");
        request.setGenre(Genre.ACTION);
        request.setDirector("Director Name");
        request.setCast("Actor A, Actor B");
        request.setReleaseYear(2023);
        request.setRating(8.5);
        request.setThumbnailUrl("http://example.com/thumb.jpg");
        request.setDurationMinutes(120);

        movie = new Movie();
        movie.setId("movie-123");
        movie.setTitle("Test Movie");
        movie.setGenre(Genre.ACTION);
        movie.setVideoStatus(VideoStatus.PENDING);
    }

    @Test
    void searchMovies_shouldReturnMatchingTitles() {
        when(movieRepository.findByTitleContainingIgnoreCase("test"))
                .thenReturn(List.of(movie));

        List<MovieResponse> responses = contentService.searchMovies("test");

        assertEquals(1, responses.size());
        assertEquals("Test Movie", responses.get(0).getTitle());
        verify(movieRepository).findByTitleContainingIgnoreCase("test");
    }

    @Test
    void updateVideoKey_shouldUpdateStatusToUploaded() {
        when(movieRepository.findById("movie-123")).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        contentService.updateVideoKey("movie-123", "raw/movie-123/uuid.mp4");

        assertEquals("raw/movie-123/uuid.mp4", movie.getVideoKey());
        assertEquals(VideoStatus.UPLOADED, movie.getVideoStatus());
        verify(movieRepository).save(movie);
    }

    @Test
    void updateVideoKey_shouldThrowWhenNotFound() {
        when(movieRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> contentService.updateVideoKey("unknown", "key"));
    }

    @Test
    void updateHlsUrl_shouldUpdateStatusToReady() {
        when(movieRepository.findById("movie-123")).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        contentService.updateHlsUrl("movie-123", "https://cdn.example.com/master.m3u8");

        assertEquals("https://cdn.example.com/master.m3u8", movie.getHlsUrl());
        assertEquals(VideoStatus.READY, movie.getVideoStatus());
        verify(movieRepository).save(movie);
    }

    @Test
    void updateHlsUrl_shouldThrowWhenNotFound() {
        when(movieRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> contentService.updateHlsUrl("unknown", "url"));
    }

    @Test
    void updateVideoStatus_shouldUpdateStatus() {
        when(movieRepository.findById("movie-123")).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        contentService.updateVideoStatus("movie-123", VideoStatus.FAILED);

        assertEquals(VideoStatus.FAILED, movie.getVideoStatus());
        verify(movieRepository).save(movie);
    }

    @Test
    void updateVideoStatus_shouldThrowWhenNotFound() {
        when(movieRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> contentService.updateVideoStatus("unknown", VideoStatus.FAILED));
    }

    @Test
    void addMovie_shouldSaveAndReturnResponse() {
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        MovieResponse response = contentService.addMovie(request);

        assertNotNull(response);
        assertEquals("movie-123", response.getId());
        assertEquals("Test Movie", response.getTitle());
        assertEquals(VideoStatus.PENDING, response.getVideoStatus());
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void getAllMovies_shouldReturnMappedList() {
        when(movieRepository.findAll()).thenReturn(List.of(movie));

        List<MovieResponse> responses = contentService.getAllMovies();

        assertEquals(1, responses.size());
        assertEquals("movie-123", responses.get(0).getId());
        verify(movieRepository).findAll();
    }

    @Test
    void getMovieById_shouldReturnMovieWhenFound() {
        when(movieRepository.findById("movie-123")).thenReturn(Optional.of(movie));

        MovieResponse response = contentService.getMovieById("movie-123");

        assertNotNull(response);
        assertEquals("movie-123", response.getId());
        verify(movieRepository).findById("movie-123");
    }

    @Test
    void getMovieById_shouldThrowWhenNotFound() {
        when(movieRepository.findById("unknown")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> contentService.getMovieById("unknown"));

        assertEquals("Movie not found: unknown", ex.getMessage());
    }
}