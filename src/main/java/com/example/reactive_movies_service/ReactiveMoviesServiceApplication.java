package com.example.reactive_movies_service;

import lombok.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
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

    //similar purpose as @RequestMapping, but easier to override
    @Bean
    RouterFunction<?> routerFunction(MovieService movieService) {
        return RouterFunctions.route(RequestPredicates.GET("/movies"),
                    req -> ServerResponse.ok()
                            .body(movieService.getAllMovies(), Movie.class))
                .andRoute(RequestPredicates.GET("/movies/{id}"),
                    req -> ServerResponse.ok()
                            .body(movieService.getMovieById(req.pathVariable("id")), Movie.class))
                .andRoute(RequestPredicates.GET("/movies/{id}/events"),
                    req -> ServerResponse.ok()
                            .contentType(MediaType.TEXT_EVENT_STREAM)
                            .body(movieService.getEvents(req.pathVariable("id")), MovieEvent.class));
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
