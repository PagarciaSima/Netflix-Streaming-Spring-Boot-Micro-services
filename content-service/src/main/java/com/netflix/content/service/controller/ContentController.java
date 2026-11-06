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
@Tag(name = "Content", description = "Operaciones del catálogo de películas (content-service)")
public class ContentController {

    private final ContentService contentService;

    /**
     * Add new movie to catalog.
     *
     * @param request DTO con los datos de la película a crear.
     * @return La película creada con su ID asignado y estado PENDING.
     */
    // Add new movie to catalog
    @PostMapping
    @Operation(summary = "Añadir una nueva película al catálogo", description = "Crea una película con estado PENDING. El vídeo aún no se ha subido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Película creada correctamente", content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (validación fallida)")
    })
    public ResponseEntity<MovieResponse> addMovie(
            @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contentService.addMovie(request));
    }

    /**
     * Get all movies.
     *
     * @return Lista de todas las películas del catálogo.
     */
    // Get all movies
    @GetMapping
    @Operation(summary = "Listar todas las películas", description = "Devuelve el catálogo completo de películas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de películas", content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontraron películas")
    })
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(contentService.getAllMovies());
    }

    /**
     * Get movie by ID.
     *
     * @param movieId Identificador único de la película.
     * @return La película solicitada.
     */
    // Get movie by ID
    @GetMapping("/{movieId}")
    @Operation(summary = "Obtener película por ID", description = "Devuelve los detalles de una película dada su identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Película encontrada", content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "404", description = "Película no encontrada")
    })
    public ResponseEntity<MovieResponse> getMovieById(
            @Parameter(description = "ID de la película", required = true) @PathVariable String movieId) {
        return ResponseEntity.ok(contentService.getMovieById(movieId));
    }

    /**
     * Get movies by genre.
     *
     * @param genre Género por el cual filtrar.
     * @return Lista de películas del género indicado.
     */
    // Get movies by genre
    @GetMapping("/genre/{genre}")
    @Operation(summary = "Obtener películas por género", description = "Devuelve todas las películas que coincidan con el género especificado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de películas del género", content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontraron películas para ese género")
    })
    public ResponseEntity<List<MovieResponse>> getMoviesByGenre(
            @Parameter(description = "Género de la película", required = true) @PathVariable Genre genre) {
        return ResponseEntity.ok(contentService.getMoviesByGenre(genre));
    }

    /**
     * Search movies by title.
     *
     * @param title Parte del título a buscar (búsqueda insensible a mayúsculas).
     * @return Lista de películas cuyo título contiene el texto dado.
     */
    // Search movies by title
    @GetMapping("/search")
    @Operation(summary = "Buscar películas por título", description = "Realiza una búsqueda de películas cuyo título contenga la cadena indicada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de películas encontradas", content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "400", description = "El parámetro title es obligatorio")
    })
    public ResponseEntity<List<MovieResponse>> searchMovies(
            @Parameter(description = "Texto a buscar en el título", required = true) @RequestParam String title) {
        return ResponseEntity.ok(contentService.searchMovies(title));
    }
}
