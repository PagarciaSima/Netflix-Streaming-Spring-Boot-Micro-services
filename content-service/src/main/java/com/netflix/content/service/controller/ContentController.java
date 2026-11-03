package com.netflix.content.service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.content.service.dto.MovieRequest;
import com.netflix.content.service.dto.MovieResponse;
import com.netflix.content.service.model.Genre;
import com.netflix.content.service.service.ContentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Slf4j
public class ContentController {

    private final ContentService contentService;

    // Add new movie to catalog
    @PostMapping
    public ResponseEntity<MovieResponse> addMovie(
            @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contentService.addMovie(request));
    }

    // Get all movies
    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(contentService.getAllMovies());
    }

    // Get movie by ID
    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponse> getMovieById(
            @PathVariable String movieId) {
        return ResponseEntity.ok(contentService.getMovieById(movieId));
    }

    // Get movies by genre
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieResponse>> getMoviesByGenre(
            @PathVariable Genre genre) {
        return ResponseEntity.ok(contentService.getMoviesByGenre(genre));
    }

    // Search movies by title
    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMovies(
            @RequestParam String title) {
        return ResponseEntity.ok(contentService.searchMovies(title));
    }
}
