package com.github.martmatix.gambaapi.gamba.controllers;

import com.github.martmatix.gambaapi.DTOs.UserWalletDTO;
import com.github.martmatix.gambaapi.gamba.constants.ErrorCodes;
import com.github.martmatix.gambaapi.gamba.entities.GambaEntity;
import com.github.martmatix.gambaapi.gamba.pokemon.Pokemon;
import com.github.martmatix.gambaapi.services.GambaService;
import com.github.martmatix.gambaapi.services.KeyLoaderService;
import com.github.martmatix.gambaapi.services.PokemonService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
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

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GambaEntity.class))),
                    @ApiResponse(responseCode = "400",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\"error\": \"Not Enough Coins!\"}"))),
                    @ApiResponse(responseCode = "500",
                            content = {
                                    @Content(mediaType = "application/json",
                                            schema = @Schema(example = "{\"error\": \"Unable To Retrieve Wallet From Money Manager Service\"}")),
                                    @Content(mediaType = "application/json",
                                            schema = @Schema(example = "{\"error\": \"Token Extraction Error\"}")),
                                    @Content(mediaType = "application/json",
                                            schema = @Schema(example = "{\"error\": \"Public Key 'decoding_key' Not Found\"}")),
                                    @Content(mediaType = "application/json",
                                            schema = @Schema(example = "{\"error\": \"Internal Server Error: Unexpected Failure\"}"))
                            })
            }
    )
    @GetMapping(path = "/pokemon/gamba/getRandomPokemon")
    public ResponseEntity<?> getRandomPokemon(@RequestHeader("Authorization") String authHeader) {
        try {
            WebClient.ResponseSpec moneySpec = gambaService.findMoney(authHeader);
            Mono<UserWalletDTO> walletMono = moneySpec.bodyToMono(UserWalletDTO.class);
            UserWalletDTO wallet = walletMono.block();
            if (wallet == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + ErrorCodes.MONEY_MANAGER_RETRIEVE_FAILURE.getCode() + "\"}");
            }
            if (wallet.getBalance() < 25) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + ErrorCodes.INSUFFICIENT_FUNDS.getCode() + "\"}");
            }

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
            gambaService.deductMoney(authHeader, -25).bodyToMono(String.class).block();

            String responseMessage = pokemonService.sendRequestToInventory(authHeader, gamba).bodyToMono(String.class).block();

            return ResponseEntity.ok(responseMessage);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Bad Request: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = GambaEntity.class))))
            }
    )
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
