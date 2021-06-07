package com.rods.magicreator.domain.ports.out;

import com.rods.magicreator.domain.models.Character;
import org.springframework.core.NestedCheckedException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface IStoreCharacters {
    Character create(Character character) throws ErrorStoringCharacterException;
    Character update(Character character) throws ErrorStoringCharacterException;
    void delete(String id) throws ErrorDeletingCharacterException;

    Page<Character> findAll(int page) throws ErrorSearchingCharactersException;
    Optional<Character> findBy(String id) throws ErrorSearchingCharactersException;
    List<Character> findBy(String name, String role, String school, String house, String patronus) throws ErrorSearchingCharactersException;

    class ErrorSearchingCharactersException extends NestedCheckedException {
        public ErrorSearchingCharactersException(Throwable cause) {
            super("Error searching characters", cause);
        }
    }

    class ErrorStoringCharacterException extends NestedCheckedException {
        public ErrorStoringCharacterException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    class ErrorDeletingCharacterException extends NestedCheckedException {
        public ErrorDeletingCharacterException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
