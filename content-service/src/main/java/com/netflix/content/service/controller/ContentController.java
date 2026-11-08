package com.netflix.content.service.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Content", description = "Content catalog operations (content-service)")
public class ContentController {

    private final ContentService contentService;

    /**
     * Add new movie to catalog.
     *
     * @param request DTO with the data of the movie to create.
     * @return The created movie with its assigned ID and PENDING status.
     */
    // Add new movie to catalog
    @PostMapping
    @Operation(summary = "Add a new movie to the catalog", description = "Creates a movie with PENDING status. The video has not been uploaded yet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movie created successfully", content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data (validation failed)")
    })
    @CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
    public ResponseEntity<MovieResponse> addMovie(
            @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS")
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                .body(contentService.addMovie(request));
    }

    /**
     * Get all movies.
     *
     * @return List of all movies in the catalog.
     */
    // Get all movies
    @GetMapping
    @Operation(summary = "List all movies", description = "Returns the complete movie catalog.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of movies", content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "404", description = "No movies found")
    })
    @CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok()
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS")
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                .body(contentService.getAllMovies());
    }

    /**
     * Get movie by ID.
     *
     * @param movieId Unique identifier of the movie.
     * @return The requested movie.
     */
    // Get movie by ID
    @GetMapping("/{movieId}")
    @Operation(summary = "Get movie by ID", description = "Returns the details of a movie given its identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movie found", content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    @CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
    public ResponseEntity<MovieResponse> getMovieById(
            @Parameter(description = "Movie ID", required = true) @PathVariable String movieId) {
        return ResponseEntity.ok(contentService.getMovieById(movieId));
    }

    /**
     * Get movies by genre.
     *
     * @param genre Genre to filter by.
     * @return List of movies of the indicated genre.
     */
    // Get movies by genre
    @GetMapping("/genre/{genre}")
    @Operation(summary = "Get movies by genre", description = "Returns all movies that match the specified genre.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of movies of the genre", content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "404", description = "No movies found for that genre")
    })
    @CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
    public ResponseEntity<List<MovieResponse>> getMoviesByGenre(
            @Parameter(description = "Movie genre", required = true) @PathVariable Genre genre) {
        return ResponseEntity.ok(contentService.getMoviesByGenre(genre));
    }

    /**
     * Search movies by title.
     *
     * @param title Part of the title to search (case-insensitive search).
     * @return List of movies whose title contains the given text.
     */
    // Search movies by title
    @GetMapping("/search")
    @Operation(summary = "Search movies by title", description = "Performs a search of movies whose title contains the indicated string.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of found movies", content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "400", description = "The title parameter is required")
    })
    @CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
    public ResponseEntity<List<MovieResponse>> searchMovies(
            @Parameter(description = "Texto a buscar en el título", required = true) @RequestParam String title) {
        return ResponseEntity.ok(contentService.searchMovies(title));
    }
}
