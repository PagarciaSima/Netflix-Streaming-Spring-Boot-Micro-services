package com.netflix.content.service.dto;

import com.netflix.content.service.model.Genre;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieRequest {

	@NotBlank(message = "Title is required")
	@Size(max = 255, message = "Title must not exceed 255 characters")
	private String title;

	@Size(max = 1000, message = "Description must not exceed 1000 characters")
	private String description;

	@NotNull(message = "Genre is required")
	private Genre genre;

	@Size(max = 255, message = "Director must not exceed 255 characters")
	private String director;

	@Size(max = 255, message = "Cast must not exceed 255 characters")
	private String cast;

	@Min(value = 1900, message = "Release year must be 1900 or later")
	@Max(value = 2100, message = "Release year must not exceed 2100")
	private int releaseYear;

	@DecimalMin(value = "0.0", inclusive = true, message = "Rating must be between 0 and 10")
	@DecimalMax(value = "10.0", inclusive = true, message = "Rating must be between 0 and 10")
	private double rating;

	@Size(max = 500, message = "Thumbnail URL must not exceed 500 characters")
	private String thumbnailUrl;

	@Min(value = 1, message = "Duration must be at least 1 minute")
	private int durationMinutes;
}