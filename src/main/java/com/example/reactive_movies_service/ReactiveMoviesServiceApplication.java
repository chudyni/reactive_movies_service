package com.example.reactive_movies_service;

import lombok.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@SpringBootApplication
public class ReactiveMoviesServiceApplication {

    //To make it work: docker run -p 27017:27017 -d mongo
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

@Service
class MovieService {
    private final MovieRepository movieRepository;

    MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Flux<Movie> getAllMovies() {
        return this.movieRepository.findAll();
    }

    public Mono<Movie> getMovieById(String id) {
        return this.movieRepository.findById(id);
    }

    //simulate traffic
    public Flux<MovieEvent> getEvents(String movieId) {
        return Flux.<MovieEvent>generate(sink -> sink.next(new MovieEvent(movieId, new Date())))
                .delayElements(Duration.ofSeconds(1l));
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
