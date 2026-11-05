package com.netflix.encoding.service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.netflix.encoding.service.event.VideoUploadedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoEventConsumer {

    private final EncodingService encodingService;

    /**
     * Listens to video.uploaded Kafka topic.
     * Triggered when Video Service uploads a raw video to S3.
     *
     * Flow:
     * Video Service → S3 upload → Kafka (video.uploaded)
     *                                      ↓
     *                             This consumer
     *                                      ↓
     *                             EncodingService → FFmpeg → S3
     *                                      ↓
     *                             Kafka (video.encoded)
     */
    @KafkaListener(
            topics = "video.uploaded",
            groupId = "encoding-service-group"
    )
    public void consumeVideoUploadedEvent(VideoUploadedEvent event) {
        log.info("Consumed VideoUploadedEvent for movie: {} file: {}",
                event.getMovieId(), event.getOriginalFileName());

        try {
            encodingService.encodeVideo(event);
        } catch (Exception e) {
            log.error("Failed to process encoding for movie: {} — {}",
                    event.getMovieId(), e.getMessage());
        }
    }
}
