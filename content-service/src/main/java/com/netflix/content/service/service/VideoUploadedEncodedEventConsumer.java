package com.netflix.content.service.service;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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
	@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
	public void consumerVideoUploadedEvent(@Payload Map<String, Object> payload) {
		try {
			String movieId = (String) payload.get("movieId");
			String videoKey = (String) payload.get("videoKey");

			log.info("Video uploaded for movie: {} key: {}", movieId, videoKey);
			contentService.updateVideoKey(movieId, videoKey);
		} catch (Exception e) {
			log.error("Error processing video.uploaded event — {}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Consumes events from the "video.encoded" Kafka topic.
	 * Updates the content's HLS URL if the encoding was successful, or marks the video status as FAILED otherwise.
	 *
	 * @param payload a map containing event data, expected to include "movieId", "success", 
	 *                and either "hlsUrl" (if successful) or "errorMessage" (if failed)
	 */
	@KafkaListener(topics = "video.encoded")
	@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
	public void consumeVideoEncodedEvent(@Payload Map<String, Object> payload) {
		try {
			String movieId = (String) payload.get("movieId");
			String hlsUrl = (String) payload.get("hlsUrl");
			Boolean success = (Boolean) payload.get("success");

			if (Boolean.TRUE.equals(success)) {
				contentService.updateHlsUrl(movieId, hlsUrl);
			} else {
				String errorMessage = (String) payload.get("errorMessage");
				log.error("Encoding failed for movie: {} — {}", movieId, errorMessage);
				contentService.updateVideoStatus(movieId, VideoStatus.FAILED);
			}
		} catch (Exception e) {
			log.error("Error processing video.encoded event — {}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Recovery method invoked when all retry attempts are exhausted for video.uploaded events.
	 * Logs the failure so it can be manually investigated.
	 *
	 * @param payload the event payload that failed processing
	 * @param e       the last exception thrown
	 */
	@Recover
	public void recoverVideoUploaded(Map<String, Object> payload, Exception e) {
		log.error("FINAL FAILURE processing video.uploaded event after 3 retries — payload: {}, error: {}",
				payload, e.getMessage());
	}

	/**
	 * Recovery method invoked when all retry attempts are exhausted for video.encoded events.
	 * Logs the failure so it can be manually investigated.
	 *
	 * @param payload the event payload that failed processing
	 * @param e       the last exception thrown
	 */
	@Recover
	public void recoverVideoEncoded(Map<String, Object> payload, Exception e) {
		log.error("FINAL FAILURE processing video.encoded event after 3 retries — payload: {}, error: {}",
				payload, e.getMessage());
	}

}