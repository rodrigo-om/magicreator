package com.rods.magicreator.domain.ports.out;

import java.util.List;

public interface IManageCharactersPersistence {
    List<Character> findAll();
    Character findBy(String id);
    Character create();
}
