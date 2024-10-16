package com.github.martmatix.gambaapi.services;

import com.github.martmatix.gambaapi.gamba.pokemon.Pokemon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class PokemonService {

    private WebClient.Builder builder;

    public Flux<Pokemon> getPokemon(String authHeader) {
        WebClient webClient = builder.baseUrl("http://localhost:8888").build();

        return webClient.get()
                .uri("/pokemon/get_random/1")
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToFlux(Pokemon.class);
    }

    @Autowired
    public void setBuilder(WebClient.Builder builder) {
        this.builder = builder;
    }
}
