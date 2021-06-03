package com.rods.magicreator.repositories.character.mongodb;

import com.rods.magicreator.domain.models.Character;
import com.rods.magicreator.domain.ports.out.IManageCharactersPersistence;
import com.rods.magicreator.repositories.character.mongodb.models.CharacterModel;
import org.bson.types.ObjectId;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.EhcacheManager;
import org.ehcache.expiry.Expirations;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CharacterMongoDBAdapter implements IManageCharactersPersistence {

    final CharacterRepository repository;

    public CharacterMongoDBAdapter(CharacterRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Character> findAll(int page) {
        return repository.findAll(PageRequest.of(page, 100)).map(this::toCharacter);
    }

    @Override
    @Cacheable("characters")
    public Optional<Character> findBy(String id) {
        return repository.findById(new ObjectId(id)).map(this::toCharacter);
    }

    @Override
    public Character create(Character character) {
        return toCharacter(
                repository.save(fromCharacter(character))
        );
    }

    @Override
    public List<Character> findBy(String name, String role, String school, String house, String patronus) {
        Example<CharacterModel> example = Example.of(
                buildExample(name, role, school, house, patronus),
                matchNameContainingAndEverythingElseMustBeExact()
        );

        return repository.findAll(example).stream()
                .map(this::toCharacter)
                .collect(Collectors.toList());
    }

    @Override
    @CachePut(value = "characters", key = "#character.id")
    public Character update(Character character) {
        return toCharacter(
                repository.save(fromCharacter(character))
        );
    }

    @Override
    @CacheEvict(value = "characters", key = "#id")
    public void delete(String id) {
        repository.deleteById(new ObjectId(id));
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
                .patronus(character.getPatronus())
                .build();
    }

    private ExampleMatcher matchNameContainingAndEverythingElseMustBeExact() {
        return ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT)
                .withMatcher("name", ExampleMatcher.GenericPropertyMatcher::contains);
    }
}

