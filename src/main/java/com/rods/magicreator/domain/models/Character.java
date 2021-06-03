package com.rods.magicreator.domain.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Domain model for working with a Harry Potter Character
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Character {
    private String id;
    private String name;
    private String role;
    private String school;
    private String house;
    private String patronus;
}
