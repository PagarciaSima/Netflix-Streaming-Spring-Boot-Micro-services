package com.netflix.video.service.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.netflix.video.service.service.VideoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Video", description = "Video upload and file management (video-service)")
public class VideoController {

    private final VideoService videoService;

    /**
     * Upload video file for a movie.
     * Accepts multipart file upload.
     *
     * POST /api/v1/videos/upload/{movieId}
     *
     * @param movieId Identifier of the movie to associate the video with.
     * @param file    Video file (multipart) to upload to S3.
     * @return Confirmation message with the S3 key of the uploaded video.
     */
    @PostMapping("/upload/{movieId}")
    @Operation(summary = "Upload a video", description = "Uploads a video file to S3 and publishes a Kafka event to start encoding.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters or empty file"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<String> uploadVideo(
            @Parameter(description = "Movie ID", required = true) @NotBlank @PathVariable String movieId,
            @Parameter(description = "Video file to upload", required = true) @NotNull @RequestParam("file") MultipartFile file) throws IOException {

        log.info("Video upload request for movie: {} file size: {}MB",
                movieId, file.getSize() / (1024 * 1024));

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String videoKey = videoService.uploadVideo(movieId, file);

        return ResponseEntity.ok(
                "Video uploaded successfully! Key: " + videoKey +
                " — Encoding started automatically via Kafka"
        );
    }
}
