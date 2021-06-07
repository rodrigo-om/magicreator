package com.rods.magicreator.domain.ports.in;

import com.rods.magicreator.domain.models.Character;
import lombok.Getter;
import org.springframework.core.NestedCheckedException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface IManageCharacters {
    Character create(Character character) throws CouldNotCreateCharacterException, IllegalArgumentException;
    Character update(Character character) throws CouldNotUpdateCharacterException, IllegalArgumentException;
    void delete(String id) throws CouldNotDeleteCharacterException;

    Page<Character> findAll(int page) throws CouldNotSearchCharactersException;
    Optional<Character> findBy(String id) throws CouldNotSearchCharactersException;
    List<Character> findBy(String name, String role, String school, String house, String patronus) throws CouldNotSearchCharactersException;

    class CouldNotCreateCharacterException extends NestedCheckedException {
        @Getter
        private final Character character;

        public CouldNotCreateCharacterException(Character character, String message, Throwable cause) {
            super(message, cause);
            this.character = character;
        }
    }

    class InvalidHouseProvidedException extends IllegalArgumentException {
        public InvalidHouseProvidedException(String houseProvided) {
            super("Invalid house provided as argument - House id: "+houseProvided);
        }
    }

    class CouldNotUpdateCharacterException extends NestedCheckedException {
        @Getter
        private final Character character;

        public CouldNotUpdateCharacterException(Character character, String message, Throwable cause) {
            super(message, cause);
            this.character = character;
        }
    }

    class CouldNotSearchCharactersException extends NestedCheckedException {
        public CouldNotSearchCharactersException(Throwable cause) {
            super("Could not search characters", cause);
        }
    }

    class CouldNotDeleteCharacterException extends NestedCheckedException {
        @Getter
        private final String id;

        public CouldNotDeleteCharacterException(String id, Throwable cause) {
            super("Could not delete characters", cause);
            this.id = id;
        }
    }
}
