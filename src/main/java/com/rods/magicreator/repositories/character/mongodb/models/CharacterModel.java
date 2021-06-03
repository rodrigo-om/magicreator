package com.rods.magicreator.repositories.character.mongodb.models;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB-compatible model for {@link com.rods.magicreator.domain.models.Character Character}
 */
@Document(collection = "character")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
/*
    I'm not much a fan of mixing db models and domain models, so even though I know I could have done this with Spring
    and that the models are veeery similar, I think in early stage of projects is where you can benefit the most from
    clear boundaries between domain and technology-dependent logics.
*/
public class CharacterModel {
    @Id
    private ObjectId id;
    private String name;
    private String role;
    private String school;
    private String house;
    private String patronus;
}
