package com.rods.magicreator.domain;

import com.rods.magicreator.domain.models.Character;
import com.rods.magicreator.domain.models.House;
import com.rods.magicreator.domain.ports.in.IDisplayHouses;
import com.rods.magicreator.domain.ports.in.IManageCharacters;
import com.rods.magicreator.domain.ports.out.IObtainHousesInfo;
import com.rods.magicreator.domain.ports.out.IObtainHousesInfo.ErrorObtainingHousesException;
import com.rods.magicreator.domain.ports.out.IStoreCharacters;
import com.rods.magicreator.domain.ports.out.IStoreCharacters.ErrorDeletingCharacterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CharactersService implements IManageCharacters, IDisplayHouses {

    private final IStoreCharacters charactersRepository;
    private final IObtainHousesInfo housesRepository;

    public CharactersService(IStoreCharacters charactersRepository, IObtainHousesInfo housesRepository) {
        this.charactersRepository = charactersRepository;
        this.housesRepository = housesRepository;
    }

    @Override
    public Page<Character> findAll(int page) throws CouldNotSearchCharactersException {
        try {
            return charactersRepository.findAll(page);
        } catch (IStoreCharacters.ErrorSearchingCharactersException e) {
            log.error("Could not search for all characters.", e);
            throw new CouldNotSearchCharactersException(e);
        }
    }

    @Override
    public Optional<Character> findBy(String id) throws CouldNotSearchCharactersException {
        try {
            return charactersRepository.findBy(id);
        } catch (IStoreCharacters.ErrorSearchingCharactersException e) {
            log.error("Could not search for characters. Id: {}", id, e);
            throw new CouldNotSearchCharactersException(e);
        }
    }

    @Override
    public Character create(Character character) throws CouldNotCreateCharacterException, IllegalArgumentException {
        try {
            if (character.getId() != null) throw new IllegalArgumentException("Character Id must be null - Did you mean to update it?");

            House house = housesRepository
                    .getHouseById(character.getHouse())
                    .orElseThrow(() -> new InvalidHouseProvidedException(character.getHouse()));

            Character characterToSave = character.withHouseName(house.getName());

            return charactersRepository.create(characterToSave);

        } catch (IllegalArgumentException e) {
            log.error("Illegal argument when trying to create a character", e);
            throw e;
        } catch (Exception e) {
            log.error("Error trying to create a character. Character {}", character, e);
            throw new CouldNotCreateCharacterException(character, e.getMessage(), e);
        }
    }

    @Override
    public List<Character> findBy(String name, String role, String school, String house, String patronus) throws CouldNotSearchCharactersException {
        try {
            return charactersRepository.findBy(name, role, school, house, patronus);
        } catch (IStoreCharacters.ErrorSearchingCharactersException e) {
            log.error("Could not search for characters.", e);
            throw new CouldNotSearchCharactersException(e);
        }
    }

    @Override
    public Character update(Character character) throws CouldNotUpdateCharacterException {
        try {
            if (character.getId() == null) throw new IllegalArgumentException("Character Id must not be null - Did you mean to create it?");

            House house = housesRepository
                    .getHouseById(character.getHouse())
                    .orElseThrow(() -> new InvalidHouseProvidedException(character.getHouse()));

            Character characterToSave = character.withHouseName(house.getName());

            return charactersRepository.update(characterToSave);

        } catch (IllegalArgumentException e) {
            log.error("Illegal argument when trying to update a character", e);
            throw e;
        } catch (Exception e) {
            log.error("Error trying to update a character", e);
            throw new CouldNotUpdateCharacterException(character, e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) throws CouldNotDeleteCharacterException {
        try {
            charactersRepository.delete(id);
        } catch (ErrorDeletingCharacterException e) {
            log.error("Could not search for characters.", e);
            throw new CouldNotDeleteCharacterException(id, e);
        }
    }

    @Override
    public List<House> findHouses() throws CouldNotSearchHousesException {
        try {
            return housesRepository.getHouses();
        } catch (ErrorObtainingHousesException e) {
            log.error("Could not search houses.", e);
            throw new CouldNotSearchHousesException(e);
        }
    }
}

