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
    private String pokemonName;

    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "base_hp", nullable = false)
    private int baseHP;

    @Column(name = "base_attack", nullable = false)
    private int baseAttack;

    @Column(name = "base_defense", nullable = false)
    private int baseDefense;

    @Column(name = "base_speed", nullable = false)
    private int baseSpeed;

    @Column(name = "is_legendary", nullable = false)
    private boolean isLegendary;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "total_rarity", nullable = false)
    private int totalRarity;

    @Column(name = "front_default", nullable = false)
    private String frontDefault;

    @Column(name = "front_shiny", nullable = false)
    private String frontShiny;

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

    public String getPokemonName() {
        return pokemonName;
    }

    public void setPokemonName(String pokemon_name) {
        this.pokemonName = pokemon_name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getBaseHP() {
        return baseHP;
    }

    public void setBaseHP(int baseHP) {
        this.baseHP = baseHP;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public void setBaseAttack(int baseAttack) {
        this.baseAttack = baseAttack;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public void setBaseDefense(int baseDefense) {
        this.baseDefense = baseDefense;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(int baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public boolean isLegendary() {
        return isLegendary;
    }

    public void setLegendary(boolean legendary) {
        isLegendary = legendary;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTotalRarity() {
        return totalRarity;
    }

    public void setTotalRarity(int totalRarity) {
        this.totalRarity = totalRarity;
    }

    public String getFrontDefault() {
        return frontDefault;
    }

    public void setFrontDefault(String frontDefault) {
        this.frontDefault = frontDefault;
    }

    public String getFrontShiny() {
        return frontShiny;
    }

    public void setFrontShiny(String frontShiny) {
        this.frontShiny = frontShiny;
    }
}
