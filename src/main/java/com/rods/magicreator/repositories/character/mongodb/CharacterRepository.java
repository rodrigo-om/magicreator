package com.rods.magicreator.repositories.character.mongodb;

import com.rods.magicreator.repositories.character.mongodb.models.CharacterModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends MongoRepository<CharacterModel, ObjectId> {
}
