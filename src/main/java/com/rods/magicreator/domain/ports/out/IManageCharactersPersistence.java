package com.rods.magicreator.domain.ports.out;

import com.rods.magicreator.domain.models.Character;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface IManageCharactersPersistence {
    Page<Character> findAll(int page);
    Optional<Character> findBy(String id);
    Character create(Character character);
    List<Character> findBy(String name, String role, String school, String house, String patronus);
    Character update(Character character);
    void delete(String id);
}
