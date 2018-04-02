package com.example.reactive_movies_service;

import lombok.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class ReactiveMoviesServiceApplication {

    @Bean
    ApplicationRunner demoData(MovieRepository movieRepository) {
        return args -> {
            movieRepository.deleteAll().thenMany(
                    Flux.just("American Beauty", "Amelia", "Forest Gump", "Exorcism of Emily Rose", "Ghostbusters", "Liar, Liar")
                            .map(Movie::new)
                            .flatMap(movieRepository::save))
                    .thenMany(movieRepository.findAll())
                    .subscribe(System.out::println);
        };
    }

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

	@NonNull
	private String title;
}
