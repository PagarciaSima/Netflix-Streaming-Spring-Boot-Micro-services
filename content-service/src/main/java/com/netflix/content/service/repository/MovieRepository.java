package com.netflix.content.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.netflix.content.service.model.Genre;
import com.netflix.content.service.model.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {
    List<Movie> findByGenre(Genre genre);
    List<Movie> findByTitleContainingIgnoreCase(String title);
}
