package com.github.martmatix.gambaapi.services;

import com.github.martmatix.gambaapi.gamba.entities.GambaEntity;
import com.github.martmatix.gambaapi.gamba.pokemon.Pokemon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class PokemonService {

    private WebClient.Builder builder;

    @Value("${pokemon.api.url}")
    private String pokemonHost;

    @Value("${inventory.api.url}")
    private String inventoryHost;

    public Flux<Pokemon> getPokemon(String authHeader) {
        WebClient webClient = builder.baseUrl(pokemonHost).build();

        return webClient.get()
                .uri("/pokemon/get_random/1")
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToFlux(Pokemon.class);
    }

    public WebClient.ResponseSpec sendRequestToInventory(String authHeader, GambaEntity gamba) {
        WebClient webClient = builder.baseUrl(inventoryHost).build();
        return webClient.post()
                .uri("/pokemon/inventory/saveGamba")
                .header("Authorization", authHeader)
                .bodyValue(gamba)
                .retrieve();
    }

    @Autowired
    public void setBuilder(WebClient.Builder builder) {
        this.builder = builder;
    }
}
