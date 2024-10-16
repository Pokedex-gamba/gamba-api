package com.github.martmatix.gambaapi.gamba.repositories;

import com.github.martmatix.gambaapi.gamba.entities.GambaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GambaRepository extends JpaRepository<GambaEntity, UUID> {
}
