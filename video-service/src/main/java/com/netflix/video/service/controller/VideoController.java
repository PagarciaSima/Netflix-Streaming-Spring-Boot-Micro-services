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
@Tag(name = "Video", description = "Subida de vídeos y gestión de archivos (video-service)")
public class VideoController {

    private final VideoService videoService;

    /**
     * Upload video file for a movie.
     * Accepts multipart file upload.
     *
     * POST /api/v1/videos/upload/{movieId}
     *
     * @param movieId Identificador de la película a la que se asocia el vídeo.
     * @param file    Archivo de vídeo (multipart) a subir a S3.
     * @return Mensaje de confirmación con la clave S3 del vídeo subido.
     */
    @PostMapping("/upload/{movieId}")
    @Operation(summary = "Subir un vídeo", description = "Sube un archivo de vídeo a S3 y publica un evento Kafka para iniciar el encoding.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vídeo subido correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos o archivo vacío"),
            @ApiResponse(responseCode = "404", description = "Película no encontrada")
    })
    public ResponseEntity<String> uploadVideo(
            @Parameter(description = "ID de la película", required = true) @NotBlank @PathVariable String movieId,
            @Parameter(description = "Archivo de vídeo a subir", required = true) @NotNull @RequestParam("file") MultipartFile file) throws IOException {

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
