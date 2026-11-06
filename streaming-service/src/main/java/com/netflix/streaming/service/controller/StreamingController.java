package com.netflix.streaming.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.streaming.service.dto.StreamingResponse;
import com.netflix.streaming.service.service.StreamingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Streaming", description = "Reproducción de vídeo HLS con URLs firmadas (streaming-service)")
public class StreamingController {

	private final StreamingService streamingService;

	/**
	 * Get streaming URL for movie.
	 *
	 * @param movieId Identificador de la película.
	 * @return DTO con la URL presignada del master playlist HLS.
	 */
	// Get streaming URL for movie
	@GetMapping("/{movieId}")
	@Operation(summary = "Obtener URL de streaming", description = "Devuelve la URL presignada del master playlist HLS para una película.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "URL de streaming generada", content = @Content(schema = @Schema(implementation = StreamingResponse.class))),
			@ApiResponse(responseCode = "404", description = "Película no lista para streaming")
	})
	public ResponseEntity<StreamingResponse> getStreamingUrl(
			@Parameter(description = "ID de la película", required = true) @NotBlank @PathVariable String movieId) {

		try {
			StreamingResponse response = streamingService.getStreamingUrl(movieId);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Serve signed m3u8 playlist content. Called by HLS player for each quality
	 * playlist.
	 *
	 * GET
	 * /api/v1/stream/{movieId}/playlist?path=encoded/movieId/1080p/playlist.m3u8
	 *
	 * @param movieId Identificador de la película.
	 * @param path    Ruta del playlist en S3.
	 * @return Contenido del playlist m3u8 con URLs firmadas.
	 */
	@GetMapping("/{movieId}/playlist")
	@Operation(summary = "Obtener playlist firmado", description = "Devuelve el contenido del playlist m3u8 con URLs presignadas para cada segmento.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Playlist m3u8 con URLs firmadas"),
			@ApiResponse(responseCode = "404", description = "Playlist no encontrado")
	})
	public ResponseEntity<String> getSignedPlaylist(
			@Parameter(description = "ID de la película", required = true) @NotBlank @PathVariable String movieId,
			@Parameter(description = "Ruta del playlist en S3", required = true) @NotBlank @RequestParam String path) {

		log.info("Playlist request for movie: {} path: {}", movieId, path);

		String signedPlaylist = streamingService.getSignedPlaylist(movieId, path);

		return ResponseEntity.ok().header("Content-Type", "application/x-mpegURL").body(signedPlaylist);
	}
}