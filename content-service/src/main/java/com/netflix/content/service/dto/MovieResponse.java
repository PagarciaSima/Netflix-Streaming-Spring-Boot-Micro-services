package com.netflix.content.service.dto;

import java.time.LocalDateTime;

import com.netflix.content.service.model.Genre;
import com.netflix.content.service.model.VideoStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieResponse {
	private String id;
	private String title;
	private String description;
	private Genre genre;
	private String director;
	private String cast;
	private int releaseYear;
	private double rating;
	private String thumbnailUrl;
	private int durationMinutes;
	private String videoKey;
	private VideoStatus videoStatus;
	private String hlsUrl;
	private LocalDateTime createdAt;
}