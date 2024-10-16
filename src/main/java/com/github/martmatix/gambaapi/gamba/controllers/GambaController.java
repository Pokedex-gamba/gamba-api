package com.github.martmatix.gambaapi.gamba.controllers;

import com.github.martmatix.gambaapi.gamba.pokemon.Pokemon;
import com.github.martmatix.gambaapi.services.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class GambaController {

    private PokemonService pokemonService;

    @GetMapping(path = "/pokemon/gamba/getRandomPokemon")
    public ResponseEntity<?> getRandomPokemon(@RequestHeader("Authorization") String authHeader) {
        try {
            Flux<Pokemon> pokemonFlux = pokemonService.getPokemon(authHeader);
            Mono<List<Pokemon>> pokemonMono = pokemonFlux.collectList();

            List<Pokemon> pokemons = pokemonMono.block();

            if (pokemons == null || pokemons.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"No Pokémon found\"}");
            }
            return ResponseEntity.ok(pokemons);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Bad Request: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    @Autowired
    public void setPokemonService(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }
}
