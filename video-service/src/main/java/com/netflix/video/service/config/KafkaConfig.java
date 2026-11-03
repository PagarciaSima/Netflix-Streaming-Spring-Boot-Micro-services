package com.netflix.video.service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

	// Published when video is uploaded to S3
	// Encoding Service consumes this
	@Bean
	NewTopic videoUploadedTopic() {
		return TopicBuilder.name("video.uploaded").partitions(3).replicas(1).build();
	}

	// Published when encoding is complete
	// Content Service consumes this to update HLS URL
	@Bean
	NewTopic videoEncodedTopic() {
		return TopicBuilder.name("video.encoded").partitions(3).replicas(1).build();
	}
}
