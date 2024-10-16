package com.github.martmatix.gambaapi.gamba.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity(name = "gamba_history")
public class GambaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private String userId;

    @Column(name = "pokemon_name", nullable = false)
    private String pokemon_name;

    @Column(name = "date", nullable = false)
    private Date date;

    public GambaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPokemon_name() {
        return pokemon_name;
    }

    public void setPokemon_name(String pokemon_name) {
        this.pokemon_name = pokemon_name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
