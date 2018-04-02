package com.example.reactive_movies_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import javax.validation.constraints.NotNull;
import java.util.Date;

@SpringBootApplication
public class ReactiveMoviesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveMoviesServiceApplication.class, args);
	}

}

interface MovieRepository extends ReactiveMongoRepository<Movie,String> {
    Flux<Movie> findByTitle(String title);
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class MovieEvent {
    private String movieId;
    private Date dateViewed;
}

@Document
@Data
@NoArgsConstructor
@RequiredArgsConstructor
class Movie {

	@Id
	private String id;

	@NotNull
	private String title;
}
