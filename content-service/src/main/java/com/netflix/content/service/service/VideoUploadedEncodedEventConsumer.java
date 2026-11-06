package com.netflix.content.service.service;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.netflix.content.service.model.VideoStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka event consumer responsible for processing video lifecycle events,
 * specifically handling video upload and encoding notifications.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoUploadedEncodedEventConsumer {

	private final ContentService contentService;

	/**
	 * Consumes events from the "video.uploaded" Kafka topic.
	 * Extracts the movie ID and video key from the payload and updates the corresponding content record.
	 *
	 * @param payload a map containing event data, expected to include "movieId" and "videoKey"
	 */
	@KafkaListener(topics = "video.uploaded")
	public void consumerVideoUploadedEvent(@Payload Map<String, Object> payload) {
		String movieId = (String) payload.get("movieId");
		String videoKey = (String) payload.get("videoKey");

		log.info("Video uploaded for movie: {} key: {}", movieId, videoKey);
		contentService.updateVideoKey(movieId, videoKey);
	}

	/**
	 * Consumes events from the "video.encoded" Kafka topic.
	 * Updates the content's HLS URL if the encoding was successful, or marks the video status as FAILED otherwise.
	 *
	 * @param payload a map containing event data, expected to include "movieId", "success", 
	 *                and either "hlsUrl" (if successful) or "errorMessage" (if failed)
	 */
	@KafkaListener(topics = "video.encoded")
	public void consumeVideoEncodedEvent(@Payload Map<String, Object> payload) {
		String movieId = (String) payload.get("movieId");
		String hlsUrl = (String) payload.get("hlsUrl");
		boolean success = (Boolean) payload.get("success");

		if (success) {
			contentService.updateHlsUrl(movieId, hlsUrl);
		} else {
			String errorMessage = (String) payload.get("errorMessage");
		log.error("Encoding failed for movie: {} — {}", movieId, errorMessage);
			contentService.updateVideoStatus(movieId, VideoStatus.FAILED);
		}
	}

}