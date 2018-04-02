package com.example.reactive_movies_service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.Assert.*;

/**
 * Created by marcin.bracisiewicz
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MovieServiceTest {

    @Autowired
    private MovieService movieService;

    @Test
    public void shouldGetEvents() {
        final String movieId = this.movieService.getAllMovies().blockFirst().getId();

        //makes the possibility to "travel in time"
        //In reactive programming I don;t know if 10 request will approach API in 10 seconds, minutes or hours.
        //"I know that I'm going to get in 10 seconds (from code), but lets wait 10 hours to be sure"
        StepVerifier.withVirtualTime(() -> this.movieService.getEvents(movieId).take(10))
            .thenAwait(Duration.ofHours(10))
            .expectNextCount(10)
            .verifyComplete();
    }

}