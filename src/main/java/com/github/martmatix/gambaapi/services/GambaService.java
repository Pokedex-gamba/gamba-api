package com.github.martmatix.gambaapi.services;

import com.github.martmatix.gambaapi.gamba.entities.GambaEntity;
import com.github.martmatix.gambaapi.gamba.repositories.GambaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class GambaService {

    private GambaRepository gambaRepository;

    @Value("${money.manager.api.url}")
    private String moneyManagerApiUrl;

    private WebClient.Builder builder;

    public WebClient.ResponseSpec findMoney(String authHeader) {
        WebClient webClient = builder.baseUrl(moneyManagerApiUrl).build();
        return webClient.get()
                .uri("moneyManager/findUserWallet")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve();
    }

    public WebClient.ResponseSpec deductMoney(String authHeader, int balance) {
        WebClient webClient = builder.baseUrl(moneyManagerApiUrl).build();
        return webClient.get()
                .uri("/moneyManager/modifyBalance/" + balance)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve();
    }

    public void saveGamba(GambaEntity gamba) {
        gambaRepository.save(gamba);
    }

    public List<GambaEntity> getGambaByUserId(String userId) {
        return gambaRepository.findAllByUserId(userId);
    }

    @Autowired
    public void setGambaRepository(GambaRepository gambaRepository) {
        this.gambaRepository = gambaRepository;
    }

    @Autowired
    public void setBuilder(WebClient.Builder builder) {
        this.builder = builder;
    }
}
