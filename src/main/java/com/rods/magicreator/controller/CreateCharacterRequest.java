package com.rods.magicreator.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateCharacterRequest {
    private String name;
    private String role;
    private String school;
    private String house;
    private String patronus;
}
