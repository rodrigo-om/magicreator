package com.rods.magicreator.domain.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "character")
public class Character {
    @Id
    private String id;
    private String role;
    private String school;
    private String house;
    private String patronus;

    public Character(String id, String role, String school, String house, String patronus) {
        this.id = id;
        this.role = role;
        this.school = school;
        this.house = house;
        this.patronus = patronus;
    }

    public String getId() {
        return id;
    }

    public Character setId(String id) {
        this.id = id;
        return this;
    }

    public String getRole() {
        return role;
    }

    public Character setRole(String role) {
        this.role = role;
        return this;
    }

    public String getSchool() {
        return school;
    }

    public Character setSchool(String school) {
        this.school = school;
        return this;
    }

    public String getHouse() {
        return house;
    }

    public Character setHouse(String house) {
        this.house = house;
        return this;
    }

    public String getPatronus() {
        return patronus;
    }

    public Character setPatronus(String patronus) {
        this.patronus = patronus;
        return this;
    }
}
