package com.github.martmatix.gambaapi.gamba.controllers;

import com.github.martmatix.gambaapi.gamba.pokemon.Pokemon;
import com.github.martmatix.gambaapi.services.KeyLoaderService;
import com.github.martmatix.gambaapi.services.PokemonService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.util.List;

@RestController
public class GambaController {

    private PokemonService pokemonService;
    private KeyLoaderService keyLoaderService;

    @GetMapping(path = "/pokemon/gamba/getRandomPokemon")
    public ResponseEntity<?> getRandomPokemon(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer", "").trim();

            PublicKey publicKey = keyLoaderService.getPublicKey("""
                    -----BEGIN PUBLIC KEY-----
                    MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvLpTEski8k6IpKntmaXA4Pg4LfW1kmYaK5Y+UctOoaKssyUIE+nZ2dEx4KtAIqsiERsKGUOfxJdxDGS0urmkxVXRnhbyOdAfflOMGEcE8sQYnNHillyvUtMACJw9/GzS7v8k+i18lPXv16b3oUXEt+sBoRSuJYGxE16sxVNdyyFECle6wbobL9Py6sIEYuIpwH0ozumRR+LSrJGjnWR2JDziGBvOmL+4x/RYHplCHzcdCvBjLZS4hMSSfYY3nCtfpWEvfIbr604aA0WT9ZfHJSnXpE9Cy77BYVkRYcb32zjnkXJ7yUJpXjVgdI/gtUi60pjYt+FhNq/SDe+xH11/0QIDAQAB
                    -----END PUBLIC KEY-----`
                    """);

            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.get("user_id", String.class);
            System.out.println(userId);

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
    public void setKeyLoaderService(KeyLoaderService keyLoaderService) {
        this.keyLoaderService = keyLoaderService;
    }

    @Autowired
    public void setPokemonService(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }
}
