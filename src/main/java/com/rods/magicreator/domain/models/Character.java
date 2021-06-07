package com.rods.magicreator.domain.models;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Domain model for working with a Harry Potter Character
 */
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@With
public class Character {
    private String id;
    private String name;
    private String role;
    private String school;
    private String house;
    private String houseName;
    private String patronus;
}
