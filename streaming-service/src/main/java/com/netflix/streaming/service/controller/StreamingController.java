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
@Tag(name = "Streaming", description = "HLS video playback with signed URLs (streaming-service)")
public class StreamingController {

	private final StreamingService streamingService;

	/**
	 * Get streaming URL for movie.
	 *
	 * @param movieId Movie identifier.
	 * @return DTO with the signed URL of the HLS master playlist.
	 */
	// Get streaming URL for movie
	@GetMapping("/{movieId}")
	@Operation(summary = "Get streaming URL", description = "Returns the signed URL of the HLS master playlist for a movie.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Streaming URL generated", content = @Content(schema = @Schema(implementation = StreamingResponse.class))),
			@ApiResponse(responseCode = "404", description = "Movie not ready for streaming")
	})
	public ResponseEntity<StreamingResponse> getStreamingUrl(
			@Parameter(description = "Movie ID", required = true) @NotBlank @PathVariable String movieId) {

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
	 * @param movieId Movie identifier.
	 * @param path    Path of the playlist in S3.
	 * @return Content of m3u8 playlist with signed URLs.
	 */
	@GetMapping("/{movieId}/playlist")
	@Operation(summary = "Get signed playlist", description = "Returns the content of the m3u8 playlist with presigned URLs for each segment.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "m3u8 playlist with signed URLs"),
			@ApiResponse(responseCode = "404", description = "Playlist not found")
	})
	public ResponseEntity<String> getSignedPlaylist(
			@Parameter(description = "Movie ID", required = true) @NotBlank @PathVariable String movieId,
			@Parameter(description = "Path of the playlist in S3", required = true) @NotBlank @RequestParam String path) {

		log.info("Playlist request for movie: {} path: {}", movieId, path);

		String signedPlaylist = streamingService.getSignedPlaylist(movieId, path);

		return ResponseEntity.ok().header("Content-Type", "application/x-mpegURL").body(signedPlaylist);
	}
}