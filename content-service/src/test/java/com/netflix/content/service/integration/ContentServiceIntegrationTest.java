package com.netflix.content.service.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.netflix.content.service.dto.MovieRequest;
import com.netflix.content.service.dto.MovieResponse;
import com.netflix.content.service.model.Genre;
import com.netflix.content.service.model.VideoStatus;
import com.netflix.content.service.repository.MovieRepository;
import com.netflix.content.service.service.ContentService;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
class ContentServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("content_db")
            .withUsername("root")
            .withPassword("testpassword");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ContentService contentService;

    @AfterEach
    void tearDown() {
        movieRepository.deleteAll();
    }

    @Test
    void addMovie_shouldPersistInMySQL() {
        MovieRequest request = new MovieRequest();
        request.setTitle("Integration Test Movie");
        request.setDescription("A movie for integration testing");
        request.setGenre(Genre.ACTION);
        request.setDirector("Test Director");
        request.setCast("Actor A, Actor B");
        request.setReleaseYear(2024);
        request.setRating(8.0);
        request.setThumbnailUrl("http://example.com/thumb.jpg");
        request.setDurationMinutes(120);

        MovieResponse response = contentService.addMovie(request);

        assertNotNull(response.getId());
        assertEquals("Integration Test Movie", response.getTitle());
        assertEquals(VideoStatus.PENDING, response.getVideoStatus());

        assertEquals(1, movieRepository.count());
        var saved = movieRepository.findById(response.getId()).orElseThrow();
        assertEquals("Integration Test Movie", saved.getTitle());
        assertEquals(Genre.ACTION, saved.getGenre());
    }

    @Test
    void getAllMovies_shouldReturnPersistedMovies() {
        MovieRequest request = new MovieRequest();
        request.setTitle("Movie A");
        request.setGenre(Genre.COMEDY);
        request.setReleaseYear(2023);
        request.setRating(7.0);
        request.setDurationMinutes(90);

        contentService.addMovie(request);

        List<MovieResponse> allMovies = contentService.getAllMovies();

        assertNotNull(allMovies);
        assertEquals(1, allMovies.size());
        assertEquals("Movie A", allMovies.get(0).getTitle());
    }
    @Test
    void updateVideoKey_shouldUpdateStatusToUploaded() {
        MovieRequest request = new MovieRequest();
        request.setTitle("Status Test Movie");
        request.setGenre(Genre.DRAMA);
        request.setReleaseYear(2022);
        request.setRating(6.5);
        request.setDurationMinutes(100);

        MovieResponse response = contentService.addMovie(request);
        String movieId = response.getId();

        contentService.updateVideoKey(movieId, "raw/movie-id/video.mp4");

        var saved = movieRepository.findById(movieId).orElseThrow();
        assertEquals("raw/movie-id/video.mp4", saved.getVideoKey());
        assertEquals(VideoStatus.UPLOADED, saved.getVideoStatus());
    }

    @Test
    void updateHlsUrl_shouldUpdateStatusToReady() {
        MovieRequest request = new MovieRequest();
        request.setTitle("HLS Test Movie");
        request.setGenre(Genre.THRILLER);
        request.setReleaseYear(2021);
        request.setRating(9.0);
        request.setDurationMinutes(110);

        MovieResponse response = contentService.addMovie(request);
        String movieId = response.getId();

        contentService.updateHlsUrl(movieId, "https://cdn.example.com/master.m3u8");

        var saved = movieRepository.findById(movieId).orElseThrow();
        assertEquals("https://cdn.example.com/master.m3u8", saved.getHlsUrl());
        assertEquals(VideoStatus.READY, saved.getVideoStatus());
    }

}