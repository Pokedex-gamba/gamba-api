package com.github.martmatix.gambaapi.gamba.controllers;

import com.github.martmatix.gambaapi.gamba.constants.ErrorCodes;
import com.github.martmatix.gambaapi.gamba.entities.GambaEntity;
import com.github.martmatix.gambaapi.gamba.pokemon.Pokemon;
import com.github.martmatix.gambaapi.services.GambaService;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
public class GambaController {

    private PokemonService pokemonService;
    private KeyLoaderService keyLoaderService;
    private GambaService gambaService;

    @GetMapping(path = "/pokemon/gamba/getRandomPokemon")
    public ResponseEntity<?> getRandomPokemon(@RequestHeader("Authorization") String authHeader) {
        try {
            Flux<Pokemon> pokemonFlux = pokemonService.getPokemon(authHeader);
            Mono<List<Pokemon>> pokemonMono = pokemonFlux.collectList();

            List<Pokemon> pokemons = pokemonMono.block();

            if (pokemons == null || pokemons.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"No Pokémon found\"}");
            }

            String userId = getUserIdFromToken(authHeader);
            if (userId.equals(ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Unable To Process Request: " + ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode() + "\"}");
            }
            if (userId.equals(ErrorCodes.PUBLIC_NOT_FOUND.getCode())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Unable To Process Request: " + ErrorCodes.PUBLIC_NOT_FOUND.getCode() + "\"}");
            }

            GambaEntity gamba = createRandomGamba(userId, pokemons.get(0).getName(), pokemons.get(0));
            gambaService.saveGamba(gamba);

            return ResponseEntity.ok(gamba);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Bad Request: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping(path = "/pokemon/gamba/getUserGamba")
    public ResponseEntity<?> getUserGamba(@RequestHeader("Authorization") String authHeader) {
        try {
            String userId = getUserIdFromToken(authHeader);
            if (userId.equals(ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Unable To Process Request: " + ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode() + "\"}");
            }
            if (userId.equals(ErrorCodes.PUBLIC_NOT_FOUND.getCode())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Unable To Process Request: " + ErrorCodes.PUBLIC_NOT_FOUND.getCode() + "\"}");
            }

            List<GambaEntity> gambaByUserId = gambaService.getGambaByUserId(userId);
            return ResponseEntity.ok(gambaByUserId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private GambaEntity createRandomGamba(String userId, String pokemonName, Pokemon pokemon) {
        Random random = new Random();
        List<String> types = Arrays.asList("Fire", "Water", "Grass", "Electric", "Ice", "Rock", "Ghost", "Dragon");

        GambaEntity gamba = new GambaEntity();
        gamba.setUserId(userId);
        gamba.setPokemonName(pokemonName);
        gamba.setDate(new Date());
        gamba.setFrontDefault(pokemon.getPictures().getFront_default());
        gamba.setFrontShiny(pokemon.getPictures().getFront_shiny());

        int baseHP = random.nextInt(150) + 50;
        int baseAttack = random.nextInt(150) + 50;
        int baseDefense = random.nextInt(150) + 50;
        int baseSpeed = random.nextInt(150) + 50;

        gamba.setBaseHP(baseHP);
        gamba.setBaseAttack(baseAttack);
        gamba.setBaseDefense(baseDefense);
        gamba.setBaseSpeed(baseSpeed);

        boolean isLegendary = random.nextInt(100) < 2;
        gamba.setLegendary(isLegendary);

        String type = types.get(random.nextInt(types.size()));
        gamba.setType(type);

        int rarityBoost = isLegendary ? 1000 : 0;

        int totalRarity = baseHP + baseAttack + baseDefense + baseSpeed + rarityBoost + ((type.equals("Dragon") || type.equals("Ghost") ? +200 : +50));
        gamba.setTotalRarity(totalRarity);

        return gamba;
    }

    private String getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer", "").trim();

        PublicKey publicKey;
        try {
            String path = GambaController.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File publicKeyFile = new File(path, "decoding_key");
            if (!publicKeyFile.exists()) {
                return ErrorCodes.PUBLIC_NOT_FOUND.getCode();
            }
            BufferedReader reader = new BufferedReader(new FileReader(publicKeyFile));
            String publicKeyContent = reader.lines().collect(Collectors.joining("\n"));
            reader.close();
            publicKey = keyLoaderService.getPublicKey(publicKeyContent);
        } catch (Exception e) {
            return ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode();
        }

        Claims claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();

        String userId = claims.get("user_id", String.class);
        if (userId == null) {
            return ErrorCodes.TOKEN_EXTRACTION_ERROR.getCode();
        }

        return userId;
    }

    @Autowired
    public void setKeyLoaderService(KeyLoaderService keyLoaderService) {
        this.keyLoaderService = keyLoaderService;
    }

    @Autowired
    public void setPokemonService(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @Autowired
    public void setGambaService(GambaService gambaService) {
        this.gambaService = gambaService;
    }
}
