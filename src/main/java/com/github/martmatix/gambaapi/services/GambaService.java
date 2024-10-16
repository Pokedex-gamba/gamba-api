package com.github.martmatix.gambaapi.services;

import com.github.martmatix.gambaapi.gamba.entities.GambaEntity;
import com.github.martmatix.gambaapi.gamba.repositories.GambaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GambaService {

    private GambaRepository gambaRepository;

    public void saveGamba(GambaEntity gamba) {
        gambaRepository.save(gamba);
    }

    @Autowired
    public void setGambaRepository(GambaRepository gambaRepository) {
        this.gambaRepository = gambaRepository;
    }
}
