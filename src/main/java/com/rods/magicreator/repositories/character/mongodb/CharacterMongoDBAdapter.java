package com.rods.magicreator.repositories.character.mongodb;

import com.rods.magicreator.domain.models.Character;
import com.rods.magicreator.domain.ports.out.IStoreCharacters;
import com.rods.magicreator.repositories.character.mongodb.models.CharacterModel;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CharacterMongoDBAdapter implements IStoreCharacters {

    final CharacterRepository repository;

    public CharacterMongoDBAdapter(CharacterRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Character> findAll(int page) throws ErrorSearchingCharactersException {
        try {
            return repository.findAll(PageRequest.of(page, 100)).map(this::toCharacter);
        } catch (Exception e) {
            log.error("Error searching all characters - Page: {}", page, e);
            throw new ErrorSearchingCharactersException(e);
        }
    }

    @Override
    @Cacheable(value = "characters", unless = "#result == null")
    public Optional<Character> findBy(String id) throws ErrorSearchingCharactersException {
        try {
            return repository.findById(new ObjectId(id)).map(this::toCharacter);
        } catch (Exception e) {
            log.error("Error searching characters - Id: {}", id, e);
            throw new ErrorSearchingCharactersException(e);
        }
    }

    @Override
    public Character create(Character character) throws ErrorStoringCharacterException {
        try {
            return toCharacter(
                    repository.save(fromCharacter(character))
            );
        } catch (Exception e) {
            log.error("Error creating character - Character Name: {}", character.getName(), e);
            throw new ErrorStoringCharacterException("Error creating characters", e);
        }
    }

    @Override
    public List<Character> findBy(String name, String role, String school, String house, String patronus) throws ErrorSearchingCharactersException {
        try {
            Example<CharacterModel> example = Example.of(
                    buildExample(name, role, school, house, patronus),
                    matchNameContainingAndEverythingElseMustBeExact()
            );

            return repository.findAll(example).stream()
                    .map(this::toCharacter)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching character by dynamic filters - Name: {}, Role: {}, School: {}, House: {}, Patronus: {}",
                    name, role, school, house, patronus, e);
            throw new ErrorSearchingCharactersException(e);
        }
    }

    @Override
    @CachePut(value = "characters", key = "#character.id")
    public Character update(Character character) throws ErrorStoringCharacterException {
        try {
            return toCharacter(
                    repository.save(fromCharacter(character))
            );
        } catch (Exception e) {
            log.error("Error updating character - Character Id: {}", character.getId(), e);
            throw new ErrorStoringCharacterException("Error updating character", e);
        }
    }

    @Override
    @CacheEvict(value = "characters", key = "#id")
    public void delete(String id) throws ErrorDeletingCharacterException {
        try {
            repository.deleteById(new ObjectId(id));
        } catch (Exception e) {
            log.error("Error deleting character - Character Id: {}", id, e);
            throw new ErrorDeletingCharacterException("Error creating characters", e);
        }
    }

    private CharacterModel buildExample(String name, String role, String school, String house, String patronus) {
        return CharacterModel.builder()
                .name(name)
                .role(role)
                .school(school)
                .house(house)
                .patronus(patronus)
                .build();
    }

    private Character toCharacter(CharacterModel model) {
        return Character.builder()
                .id(model.getId().toString())
                .name(model.getName())
                .role(model.getRole())
                .school(model.getSchool())
                .house(model.getHouse())
                .houseName(model.getHouseName())
                .patronus(model.getPatronus())
                .build();
    }

    private CharacterModel fromCharacter(Character character) {
        return CharacterModel.builder()
                .id(character.getId() != null ? new ObjectId(character.getId()) : null)
                .name(character.getName())
                .role(character.getRole())
                .school(character.getSchool())
                .house(character.getHouse())
                .houseName(character.getHouseName())
                .patronus(character.getPatronus())
                .build();
    }

    private ExampleMatcher matchNameContainingAndEverythingElseMustBeExact() {
        return ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT)
                .withMatcher("name", ExampleMatcher.GenericPropertyMatcher::contains);
    }
}

